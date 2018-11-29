/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.readLines;
import static org.gradle.util.WrapUtil.toSet;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(TemporaryFolderExtension.class)
class WriterHelperTest {

    private static final String TEST_PROPERTIES = "testProperties";
    private static final String PROPERTIES = "properties";
    private static final String YML_FILE = "yml";
    private static final String JSON_FILE = "json";
    private static final String TEST_FILE = "test-file";

    private DocumentFileUtils documentFileUtils = DocumentFileUtils.INSTANCE;
    private JsonTools jsonTools = JsonTools.INSTANCE;
    private File testProjectDir;

    @BeforeEach
    void before(TemporaryFolder temporaryFolder) {
        testProjectDir = new File(temporaryFolder.getRoot(), "exportDir_" + System.currentTimeMillis());
        testProjectDir.mkdir();
    }

    // tests for properties merging behaviour

    @Test
    void writePropertiesFile_noMerging() throws IOException {
        final HashMap<String, String> expectedProperties = new HashMap<>(
                ImmutableMap.<String, String>builder()
                        .put("key1", "value1")
                        .put("key2", "value2")
                        .build()
        );

        final Properties properties = new Properties();
        properties.putAll(ImmutableMap.of("key1", "value1", "key2", "value2"));

        WriterHelper.writePropertiesFile(testProjectDir, documentFileUtils, properties, TEST_PROPERTIES);

        assertPropertiesContents(expectedProperties);
    }

    @Test
    void writePropertiesFile_merging() throws IOException, URISyntaxException {
        // set up a temp file with the current contents
        setupCurrentFile(TEST_PROPERTIES, PROPERTIES);

        final Properties properties = new Properties();
        properties.putAll(ImmutableMap.of("key1", "value1", "key2", "value2"));

        WriterHelper.writePropertiesFile(testProjectDir, documentFileUtils, properties, TEST_PROPERTIES);

        assertPropertiesContents(
                new HashMap<>(
                        ImmutableMap.<String, String>builder()
                                .put("key1", "value1")
                                .put("key2", "value2")
                                .put("key3", "value3")
                                .put("key4", "value4")
                                .build()
                )
        );
    }

    @Test
    void writePropertiesFile_merging_skippingEmptyValues() throws IOException, URISyntaxException {
        // set up a temp file with the current contents
        setupCurrentFile(TEST_PROPERTIES, PROPERTIES);

        final Properties properties = new Properties();
        properties.put("key1", "");
        properties.put("key4", "");

        WriterHelper.writePropertiesFile(testProjectDir, documentFileUtils, properties, TEST_PROPERTIES);

        assertPropertiesContents(
                new HashMap<>(
                        ImmutableMap.<String, String>builder()
                                .put("key1", "value11")
                                .put("key3", "value3")
                                .put("key4", "value4")
                                .build()
                )
        );
    }

    private void assertPropertiesContents(final Map<String, String> expectedProperties) throws IOException {
        // check file existing and permissions
        final File propertiesFile = checkFileBasics(WriterHelperTest.TEST_PROPERTIES, PROPERTIES);

        // load existing file
        final FileInputStream stream = FileUtils.openInputStream(propertiesFile);
        final Properties writtenProperties = new Properties();
        writtenProperties.load(stream);
        stream.close();

        // assert contents and match with the expected ones
        // contents are removed from the map and properties to ensure both have exact the same content
        assertFalse(writtenProperties.isEmpty());
        assertEquals(expectedProperties.size(), writtenProperties.size());
        ImmutableMap.copyOf(expectedProperties).forEach((k, v) -> {
            Object actualValue = writtenProperties.remove(k);
            assertNotNull(actualValue);
            assertEquals(v, actualValue);
            expectedProperties.remove(k);
        });

        // check if expected and actual are empty after entry checking
        assertTrue(expectedProperties.isEmpty());
        assertTrue(writtenProperties.isEmpty());
    }

