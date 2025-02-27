package szp.rafael.flink.e2e.eo.processors;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import szp.rafael.flink.e2e.eo.exception.ProcessingException;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Optional;

public class FaultyProcessor extends KeyedProcessFunction<Long, Tuple2<Long, String>, Tuple2<Long, String>> {

    // For learning purposes
    private transient ValueState<Long> messageIdState;
    private transient ValueState<Boolean> presentState;
    private static final Logger LOG = LoggerFactory.getLogger(FaultyProcessor.class);
    private final OutputTag<Tuple2<Long, String>> errorOutputTag;

    public FaultyProcessor(OutputTag<Tuple2<Long, String>> errorOutputTag) {
        this.errorOutputTag = errorOutputTag;
    }

    @Override
    public void open(OpenContext openContext) throws Exception {
        this.messageIdState = getRuntimeContext().getState(new ValueStateDescriptor<>("read-messages", Long.class));
        this.presentState = getRuntimeContext().getState(new ValueStateDescriptor<>("present-messages", Boolean.class));
    }

    @Override
    public void processElement(Tuple2<Long, String> tuple, KeyedProcessFunction<Long, Tuple2<Long, String>, Tuple2<Long, String>>.Context context, Collector<Tuple2<Long, String>> collector) throws Exception {
        var isPresent = presentState.value() != null && presentState.value();
        if(isPresent) {
            LOG.warn("Message id {} already processed", tuple.f0);
        }else{
            LOG.info("Processing message id {} - {}", tuple.f0,tuple.f1);
        }
        var fruit = processFruit(tuple.f1);
        if (fruit.isPresent()) {
            collector.collect(new Tuple2<Long, String>(tuple.f0, fruit.get()));
            presentState.update(true);
        } else {
            context.output(errorOutputTag, new Tuple2<Long, String>(tuple.f0, tuple.f1));
        }
        messageIdState.clear();
    }

    private Optional<String> processFruit(String jsonFruit) {

        try {
            JSONObject data = JSON.parseObject(jsonFruit);
            var name = data.getString("name");
            if (name != null) {
                name = name.toUpperCase();
                data.put("name", name);
            }
            messageIdState.update(data.getLong("id"));
            simulateError();
            data.put("processed",true);
            return Optional.of(data.toJSONString());

        } catch (ProcessingException | IOException e) {
            LOG.warn("Failed to update message id state", e.getMessage());
        }

        return Optional.empty();

    }

    private void simulateError() throws ProcessingException {
        Long value = readValue();
        SecureRandom random = new SecureRandom();
        boolean error = random.nextBoolean() && random.nextLong(1, 400) % 3 == 0;
        error &= System.currentTimeMillis() % 2 == 0;
        if (error) {
            LOG.warn("Generating error. Message id: {}", value);
            throw new ProcessingException("Simulating error. Message id: " + value);
        }
    }

    private Long readValue() {
        try {
            return messageIdState.value() != null && messageIdState.value() != 0 ? messageIdState.value() : 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
