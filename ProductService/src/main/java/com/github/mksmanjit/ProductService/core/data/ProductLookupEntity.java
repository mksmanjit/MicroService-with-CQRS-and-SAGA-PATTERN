package com.github.mksmanjit.ProductService.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Data
@NoArgsConstructor
@Table(name = "productlookup")
@AllArgsConstructor
public class ProductLookupEntity implements Serializable {
    private static final long serialVersionUID = 5610115881084275170L;
    @Id
    private String productId;
    @Column(unique = true)
    private String title;
}
