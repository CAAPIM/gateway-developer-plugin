package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleDocumentBuilder.GATEWAY_MANAGEMENT;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleDocumentBuilder.L7;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class PrivateKeyImportContextBuilder {

    public Element build(PrivateKey privateKey, Document document) {
        if (privateKey != null && privateKey.getPrivateKeyFile() != null) {
            try {
                String pkcs12Data =
                        Base64.encodeBase64String(IOUtils.toByteArray(privateKey.getPrivateKeyFile().getWithIO()));
                Element privateKeyImportContext = createElementWithChildren(
                        document,
                        PRIVATE_KEY_IMPORT_CONTEXT,
                        createElementWithTextContent(document, PRIVATE_KEY_PKCS12_DATA, pkcs12Data),
                        createElementWithTextContent(document, PRIVATE_KEY_ALIAS, privateKey.getAlias()),
                        createElementWithTextContent(document, PRIVATE_KEY_PASSWORD, privateKey.getKeyPassword()));
                privateKeyImportContext.setAttribute(L7, GATEWAY_MANAGEMENT);
                return privateKeyImportContext;
            } catch (IOException e) {
                throw new EntityBuilderException("Couldn't load private key file: " + privateKey.getAlias());
            }
        }
        return null;
    }
}
