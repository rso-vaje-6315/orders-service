package si.rso.orders.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle(value = "service-config", watch = true)
public class ServiceConfig {
    
    @ConfigValue("products-url")
    private String productApiUrl;
    
    public String getProductApiUrl() {
        return productApiUrl;
    }
    
    public void setProductApiUrl(String productApiUrl) {
        this.productApiUrl = productApiUrl;
    }
}
