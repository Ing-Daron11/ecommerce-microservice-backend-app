package com.selimhorri.app.config;

import org.junit.jupiter.api.Test;
import com.selimhorri.app.config.client.ClientConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientConfigTest {

    @Test
    void testClientConfigInstantiation() {
        ClientConfig config = new ClientConfig();
        assertNotNull(config);
    }
}
