package si.rso.orders.restclients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import si.rso.cart.lib.ShoppingCart;

import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "products")
@Dependent
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProductsApi {

    @GET
    @Path("/shopping-cart/me")
    List<ShoppingCart> getShoppingCartsForCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String authToken);
}
