package com.github.mksmanjit.OrdersService.query;

import com.github.mksmanjit.OrdersService.core.data.OrderEntity;
import com.github.mksmanjit.OrdersService.core.data.OrdersRepository;
import com.github.mksmanjit.OrdersService.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueryHandler {

    @Autowired
    private OrdersRepository ordersRepository;

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery query) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(query.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(), "");
    }
}
