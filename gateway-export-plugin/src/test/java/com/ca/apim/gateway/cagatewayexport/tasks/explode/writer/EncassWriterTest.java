/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassArgument;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassResult;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createEncass;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EncassWriterTest {
    private EncassWriter writer = new EncassWriter(DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);

    @Test
    void testGetBeanArgsAndResultsAreOrdered() {
        Encass bean = createEncass("my-encass", "123", "abc", "policy-123",
                Stream.of(new EncassArgument("a", "t"), new EncassArgument("x", "t"), new EncassArgument("1", "t"), new EncassArgument("B", "t")).collect(toSet()),
                Stream.of(new EncassResult("B", "t"), new EncassResult("1", "t"), new EncassResult("x", "t"), new EncassResult("a", "t")).collect(toSet()));

        bean.sortArgumentsAndResults();
        assertNotNull(bean.getArguments());
        assertEquals(4, bean.getArguments().size());
        Iterator<EncassArgument> argIter = bean.getArguments().iterator();
        assertEquals("1", argIter.next().getName());
        assertEquals("B", argIter.next().getName());
        assertEquals("a", argIter.next().getName());
        assertEquals("x", argIter.next().getName());

        assertNotNull(bean.getResults());
        assertEquals(4, bean.getResults().size());
        Iterator<EncassResult> resIter = bean.getResults().iterator();
        assertEquals("1", resIter.next().getName());
        assertEquals("B", resIter.next().getName());
        assertEquals("a", resIter.next().getName());
        assertEquals("x", resIter.next().getName());
    }

}