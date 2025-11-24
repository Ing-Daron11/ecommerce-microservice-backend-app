package com.selimhorri.app.config;

import org.junit.jupiter.api.Test;
import com.selimhorri.app.config.mapper.MapperConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperConfigTest {

    @Test
    void testMapperConfigInstantiation() {
        MapperConfig config = new MapperConfig();
        assertNotNull(config);
    }
}
