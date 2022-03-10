package com.github.mksmanjit.OrdersService.command.commands;

import lombok.Builder;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Builder
@Data
public class ApproveOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
}
