package com.selimhorri.app.business.product.strategy.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.selimhorri.app.business.product.strategy.ProductSortStrategy;
import com.selimhorri.app.business.product.model.ProductDto;

@Component
public class PriceSortStrategy implements ProductSortStrategy {

    @Override
    public List<ProductDto> sort(List<ProductDto> products) {
        if (products == null) return null;
        return products.stream()
                .sorted(Comparator.comparing(ProductDto::getPriceUnit))
                .collect(Collectors.toList());
    }

    @Override
    public String getStrategyName() {
        return "price";
    }
}
