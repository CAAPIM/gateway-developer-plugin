package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.*;

import java.util.Base64;
import java.util.logging.Level;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

/**
 * Implementations of this class can modify policy XML structure to make it deployable in gateway.
 */
public interface PolicyAssertionBuilder {
    /**
     * Build the policy XML assertion element.
     *
     * @param element              context containing required data for building xml
     * @param policyBuilderContext Policy builder context
     * @throws DocumentParseException if there is any issue to read policy contents
     */
    void buildAssertionElement(Element element, PolicyBuilderContext policyBuilderContext) throws DocumentParseException;

    /**
     * @return the XML tag name for the assertion handled by this builder
     */
    String getAssertionTagName();

    /**
     * Default method that reads given annotated entity id
     *
     * @param gatewayEntity GatewayEntity
     * @param idGenerator   IdGenerator
     * @return String
     */
    default String getIdFromAnnotableEntity(GatewayEntity gatewayEntity, IdGenerator idGenerator) {
        if (gatewayEntity instanceof AnnotableEntity) {
            AnnotatedEntity annotatedEntity = ((AnnotableEntity) gatewayEntity).getAnnotatedEntity();
            if (annotatedEntity != null && annotatedEntity.getId() != null) {
                return annotatedEntity.getId();
            }
        }
        return idGenerator.generate();
    }

     static void prepareBase64Element(Document policyDocument, Element assertionElement, String elementName, String base64ElementName) {
        Element element;
        try {
            element = getSingleElement(assertionElement, elementName);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Did not find '" + elementName + "' tag for SetVariableAssertion. Not generating Base64ed version");
        }

        String expression = getCDataOrText(element);
        String encoded = Base64.getEncoder().encodeToString(expression.getBytes());
        assertionElement.insertBefore(createElementWithAttribute(policyDocument, base64ElementName, STRING_VALUE, encoded), element);
        assertionElement.removeChild(element);
    }

    static String getCDataOrText(Element element) {
        StringBuilder content = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (Node child : nodeList(children)) {
            short nodeType = child.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                content.append(child.getTextContent());
            } else if (nodeType == Node.CDATA_SECTION_NODE) {
                content.append(((CDATASection) child).getData());
                break;
            } else {
                throw new EntityBuilderException("Unexpected set variable assertion expression node type: " + child.getNodeName());
            }
        }
        return StringEscapeUtils.unescapeXml(content.toString());
    }
}
