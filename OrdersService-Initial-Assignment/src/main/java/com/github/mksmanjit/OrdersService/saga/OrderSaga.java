package com.github.mksmanjit.OrdersService.saga;

import com.github.mksmanjit.OrdersService.command.commands.ApproveOrderCommand;
import com.github.mksmanjit.OrdersService.command.commands.RejectOrderCommand;
import com.github.mksmanjit.OrdersService.core.events.OrderApprovedEvent;
import com.github.mksmanjit.OrdersService.core.events.OrderCreatedEvent;
import com.github.mksmanjit.OrdersService.core.events.OrderRejectedEvent;
import com.github.mksmanjit.OrdersService.core.model.OrderSummary;
import com.github.mksmanjit.OrdersService.query.FindOrderQuery;
import com.github.mksmanjit.core.commands.CancelProductReservationCommand;
import com.github.mksmanjit.core.commands.ProcessPaymentCommand;
import com.github.mksmanjit.core.commands.ReserveProductCommand;
import com.github.mksmanjit.core.events.PaymentProcessedEvent;
import com.github.mksmanjit.core.events.ProductReservationCancelledEvent;
import com.github.mksmanjit.core.events.ProductReservedEvent;
import com.github.mksmanjit.core.model.User;
import com.github.mksmanjit.core.query.FetchUserPaymentDetailsQuery;
import lombok.extern.apachecommons.CommonsLog;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
@CommonsLog
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;
    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private String scheduleId;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        log.info("OrderCreatedEvent handled for orderId: " + reserveProductCommand.getOrderId()
                + " and productId: " + reserveProductCommand.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()) {
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                            commandResultMessage.exceptionResult().getMessage());
                    commandGateway.send(rejectOrderCommand);
                }

            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {
        log.info("ProductReservedEvent handled for orderId: " + productReservedEvent.getOrderId()
                + " and productId: " + productReservedEvent.getProductId());
        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery
                = new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
        User user = null;
        try {
            user = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex) {
            log.error("Starting Compnesating transation", ex);
            cancelProductReservation(productReservedEvent, ex.getMessage());

        }
        if(user == null) {
            // start compensating transaction
            cancelProductReservation(productReservedEvent, "Could not fetch user payment detail");
        }
        log.info("Successfully fetched user payment details for user " + user.getFirstName());
        scheduleId = deadlineManager.schedule(Duration.of(10, ChronoUnit.SECONDS),
                "payment-processing-deadline", productReservedEvent);
      //  if (true) return;

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();
        commandGateway.send(processPaymentCommand, new CommandCallback<ProcessPaymentCommand, Object>() {

            @Override
            public void onResult(CommandMessage<? extends ProcessPaymentCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()) {
                    cancelProductReservation(productReservedEvent, "Internal Server Error");
                }
            }
        });
    }

    private void cancelProductReservation(ProductReservedEvent event, String reason) {
        cancelDeadline();
        CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId())
                .reason(reason)
                .build();

        commandGateway.send(cancelProductReservationCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {
        cancelDeadline();
        ApproveOrderCommand approveOrderCommand = ApproveOrderCommand.builder()
                .orderId(paymentProcessedEvent.getOrderId()).build();
        commandGateway.send(approveOrderCommand);

    }

    private void cancelDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule("payment-processing-deadline", scheduleId);
            scheduleId = null;
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        log.info("Order is Approved.Order saga is completed with order Id " + orderApprovedEvent.getOrderId());
       // SagaLifecycle.end();
        queryUpdateEmitter.emit(FindOrderQuery.class,query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(),orderApprovedEvent.getOrderStatus(), ""));

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent event) {
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(event.getOrderId(), event.getReason());
        commandGateway.send(rejectOrderCommand);

    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent event) {
       log.info("Successfully Rejected order with id " + event.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class,query -> true,
                new OrderSummary(event.getOrderId(),event.getOrderStatus(), event.getReason()));

    }

    @DeadlineHandler(deadlineName = "payment-processing-deadline")
    public void handlePaymentDeadline(ProductReservedEvent event) {
        log.info("Payment processing deadline took place. Sending a compensating command to cancel the product reservation");
        cancelProductReservation(event,"Payment timeout");

    }
}
