package szp.rafael.flink.e2e.eo.source.rmq.serializers;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.util.InstantiationUtil;
import szp.rafael.flink.e2e.eo.source.rmq.RMQEnumerator;
import szp.rafael.flink.e2e.eo.source.rmq.RMQSplit;

import java.io.IOException;
import java.util.Set;

public class RMQEnumeratorSerializer implements SimpleVersionedSerializer<Set<RMQSplit>> {
    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public byte[] serialize(Set<RMQSplit> rmqEnumerator) throws IOException {
        return InstantiationUtil.serializeObject(rmqEnumerator);
    }

    @Override
    public Set<RMQSplit> deserialize(int i, byte[] bytes) throws IOException {
        try {
            return InstantiationUtil.deserializeObject(bytes, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize the enumerator",e);
        }
    }
}
