package si.rso.orders.lib;

import si.rso.orders.lib.enums.OrderStatus;

import java.util.List;

public class Order extends BaseType {

    private String customerId;

    private double price;

    private OrderStatus status;
    
    private String addressId;
    
    private OrderAddress address;
    
    private List<OrderProduct> products;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public OrderAddress getAddress() {
        return address;
    }
    
    public void setAddress(OrderAddress address) {
        this.address = address;
    }
    
    public String getAddressId() {
        return addressId;
    }
    
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    
    public List<OrderProduct> getProducts() {
        return products;
    }
    
    public void setProducts(List<OrderProduct> products) {
        this.products = products;
    }
}
