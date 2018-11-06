/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class StoredPasswordLoader implements BundleEntityLoader {

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element storedPass = getSingleChildElement(getSingleChildElement(element, RESOURCE), STORED_PASSWD);

        final String name = getSingleChildElementTextContent(storedPass, NAME);
        final Map<String, Object> properties = BuilderUtils.mapPropertiesElements(getSingleChildElement(storedPass, PROPERTIES, true), PROPERTIES);

        StoredPassword password = new StoredPassword();
        password.setId(storedPass.getAttribute(ATTRIBUTE_ID));
        password.setName(name);
        password.setProperties(properties);

        bundle.getStoredPasswords().put(name, password);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.STORED_PASSWORD_TYPE;
    }
}
