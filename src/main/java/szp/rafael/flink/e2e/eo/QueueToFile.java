package szp.rafael.flink.e2e.eo;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import szp.rafael.flink.e2e.eo.factory.QueueToFileFactory;
import szp.rafael.flink.e2e.eo.source.rmq.RMQSource;

import javax.sql.DataSource;
import java.util.Properties;

public class QueueToFile {
    public static void main(String[] args) throws Exception {

        ParameterTool params = ParameterTool.fromArgs(args);

        var env = QueueToFileFactory.createExecutionEnvironment(params);
        Properties props = new Properties();
        RMQSource source = new RMQSource(props);
        env.fromSource(source, WatermarkStrategy.noWatermarks(),"RMQ Source example").print();



        env.execute("Queue to File");

    }
}
