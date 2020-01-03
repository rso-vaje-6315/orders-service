package si.rso.orders.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import si.rso.orders.api.config.AuthRole;

import javax.annotation.security.DeclareRoles;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/v1")
@DeclareRoles({AuthRole.SERVICE, AuthRole.ADMIN, AuthRole.SELLER, AuthRole.CUSTOMER})
@OpenAPIDefinition(
        info = @Info(title = "Orders service", version = "1.0.0", contact = @Contact(name = "Matej Bizjak"),
                description = "Service for managing orders.")
)
public class RestService extends Application {

}