    @NotNull
    private File checkFileBasics(String fileName, String extension) {
        final File propertiesFile = new File(new File(testProjectDir, "config"), fileName + "." + extension);
        assertTrue(propertiesFile.exists());
        assertFalse(propertiesFile.isDirectory());
        assertTrue(propertiesFile.canRead());
        return propertiesFile;
    }

    // tests for yaml/json merging behaviour

    @Test
    void writeFile_yaml_noMerging() throws IOException {
        writeFileTest_noMerging(JsonTools.YAML, YML_FILE);
    }

    @Test
    void writeFile_json_noMerging() throws IOException {
        writeFileTest_noMerging(JsonTools.JSON, JSON_FILE);
    }

    private void writeFileTest_noMerging(String type, String extension) throws IOException {
        final TestBean bean = new TestBean()
                .setAttributeString("Value1")
                .setAttributeNumber(1)
                .setAttributeList(asList("GG", "WW", "77"))
                .setAttributeMap(ImmutableMap.of("PP", "QQ", "RR", "SS", "TT", "UU"));
        bean.setName("Test1");
        final Map<String, TestBean> beans = ImmutableMap.of("Test1", bean);

        this.jsonTools.setOutputType(type);
        WriterHelper.writeFile(testProjectDir, documentFileUtils, jsonTools, beans, TEST_FILE, TestBean.class);

        // check file existing and permissions
        final File propertiesFile = checkFileBasics(TEST_FILE, extension);
        // read and check contents
        final Map<String, TestBean> contents = jsonTools.getObjectMapper().readValue(propertiesFile, TestBean.MAPPING);

        assertNotNull(contents);
        assertFalse(contents.isEmpty());
        assertEquals(1, contents.size());

        TestBean currentBean = contents.get("Test1");
        assertNotNull(currentBean);
        assertNotNull(currentBean.attributeString);
        assertEquals(bean.attributeString, currentBean.attributeString);
        assertNotNull(currentBean.attributeNumber);
        assertEquals(bean.attributeNumber, currentBean.attributeNumber);
        assertNotNull(currentBean.attributeList);
        assertEquals(bean.attributeList.size(), currentBean.attributeList.size());
        assertTrue(currentBean.attributeList.containsAll(bean.attributeList));
        assertMapContents(bean.attributeMap, currentBean.attributeMap);
    }

    @Test
    void writeFile_yaml_merging() throws IOException, URISyntaxException {
        // set up a temp file with the current contents
        setupCurrentFile(TEST_FILE, YML_FILE);

        writeFileTest_merging(JsonTools.YAML, YML_FILE);
    }

    @Test
    void writeFile_json_merging() throws IOException, URISyntaxException {
        // set up a temp file with the current contents
        setupCurrentFile(TEST_FILE, JSON_FILE);

        writeFileTest_merging(JsonTools.JSON, JSON_FILE);
    }

