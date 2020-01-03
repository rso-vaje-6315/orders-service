package si.rso.orders.restclients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "shopping-cart")
@Path("/shopping-cart")
@Dependent
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ShoppingCartApi {

    @GET
    @Path("/me")
//    List<ShoppingCart> getShoppingCartsForCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String authToken);
    Response getShoppingCartsForCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String authToken);
}
