package si.rso.orders.api.endpoints;

import com.kumuluz.ee.logs.cdi.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Log
@Path("/plague")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlagueEndpoint {

    @Operation(description = "Kills service to test Kubernetes readiness and liveness checks",
            summary = "Kills service", tags = "test",
            responses = {
                    @ApiResponse(responseCode = "999", description = "Service killed.")
            })
    @POST
    @Path("/unleash")
    public Response unleashPlague() {
        System.exit(99);
        return Response.status(999).build();
    }

}
