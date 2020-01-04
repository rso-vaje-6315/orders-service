package si.rso.orders.api.endpoints;

import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.rest.beans.QueryParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import si.rso.orders.api.config.AuthRole;
import si.rso.orders.lib.Order;
import si.rso.orders.services.OrderService;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log
@Path("/orders")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderEndpoint {

    @Inject
    private OrderService orderService;

    @Context
    protected UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    private Optional<KeycloakSecurityContext> getKeycloakSecurityContext() {
        if (securityContext != null && securityContext.getUserPrincipal() instanceof KeycloakPrincipal) {
            KeycloakPrincipal principal = ((KeycloakPrincipal) securityContext.getUserPrincipal());
            return Optional.of(principal.getKeycloakSecurityContext());
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getMyCustomerId() throws NoSuchElementException {
        KeycloakSecurityContext context = getKeycloakSecurityContext().orElseThrow();
        AccessToken token = context.getToken();
        return Optional.ofNullable(token.getSubject());
    }

    private Optional<String> getMyTokenString() throws NoSuchElementException {
        KeycloakSecurityContext context = getKeycloakSecurityContext().orElseThrow();
        return Optional.ofNullable(context.getTokenString());
    }

    @GET
    @Path("/me")
    @RolesAllowed({AuthRole.CUSTOMER})
    @Operation(description = "Customer retrieves their orders.",
            summary = "Returns users' orders.", tags = "orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns users' orders.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class))))
            })
    public Response getOrdersByCustomer() {
        try {
            String customerId = getMyCustomerId().orElseThrow();

            List<Order> orders = orderService.getOrdersByCustomer(customerId);

            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @GET
    @Path("/{orderId}")
    @RolesAllowed({AuthRole.ADMIN, AuthRole.SELLER})
    @Operation(description = "Retrieves order by orderId.",
            summary = "Returns an order.", tags = "orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns an order.",
                            content = @Content(schema = @Schema(implementation = Order.class)))
            })
    public Response getOrder(@PathParam("orderId") String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @GET
    @RolesAllowed({AuthRole.ADMIN, AuthRole.SELLER})
    @Operation(description = "Retrieves orders.",
            summary = "Returns orders.", tags = "orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns an order.",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class))))
            })
    public Response getOrders() {
        // TODO default limit could be stored in the config server
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0).defaultLimit(10).build();
        try {
            List<Order> orders = orderService.getOrders(queryParameters);
            return Response.ok(orders).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @PUT
    @Counted(name = "update-order-count")
    @RolesAllowed({AuthRole.ADMIN, AuthRole.SELLER})
    @Operation(description = "Updates an order.",
            summary = "Updates an orders.", tags = "orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updates an order.",
                            content = @Content(schema = @Schema(implementation = Order.class)))
            })
    public Response updateOrder(Order order) {
        return Response.ok().build();

    }

    @POST
    @Timed(name = "create-order-time")
    @Counted(name = "create-order-count")
    @RolesAllowed({AuthRole.CUSTOMER})
    @Operation(description = "Creates new order.",
            summary = "Creates new order.", tags = "orders",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Creates new order..",
                            content = @Content(schema = @Schema(implementation = Order.class)))
            })
    public Response createOrder(Order order) {
        try {
            String customerTokenString = getMyTokenString().orElseThrow();
            KeycloakSecurityContext context = getKeycloakSecurityContext().orElseThrow();
            Order createdOrder = orderService.createOrder(order, customerTokenString, context.getToken().getSubject());

            return Response.ok(createdOrder).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }
}
