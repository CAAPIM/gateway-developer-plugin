/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.Encass;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.EncassParam;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EncassWriterTest {
    private EncassWriter writer = new EncassWriter(DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);

    @Test
    void testGetBeanArgsAndResultsAreOrdered() {
        EncassEntity entity = new EncassEntity("my-encass", "123", "abc", "policy-123",
                Arrays.asList(new EncassEntity.EncassParam("a", "t"), new EncassEntity.EncassParam("x", "t"), new EncassEntity.EncassParam("1", "t"), new EncassEntity.EncassParam("B", "t")),
                Arrays.asList(new EncassEntity.EncassParam("B", "t"), new EncassEntity.EncassParam("1", "t"), new EncassEntity.EncassParam("x", "t"), new EncassEntity.EncassParam("a", "t")));

        final Encass bean = writer.getEncassBean(entity);
        assertNotNull(bean);
        assertNotNull(bean.getArguments());
        assertEquals(4, bean.getArguments().size());
        Iterator<EncassParam> argIter = bean.getArguments().iterator();
        assertEquals("1", argIter.next().getName());
        assertEquals("B", argIter.next().getName());
        assertEquals("a", argIter.next().getName());
        assertEquals("x", argIter.next().getName());

        assertNotNull(bean.getResults());
        assertEquals(4, bean.getResults().size());
        Iterator<EncassParam> resIter = bean.getResults().iterator();
        assertEquals("1", resIter.next().getName());
        assertEquals("B", resIter.next().getName());
        assertEquals("a", resIter.next().getName());
        assertEquals("x", resIter.next().getName());
    }
}