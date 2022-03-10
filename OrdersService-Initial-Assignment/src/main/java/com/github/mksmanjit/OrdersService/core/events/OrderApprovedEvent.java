/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mksmanjit.OrdersService.core.events;

import com.github.mksmanjit.OrdersService.core.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderApprovedEvent {
    private String orderId;
    private OrderStatus orderStatus;
}
