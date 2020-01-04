package si.rso.orders.restclients;

import javax.json.JsonArray;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ShoppingCartApi {

    @GET
    @Path("/shopping-cart/me")
    JsonArray getShoppingCartsForCustomer(@HeaderParam(HttpHeaders.AUTHORIZATION) String authToken);
}
