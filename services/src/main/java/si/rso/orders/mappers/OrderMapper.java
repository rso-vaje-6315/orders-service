package si.rso.orders.mappers;

import si.rso.orders.lib.Order;
import si.rso.orders.lib.OrderAddress;
import si.rso.orders.lib.OrderProduct;
import si.rso.orders.persistence.OrderEntity;
import si.rso.orders.persistence.OrderProductEntity;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order fromEntity(OrderEntity entity) {
        Order order = new Order();
        order.setId(entity.getId());
        order.setTimestamp(entity.getTimestamp());
        order.setCustomerId(entity.getCustomerId());
        order.setPrice(entity.getTotalPrice());
        order.setStatus(entity.getStatus());
    
        OrderAddress address = new OrderAddress();
        address.setEmail(entity.getCustomerEmail());
        address.setPhone(entity.getCustomerPhone());
        address.setCountry(entity.getCustomerCountry());
        address.setName(entity.getCustomerName());
        address.setPost(entity.getCustomerPost());
        address.setStreet(entity.getCustomerStreet());
        order.setAddress(address);
        
        if (entity.getProducts() != null) {
            order.setProducts(entity.getProducts()
            .stream()
            .map(OrderMapper::fromEntity)
            .collect(Collectors.toList()));
        } else {
            order.setProducts(new ArrayList<>());
        }

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
    
    public static OrderProduct fromEntity(OrderProductEntity entity) {
        OrderProduct product = new OrderProduct();
        product.setId(entity.getId());
        product.setTimestamp(entity.getTimestamp());
        product.setProductId(entity.getProductId());
        product.setCode(entity.getCode());
        product.setName(entity.getName());
        product.setPricePerItem(entity.getPricePerItem());
        product.setQuantity(entity.getQuantity());
        return product;
    }
}
