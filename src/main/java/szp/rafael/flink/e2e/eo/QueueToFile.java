package szp.rafael.flink.e2e.eo;

import com.alibaba.fastjson2.JSON;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;
import szp.rafael.flink.e2e.eo.factory.QueueToFileFactory;
import szp.rafael.flink.e2e.eo.processors.FaultyProcessor;
import szp.rafael.flink.e2e.eo.source.rmq.RMQSource;

import javax.sql.DataSource;
import java.util.Properties;

public class QueueToFile {
    public static void main(String[] args) throws Exception {

        ParameterTool params = ParameterTool.fromArgs(args);

        var env = QueueToFileFactory.createExecutionEnvironment(params);
        Properties props = new Properties();
        RMQSource source = new RMQSource(props);

        KeyedStream<Tuple2<Long, String>, Long> fruitStream = env.fromSource(source, WatermarkStrategy.noWatermarks(), "RMQ Source example")
                .map(new MapFunction<String, Tuple2<Long,String>>() {
                    @Override
                    public Tuple2<Long, String> map(String f) throws Exception {
                        var fruit = JSON.parseObject(f);
                        return new Tuple2<Long,String>(fruit.getLong("id"), f);
                    }
                })
                .uid("map-fruit")
                .keyBy(f -> f.f0);

        OutputTag<Tuple2<Long,String>> errorOutputTag = new OutputTag<Tuple2<Long, String>>("error-output"){};

        SingleOutputStreamOperator<Tuple2<Long, String>> processedFruitStream = fruitStream.process(new FaultyProcessor(errorOutputTag)).uid("process-fruit");

        fruitStream         .print("orig");
        processedFruitStream.print("proc");

        DataStream<Tuple2<Long, String>> sideOutputStream = processedFruitStream.getSideOutput(errorOutputTag);
        sideOutputStream.print("erro");

        env.execute("Queue to File");

    }
}
