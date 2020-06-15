package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import org.w3c.dom.Element;

public class SsgActiveConnectorLoaderTest {

    public void testLoad(){
        SsgActiveConnectorLoader ssgActiveConnectorLoader = new SsgActiveConnectorLoader();
        Bundle bundle = new Bundle();
        Element element =
        ssgActiveConnectorLoader.load(bundle, );
    }
}
