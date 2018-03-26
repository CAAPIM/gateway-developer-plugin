/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WriterHelper {

    private WriterHelper() {
    }

    public static Element stringToXML(DocumentTools documentTools, String string) throws DocumentParseException {
        Document document = documentTools.parse(string);
        documentTools.cleanup(document);
        return document.getDocumentElement();
    }
}
