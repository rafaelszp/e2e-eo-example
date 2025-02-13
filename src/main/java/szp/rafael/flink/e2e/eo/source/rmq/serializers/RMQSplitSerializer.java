package szp.rafael.flink.e2e.eo.source.rmq.serializers;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.util.InstantiationUtil;
import szp.rafael.flink.e2e.eo.source.rmq.RMQSplit;

import java.io.IOException;
/**
 * This class is intended only for POC. There may be better alternatives like:
 * - Avro + Schema Registry
 * - Protobuff
 * - Thrift */
public class RMQSplitSerializer implements SimpleVersionedSerializer<RMQSplit> {

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public byte[] serialize(RMQSplit rmqSplit) throws IOException {
        return InstantiationUtil.serializeObject(rmqSplit);
    }

    @Override
    public RMQSplit deserialize(int i, byte[] bytes) throws IOException {
        try {
            return InstantiationUtil.deserializeObject(bytes, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize the split",e);
        }
    }
}
