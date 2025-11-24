package com.selimhorri.app.business.product.strategy;

import java.util.List;
import com.selimhorri.app.business.product.model.ProductDto;

public interface ProductSortStrategy {
    List<ProductDto> sort(List<ProductDto> products);
    String getStrategyName();
}
