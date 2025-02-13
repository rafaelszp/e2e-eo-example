package szp.rafael.flink.e2e.eo.source.rmq;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.connector.source.SourceSplit;

import java.io.Serializable;

@Getter
public class RMQSplit implements SourceSplit, Serializable {

    private final String queueName;
    @Setter
    private long lastDeliveryTag;
    @Setter
    private String lastMessageId;


    public RMQSplit(String queueName, long lastDeliveryTag, String lastMessageId) {
        this.queueName = queueName;
        this.lastDeliveryTag = lastDeliveryTag;
        this.lastMessageId = lastMessageId;
    }

    /*
    * For this example there will be only one split
    * thus the splitId it's the queueName*/
    @Override
    public String splitId() {
        return queueName;
    }

    public String toJson() {
        return "{queueName: " + queueName + ", lastDeliveryTag: " + lastDeliveryTag + ", lastMessageId: " + lastMessageId + "}";
    }

}
