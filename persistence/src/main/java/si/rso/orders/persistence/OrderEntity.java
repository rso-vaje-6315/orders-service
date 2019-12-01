package si.rso.orders.persistence;

import javax.persistence.*;

@Entity
@Table(name = "orders")
@NamedQueries(value = {
        @NamedQuery(name = OrderEntity.FIND_BY_CUSTOMER, query = "SELECT o FROM OrderEntity o WHERE o.customerId = :customerId")
})
public class OrderEntity extends BaseEntity {

    public static final String FIND_BY_CUSTOMER = "OrderEntity.findByCustomer";

    @Column(name = "customer_id")
    private String customerId;

    private float price;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
