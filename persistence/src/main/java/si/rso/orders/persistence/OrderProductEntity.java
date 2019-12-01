package si.rso.orders.persistence;

import javax.persistence.*;

@Entity
@Table(name = "order_products")
@NamedQueries(value = {
        @NamedQuery(name = OrderProductEntity.FIND_BY_ORDER_ID,
                query = "SELECT op FROM OrderProductEntity op WHERE op.orderId = :orderId")
})
public class OrderProductEntity extends BaseEntity {

    public static final String FIND_BY_ORDER_ID = "OrderEntity.findByOrderId";

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "product_id")
    private String productId;

    private int quantity;

    @Column(name = "price_per_item")
    private float pricePerItem;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public float getPricePerItem() {
        return pricePerItem;
    }

    public void setPricePerItem(float pricePerItem) {
        this.pricePerItem = pricePerItem;
    }
}
