package com.selimhorri.app.business.feature.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.selimhorri.app.business.feature.service.FeatureToggleService;

@Service
public class FeatureToggleServiceImpl implements FeatureToggleService {

    @Value("${feature.products.readonly:false}")
    private boolean readOnlyMode;

    @Override
    public boolean isProductsReadOnly() {
        return readOnlyMode;
    }
}
