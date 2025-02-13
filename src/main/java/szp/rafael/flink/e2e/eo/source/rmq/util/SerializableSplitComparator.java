package szp.rafael.flink.e2e.eo.source.rmq.util;

import szp.rafael.flink.e2e.eo.source.rmq.RMQSplit;

import java.io.Serializable;
import java.util.Comparator;

public class SerializableSplitComparator extends Object implements Comparator<RMQSplit>, Serializable {

    @Override
    public int compare(RMQSplit o1, RMQSplit o2) {
        return o1.splitId().compareTo(o2.splitId());
    }
}
