package com.ca.apim.gateway.cagatewayconfig.util.bundle;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyEntityBuilder.ZERO_GUID;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.ATTRIBUTE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.RESOURCE;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.ENCAPSULATED_ASSERTION_CONFIG_GUID;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.nodeList;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class DependencyBundlesProcessorTest {

    private static final String TEST_1_BUNDLE = "DependencyBundleProcessorTest_1.bundle";
    private static final String TEST_2_BUNDLE = "DependencyBundleProcessorTest_2.bundle";
    private static final String TEST_3_BUNDLE = "DependencyBundleProcessorTest_3.bundle";
    private static final String TEST_4_BUNDLE = "DependencyBundleProcessorTest_4.bundle";
    private static final String TEST_5_BUNDLE = "DependencyBundleProcessorTest_5.bundle";
    private TemporaryFolder rootProjectDir;
    private DependencyBundlesProcessor processor;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
        processor = InjectionRegistry.getInstance(DependencyBundlesProcessor.class);
    }

    @Test
    void testAttachingEncasses() throws IOException, DocumentParseException {
        File originFolder = new File(rootProjectDir.getRoot(), "original");
        originFolder.mkdirs();
        File destinationFolder = new File(rootProjectDir.getRoot(), "processed");
        destinationFolder.mkdirs();

        // copy the bundles
        byte[] bundle1Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_1_BUNDLE));
        byte[] bundle2Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_2_BUNDLE));
        File file1 = new File(originFolder, TEST_1_BUNDLE);
        file1.createNewFile();
        File file2 = new File(originFolder, TEST_2_BUNDLE);
        file2.createNewFile();
        Files.write(file1.toPath(), bundle1Contents);
        Files.write(file2.toPath(), bundle2Contents);

        // and process them
        LinkedList<File> processed = processor.process(Stream.of(new File(originFolder, TEST_1_BUNDLE), new File(originFolder, TEST_2_BUNDLE)).collect(toCollection(LinkedList::new)), destinationFolder.toString());

        assertNotNull(processed);
        assertEquals(2, processed.size());

        File bundle1 = processed.get(0);
        assertEquals(destinationFolder.toString() + File.separator + TEST_1_BUNDLE, bundle1.toString());

        File bundle2 = processed.get(1);
        assertEquals(destinationFolder.toString() + File.separator + TEST_2_BUNDLE, bundle2.toString());
        Document bundle2Doc = DocumentTools.INSTANCE.parse(bundle2);
        NodeList resources = bundle2Doc.getDocumentElement().getElementsByTagName(RESOURCE);
        assertNotNull(resources);
        assertEquals(3, resources.getLength());
        Element resourceElement = StreamSupport.stream(nodeList(resources).spliterator(), false).map(n -> (Element) n).filter(e -> PolicyEntityBuilder.POLICY.equals(e.getAttribute(ATTRIBUTE_TYPE))).findFirst().orElse(null);
        assertNotNull(resourceElement);
        String policyXML = resourceElement.getTextContent();
        assertNotNull(policyXML);
        Element policyElement = DocumentTools.INSTANCE.parse(policyXML).getDocumentElement();
        assertNotNull(policyElement);
        NodeList guids = policyElement.getElementsByTagName(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertNotNull(guids);
        assertEquals(1, guids.getLength());
        String guid = guids.item(0).getAttributes().getNamedItem(STRING_VALUE).getTextContent();

        assertEquals("283e93c6-9cf6-46f1-a34a-cf333bf4f1c3", guid);
    }

    @Test
    void testAttachingEncassesInServicePolicy() throws IOException, DocumentParseException {
        File originFolder = new File(rootProjectDir.getRoot(), "original");
        originFolder.mkdirs();
        File destinationFolder = new File(rootProjectDir.getRoot(), "processed");
        destinationFolder.mkdirs();

        // copy the bundles
        byte[] bundle1Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_1_BUNDLE));
        byte[] bundle4Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_4_BUNDLE));
        File file1 = new File(originFolder, TEST_1_BUNDLE);
        file1.createNewFile();
        File file4 = new File(originFolder, TEST_4_BUNDLE);
        file4.createNewFile();
        Files.write(file1.toPath(), bundle1Contents);
        Files.write(file4.toPath(), bundle4Contents);

        // and process them
        LinkedList<File> processed = processor.process(Stream.of(new File(originFolder, TEST_1_BUNDLE), new File(originFolder, TEST_4_BUNDLE)).collect(toCollection(LinkedList::new)), destinationFolder.toString());

        assertNotNull(processed);
        assertEquals(2, processed.size());

        File bundle1 = processed.get(0);
        assertEquals(destinationFolder.toString() + File.separator + TEST_1_BUNDLE, bundle1.toString());

        File bundle4 = processed.get(1);
        assertEquals(destinationFolder.toString() + File.separator + TEST_4_BUNDLE, bundle4.toString());
        Document bundle4Doc = DocumentTools.INSTANCE.parse(bundle4);
        NodeList resources = bundle4Doc.getDocumentElement().getElementsByTagName(RESOURCE);
        assertNotNull(resources);
        assertEquals(3, resources.getLength());
        Element resourceElement = StreamSupport.stream(nodeList(resources).spliterator(), false).map(n -> (Element) n).filter(e -> PolicyEntityBuilder.POLICY.equals(e.getAttribute(ATTRIBUTE_TYPE))).findFirst().orElse(null);
        assertNotNull(resourceElement);
        String policyXML = resourceElement.getTextContent();
        assertNotNull(policyXML);
        Element policyElement = DocumentTools.INSTANCE.parse(policyXML).getDocumentElement();
        assertNotNull(policyElement);
        NodeList guids = policyElement.getElementsByTagName(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertNotNull(guids);
        assertEquals(1, guids.getLength());
        String guid = guids.item(0).getAttributes().getNamedItem(STRING_VALUE).getTextContent();

        assertEquals("283e93c6-9cf6-46f1-a34a-cf333bf4f1c3", guid);
    }

    @Test
    void testAttachingEncasses_notFound() throws IOException, DocumentParseException {
        File originFolder = new File(rootProjectDir.getRoot(), "original");
        originFolder.mkdirs();
        File destinationFolder = new File(rootProjectDir.getRoot(), "processed");
        destinationFolder.mkdirs();

        // copy the bundles
        byte[] bundle2Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_2_BUNDLE));
        byte[] bundle3Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_3_BUNDLE));
        File file2 = new File(originFolder, TEST_2_BUNDLE);
        file2.createNewFile();
        File file3 = new File(originFolder, TEST_3_BUNDLE);
        file3.createNewFile();
        Files.write(file2.toPath(), bundle2Contents);
        Files.write(file3.toPath(), bundle3Contents);

        // and process them
        LinkedList<File> processed = processor.process(Stream.of(new File(originFolder, TEST_2_BUNDLE), new File(originFolder, TEST_3_BUNDLE)).collect(toCollection(LinkedList::new)), destinationFolder.toString());

        assertNotNull(processed);
        assertEquals(2, processed.size());

        File bundle3 = processed.get(1);
        assertEquals(destinationFolder.toString() + File.separator + TEST_3_BUNDLE, bundle3.toString());

        File bundle2 = processed.get(0);
        assertEquals(destinationFolder.toString() + File.separator + TEST_2_BUNDLE, bundle2.toString());
        Document bundle2Doc = DocumentTools.INSTANCE.parse(bundle2);
        NodeList resources = bundle2Doc.getDocumentElement().getElementsByTagName(RESOURCE);
        assertNotNull(resources);
        assertEquals(3, resources.getLength());
        Element resourceElement = StreamSupport.stream(nodeList(resources).spliterator(), false).map(n -> (Element) n).filter(e -> PolicyEntityBuilder.POLICY.equals(e.getAttribute(ATTRIBUTE_TYPE))).findFirst().orElse(null);
        assertNotNull(resourceElement);
        String policyXML = resourceElement.getTextContent();
        assertNotNull(policyXML);
        Element policyElement = DocumentTools.INSTANCE.parse(policyXML).getDocumentElement();
        assertNotNull(policyElement);
        NodeList guids = policyElement.getElementsByTagName(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertNotNull(guids);
        assertEquals(1, guids.getLength());
        String guid = guids.item(0).getAttributes().getNamedItem(STRING_VALUE).getTextContent();

        assertEquals(ZERO_GUID, guid);
    }

    @Test
    void testErrorOnDuplicatePolicies() throws IOException {
        File originFolder = new File(rootProjectDir.getRoot(), "original");
        originFolder.mkdirs();
        File destinationFolder = new File(rootProjectDir.getRoot(), "processed");
        destinationFolder.mkdirs();

        // copy the bundles
        byte[] bundle2Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_2_BUNDLE));
        byte[] bundle3Contents = IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResource(TEST_5_BUNDLE));
        File file2 = new File(originFolder, TEST_2_BUNDLE);
        file2.createNewFile();
        File file3 = new File(originFolder, TEST_5_BUNDLE);
        file3.createNewFile();
        Files.write(file2.toPath(), bundle2Contents);
        Files.write(file3.toPath(), bundle3Contents);

        // and try process them with error
        assertThrows(BundleLoadException.class, () -> processor.process(Stream.of(new File(originFolder, TEST_2_BUNDLE), new File(originFolder, TEST_3_BUNDLE)).collect(toCollection(LinkedList::new)), destinationFolder.toString()));
    }
}