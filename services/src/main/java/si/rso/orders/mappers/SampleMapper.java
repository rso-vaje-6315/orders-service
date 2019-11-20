package si.rso.orders.mappers;

import si.rso.orders.lib.Sample;
import si.rso.orders.persistence.SampleEntity;

public class SampleMapper {
    
    public static Sample fromEntity(SampleEntity entity) {
        return new Sample();
    }
    
}