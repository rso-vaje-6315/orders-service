package si.rso.orders.services.impl;

import si.rso.orders.lib.Order;
import si.rso.orders.mappers.OrderMapper;
import si.rso.orders.persistence.OrderEntity;
import si.rso.orders.services.OrderService;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    @PersistenceContext(unitName = "main-jpa-unit")
    private EntityManager em;

    @Override
    public List<Order> getOrdersByCustomer(String customerId) {
        TypedQuery<OrderEntity> query = em.createNamedQuery(OrderEntity.FIND_BY_CUSTOMER, OrderEntity.class);
        query.setParameter("customerId", customerId);

        return query.getResultStream()
                .map(OrderMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Order getOrder(String orderId) {
        // TODO
        return null;
    }
}
