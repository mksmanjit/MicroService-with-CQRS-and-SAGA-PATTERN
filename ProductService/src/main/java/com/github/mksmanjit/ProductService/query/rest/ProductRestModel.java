package com.github.mksmanjit.ProductService.query.rest;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@FieldNameConstants
public class ProductRestModel implements Serializable {
    private static final long serialVersionUID = 6750123627049586260L;
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
