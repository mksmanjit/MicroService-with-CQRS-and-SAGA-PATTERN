package com.github.mksmanjit.ProductService.query;

import com.github.mksmanjit.ProductService.core.data.ProductEntity;
import com.github.mksmanjit.ProductService.core.data.ProductRepository;
import com.github.mksmanjit.ProductService.core.event.ProductCreatedEvent;
import com.github.mksmanjit.ProductService.query.rest.ProductRestModel;
import com.github.mksmanjit.core.events.ProductReservationCancelledEvent;
import com.github.mksmanjit.core.events.ProductReservedEvent;
import lombok.extern.apachecommons.CommonsLog;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
@ProcessingGroup("product-group")
public class ProductEventHandler {

    @Autowired
    private ProductRepository productRepository;

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException ex) throws IllegalArgumentException {
        throw ex;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception ex) throws Exception {
        throw ex;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {
        ProductEntity entity = new ProductEntity();
        BeanUtils.copyProperties(event,entity);
        productRepository.save(entity);
    }

    @EventHandler
    public void on(ProductReservationCancelledEvent event) throws Exception {
        ProductEntity entity = productRepository.findByProductId(event.getProductId());
        entity.setQuantity(entity.getQuantity() + event.getQuantity());
        productRepository.save(entity);
        log.info("ProductReservationCancelledEvent is called for productId: "
                + event.getProductId() + " and orderId: " + event.getOrderId());
    }

    @EventHandler
    public void on(ProductReservedEvent event) throws Exception {
        ProductEntity entity = productRepository.findByProductId(event.getProductId());
        entity.setQuantity(entity.getQuantity() - event.getQuantity());
        productRepository.save(entity);
        log.info("ProductReservedEvent is called for productId: "
                + event.getProductId() + " and orderId: " + event.getOrderId());
    }

    @ResetHandler
    public void reset() {
        productRepository.deleteAll();
    }
}
