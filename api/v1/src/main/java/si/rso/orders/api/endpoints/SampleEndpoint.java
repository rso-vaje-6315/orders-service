package si.rso.orders.api.endpoints;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import com.kumuluz.ee.rest.beans.QueryParameters;
import si.rso.orders.lib.Sample;
import si.rso.orders.services.SampleService;

@Path("/sample")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SampleEndpoint {
    
    @Inject
    private SampleService sampleService;
    
    @Context
    protected UriInfo uriInfo;
    
    @GET
    public Response getGreetings() {
        QueryParameters query = QueryParameters.query(uriInfo.getRequestUri().getQuery()).build();
        List<Sample> samples = sampleService.getSamples(query);
        long samplesCount = sampleService.getSamplesCount(query);
        return Response.ok(samples).header("X-Total-Count", samplesCount).build();
    }

}
