/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mksmanjit.OrdersService.command;

import com.github.mksmanjit.OrdersService.command.commands.ApproveOrderCommand;
import com.github.mksmanjit.OrdersService.command.commands.RejectOrderCommand;
import com.github.mksmanjit.OrdersService.core.events.OrderApprovedEvent;
import com.github.mksmanjit.OrdersService.core.events.OrderCreatedEvent;
import com.github.mksmanjit.OrdersService.core.events.OrderRejectedEvent;
import com.github.mksmanjit.OrdersService.core.model.OrderStatus;
import com.github.mksmanjit.OrdersService.command.commands.CreateOrderCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;
    
    public OrderAggregate() {
    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) {   
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand, orderCreatedEvent);
        
        AggregateLifecycle.apply(orderCreatedEvent);
    }

    @CommandHandler
    public void handle(ApproveOrderCommand approveOrderCommand) {
        OrderApprovedEvent orderApprovedEvent
                = new OrderApprovedEvent(approveOrderCommand.getOrderId(),OrderStatus.APPROVED);
        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @CommandHandler
    public void handle(RejectOrderCommand rejectOrderCommand) {
        OrderRejectedEvent orderRejectedEvent
                = new OrderRejectedEvent(rejectOrderCommand.getOrderId(),rejectOrderCommand.getReason());
        AggregateLifecycle.apply(orderRejectedEvent);
    }

    @EventSourcingHandler
    public void on(OrderRejectedEvent rejectedEvent) throws Exception {
        this.orderStatus = rejectedEvent.getOrderStatus();
    }

    @EventSourcingHandler
    public void on(OrderApprovedEvent approvedEvent) throws Exception {
        this.orderStatus = approvedEvent.getOrderStatus();
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) throws Exception {
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.addressId = orderCreatedEvent.getAddressId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.orderStatus = orderCreatedEvent.getOrderStatus();
    }
 

}
