package si.rso.orders.mappers;

import si.rso.orders.lib.Order;
import si.rso.orders.persistence.OrderEntity;

public class OrderMapper {

    public static Order fromEntity(OrderEntity entity) {
        Order order = new Order();
        order.setId(entity.getId());
        order.setTimestamp(entity.getTimestamp());
        order.setCustomerId(entity.getCustomerId());
        order.setPrice(entity.getPrice());

        return order;
    }
}
