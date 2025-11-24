package com.selimhorri.app.business.product.strategy.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.selimhorri.app.business.product.strategy.ProductSortStrategy;
import com.selimhorri.app.business.product.model.ProductDto;

@Component
public class DefaultSortStrategy implements ProductSortStrategy {

    @Override
    public List<ProductDto> sort(List<ProductDto> products) {
        return products;
    }

    @Override
    public String getStrategyName() {
        return "default";
    }
}