    private void writeFileTest_merging(String type, String extension) throws IOException {
        final TestBean bean = new TestBean()
                .setAttributeString("Value1")
                .setAttributeNumber(1)
                .setAttributeList(asList("GG", "WW", "77"))
                .setAttributeSet(toSet("GG", "WW", "77"))
                .setAttributeMap(ImmutableMap.of("PP", "QQ", "RR", "SS", "TT", "UU"));
        bean.setName("Test1");
        final TestBean beanToMerge = new TestBean()
                .setAttributeString("ValueMerged")
                .setAttributeNumber(654321)
                .setAttributeList(asList("GG", "WW", "77"))
                .setAttributeSet(toSet("GG", "WW", "77"))
                .setAttributeMap(ImmutableMap.of("XX", "QQ", "RR", "SS", "ZZ", "UU"));
        beanToMerge.setName("Test");
        final Map<String, TestBean> beans = new HashMap<>(ImmutableMap.of("Test1", bean, "Test", beanToMerge));

        this.jsonTools.setOutputType(type);
        WriterHelper.writeFile(testProjectDir, documentFileUtils, jsonTools, beans, TEST_FILE, TestBean.class);

        // check file existing and permissions
        final File propertiesFile = checkFileBasics(TEST_FILE, extension);
        // read and check contents
        final Map<String, TestBean> contents = jsonTools.getObjectMapper().readValue(propertiesFile, TestBean.MAPPING);

        assertNotNull(contents);
        assertFalse(contents.isEmpty());
        assertEquals(beans.size(), contents.size());

        // nonmerged bean
        TestBean currentBean = contents.get("Test1");
        assertNotNull(currentBean);
        assertNotNull(currentBean.attributeString);
        assertEquals(bean.attributeString, currentBean.attributeString);
        assertNotNull(currentBean.attributeNumber);
        assertEquals(bean.attributeNumber, currentBean.attributeNumber);
        assertNotNull(currentBean.attributeList);
        assertEquals(bean.attributeList.size(), currentBean.attributeList.size());
        assertTrue(currentBean.attributeList.containsAll(bean.attributeList));
        assertNotNull(currentBean.attributeSet);
        assertEquals(beanToMerge.attributeSet.size(), currentBean.attributeSet.size());
        assertTrue(currentBean.attributeSet.containsAll(beanToMerge.attributeSet));
        assertMapContents(bean.attributeMap, currentBean.attributeMap);

        // merged bean
        currentBean = contents.get("Test");
        assertNotNull(currentBean);
        assertNotNull(currentBean.attributeString);
        assertEquals(beanToMerge.attributeString, currentBean.attributeString);
        assertNotNull(currentBean.attributeNumber);
        assertEquals(beanToMerge.attributeNumber, currentBean.attributeNumber);
        assertNotNull(currentBean.attributeList);
        assertEquals(beanToMerge.attributeList.size(), currentBean.attributeList.size());
        assertTrue(currentBean.attributeList.containsAll(beanToMerge.attributeList));
        assertNotNull(currentBean.attributeSet);
        assertEquals(beanToMerge.attributeSet.size(), currentBean.attributeSet.size());
        assertTrue(currentBean.attributeSet.containsAll(beanToMerge.attributeSet));
        assertMapContents(beanToMerge.attributeMap, currentBean.attributeMap);
    }

