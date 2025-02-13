package szp.rafael.flink.e2e.eo.factory;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.ExternalizedCheckpointRetention;
import org.apache.flink.configuration.RestartStrategyOptions;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

public class QueueToFileFactory {

    public static StreamExecutionEnvironment createExecutionEnvironment(ParameterTool params) {
        var env = StreamExecutionEnvironment.getExecutionEnvironment();

        Configuration config = new Configuration();
        config.set(RestartStrategyOptions.RESTART_STRATEGY, "fixed-delay");
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_ATTEMPTS, 100); // number of restart attempts
        config.set(RestartStrategyOptions.RESTART_STRATEGY_FIXED_DELAY_DELAY, Duration.ofSeconds(3)); // delay

        env.enableCheckpointing(100 * 10); // Checkpoint em ms
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE); // Modo de checkpoint

        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(5 * 100); // Pausa mínima entre checkpoints
        env.getCheckpointConfig().setCheckpointTimeout(1000 * 60); // Timeout de checkpoint
        env.getCheckpointConfig().setTolerableCheckpointFailureNumber(10);
        env.getCheckpointConfig().setExternalizedCheckpointRetention(ExternalizedCheckpointRetention.RETAIN_ON_CANCELLATION);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        config.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
        config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "hdfs://localhost:9000/checkpoints");
        config.set(CheckpointingOptions.FS_WRITE_BUFFER_SIZE, 1024);
        config.set(StateBackendOptions.STATE_BACKEND, "rocksdb");


        if(!params.has("production")){
            config.setString("taskmanager.memory.network.max", "1gb");
            config.setString("taskmanager.memory.size", "1gb");
        }

        env.configure(config);

        return env;
    }

}
