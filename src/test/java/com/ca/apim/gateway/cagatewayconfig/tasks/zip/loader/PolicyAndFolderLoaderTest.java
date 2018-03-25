package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class PolicyAndFolderLoaderTest {

    @Mock
    private FileUtils fileUtils;

    @Rule
    public final TemporaryFolder rootProjectDir = new TemporaryFolder();

    @Test
    public void getPolicyAsStringTest() {

        File root = new File("a");
        File a = new File(root, "a");
        File b = new File(a, "b");
        File c = new File(b, "c");
        File policy = new File(c, "policy.xml");

        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE);

        String path = policyAndFolderLoader.getPath(policy, root);

        Assert.assertEquals("a/b/c/policy.xml", path);
    }

    @Test
    public void getPolicyNameTest() {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE);

        File policy = new File("policy.xml");
        Assert.assertEquals("policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("policy.xml");
        Assert.assertEquals("policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("my.policy.xml");
        Assert.assertEquals("my.policy", policyAndFolderLoader.getPolicyName(policy));

        policy = new File("something");
        Assert.assertEquals("something", policyAndFolderLoader.getPolicyName(policy));

    }

    @Test
    public void testLoad() throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(fileUtils);
        Mockito.when(fileUtils.getFileAsString(Mockito.any(File.class))).thenReturn("policy-content");

        File policyFolder = rootProjectDir.newFolder("policy");
        File a = new File(policyFolder, "a");
        Assert.assertTrue(a.mkdir());
        File b = new File(a, "b");
        Assert.assertTrue(b.mkdir());
        File c = new File(b, "c");
        Assert.assertTrue(c.mkdir());
        File policy = new File(c, "policy.xml");
        Files.touch(policy);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, rootProjectDir.getRoot());

        Assert.assertNotNull(bundle.getFolders().get(""));
        Assert.assertNotNull(bundle.getFolders().get("a/"));
        Assert.assertNotNull(bundle.getFolders().get("a/b/"));
        Assert.assertNotNull(bundle.getFolders().get("a/b/c/"));

        Assert.assertNotNull(bundle.getPolicies().get("a/b/c/policy.xml"));
    }


    @Test
    public void testLoadNoPolicyFolder() {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, rootProjectDir.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }

    @Test(expected = BundleLoadException.class)
    public void testLoadPolicyFolderIsFile() throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(FileUtils.INSTANCE);

        rootProjectDir.newFile("policy");

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, rootProjectDir.getRoot());
    }
}