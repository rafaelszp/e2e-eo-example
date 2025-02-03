package szp.rafael.flink.e2e.eo.producers;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import szp.rafael.flink.e2e.eo.factory.FruitFactory;


public class JsonStringProducer {

    public static final String RABBITMQ_PASSWORD = "password";
    private final static String QUEUE_NAME = "fruit-queue";
    public static final String RABBIT_HOST = "localhost";
    public static final String RABBITMQ_USER = "user";

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JsonStringProducer.class);


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBIT_HOST);
        factory.setUsername(RABBITMQ_USER);
        factory.setPassword(RABBITMQ_PASSWORD);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = FruitFactory.createJsonFruit();
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            logger.info(" [x] Sent '" + message + "'");
        }
    }




}
