package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Element;

public class ListenPortLoader implements BundleEntityLoader {

    private final DocumentTools documentTools;

    ListenPortLoader(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    @Override
    public void load(Bundle bundle, Element element) {

    }
}
