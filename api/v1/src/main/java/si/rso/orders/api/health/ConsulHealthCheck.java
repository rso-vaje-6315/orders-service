package si.rso.orders.api.health;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Readiness
@ApplicationScoped
public class ConsulHealthCheck implements HealthCheck {
    
    public static final Logger LOG = LogManager.getLogger(ConsulHealthCheck.class.getSimpleName());

    @Override
    public HealthCheckResponse call() {
        String agentUrl = ConfigurationUtil.getInstance().get("kumuluzee.config.consul.agent").orElse("http://localhost:8500");
        String healthUrl = agentUrl + "/v1/status/leader";

        Response response = ClientBuilder.newClient()
                .target(healthUrl)
                .request(MediaType.APPLICATION_JSON)
                .build(HttpMethod.GET)
                .invoke();

        if (response.getStatus() == 200) {
            LOG.info("Consul healthcheck OK.");
            return HealthCheckResponse.named(ConsulHealthCheck.class.getSimpleName())
                    .up()
                    .withData(healthUrl, "UP")
                    .build();
        }
        LOG.error("Failed Consul healthcheck!");
        return HealthCheckResponse.named(ConsulHealthCheck.class.getSimpleName()).down().build();
    }
}
