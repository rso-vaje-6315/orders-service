package si.rso.orders.mappers;

import si.rso.orders.lib.Order;
import si.rso.orders.lib.OrderAddress;
import si.rso.orders.persistence.OrderEntity;

public class OrderMapper {

    public static Order fromEntity(OrderEntity entity) {
        Order order = new Order();
        order.setId(entity.getId());
        order.setTimestamp(entity.getTimestamp());
        order.setCustomerId(entity.getCustomerId());
        order.setPrice(entity.getTotalPrice());
    
        OrderAddress address = new OrderAddress();
        address.setEmail(entity.getCustomerEmail());
        address.setPhone(entity.getCustomerPhone());
        address.setCountry(entity.getCustomerCountry());
        address.setName(entity.getCustomerName());
        address.setPost(entity.getCustomerPost());
        address.setStreet(entity.getCustomerStreet());
        order.setAddress(address);

        return order;
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(order.getId());
        orderEntity.setTimestamp(order.getTimestamp());
        orderEntity.setCustomerId(order.getCustomerId());
        orderEntity.setTotalPrice(order.getPrice());

        return orderEntity;
    }
}
