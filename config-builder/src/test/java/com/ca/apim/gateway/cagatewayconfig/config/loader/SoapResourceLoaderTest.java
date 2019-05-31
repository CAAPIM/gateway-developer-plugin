package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.SoapResource;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.SOAP_RESOURCES_FOLDER;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class SoapResourceLoaderTest {

    @Mock
    private FileUtils fileUtils;

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoad(TemporaryFolder temporaryFolder) throws IOException {
        SoapResourceLoader soapResourceLoader = new SoapResourceLoader(fileUtils);

        File soapResourceFolder = temporaryFolder.createDirectory(SOAP_RESOURCES_FOLDER);
        File a = new File(soapResourceFolder, "a");
        Assert.assertTrue(a.mkdir());
        File b = new File(a, "b");
        Assert.assertTrue(b.mkdir());
        File c = new File(b, "c");
        Assert.assertTrue(c.mkdir());
        File soapResource = new File(c, "wsdl.wsdl");
        Files.touch(soapResource);
        soapResource = new File(c, "schema.xsd");
        Files.touch(soapResource);
        soapResource = new File(c, "invalid.txt");
        Files.touch(soapResource);

        Bundle bundle = new Bundle();
        soapResourceLoader.load(bundle, temporaryFolder.getRoot());

        SoapResource loadedSoapResource = bundle.getSoapResources().get("a/b/c/wsdl.wsdl");
        Assert.assertNotNull(loadedSoapResource);

        loadedSoapResource = bundle.getSoapResources().get("a/b/c/schema.xsd");
        Assert.assertNotNull(loadedSoapResource);

        loadedSoapResource = bundle.getSoapResources().get("a/b/c/invalid.txt");
        Assert.assertNull(loadedSoapResource);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadSingle(TemporaryFolder temporaryFolder) {
        SoapResourceLoader soapResourceLoader = new SoapResourceLoader(fileUtils);
        assertThrows(ConfigLoadException.class, () -> soapResourceLoader.loadSingle("soapResource", temporaryFolder.getRoot()));

        Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> soapResourceLoader.load(bundle, "soapResource", "soapResource"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadNoSoapResourceFolder(TemporaryFolder temporaryFolder) {
        SoapResourceLoader soapResourceLoader = new SoapResourceLoader(fileUtils);

        Bundle bundle = new Bundle();
        soapResourceLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadEmptyResourceFolder(TemporaryFolder temporaryFolder) {
        SoapResourceLoader soapResourceLoader = new SoapResourceLoader(fileUtils);
        temporaryFolder.createDirectory(SOAP_RESOURCES_FOLDER);

        Bundle bundle = new Bundle();
        soapResourceLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }
}
