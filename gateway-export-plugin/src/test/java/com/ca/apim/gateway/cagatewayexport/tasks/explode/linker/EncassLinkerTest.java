/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.util.policy.EncassPolicyXMLSimplifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.API_PORTAL_ENCASS_INTEGRATION;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

class EncassLinkerTest {
    private EncassLinker encassLinker;
    private Encass myEncass;
    private Bundle bundle;
    private static final String ENCASS_POLICY_WITH_PORTAL_INTEGRATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
            "    <wsp:All wsp:Usage=\"Required\">\n" +
            "        <L7p:ApiPortalEncassIntegration/>\n" +
            "        <L7p:CommentAssertion>\n" +
            "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
            "        </L7p:CommentAssertion>\n" +
            "    </wsp:All>\n" +
            "</wsp:Policy>";

    private static final String ENCASS_POLICY_WITH_PORTAL_INTEGRATION_DISABLED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
            "    <wsp:All wsp:Usage=\"Required\">\n" +
            "        <L7p:ApiPortalEncassIntegration>\n" +
            "            <L7p:Enabled booleanValue=\"false\"/>\n" +
            "        </L7p:ApiPortalEncassIntegration>\n" +
            "        <L7p:CommentAssertion>\n" +
            "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
            "        </L7p:CommentAssertion>\n" +
            "    </wsp:All>\n" +
            "</wsp:Policy>";

    @BeforeEach
    void setUp() {
        encassLinker = new EncassLinker(new EncassPolicyXMLSimplifier());
        myEncass = createEncass("myEncass", "1", "1", "1");
        bundle = new Bundle();
        bundle.addEntity(myEncass);
    }

    @Test
    void link() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myEncass.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1", "1", "1", DocumentUtils.stringToXML(DocumentTools.INSTANCE, ENCASS_POLICY_WITH_PORTAL_INTEGRATION), EMPTY));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        encassLinker.link(bundle, fullBundle);

        assertEquals("myEncassPolicy", bundle.getEntities(Encass.class).get("1").getPath());
    }

    @Test
    void linkMissingPolicy() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myEncass.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1","1", "1", DocumentUtils.stringToXML(DocumentTools.INSTANCE, ENCASS_POLICY_WITH_PORTAL_INTEGRATION), ""));
        fullBundle.addEntity(createFolder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }

    @Test
    void linkPortalTemplateFlag() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myEncass.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1","1", "1", DocumentUtils.stringToXML(DocumentTools.INSTANCE, ENCASS_POLICY_WITH_PORTAL_INTEGRATION), EMPTY));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        encassLinker.link(bundle, fullBundle);
        Encass linkedEncass = bundle.getEntities(Encass.class).get("1");
        assertEquals("myEncassPolicy", linkedEncass.getPath());
        assertEquals(2, linkedEncass.getProperties().size());
        assertTrue(Boolean.valueOf(linkedEncass.getProperties().get(L7_TEMPLATE).toString()));

        Policy updatedPolicy =  fullBundle.getEntities(Policy.class).get("1");
        assertThrows(DocumentParseException.class, () -> getSingleElement(updatedPolicy.getPolicyDocument(), API_PORTAL_ENCASS_INTEGRATION));
    }

    @Test
    void linkPortalTemplateDisabledFlag() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myEncass.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1","1", "1", DocumentUtils.stringToXML(DocumentTools.INSTANCE, ENCASS_POLICY_WITH_PORTAL_INTEGRATION_DISABLED), EMPTY));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        encassLinker.link(bundle, fullBundle);
        Encass linkedEncass = bundle.getEntities(Encass.class).get("1");
        assertEquals("myEncassPolicy", linkedEncass.getPath());
        assertEquals(2, linkedEncass.getProperties().size());
        assertFalse(Boolean.valueOf(linkedEncass.getProperties().get(L7_TEMPLATE).toString()));

        Policy updatedPolicy =  fullBundle.getEntities(Policy.class).get("1");
        assertThrows(DocumentParseException.class, () -> getSingleElement(updatedPolicy.getPolicyDocument(), API_PORTAL_ENCASS_INTEGRATION));
    }
}