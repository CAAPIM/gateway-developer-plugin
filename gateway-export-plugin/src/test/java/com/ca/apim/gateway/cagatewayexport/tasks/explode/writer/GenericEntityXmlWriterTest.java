/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.apache.commons.io.FileUtils.readFileToString;

class GenericEntityXmlWriterTest {

    private GenericEntityXmlWriter writer = new GenericEntityXmlWriter(JsonTools.INSTANCE, FileUtils.INSTANCE, DocumentTools.INSTANCE);

    @Test
    void test() throws IOException, DocumentParseException {
        String xml = readFileToString(new File("/home/joaoborges/Desktop/genericEntity.xml"), Charset.defaultCharset());
        System.out.println(JsonTools.INSTANCE.getObjectWriter(JsonTools.JSON).writeValueAsString(writer.xmlToJson(xml)));
    }
}
