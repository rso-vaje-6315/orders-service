package si.rso.orders.producers;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.grpc.client.GrpcChannelConfig;
import com.kumuluz.ee.grpc.client.GrpcChannels;
import com.kumuluz.ee.grpc.client.GrpcClient;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import si.rso.orders.config.ServiceConfig;
import si.rso.orders.lib.annotations.DiscoverGrpcClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ApplicationScoped
public class GrpcClientProducer {
    
    private static final Logger LOG = LogManager.getLogger(GrpcClientProducer.class.getSimpleName());
    
    public static final Pattern hostRegex = Pattern.compile("^(https?://)?(.+?)(:.+)?$");
    
    @Inject
    @DiscoverService("invoice-service")
    private Optional<String> invoiceServiceUrl;
    
    @Inject
    @DiscoverService("customers-service")
    private Optional<String> customersServiceUrl;
    
    @Inject
    private ServiceConfig serviceConfig;

    @Produces
    @DiscoverGrpcClient
    public Optional<GrpcClient> produceClient(InjectionPoint injectionPoint) {
        try {
            DiscoverGrpcClient annotation = injectionPoint.getAnnotated().getAnnotation(DiscoverGrpcClient.class);
            
            if (!serviceConfig.isGrpcDiscovery()) {
                GrpcChannels clientPool = GrpcChannels.getInstance();
                GrpcChannelConfig channelConfig = clientPool.getGrpcClientConfig(annotation.clientName());
                return Optional.of(new GrpcClient(channelConfig));
            }
            
            Optional<String> clientHostname = this.getClientHostname(annotation.clientName());
            
            if (clientHostname.isEmpty()) {
                return Optional.empty();
            }
            GrpcChannelConfig.Builder clientBuilder = new GrpcChannelConfig.Builder();
            
            clientBuilder.address(clientHostname.get());
            clientBuilder.port(8443);
            clientBuilder.name(annotation.clientName());
            GrpcChannelConfig channelConfig = clientBuilder.build();
            LOG.info("Discovered gRPC service {} on address {}", annotation.clientName(), clientHostname.get() + ":8443");
            return Optional.of(new GrpcClient(channelConfig));
        } catch (SSLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    private Optional<String> getClientHostname(String clientName) {
        String url;
        if (clientName.equals("invoice-service")) {
            if (this.invoiceServiceUrl.isPresent()) {
                url = this.invoiceServiceUrl.get();
            } else {
                return Optional.empty();
            }
        } else if (clientName.equals("customers-service")) {
            if (this.customersServiceUrl.isPresent()) {
                url = this.customersServiceUrl.get();
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
        Matcher matcher = hostRegex.matcher(url);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(2));
        } else {
            return Optional.empty();
        }
    }

}
