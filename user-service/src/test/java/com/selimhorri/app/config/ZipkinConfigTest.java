package com.selimhorri.app.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ZipkinConfigTest {

    @Test
    void testZipkinConfigInstantiation() {
        ZipkinConfig config = new ZipkinConfig();
        assertNotNull(config);
    }
}
