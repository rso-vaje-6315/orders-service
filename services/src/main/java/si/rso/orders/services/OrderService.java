package si.rso.orders.services;

import si.rso.orders.lib.Order;

import java.util.List;

public interface OrderService {

    List<Order> getOrdersByCustomer(String customerId);

    Order getOrder(String orderId);
}
