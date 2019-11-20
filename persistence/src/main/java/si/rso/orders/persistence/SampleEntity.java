package si.rso.orders.persistence;

import javax.persistence.*;

@Entity
@Table(name = "samples")
@NamedQueries(value = {
    @NamedQuery(name = SampleEntity.FIND_BY_AGE, query = "SELECT s FROM SampleEntity s WHERE s.age = :age")
})
public class SampleEntity extends BaseEntity {

    public static final String FIND_BY_AGE = "SampleEntity.findByAge";
    
    @Column(name = "greeting")
    private String greeting;
    
    @Column(name = "age")
    private int age;
    
    public String getGreeting() {
        return greeting;
    }
    
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
}