package si.rso.orders.services;

import com.kumuluz.ee.rest.beans.QueryParameters;
import si.rso.orders.lib.Order;

import java.net.MalformedURLException;
import java.util.List;

public interface OrderService {

    List<Order> getOrdersByCustomer(String customerId);

    Order getOrder(String orderId);

    List<Order> getOrders(QueryParameters queryParameters);

    Order updateOrder(Order order);

    Order createOrder(String authToken) throws MalformedURLException;
}
