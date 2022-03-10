package com.github.mksmanjit.ProductService.command.interceptor;

import com.github.mksmanjit.ProductService.command.CreateProductCommand;
import com.github.mksmanjit.ProductService.core.data.ProductLookupEntity;
import com.github.mksmanjit.ProductService.core.data.ProductLookupRepository;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;

@Component
@CommonsLog
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    @Autowired
    private ProductLookupRepository productLookupRepository;
    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> list) {
       return (index, command) -> {
           log.info("Intercepted Command: " + command.getPayloadType());
            if(CreateProductCommand.class.equals(command.getPayloadType())) {
                CreateProductCommand createProductCommand = (CreateProductCommand)command.getPayload();
                ProductLookupEntity dbEntity = productLookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(), createProductCommand.getTitle());
                if(dbEntity != null) throw  new IllegalStateException(String.format("Product with productId %s or title %s already exists",
                        createProductCommand.getProductId(),createProductCommand.getTitle()));
            }
            return command;
        };
    }
}
