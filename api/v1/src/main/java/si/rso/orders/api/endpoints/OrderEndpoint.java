package si.rso.orders.api.endpoints;

import si.rso.orders.lib.Order;
import si.rso.orders.services.OrderService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/orders")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderEndpoint {

    @Inject
    private OrderService orderService;

    @GET
    @Path("/me")
    public Response getOrdersByCustomer() {
        String customerId = "9349cf54-1946-4915-be7e-7decb9090e8e"; // TODO dobi dejanskega prijavljenega userja

        List<Order> orders = orderService.getOrdersByCustomer(customerId);

        return Response.ok(orders).build();
    }

    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("orderId") String orderId) {
        Order order = orderService.getOrder(orderId);

        return Response.ok(order).build();
    }

//    @GET
//    @Path("/{orderId}")
//    @Retry(maxRetries = 4)
//    @Fallback(fallbackMethod = "returnOrderWithoutProductData")
//    @RolesAllowed("admin")
//    public Response getOrder(@PathParam("orderId") String orderId) {
//        try {
//            OrderResponse orderResponse = ordersBean.getOrder(orderId, true);
//
//            if (orderResponse.getOrder() == null) {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }
//
//            return Response.status(Response.Status.OK).entity(orderResponse).build();
//        } catch (Exception e) {
//            throw new RuntimeException("Resource failure");
//        }
//    }
}
