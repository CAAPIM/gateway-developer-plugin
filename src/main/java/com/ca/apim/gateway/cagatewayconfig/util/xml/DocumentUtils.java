package com.ca.apim.gateway.cagatewayconfig.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility methods to manage xml elements.
 */
public class DocumentUtils {

    public static Element createElementWithTextContent(Document document, String elementName, String textContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(textContent);
        return element;
    }

}
