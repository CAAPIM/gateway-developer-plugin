package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Wsdl;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
public class WsdlLoaderTest {

    @Mock
    private FileUtils fileUtils;

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoad(TemporaryFolder temporaryFolder) throws IOException {
        WsdlLoader wsdlLoader = new WsdlLoader(fileUtils);

        File wsdlFolder = temporaryFolder.createDirectory("wsdl");
        File a = new File(wsdlFolder, "a");
        Assert.assertTrue(a.mkdir());
        File b = new File(a, "b");
        Assert.assertTrue(b.mkdir());
        File c = new File(b, "c");
        Assert.assertTrue(c.mkdir());
        File wsdl = new File(c, "wsdl.wsdl");
        Files.touch(wsdl);

        Bundle bundle = new Bundle();
        wsdlLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertNotNull(bundle.getFolders().get(""));
        Assert.assertNotNull(bundle.getFolders().get("a/"));
        Assert.assertNotNull(bundle.getFolders().get("a/b/"));
        Folder parentFolder = bundle.getFolders().get("a/b/c/");
        Assert.assertNotNull(parentFolder);

        Wsdl loadedWsdl = bundle.getWsdls().get("a/b/c/wsdl");
        Assert.assertNotNull(loadedWsdl);
        Assert.assertEquals(parentFolder, loadedWsdl.getParentFolder());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadSingle(TemporaryFolder temporaryFolder) {
        WsdlLoader wsdlLoader = new WsdlLoader(fileUtils);
        assertThrows(ConfigLoadException.class, () -> wsdlLoader.loadSingle("wsdl", temporaryFolder.getRoot()));

        Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> wsdlLoader.load(bundle, "wsdl", "wsdl"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadNoWsdlFolder(TemporaryFolder temporaryFolder) {
        WsdlLoader wsdlLoader = new WsdlLoader(fileUtils);

        Bundle bundle = new Bundle();
        wsdlLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoad2(TemporaryFolder temporaryFolder) throws IOException {
        WsdlLoader wsdlLoader = new WsdlLoader(fileUtils);

        File wsdlFolder = temporaryFolder.createDirectory("wsdl");
        File a = new File(wsdlFolder, "a");
        Assert.assertTrue(a.mkdir());
        File wsdl = new File(a, "wsdl.wsdl");
        File wsdl2 = new File(a, "wsdl.wsdl");
        Files.touch(wsdl);
        Files.touch(wsdl2);

        Bundle bundle = new Bundle();
        wsdlLoader.load(bundle, temporaryFolder.getRoot());

        Wsdl loadedWsdl = bundle.getWsdls().get("a/wsdl");
    }
}
