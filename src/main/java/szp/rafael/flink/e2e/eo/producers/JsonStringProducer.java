package szp.rafael.flink.e2e.eo.producers;


import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import szp.rafael.flink.e2e.eo.dto.Fruit;
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
            for(int i = 0; i < 10; i++) {
                Fruit fruit = FruitFactory.createFruit();
                String message = JSON.toJSONString(fruit);
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().correlationId(fruit.getName())
                        .messageId(fruit.getId().toString())
                        .build();
                channel.basicPublish("", QUEUE_NAME, properties, message.getBytes());
                logger.info(" [x] Sent '" + message + "'");
            }
        }
    }




}
