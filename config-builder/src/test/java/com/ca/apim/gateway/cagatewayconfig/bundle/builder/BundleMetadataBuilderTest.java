/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.BundleFileBuilder;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilderTestHelper.*;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.DELETE_BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils.METADATA_FILE_NAME_SUFFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({TemporaryFolderExtension.class, MockitoExtension.class})
public class BundleMetadataBuilderTest {

    @Mock
    EntityLoaderRegistry entityLoaderRegistry;
    @Mock
    BundleCache bundleCache;

    @Test
    public void testAnnotatedEncassBundleFileNames(final TemporaryFolder temporaryFolder) {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(BASIC_ENCASS_POLICY, false);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestBundleLoader(bundle)));

        List<File> dummyList = new ArrayList<>();
        dummyList.add(new File("test"));
        when(bundleCache.getBundleFromFile(any(File.class))).thenReturn(new Bundle());

        BundleFileBuilder bundleFileBuilder = new BundleFileBuilder(DocumentTools.INSTANCE, DocumentFileUtils.INSTANCE,
                JsonFileUtils.INSTANCE, entityLoaderRegistry, builder, bundleCache);

        File bundleOutput = temporaryFolder.createDirectory("output");
        try {
            bundleFileBuilder.buildBundle(temporaryFolder.getRoot(), bundleOutput, dummyList,
                    "my-bundle", "my-bundle-group", "1.0");

            assertTrue(bundleOutput.exists());
            assertEquals(3, bundleOutput.listFiles().length);
            for (File generatedFile : bundleOutput.listFiles()) {
                if (StringUtils.endsWith(generatedFile.getName(), DELETE_BUNDLE_EXTENSION)) {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0" + DELETE_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else if (StringUtils.endsWith(generatedFile.getName(), BUNDLE_EXTENSION)) {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0" + BUNDLE_EXTENSION, generatedFile.getName());
                } else {
                    assertEquals(TEST_ENCASS_ANNOTATION_NAME + "-1.0" + METADATA_FILE_NAME_SUFFIX,
                            generatedFile.getName());
                }
            }
        } finally {
            deleteDirectory(bundleOutput);
        }

        bundle.getEncasses().clear();
        // Remove "name" attribute from the @bundle annotation.
        encass.getAnnotations().parallelStream()
                .filter(ann -> AnnotationConstants.ANNOTATION_TYPE_BUNDLE.equals(ann.getType()))
                .findFirst().get().setName(null);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));
        encass.setAnnotatedEntity(null);
        when(entityLoaderRegistry.getEntityLoaders()).thenReturn(Collections.singleton(new TestBundleLoader(bundle)));

        bundleOutput = temporaryFolder.createDirectory("output");
        try {
            bundleFileBuilder.buildBundle(temporaryFolder.getRoot(), bundleOutput, dummyList, "my-bundle",
                    "my-bundle-group", "1.0");

            bundleOutput = new File(temporaryFolder.getRoot(), "output");
            assertTrue(bundleOutput.exists());
            assertEquals(3, bundleOutput.listFiles().length);
            for (File generatedFile : bundleOutput.listFiles()) {
                if (StringUtils.endsWith(generatedFile.getName(), ".delete.bundle")) {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0" + DELETE_BUNDLE_EXTENSION,
                            generatedFile.getName());
                } else if (StringUtils.endsWith(generatedFile.getName(), ".bundle")) {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0" + BUNDLE_EXTENSION, generatedFile.getName());
                } else {
                    assertEquals("my-bundle-" + encass.getName() + "-1.0" + METADATA_FILE_NAME_SUFFIX,
                            generatedFile.getName());
                }
            }
        } finally {
            deleteDirectory(bundleOutput);
        }
    }

    @Test
    public void testAnnotatedEncassMetadata() throws JsonProcessingException {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(ENCASS_POLICY_WITH_ENV_DEPENDENCIES, true);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY);
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "my-bundle-group", "1.0");
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get(TEST_ENCASS_ANNOTATION_NAME + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertEquals(TEST_ENCASS_ANNOTATION_NAME, metadata.getName());
        assertEquals(TEST_ENCASS_ANNOTATION_DESC, metadata.getDescription());
        assertEquals(TEST_ENCASS_ANNOTATION_TAGS, metadata.getTags());

        verifyAnnotatedEncassBundleMetadata(bundles, bundle, encass);
    }

    /**
     * Test annotated encass metadata which contain only type of annotation is annotation details. For example,
     * only "@bundle" without name, description and tags
     */
    @Test
    public void testAnnotatedEncassMetadata_ExcludingOptionalAnnotationFields() throws JsonProcessingException {
        BundleEntityBuilder builder = createBundleEntityBuilder();

        Bundle bundle = createBundle(ENCASS_POLICY_WITH_ENV_DEPENDENCIES, true);
        Encass encass = buildTestEncassWithAnnotation(TEST_GUID, TEST_ENCASS_POLICY);
        encass.getAnnotations().forEach(a -> {
            a.setName(null);
            a.setDescription(null);
            a.setTags(Collections.emptySet());
        });
        bundle.putAllEncasses(ImmutableMap.of(TEST_ENCASS, encass));

        Map<String, BundleArtifacts> bundles = builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), "my-bundle", "my-bundle-group", "1.0");
        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.get("my-bundle-" + encass.getName() + "-1.0").getBundleMetadata();
        assertNotNull(metadata);
        assertEquals("my-bundle-" + encass.getName(), metadata.getName());
        assertEquals(encass.getProperties().get("description"), metadata.getDescription());
        assertEquals(0, metadata.getTags().size());

        verifyAnnotatedEncassBundleMetadata(bundles, bundle, encass);
    }

    private void verifyAnnotatedEncassBundleMetadata(Map<String, Pair<Element, BundleMetadata>> bundles,
                                                     Bundle bundle, Encass encass) throws JsonProcessingException {
        Map<String, Metadata> expectedEnvMetadata = new HashMap<>();
        for (Dependency dependency : bundle.getDependencyMap().entrySet().iterator().next().getValue()) {
            expectedEnvMetadata.put(dependency.getType(), new Metadata() {
                @Override
                public String getType() {
                    return dependency.getType();
                }

                @Override
                public String getName() {
                    return dependency.getName();
                }
            });
        }

        assertNotNull(bundles);
        assertEquals(1, bundles.size());
        BundleMetadata metadata = bundles.entrySet().iterator().next().getValue().getRight();
        assertNotNull(metadata);
        assertEquals("my-bundle-group", metadata.getGroupName());
        assertEquals("encass", metadata.getType());
        assertEquals("1.0", metadata.getVersion());
        assertEquals(1, metadata.getDefinedEntities().size());
        Optional<Metadata> definedEntities = metadata.getDefinedEntities().stream().findFirst();
        assertTrue(definedEntities.isPresent());
        assertEquals("ENCAPSULATED_ASSERTION", definedEntities.get().getType());
        assertEquals(encass.getName(), definedEntities.get().getName());
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(definedEntities.get());
        Assert.assertThat(json, CoreMatchers.containsString("\"arguments\":[{\"type\":\"message\",\"name\":\"source\",\"requireExplicit\":true,\"label\":\"Some label\"}]"));
        Assert.assertThat(json, CoreMatchers.containsString("\"results\":[{\"name\":\"result.msg\",\"type\":\"message\"}]"));
        assertEquals(4, metadata.getEnvironmentEntities().size());

        for (Metadata envMeta : metadata.getEnvironmentEntities()) {
            assertTrue(expectedEnvMetadata.containsKey(envMeta.getType()));
            assertEquals(expectedEnvMetadata.get(envMeta.getType()).getName(), envMeta.getName());
        }
    }

    private Bundle createBundle(String policyXmlString, boolean includeDependencies) {
        Bundle bundle = new Bundle();
        Folder root = createRoot();
        bundle.getFolders().put(EMPTY, root);

        Folder dummyFolder = createFolder("dummy", TEST_GUID, ROOT_FOLDER);
        dummyFolder.setParentFolder(Folder.ROOT_FOLDER);
        bundle.getFolders().put(dummyFolder.getPath(), dummyFolder);

        Policy policy = new Policy();
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setName(TEST_ENCASS_POLICY);
        policy.setId(TEST_POLICY_ID);
        policy.setGuid(TEST_GUID);
        policy.setPolicyXML(policyXmlString);
        policy.setPath(TEST_ENCASS_POLICY);
        bundle.getPolicies().put(TEST_ENCASS_POLICY, policy);
        Dependency policyDependency = new Dependency(TEST_POLICY_ID, Policy.class, TEST_ENCASS_POLICY,
                EntityTypes.POLICY_TYPE);
        Dependency encassDependency = new Dependency(TEST_ENCASS_ID, Encass.class, TEST_ENCASS,
                EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        bundle.setDependencyMap(new HashMap<>());
        bundle.getDependencyMap().put(policyDependency, new ArrayList<>(Collections.singletonList(encassDependency)));

        if (includeDependencies) {
            JdbcConnection jdbcConnection = new JdbcConnection();
            jdbcConnection.setDriverClass("com.l7tech.jdbc.mysql.MySQLDriver");
            jdbcConnection.setJdbcUrl("jdbc:mysql://localhost:3306/ssg");
            jdbcConnection.setUser("root");
            jdbcConnection.setName("some-jdbc");
            bundle.getJdbcConnections().put("some-jdbc", jdbcConnection);
            Dependency jdbcDependency = new Dependency(jdbcConnection.getName(), "JDBC_CONNECTION");

            ClusterProperty clusterProperty = new ClusterProperty();
            clusterProperty.setName("email.useDefaultSsl");
            clusterProperty.setValue("true");
            bundle.getClusterProperties().put("email.useDefaultSsl", clusterProperty);
            Dependency clusterDependency = new Dependency(clusterProperty.getName(), "CLUSTER_PROPERTY");

            StoredPassword storedPassword = new StoredPassword();
            storedPassword.setName("secure-pass");
            storedPassword.setProperties(Maps.newHashMap(ImmutableMap.of("description", "sec pass", "type", "Password", "usageFromVariable", true)));
            bundle.getStoredPasswords().put("secure-pass", storedPassword);
            Dependency passwordDependency = new Dependency(storedPassword.getName(), "SECURE_PASSWORD");

            TrustedCert trustedCert = new TrustedCert(Maps.newHashMap(ImmutableMap.of(
                    "revocationCheckingEnabled", "true",
                    "trustedForSigningServerCerts", "true",
                    "trustedForSsl", "true")));
            trustedCert.setName("apim-hugh-new.lvn.broadcom.net");
            TrustedCert.CertificateData certificateData = new TrustedCert.CertificateData("CN=apim-hugh-new.lvn" +
                    ".broadcom.net", new BigInteger("12718618715409400804"), "CN=pim-hugh-new.lvn.broadcom.net", "MIIDAjCCAeqgAwIBAgIJALCBnFXlnMPkMA0GCSqGSIb3DQEBCwUAMCkxJzAlBgNVBAMTHmFwaW0taHVnaC1uZXcubHZuLmJyb2FkY29tLm5ldDAeFw0xOTAyMDUwNDQ4NTVaFw0yOTAyMDIwNDQ4NTVaMCkxJzAlBgNVBAMTHmFwaW0taHVnaC1uZXcubHZuLmJyb2FkY29tLm5ldDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALb1txkLi40e0DUXl1MNzDPplB0IKdUDD1Hsx4VgBqAa5TbZZwwQKfGx+oEsDlZamTpu8h1yjuguLNbLbOZFbZ71RBCqKGAy1g2oi6mBiJoTGOzcUzLhUS1M4uC2HQIZzWcNeMbBJWn203IwfvYLpLlenVs6UKTGqJq+TUT6DzqYMypzSj7J4/z5Eml5SUjYq6L/OOkHKjX6dvKBu25mcbahaqV0yIoaF2bO7GR1jrCsIxTv/b/jV+hMbyOpkS+kmYtDecPQs3+rfKNf/N81cidjf/u7vTfXj+IdFQpsw0V3fyLG2WWN4bVgmqAjQFO3ImdwO9RuIGmDzojZ7madJ1UCAwEAAaMtMCswKQYDVR0RBCIwIIIeYXBpbS1odWdoLW5ldy5sdm4uYnJvYWRjb20ubmV0MA0GCSqGSIb3DQEBCwUAA4IBAQAx1YWgkJXt9esh7GHvpx9DDeBLQckEI7YmgVtY8f3OJubcaTbNWEHPpmqz/pelVEh2nTeu5XOPby2SiDipMDLEprGjw92R6Uye/yvvtmoi1Rrnkzmq9jaTb8aWOCU9KdirvaWGnLJPHsovLgfLOrUtmDfUZjjAX/zrPQXI4NGDAJ52gUCK3NNYNCKedduMuLrtSxx1PVqkJpW8IC2ozh0HijezcuwgmK1gu3vzyS8POTrqBLxOk0PD/NggZEDiR3AdxpnWWygGJIEbC4wd84WVg8ENcyrBSWSPQhU9Rtql3HXcCQn7XrS9Qu+sx0bAby8JebKfgV0wRCPUk/xC5MBd");
            trustedCert.setCertificateData(certificateData);
            bundle.getTrustedCerts().put(trustedCert.getName(), trustedCert);
            Dependency trustedCertDependency = new Dependency(trustedCert.getName(), "TRUSTED_CERT");

            // Add dependencies
            bundle.getDependencyMap().get(policyDependency).add(jdbcDependency);
            bundle.getDependencyMap().get(policyDependency).add(clusterDependency);
            bundle.getDependencyMap().get(policyDependency).add(passwordDependency);
            bundle.getDependencyMap().get(policyDependency).add(trustedCertDependency);
            Set<Dependency> dependencies = new HashSet<>();
            dependencies.add(jdbcDependency);
            dependencies.add(clusterDependency);
            dependencies.add(passwordDependency);
            dependencies.add(trustedCertDependency);

            policy.setUsedEntities(dependencies);
        }
        return bundle;
    }

    private BundleEntityBuilder createBundleEntityBuilder() {
        FolderEntityBuilder folderBuilder = new FolderEntityBuilder(ID_GENERATOR);
        PolicyEntityBuilder policyBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, ID_GENERATOR);
        EncassEntityBuilder encassBuilder = new EncassEntityBuilder(ID_GENERATOR);
        StoredPasswordEntityBuilder storedPasswordEntityBuilder = new StoredPasswordEntityBuilder(ID_GENERATOR);
        JdbcConnectionEntityBuilder jdbcConnectionEntityBuilder = new JdbcConnectionEntityBuilder(ID_GENERATOR);
        ClusterPropertyEntityBuilder clusterPropertyEntityBuilder = new ClusterPropertyEntityBuilder(ID_GENERATOR);
        TrustedCertEntityBuilder trustedCertEntityBuilder = new TrustedCertEntityBuilder(ID_GENERATOR, null, certFact);

        Set<EntityBuilder> entityBuilders = new HashSet<>();
        entityBuilders.add(folderBuilder);
        entityBuilders.add(policyBuilder);
        entityBuilders.add(encassBuilder);
        entityBuilders.add(storedPasswordEntityBuilder);
        entityBuilders.add(jdbcConnectionEntityBuilder);
        entityBuilders.add(clusterPropertyEntityBuilder);
        entityBuilders.add(trustedCertEntityBuilder);

        return new BundleEntityBuilder(entityBuilders, new BundleDocumentBuilder(), new BundleMetadataBuilder(), entityTypeRegistry);
    }

    private static Encass buildTestEncassWithAnnotation(String encassGuid, String policyPath) {
        Encass encass = new Encass();
        encass.setName(TEST_ENCASS);
        encass.setPolicy(policyPath);
        encass.setId(TEST_ENCASS_ID);
        encass.setArguments(new LinkedHashSet<>(Collections.singletonList(new EncassArgument("source", "message",
                true, "Some label"))));
        encass.setResults(new LinkedHashSet<>(Collections.singletonList(new EncassResult("result.msg", "message"))));
        encass.setGuid(encassGuid);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation("@bundle");
        annotation.setName(TEST_ENCASS_ANNOTATION_NAME);
        annotation.setDescription(TEST_ENCASS_ANNOTATION_DESC);
        annotation.setTags(TEST_ENCASS_ANNOTATION_TAGS);
        annotations.add(annotation);
        encass.setAnnotations(annotations);
        encass.setProperties(ImmutableMap.of(
                PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION,
                PALETTE_ICON_RESOURCE_NAME, "someImage",
                ALLOW_TRACING, "false",
                DESCRIPTION, "someDescription",
                PASS_METRICS_TO_PARENT, "false"));
        return encass;
    }

    private void deleteDirectory(File directory) {
        Arrays.stream(directory.listFiles()).forEach(f -> f.delete());
        directory.delete();
    }

    static class TestBundleLoader implements EntityLoader {
        private final Bundle bundle;

        TestBundleLoader(Bundle bundle) {
            this.bundle = bundle;
        }

        @Override
        public void load(Bundle bundle, File rootDir) {
            bundle.setDependencyMap(this.bundle.getDependencyMap());
            bundle.setDependencies(this.bundle.getDependencies());
            bundle.setFolderTree(this.bundle.getFolderTree());
            bundle.setLoadingMode(bundle.getLoadingMode());
            bundle.putAllFolders(this.bundle.getFolders());
            bundle.putAllPolicies(this.bundle.getPolicies());
            bundle.putAllServices(this.bundle.getServices());
            bundle.putAllEncasses(this.bundle.getEncasses());
            bundle.putAllTrustedCerts(this.bundle.getTrustedCerts());
            bundle.putAllJdbcConnections(this.bundle.getJdbcConnections());
        }

        @Override
        public void load(Bundle bundle, String name, String value) {

        }

        @Override
        public Object loadSingle(String name, File entitiesFile) {
            return null;
        }

        @Override
        public String getEntityType() {
            return null;
        }
    }
}
