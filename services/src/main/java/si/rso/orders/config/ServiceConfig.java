package si.rso.orders.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle(value = "service-config", watch = true)
public class ServiceConfig {

    @ConfigValue("maintenance")
    private boolean maintenance;
    
    @ConfigValue("grpc-discovery")
    private boolean grpcDiscovery;

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
    
    public boolean isGrpcDiscovery() {
        return grpcDiscovery;
    }
    
    public void setGrpcDiscovery(boolean grpcDiscovery) {
        this.grpcDiscovery = grpcDiscovery;
    }
}
