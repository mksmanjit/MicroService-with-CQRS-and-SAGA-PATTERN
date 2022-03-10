package com.github.mksmanjit.ProductService.command.rest;

import com.github.mksmanjit.ProductService.command.CreateProductCommand;
import com.github.mksmanjit.ProductService.controller.model.ProductModel;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductCommandController {

    @Autowired
    private Environment environment;

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping
    public String createProduct(@Valid @RequestBody ProductModel productModel) {
        CreateProductCommand createProductCommand = CreateProductCommand.builder().price(productModel.getPrice())
                .quantity(productModel.getQuantity())
                .title(productModel.getTitle())
                .productId(UUID.randomUUID().toString()).build();
        Runnable runnable = () -> System.out.println("hello");
        String returValue = null;
        return commandGateway.sendAndWait(createProductCommand);
    }
}
