package si.rso.orders.restclients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient
public interface StockApi {

    @GET
    @Path("/allstock/{productId}")
    Long getNumberOfAllProducts(@PathParam("productId") String productId);
}
