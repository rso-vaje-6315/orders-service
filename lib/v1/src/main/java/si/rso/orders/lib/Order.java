package si.rso.orders.lib;

public class Order extends BaseType {

    private String customerId;

    private double price;

    private int status;
    
    private String addressId;
    
    private OrderAddress address;

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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
}
