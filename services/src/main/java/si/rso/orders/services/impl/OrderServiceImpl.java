package si.rso.orders.services.impl;

import si.rso.orders.lib.Order;
import si.rso.orders.services.OrderService;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class OrderServiceImpl implements OrderService {

    @PersistenceContext(unitName = "main-jpa-unit")
    private EntityManager em;

    @Override
    public Order getOrder(String orderId) {
        // TODO
        return null;
    }
}
