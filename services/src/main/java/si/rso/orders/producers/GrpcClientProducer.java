package si.rso.orders.producers;

import com.kumuluz.ee.grpc.client.GrpcChannelConfig;
import com.kumuluz.ee.grpc.client.GrpcChannels;
import com.kumuluz.ee.grpc.client.GrpcClient;
import si.rso.orders.lib.annotations.CreateGrpcClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.net.ssl.SSLException;


@ApplicationScoped
public class GrpcClientProducer {

    @Produces
    @CreateGrpcClient
    public GrpcClient produceClient(InjectionPoint injectionPoint) {
        try {
            CreateGrpcClient annotation = injectionPoint.getAnnotated().getAnnotation(CreateGrpcClient.class);
            
            GrpcChannels clientPool = GrpcChannels.getInstance();
            GrpcChannelConfig channelConfig = clientPool.getGrpcClientConfig(annotation.clientName());
            return new GrpcClient(channelConfig);
        } catch (SSLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
