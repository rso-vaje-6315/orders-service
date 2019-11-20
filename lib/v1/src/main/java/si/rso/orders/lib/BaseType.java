package si.rso.orders.lib;

import java.util.Date;

public class BaseType {
    
    private String id;
    
    private Date timestamp;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}