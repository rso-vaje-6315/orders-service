package si.rso.orders.restclients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import si.rso.products.lib.Product;

import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "products")
@Dependent
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProductsApi {

    @GET
    @Path("/products/{productId}")
    Product getProduct(@PathParam("productId") String productId);
}
