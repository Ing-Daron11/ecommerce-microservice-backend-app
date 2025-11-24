package com.selimhorri.app.config;

import org.junit.jupiter.api.Test;
import com.selimhorri.app.config.client.ClientConfig;
import com.selimhorri.app.config.mapper.MapperConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentConfigTest {

    @Test
    void testClientConfigInstantiation() {
        ClientConfig config = new ClientConfig();
        assertNotNull(config);
    }

    @Test
    void testMapperConfigInstantiation() {
        MapperConfig config = new MapperConfig();
        assertNotNull(config);
    }
}