    private static void assertMapContents(Map<String, ?> expected, Map<String, ?> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            assertNotNull(actual.get(key));
            assertEquals(value, actual.get(key));
            actual.remove(key);
        });

        assertTrue(actual.isEmpty());
    }

    private void setupCurrentFile(String name, String extension) throws IOException, URISyntaxException {
        final InputStream currentFile = newInputStream(Paths.get(this.getClass().getClassLoader().getResource("config/" + name + "." + extension).toURI()));
        final File destCurrentFile = new File(new File(testProjectDir, "config"), name + "." + extension);
        FileUtils.copyInputStreamToFile(currentFile, destCurrentFile);
        currentFile.close();
    }

    @Test
    void writeProperties_preserve_order() throws IOException, URISyntaxException {
        // set up a temp file with the current contents
        setupCurrentFile(TEST_PROPERTIES, PROPERTIES);

        final Properties properties = new Properties();
        properties.putAll(ImmutableMap.of("key1", "value1", "key2", "value2"));

        WriterHelper.writePropertiesFile(testProjectDir, documentFileUtils, properties, TEST_PROPERTIES);
        // check file existing and permissions
        final File propertiesFile = checkFileBasics(WriterHelperTest.TEST_PROPERTIES, PROPERTIES);

        // load existing file
        final List<String> lines = readLines(propertiesFile, StandardCharsets.ISO_8859_1);

        // ensure ordering
        assertEquals(4, lines.size());
        assertTrue(lines.get(0).startsWith("key1"));
        assertTrue(lines.get(1).startsWith("key3"));
        assertTrue(lines.get(2).startsWith("key4"));
        assertTrue(lines.get(3).startsWith("key2"));
    }

    @Test
    void writeFile_preserve_order() throws IOException {
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String testFile = TEST_FILE + i;
            //generates 20 beans between 0 and 20
            Map<String, TestBean> originalBeans = new LinkedHashMap<>();
            for (int j = 0; j < 20; j++) {
                int num = random.nextInt(20) + 10;
                originalBeans.put("bean_" + num, new TestBean("bean_" + num));
            }

            this.jsonTools.setOutputType(YML_FILE);
            WriterHelper.writeFile(testProjectDir, documentFileUtils, jsonTools, originalBeans, testFile, TestBean.class);

            // check file existing and permissions
            final File propertiesFile = checkFileBasics(testFile, YML_FILE);
            // read and check contents
            final LinkedHashMap<String, TestBean> originalReloaded = jsonTools.getObjectMapper().readValue(propertiesFile, TestBean.MAPPING);

            // validate the order they were written in
            Iterator<Map.Entry<String, TestBean>> reloadedIterator = originalReloaded.entrySet().iterator();
            originalBeans.forEach((b, v) -> assertEquals(reloadedIterator.next().getKey(), b));


            //generates 20 beans between 0 and 40
            Map<String, TestBean> newBeans = new LinkedHashMap<>();
            for (int j = 0; j < 20; j++) {
                int num = random.nextInt(40);
                newBeans.put("bean_" + num, new TestBean("bean_" + num));
            }

            //write new beans
            WriterHelper.writeFile(testProjectDir, documentFileUtils, jsonTools, newBeans, testFile, TestBean.class);
            // read and check contents
            final LinkedHashMap<String, TestBean> mergedBeans = jsonTools.getObjectMapper().readValue(propertiesFile, TestBean.MAPPING);
            //validate that original order is preserved and that new beans are added to the end
            Iterator<Map.Entry<String, TestBean>> mergedBeanIterator = mergedBeans.entrySet().iterator();
            originalBeans.forEach((b, v) -> assertEquals(mergedBeanIterator.next().getKey(), b));
            newBeans.entrySet().stream().filter(e -> !originalBeans.containsKey(e.getKey())).forEach(e -> assertEquals(mergedBeanIterator.next().getKey(), e.getKey()));
        }
    }

    @JsonInclude(NON_NULL)
    public static class TestBean extends GatewayEntity {

        private static final TypeReference<LinkedHashMap<String, TestBean>> MAPPING = new TypeReference<LinkedHashMap<String, TestBean>>() {
        };

        private String attributeString;
        private Integer attributeNumber;
        private List<String> attributeList;
        private Set<String> attributeSet;
        private Map<String, String> attributeMap;

        TestBean() {

        }

        TestBean(String attributeString) {
            setName(attributeString);
            this.attributeString = attributeString;
        }

        public String getAttributeString() {
            return attributeString;
        }

        TestBean setAttributeString(String attributeString) {
            this.attributeString = attributeString;
            return this;
        }

        public Integer getAttributeNumber() {
            return attributeNumber;
        }

        TestBean setAttributeNumber(Integer attributeNumber) {
            this.attributeNumber = attributeNumber;
            return this;
        }

        public List<String> getAttributeList() {
            return attributeList;
        }

        TestBean setAttributeList(List<String> attributeList) {
            this.attributeList = attributeList;
            return this;
        }

        public Set<String> getAttributeSet() {
            return attributeSet;
        }

        TestBean setAttributeSet(Set<String> attributeSet) {
            this.attributeSet = attributeSet;
            return this;
        }

        public Map<String, String> getAttributeMap() {
            return attributeMap;
        }

        TestBean setAttributeMap(Map<String, String> attributeMap) {
            this.attributeMap = attributeMap;
            return this;
        }
    }
}