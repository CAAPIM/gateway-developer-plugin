/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.TestUtils;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.policy.ServicePolicyXMLSimplifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil.MIGRATE_PORTAL_INTEGRATIONS_ASSERTIONS_PROPERTY;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.SERVICE_DETAIL;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.API_PORTAL_INTEGRATION_FLAG;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Extensions({ @ExtendWith(MockitoExtension.class) })
class ServiceLinkerTest {

    @Mock
    private PolicyXMLSimplifier policyXMLSimplifier;
    private ServiceLinker linker;
    private Service myService;
    private Bundle bundle;
    private static final String SERVICE_POLICY_WITH_PORTAL_INTEGRATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
            "    <wsp:All wsp:Usage=\"Required\">\n" +
            "        <L7p:ApiPortalIntegration>\n" +
            "            <L7p:ApiGroup stringValue=\"\"/>\n" +
            "            <L7p:ApiId stringValue=\"71886958-5b81-4058-85c0-3505aeb14231\"/>\n" +
            "            <L7p:PortalManagedApiFlag stringValue=\"L7p:ApiPortalManagedServiceAssertion\"/>\n" +
            "        </L7p:ApiPortalIntegration>" +
            "        <L7p:CommentAssertion>\n" +
            "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
            "        </L7p:CommentAssertion>\n" +
            "    </wsp:All>\n" +
            "</wsp:Policy>";

    private static final String SERVICE_POLICY_WITH_PORTAL_INTEGRATION_DISABLED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
            "    <wsp:All wsp:Usage=\"Required\">\n" +
            "        <L7p:ApiPortalIntegration>\n" +
            "        <L7p:Enabled booleanValue=\"false\"/>" +
            "            <L7p:ApiGroup stringValue=\"\"/>\n" +
            "            <L7p:ApiId stringValue=\"71886958-5b81-4058-85c0-3505aeb14231\"/>\n" +
            "            <L7p:PortalManagedApiFlag stringValue=\"L7p:ApiPortalManagedServiceAssertion\"/>\n" +
            "        </L7p:ApiPortalIntegration>" +
            "        <L7p:CommentAssertion>\n" +
            "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
            "        </L7p:CommentAssertion>\n" +
            "    </wsp:All>\n" +
            "</wsp:Policy>";

    @BeforeEach
    void before() {
        System.setProperty(MIGRATE_PORTAL_INTEGRATIONS_ASSERTIONS_PROPERTY,
                ServiceAndPolicyLoaderUtil.MIGRATE_PORTAL_INTEGRATIONS_ASSERTIONS_PROPERTY_DEFAULT);
        linker = new ServiceLinker(DocumentTools.INSTANCE, policyXMLSimplifier, new ServicePolicyXMLSimplifier());
    }

