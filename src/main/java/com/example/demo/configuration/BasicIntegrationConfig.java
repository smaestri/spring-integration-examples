package com.example.demo.configuration;

import com.example.demo.domain.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@EnableIntegration
public class BasicIntegrationConfig {

    private static final BigDecimal BOOK_DISCOUNT = new BigDecimal(0.05);
    private static final BigDecimal MUSIC_DISCOUNT = new BigDecimal(0.10);
    private static final BigDecimal SOFTWARE_DISCOUNT = new BigDecimal(0.15);


    @Bean
    public DirectChannel ordersChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel deliveries() {
        DirectChannel channel = new DirectChannel();
        channel.addInterceptor(tap());
        return channel;
    }

    @Bean
    public WireTap tap() {
        return new WireTap("logging");
    }

    @ServiceActivator(inputChannel = "logging")
    @Bean
    public LoggingHandler logger() {
        LoggingHandler logger = new LoggingHandler(LoggingHandler.Level.INFO);
        logger.setLogExpressionString("'Files:' + payload");
        return logger;
    }

    @Bean
    public DirectChannel ordersItemsChannel() {
        return new DirectChannel();
    }

    @Bean
    public DirectChannel processedItems() {
        return new DirectChannel();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata poller() {
        return Pollers.fixedRate(500).get();
    }

    @Bean
    public QueueChannel bookItemsChannel() {
        return new QueueChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "bookItemsChannel")
    public AbstractReplyProducingMessageHandler processBook() {

        AbstractReplyProducingMessageHandler mh = new AbstractReplyProducingMessageHandler() {
            @Override
            protected Object handleRequestMessage(Message<?> message) {
                OrderItem bookOrderItem = (OrderItem) message.getPayload();
                final BigDecimal finalPrice = calculateDiscountedPrice(bookOrderItem, BOOK_DISCOUNT);

                bookOrderItem.setDiscountedPrice(finalPrice);

                return bookOrderItem;
            }
        };
        mh.setOutputChannelName("processedItems");
        return mh;


    }


//    @Bean
//    public QueueChannel musicItemsChannel() {
//        return new QueueChannel();
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "musicItemsChannel", outputChannel = "processedItems")
//    public OrderItem processMusic(OrderItem musicOrderItem) {
//        final BigDecimal finalPrice = calculateDiscountedPrice(musicOrderItem, MUSIC_DISCOUNT);
//
//        musicOrderItem.setDiscountedPrice(finalPrice);
//
//        return musicOrderItem;
//    }

//    @Bean
//    public QueueChannel softwareItemsChannel() {
//        return new QueueChannel();
//    }
//
//    @Bean
//    @ServiceActivator(inputChannel = "softwareItemsChannel", outputChannel = "processedItems")
//    public OrderItem processSoftware(OrderItem softwareOrderItem) {
//        final BigDecimal finalPrice = calculateDiscountedPrice(softwareOrderItem, SOFTWARE_DISCOUNT);
//
//        softwareOrderItem.setDiscountedPrice(finalPrice);
//
//        return softwareOrderItem;
//    }


    @Splitter(inputChannel = "ordersChannel", outputChannel = "orderItemsChannel")
    List<OrderItem> extractItems(Order order) {
        return order.getOrderItems();
    }

    @Router(inputChannel = "orderItemsChannel")
    public String routeOrder(Object payload) {

        OrderItem orderItem = (OrderItem) payload;
        String channel = "";
        if (isBook(orderItem)) {
            channel = "bookItemsChannel";
        }
//        else if(isMusic(orderItem)) {
//            channel = "musicItemsChannel";
//        }
//        else if(isSoftware(orderItem)) {
//            channel = "softwareItemsChannel";
//        }
        return channel;
    }


    @Aggregator(inputChannel = "processedItems", outputChannel = "deliveries")
    public Order agregate(List<OrderItem> orderItems) {
        final Order order = new Order();
        order.setOrderItems(orderItems);
        return order;
    }

    private Boolean isBook(OrderItem orderItem) {
        return orderItem.getItem() instanceof Book;
    }

    private Boolean isMusic(OrderItem orderItem) {
        return orderItem.getItem() instanceof MusicCD;
    }

    private Boolean isSoftware(OrderItem orderItem) {
        return orderItem.getItem() instanceof Software;
    }

    private BigDecimal calculateDiscountedPrice(final OrderItem orderItem, final BigDecimal discount) {

        final BigDecimal discountedPrice = round(orderItem.getTotalPrice().multiply(discount));
        final BigDecimal finalPrice = round(orderItem.getTotalPrice().subtract(discountedPrice));


        return finalPrice;
    }

    private BigDecimal round(final BigDecimal value) {
        return value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }


}