/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.STORED_PASSWORD_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang.StringUtils.reverse;
import static org.junit.jupiter.api.Assertions.*;

class StoredPasswordEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String PWD_1 = "Pwd1";
    private static final String PWD_2 = "Pwd2";

    @Test
    void buildFromEmptyBundle_noPasswords() {
        StoredPasswordEntityBuilder builder = new StoredPasswordEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), EntityBuilder.BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildBundleWithPasswords() {
        StoredPasswordEntityBuilder builder = new StoredPasswordEntityBuilder(ID_GENERATOR);
        StoredPassword pwd1 = buildStoredPassword(PWD_1);
        StoredPassword pwd2 = buildStoredPassword(PWD_2);
        Bundle bundle = new Bundle();
        bundle.putAllStoredPasswords(ImmutableMap.of(PWD_1, pwd1, PWD_2, pwd2));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());

        MutableBoolean containsPwd1 = new MutableBoolean();
        MutableBoolean containsPwd2 = new MutableBoolean();
        entities.forEach(e -> {
            switch (e.getName()) {
                case PWD_1:
                    checkPassword(containsPwd1, PWD_1, e, pwd1);
                    break;
                case PWD_2:
                    checkPassword(containsPwd2, PWD_2, e, pwd2);
                    break;
                default:
                    fail("unexpected password: " + e.getName());
                    break;
            }
        });

        assertTrue(containsPwd1.booleanValue());
        assertTrue(containsPwd2.booleanValue());
    }

    private static void checkPassword(MutableBoolean flag, String pwdName, Entity e, StoredPassword pwd) {
        flag.setValue(true);

        assertNotNull(e.getXml());
        assertNotNull(e.getId());
        assertNotNull(e.getType());
        assertEquals(STORED_PASSWORD_TYPE, e.getType());

        Element xml = e.getXml();
        assertEquals(STORED_PASSWD, xml.getNodeName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(pwdName, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(reverse(pwdName), getSingleChildElementTextContent(xml, PASSWORD));

        Element properties = getSingleChildElement(xml, PROPERTIES);
        assertNotNull(properties);
        assertPropertiesContent(pwd.getProperties(), mapPropertiesElements(properties, PROPERTIES));
    }

    private static StoredPassword buildStoredPassword(String name) {
        StoredPassword storedPassword = new StoredPassword();
        storedPassword.setName(name);
        storedPassword.setPassword(reverse(name));
        storedPassword.addDefaultProperties();
        return storedPassword;
    }
}
