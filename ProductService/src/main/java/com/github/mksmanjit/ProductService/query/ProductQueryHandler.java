package com.github.mksmanjit.ProductService.query;

import com.github.mksmanjit.ProductService.core.data.ProductEntity;
import com.github.mksmanjit.ProductService.core.data.ProductRepository;
import com.github.mksmanjit.ProductService.query.rest.ProductRestModel;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductQueryHandler {

    @Autowired
    private ProductRepository productRepository;

    @QueryHandler
    public List<ProductRestModel> findProduct(FindProductQuery query) {
        List<ProductEntity> storedProduct = productRepository.findAll();
        return storedProduct.stream().map(obj -> {ProductRestModel model = new ProductRestModel();BeanUtils.copyProperties(obj, model); return model;}).collect(Collectors.toList());
    }
}
