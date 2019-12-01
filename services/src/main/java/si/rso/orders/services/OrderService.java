package si.rso.orders.services;

import si.rso.orders.lib.Order;

public interface OrderService {

    Order getOrder(String orderId);
}
