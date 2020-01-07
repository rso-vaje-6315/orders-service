package si.rso.orders.api;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import si.rso.orders.api.config.AuthRole;
import si.rso.orders.api.endpoints.OrderEndpoint;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/v1")
@RegisterService
@DeclareRoles({AuthRole.SERVICE, AuthRole.ADMIN, AuthRole.SELLER, AuthRole.CUSTOMER})
@OpenAPIDefinition(
        info = @Info(title = "Orders service", version = "1.0.0", contact = @Contact(name = "Matej Bizjak"),
                description = "Service for managing orders.")
)
public class RestService extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        classes.add(OrderEndpoint.class);
        
        return classes;
    }
}
