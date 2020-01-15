package si.rso.orders.producers;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import si.rso.analytics.lib.Analytics;
import si.rso.analytics.lib.AnalyticsStreamConfig;
import si.rso.event.streaming.EventStreamMessage;
import si.rso.event.streaming.EventStreamMessageBuilder;
import si.rso.event.streaming.JacksonMapper;
import si.rso.orders.persistence.OrderProductEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KafkaProducer {

    @Inject
    @StreamProducer
    private Producer<String, EventStreamMessage> producer;

    public void sendToAnalytics(OrderProductEntity orderProductEntity) {

        Analytics analytics = new Analytics();
        analytics.setProductId(orderProductEntity.getProductId());
        analytics.setNumberOfOrders(orderProductEntity.getQuantity());
        analytics.setIncome(orderProductEntity.getPricePerItem() * orderProductEntity.getQuantity());

        EventStreamMessage message = EventStreamMessageBuilder.getInstance()
                .ofType(AnalyticsStreamConfig.SEND_NOTIFICATION_EVENT_ID)
                .withData(JacksonMapper.stringify(analytics))
                .build();

        ProducerRecord<String, EventStreamMessage> record = new ProducerRecord<>(
                AnalyticsStreamConfig.NOTIFICATIONS_CHANNEL, "key", message
        );

        producer.send(record, (meta, exc) -> {
            if (exc != null) {
                exc.printStackTrace();
            }
        });
    }
}
