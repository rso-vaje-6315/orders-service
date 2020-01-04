package si.rso.orders.restclients;

import si.rso.products.lib.Product;

import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProductsApi {
    
    @POST
    @Path("/graphql")
    @Consumes("application/graphql")
    JsonObject productGraphql(String query);
    
    @GET
    @Path("/v1/products/{productId}")
    Product getProduct(@PathParam("productId") String productId);
}