    private Service createService(String serviceName, String policyXML){
        Service service = new Service();
        service.setPolicy(policyXML);
        service.setName(serviceName);
        final Element serviceXml = TestUtils.createServiceXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true);
        service.setServiceDetailsElement((Element) serviceXml.getElementsByTagName(SERVICE_DETAIL).item(0));
        return service;
    }

    @Test
    void linkInvalidPolicy() {
        Service service = new Service();
        service.setPolicy("policy");
        assertThrows(WriteException.class, () -> linker.link(service, null, null));
    }

    @Test
    void linkNoServiceProperties() {
        Bundle bundle = new Bundle();
        link(bundle, false);
        assertTrue(bundle.getServiceEnvironmentProperties().isEmpty());
    }

    @Test
    void linkWithServiceProperties() {
        Bundle bundle = new Bundle();
        link(bundle, true);
        assertFalse(bundle.getServiceEnvironmentProperties().isEmpty());
        assertEquals(1, bundle.getServiceEnvironmentProperties().size());
        ServiceEnvironmentProperty property = bundle.getServiceEnvironmentProperties().get("service.prop");
        assertNotNull(property);
        assertEquals("value2", property.getValue());
        assertEquals("service.prop", property.getKey());
    }

    private void link(Bundle bundle, boolean serviceProperties) {
        Folder folder = new Folder();
        folder.setName("folder");
        folder.setId("folder");
        folder.setParentFolder(Folder.ROOT_FOLDER);
        bundle.getFolders().put("folder", folder);
        bundle.getFolders().put(Folder.ROOT_FOLDER_ID, Folder.ROOT_FOLDER);
        FolderTree tree = new FolderTree(bundle.getFolders().values());
        bundle.setFolderTree(tree);

        Service service = new Service();
        service.setPolicy("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        service.setParentFolder(folder);
        service.setName("service");
        final Element serviceXml = TestUtils.createServiceXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), serviceProperties);
        service.setServiceDetailsElement((Element) serviceXml.getElementsByTagName(SERVICE_DETAIL).item(0));

        linker.link(service, bundle, bundle);

        assertNotNull(service.getPath());
        assertEquals("folder/service", service.getPath());
        assertNotNull(service.getPolicyXML());
    }


    @Test
    void linkPortalTemplateFlag() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myService = createService("myService", SERVICE_POLICY_WITH_PORTAL_INTEGRATION);
        bundle = new Bundle();
        bundle.addEntity(myService);
        myService.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        Folder parentFolder = createFolder("myFolder", "1", null);
        myService.setParentFolder(parentFolder);
        fullBundle.addEntity(myService);
        fullBundle.addEntity(parentFolder);

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        linker.link(myService, fullBundle, bundle);
        Set<Annotation> annotations = myService.getAnnotations();
        assertTrue(annotations.isEmpty());

        Element updatedPolicy =  myService.getPolicyXML();
        assertDoesNotThrow(() -> getSingleElement(updatedPolicy, API_PORTAL_INTEGRATION_FLAG));
    }

    @Test
    void linkPortalTemplateDisabledFlag() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myService = createService("myService", SERVICE_POLICY_WITH_PORTAL_INTEGRATION_DISABLED);
        bundle = new Bundle();
        bundle.addEntity(myService);
        myService.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        Folder parentFolder = createFolder("myFolder", "1", null);
        myService.setParentFolder(parentFolder);
        fullBundle.addEntity(myService);
        fullBundle.addEntity(parentFolder);

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        linker.link(myService, fullBundle, bundle);
        Set<Annotation> annotations = myService.getAnnotations();
        assertTrue(annotations.isEmpty());

        Element updatedPolicy =  myService.getPolicyXML();
        assertDoesNotThrow(() -> getSingleElement(updatedPolicy, API_PORTAL_INTEGRATION_FLAG));
    }

    @Test
    void testMigratePortalAssertionFlag() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myService = createService("myService", SERVICE_POLICY_WITH_PORTAL_INTEGRATION);
        bundle = new Bundle();
        bundle.addEntity(myService);
        System.setProperty(MIGRATE_PORTAL_INTEGRATIONS_ASSERTIONS_PROPERTY, "true");
        myService.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        Folder parentFolder = createFolder("myFolder", "1", null);
        myService.setParentFolder(parentFolder);
        fullBundle.addEntity(myService);
        fullBundle.addEntity(parentFolder);

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        linker.link(myService, fullBundle, bundle);
        Set<Annotation> annotations = myService.getAnnotations();
        assertFalse(annotations.isEmpty());
        assertTrue(annotations.contains(new Annotation(AnnotationType.BUNDLE)));

        Element updatedPolicy =  myService.getPolicyXML();
        assertThrows(DocumentParseException.class, () -> getSingleElement(updatedPolicy, API_PORTAL_INTEGRATION_FLAG));
    }


    @Test
    void testPortalFlagForNonPortalManagedEncass() throws DocumentParseException {
        Bundle fullBundle = new Bundle();
        myService = createService("myService", SERVICE_POLICY_WITH_PORTAL_INTEGRATION_DISABLED);
        bundle = new Bundle();
        bundle.addEntity(myService);
        System.setProperty(MIGRATE_PORTAL_INTEGRATIONS_ASSERTIONS_PROPERTY, "true");
        myService.setProperties(new HashMap<String, Object>() {{
            put(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        }});
        Folder parentFolder = createFolder("myFolder", "1", null);
        myService.setParentFolder(parentFolder);
        fullBundle.addEntity(myService);
        fullBundle.addEntity(parentFolder);

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);
        linker.link(myService, fullBundle, bundle);
        Set<Annotation> annotations = myService.getAnnotations();
        assertTrue(annotations.isEmpty());

        Element updatedPolicy =  myService.getPolicyXML();
        assertDoesNotThrow(() -> getSingleElement(updatedPolicy, API_PORTAL_INTEGRATION_FLAG));
    }

}