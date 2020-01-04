package si.rso.orders.persistence;

import javax.persistence.*;

@Entity
@Table(name = "order_products")
@NamedQueries(value = {
        @NamedQuery(name = OrderProductEntity.FIND_BY_ORDER_ID,
                query = "SELECT op FROM OrderProductEntity op WHERE op.order.id = :orderId")
})
public class OrderProductEntity extends BaseEntity {

    public static final String FIND_BY_ORDER_ID = "OrderEntity.findByOrderId";

    @Column
    private String code;
    
    @Column
    private String name;
    
    @Column(name = "total_price")
    private double totalPrice;
    
    @Column(name = "product_id")
    private String productId;

    @Column
    private int quantity;

    @Column(name = "price_per_item")
    private double pricePerItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public double getPricePerItem() {
        return pricePerItem;
    }
    
    public void setPricePerItem(double pricePerItem) {
        this.pricePerItem = pricePerItem;
    }
    
    public OrderEntity getOrder() {
        return order;
    }
    
    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
