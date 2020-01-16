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
    
    @ConfigValue("shopping-cart-url")
    private String shoppingCartUrl;
    
    @ConfigValue("products-url")
    private String productsUrl;
    
    @ConfigValue("stock-url")
    private String stockUrl;

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
    
    public String getShoppingCartUrl() {
        return shoppingCartUrl;
    }
    
    public void setShoppingCartUrl(String shoppingCartUrl) {
        this.shoppingCartUrl = shoppingCartUrl;
    }
    
    public String getProductsUrl() {
        return productsUrl;
    }
    
    public void setProductsUrl(String productsUrl) {
        this.productsUrl = productsUrl;
    }
    
    public String getStockUrl() {
        return stockUrl;
    }
    
    public void setStockUrl(String stockUrl) {
        this.stockUrl = stockUrl;
    }
}
