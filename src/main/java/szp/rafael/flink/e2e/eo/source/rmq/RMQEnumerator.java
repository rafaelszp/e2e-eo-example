package szp.rafael.flink.e2e.eo.source.rmq;

import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.api.connector.source.SplitsAssignment;
import org.jetbrains.annotations.Nullable;
import szp.rafael.flink.e2e.eo.source.rmq.util.SerializableSplitComparator;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
/**
 * This POC enumerator can handle only one split for study purposes*/
public class RMQEnumerator implements SplitEnumerator<RMQSplit, Set<RMQSplit>>, Serializable {

    private final SortedSet<RMQSplit> pool;
    private final SplitEnumeratorContext<RMQSplit> context;
    private final List<Long> successfulCheckpoints;
    private final Properties properties;

    public RMQEnumerator(SplitEnumeratorContext<RMQSplit> context, Properties properties) {
        this.properties = properties;
        this.pool = new TreeSet<>(new SerializableSplitComparator());
        this.context = context;
        successfulCheckpoints = Collections.synchronizedList(new ArrayList<>());
        /*
        * For learning purposes we are adding only one split to the pool
        * as we are not handling multiple connections with rabbitmq at once
        * */
        pool.add(new RMQSplit(properties.getProperty("queueName","fruit-queue"), Long.MIN_VALUE,""));
    }

    @Override
    public void start() {

    }

    @Override
    public void handleSplitRequest(int i, @Nullable String s) {

    }

    /**
     * When there is an error, we needd to add the splits assigned to the failing instances back to flink*/
    @Override
    public void addSplitsBack(List<RMQSplit> list, int subtaskId) {
        pool.addAll(list);
    }

    /**
     * When flink adds a new reader we need to inform it
     * what splits are unassigned to the context */
    @Override
    public void addReader(int subtaskId) {
        Optional<RMQSplit> first = pool.stream().findFirst();
        List<RMQSplit> splits = first.map(List::of).orElseGet(List::of);
        var assignment = new SplitsAssignment<>(Collections.singletonMap(subtaskId, splits));
        context.assignSplits(assignment);
        //clearing the pool
        pool.clear();
    }

    @Override
    public Set<RMQSplit> snapshotState(long l) throws Exception {
        //snapshoting the state with splits that are not assigned
        //I think it doesn't make sense to snapshot the assigned splits as they are being processed
        return pool;
    }

    @Override
    public void close() throws IOException {

    }

    //Keeping track of the successful checkpoints
    @Override
    public void notifyCheckpointComplete(long checkpointId) throws Exception {
        successfulCheckpoints.add(checkpointId);
    }
}
