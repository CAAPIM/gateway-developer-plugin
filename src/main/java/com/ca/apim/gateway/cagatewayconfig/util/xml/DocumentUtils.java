package com.ca.apim.gateway.cagatewayconfig.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * Utility methods to manage xml elements.
 */
public class DocumentUtils {

    public static Element createElementWithTextContent(Document document, String elementName, Object textContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(textContent != null ? textContent.toString() : EMPTY);
        return element;
    }

    public static Element createElementWithAttribute(Document document, String elementName, String attributeName, String attributeValue) {
        Element element = document.createElement(elementName);
        element.setAttribute(attributeName, attributeValue);
        return element;
    }

}
