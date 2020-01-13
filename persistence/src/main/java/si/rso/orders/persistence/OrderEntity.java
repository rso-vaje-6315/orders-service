package si.rso.orders.persistence;

import si.rso.orders.lib.enums.OrderStatus;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
@NamedQueries({
    @NamedQuery(name = OrderEntity.FIND_BY_CUSTOMER, query = "SELECT o FROM OrderEntity o WHERE o.customerId = :customerId")
})
public class OrderEntity extends BaseEntity {
    
    public static final String FIND_BY_CUSTOMER = "OrderEntity.findByCustomer";
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "order_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "customer_street")
    private String customerStreet;
    
    @Column(name = "customer_post")
    private String customerPost;
    
    @Column(name = "customer_country")
    private String customerCountry;
    
    @Column(name = "customer_phone")
    private String customerPhone;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", fetch = FetchType.EAGER)
    private List<OrderProductEntity> products;
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerStreet() {
        return customerStreet;
    }
    
    public void setCustomerStreet(String customerStreet) {
        this.customerStreet = customerStreet;
    }
    
    public String getCustomerPost() {
        return customerPost;
    }
    
    public void setCustomerPost(String customerPost) {
        this.customerPost = customerPost;
    }
    
    public String getCustomerCountry() {
        return customerCountry;
    }
    
    public void setCustomerCountry(String customerCountry) {
        this.customerCountry = customerCountry;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public List<OrderProductEntity> getProducts() {
        return products;
    }
    
    public void setProducts(List<OrderProductEntity> products) {
        this.products = products;
    }
}
