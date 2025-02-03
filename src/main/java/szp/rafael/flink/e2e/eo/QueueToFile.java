package szp.rafael.flink.e2e.eo;

import org.apache.flink.api.java.utils.ParameterTool;
import szp.rafael.flink.e2e.eo.factory.QueueToFileFactory;

public class QueueToFile {
    public static void main(String[] args) throws Exception {

        ParameterTool params = ParameterTool.fromArgs(args);

        var env = QueueToFileFactory.createExecutionEnvironment(params);



        env.execute("Queue to File");

    }
}
