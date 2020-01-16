package si.rso.orders.restclients;

import si.rso.cart.lib.ShoppingCart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface StockApi {

    @GET
    @Path("/v1/warehouses/allstock/{productId}")
    ShoppingCart getNumberOfAllProducts(@PathParam("productId") String productId);
}
