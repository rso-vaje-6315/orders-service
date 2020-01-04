package si.rso.orders.restclients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RegisterRestClient(configKey = "shopping-cart")
@Dependent
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ShoppingCartApi {

    @GET
    @Path("/shopping-cart/me")
    Response getShoppingCartsForCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String authToken);
}
