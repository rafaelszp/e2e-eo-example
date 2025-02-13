package szp.rafael.flink.e2e.eo.source.rmq;

import org.apache.flink.api.connector.source.Boundedness;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import szp.rafael.flink.e2e.eo.source.rmq.serializers.RMQEnumeratorSerializer;
import szp.rafael.flink.e2e.eo.source.rmq.serializers.RMQSplitSerializer;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RMQSource implements Source<String, RMQSplit, Set<RMQSplit>> {

    private static final Logger LOG = LoggerFactory.getLogger(RMQSource.class);
    private final static AtomicInteger readerId = new AtomicInteger(0);

    private final Properties properties;

    public RMQSource(Properties properties) {
        this.properties = properties;
    }

    @Override
    public Boundedness getBoundedness() {
        return Boundedness.CONTINUOUS_UNBOUNDED;
    }

    @Override
    public SplitEnumerator<RMQSplit, Set<RMQSplit>> createEnumerator(SplitEnumeratorContext<RMQSplit> splitEnumeratorContext) throws Exception {
        LOG.info("######################################## Creating enumerator");
        return new RMQEnumerator(splitEnumeratorContext,properties);
    }

    @Override
    public SplitEnumerator<RMQSplit, Set<RMQSplit>> restoreEnumerator(SplitEnumeratorContext<RMQSplit> splitEnumeratorContext, Set<RMQSplit> rmqSplitSet) throws Exception {
        LOG.info("######################################## Restoring enumerator");
        return new RMQEnumerator(splitEnumeratorContext,properties);
    }

    @Override
    public SimpleVersionedSerializer<RMQSplit> getSplitSerializer() {
        return new RMQSplitSerializer();
    }

    @Override
    public SimpleVersionedSerializer<Set<RMQSplit>> getEnumeratorCheckpointSerializer() {
        return new RMQEnumeratorSerializer();
    }

    @Override
    public SourceReader<String, RMQSplit> createReader(SourceReaderContext sourceReaderContext) throws Exception {
        LOG.info("######################################## Creating Reader: {}", RMQSource.readerId.incrementAndGet());
        return new RMQReader(sourceReaderContext, properties,readerId.get());
    }
}
