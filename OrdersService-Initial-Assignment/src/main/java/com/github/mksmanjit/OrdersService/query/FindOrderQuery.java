package com.github.mksmanjit.OrdersService.query;

import lombok.Value;

@Value
public class FindOrderQuery {
    private final String orderId;
}
