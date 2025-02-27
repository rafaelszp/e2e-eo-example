package szp.rafael.flink.e2e.eo.source.rmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.flink.api.connector.source.ReaderOutput;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.base.source.reader.synchronization.FutureCompletingBlockingQueue;
import org.apache.flink.core.io.InputStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class RMQReader implements SourceReader<String, RMQSplit> {

    private static final Logger LOG = LoggerFactory.getLogger(RMQReader.class);
    private final String queueName;
    private final String host;
    private final String username;
    private final String password;
    private final SourceReaderContext context;
    private final Properties properties;
    private Channel channel;
    private RMQSplit currentSplit;
    private final FutureCompletingBlockingQueue<Tuple3<Long, String, String>> elementsQueue;
    private AMQP.Queue.DeclareOk declareOk;
    private String consumerTag;
    private int readerId;
    private AtomicLong readMessages = new AtomicLong(0);


    public RMQReader(SourceReaderContext sourceReaderContext, Properties properties, int readerId) {
        this.properties = properties;
        this.queueName = properties.getProperty("queueName", "fruit-queue");
        this.host = properties.getProperty("host", "localhost");
        this.username = properties.getProperty("username", "user");
        this.password = properties.getProperty("password", "password");
        this.elementsQueue = new FutureCompletingBlockingQueue<>();
        this.context = sourceReaderContext;
        this.readerId = readerId;
    }

    @Override
    public void start() {
        LOG.debug("################################################## STARTING READER {}", readerId);
    }

    @Override
    public InputStatus pollNext(ReaderOutput<String> readerOutput) throws Exception {
        if (currentSplit == null) {
            return InputStatus.NOTHING_AVAILABLE;
        }
        Tuple3<Long, String, String> element = elementsQueue.peek();
        if (element != null) {

            try {
                if (element.f1.equals(currentSplit.getLastMessageId())) {
                    LOG.info("**************************************************************** Skipping already collected message {}", currentSplit.toJson());
                } else {
                    readerOutput.collect(element.f2);
                    LOG.debug("################################################## reader {} - Collected: {} - read by this split {}", readerId, element, readMessages);
                }
                currentSplit.setLastMessageId(element.f1);
                simulateError(true);
//                channel.basicAck(currentSplit.getLastDeliveryTag(), false);
                channel.basicAck(element.f0, false);
                currentSplit.setLastDeliveryTag(element.f0);
                elementsQueue.poll();
                LOG.debug("################################################## reader {} - Acked: {} - read by this split {}", readerId, element, readMessages);
            }catch(com.rabbitmq.client.AlreadyClosedException e){
                LOG.warn("Message Already Acked: {}", element);
                elementsQueue.poll();
            } catch (Exception e) {
                LOG.warn("\n---------------------------------------------------------------------------------------------------------------------------------------------------");
                LOG.warn("Error simulation, what is going to happen? elmnt:{}", element);
                LOG.warn("Error simulation, what is going to happen? split: {}", currentSplit.toJson());
                LOG.warn("Exception class: {}. Message {}",e.getClass().getName(),e.getMessage());
                LOG.warn("---------------------------------------------------------------------------------------------------------------------------------------------------\n");

            }
            return InputStatus.MORE_AVAILABLE;
        }
        return InputStatus.NOTHING_AVAILABLE;
    }

    private DeliverCallback deliverCallback() throws IOException {

        return (consumerTag, delivery) -> {
            try {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                LOG.debug(" [x] Received '{}'", message);
                Tuple3<Long, String, String> element = new Tuple3<>(delivery.getEnvelope().getDeliveryTag(), delivery.getProperties().getMessageId(), message);
                elementsQueue.put(context.getIndexOfSubtask(), element);
                readMessages.incrementAndGet();
            } catch (Exception e) {
                closeChannel();
                prepareConsumer();
            }
        };
    }

    private void simulateError(boolean active) {
        if(!active) return;
        boolean error = new SecureRandom().nextBoolean() && readMessages.get() > 5;
        error &= System.currentTimeMillis() % 2 == 0;
        if (error) {
            throw new RuntimeException("Simulating error. Read Messages: " + readMessages);
        }
    }

    @Override
    public List<RMQSplit> snapshotState(long l) {
        return currentSplit != null ? List.of(currentSplit) : List.of();
    }

    @Override
    public CompletableFuture<Void> isAvailable() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        return elementsQueue.getAvailabilityFuture().thenAccept(e -> {
            LOG.debug("################################################## reader {} - IS AVAILABLE: {}", readerId, e);
            future.complete(e);
        });
    }

    @Override
    public void addSplits(List<RMQSplit> list) {
        if (!list.isEmpty()) {
            this.currentSplit = list.get(0);
            LOG.debug("################################################## reader {} - ADDING SPLIT: {}", readerId, currentSplit.toJson());
            prepareConsumer();
        }
    }

    @Override
    public void notifyNoMoreSplits() {
        LOG.debug("################################################## reader {} - NO MORE SPLITS", readerId);
    }

    @Override
    public void close() throws Exception {
        LOG.debug("################################################## CLOSING READER: {}", readerId);
        closeChannel();
    }

    public void prepareConsumer() {
        try {
            if (currentSplit != null && declareOk == null && consumerTag == null) {
                LOG.debug("################################################## reader {} - PREPARING CONSUMER {}", readerId, currentSplit.toJson());
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setUsername(username);
                factory.setPassword(password);
                Connection connection = factory.newConnection();
                channel = connection.createChannel();
                channel.basicQos(0); //Quantos nack suportados? https://www.rabbitmq.com/docs/consumer-prefetch

                declareOk = channel.queueDeclare(currentSplit.splitId(), false, false, false, null);
                consumerTag = channel.basicConsume(currentSplit.splitId(), false, deliverCallback(), consumerTag -> {
                    LOG.debug("Consumer {} cancelled", consumerTag);
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error declaring queue", e);
        }
    }

    private void closeChannel() throws IOException {
        LOG.debug("################################################## reader {} - CLOSING CHANNEL", readerId);
        try {
            declareOk = null;
            consumerTag = null;
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
        } catch (TimeoutException e) {
            LOG.warn("Error closing channel", e);
        }
    }


}
