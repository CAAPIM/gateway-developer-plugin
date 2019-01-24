/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XMLPolicyConverterTest {

    private XMLPolicyConverter xmlPolicyConverter;
    @Mock
    private DocumentTools documentFileUtils;

    @BeforeEach
    void beforeEach() {
        xmlPolicyConverter = new XMLPolicyConverter(documentFileUtils);
    }

    @Test
    void canConvertTest() {
        //always false
        assertFalse(xmlPolicyConverter.canConvert("someOtherPolicyName", null));
    }

    @Test
    void convertFromPolicyElement() throws IOException {
        doAnswer(invocation -> {
            IOUtils.write("out", invocation.getArgument(1), StandardCharsets.UTF_8);
            ((OutputStream) invocation.getArgument(1)).close();
            return null;
        }).when(documentFileUtils).printXML(nullable(Element.class), any(OutputStream.class), any(Boolean.class));

        InputStream xmlStream = xmlPolicyConverter.convertFromPolicyElement(null);
        String xmlString = IOUtils.toString(xmlStream, StandardCharsets.UTF_8);
        assertEquals("out", xmlString);
    }
}