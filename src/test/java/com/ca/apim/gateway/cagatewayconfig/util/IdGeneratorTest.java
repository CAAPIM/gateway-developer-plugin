package com.ca.apim.gateway.cagatewayconfig.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {

    @Test
    void generate() {
        IdGenerator idGenerator = new IdGenerator();
        IdGenerator idGenerator2 = new IdGenerator();

        String id1 = idGenerator.generate();
        Assert.assertNotEquals(id1, idGenerator.generate());
        Assert.assertNotEquals(id1, idGenerator2.generate());
    }
}