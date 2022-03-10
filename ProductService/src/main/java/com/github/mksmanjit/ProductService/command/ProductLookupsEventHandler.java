package com.github.mksmanjit.ProductService.command;

import com.github.mksmanjit.ProductService.core.data.ProductLookupEntity;
import com.github.mksmanjit.ProductService.core.data.ProductLookupRepository;
import com.github.mksmanjit.ProductService.core.event.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupsEventHandler {

    @Autowired
    private ProductLookupRepository productLookupRepository;

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductLookupEntity entity = new ProductLookupEntity(event.getProductId(), event.getTitle());
        productLookupRepository.save(entity);
    }

    @ResetHandler
    public void reset() {
        productLookupRepository.deleteAll();
    }
}
