package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY_DEFAULT;

public class EntityBuilderHelperTest {

    @Before
    public void beforeTest() {
        System.setProperty(DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY,
                DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY_DEFAULT);
    }

    @Test
    public void testDefaultEntityMappingAction() {
        Assert.assertTrue(StringUtils.isNotBlank(EntityBuilderHelper.getDefaultEntityMappingAction()));
        Assert.assertTrue(MappingActions.NEW_OR_EXISTING.equals(EntityBuilderHelper.getDefaultEntityMappingAction()) ||
                MappingActions.NEW_OR_UPDATE.equals(EntityBuilderHelper.getDefaultEntityMappingAction()));

        System.setProperty(DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY, MappingActions.NEW_OR_EXISTING);
        Assert.assertEquals(MappingActions.NEW_OR_EXISTING, EntityBuilderHelper.getDefaultEntityMappingAction());

        System.setProperty(DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY, MappingActions.NEW_OR_UPDATE);
        Assert.assertEquals(MappingActions.NEW_OR_UPDATE, EntityBuilderHelper.getDefaultEntityMappingAction());
    }

    @Test
    public void testGetEntityWithMappings() throws DocumentParseException {
        Map<String, Object> mappingProperties = new HashMap<>();
        mappingProperties.put(MappingProperties.MAP_BY, MappingProperties.PATH);
        String id = "testId";
        String path = "root/subpath/test";
        DocumentTools documentTools = DocumentTools.INSTANCE;
        Document document = documentTools.parse("<Policy></Policy>");
        Entity entity = EntityBuilderHelper.getEntityWithMappings(EntityTypes.POLICY_TYPE, path, id,
                document.getDocumentElement(), MappingActions.NEW_OR_UPDATE, mappingProperties);
        Assert.assertEquals(id, entity.getId());
        Assert.assertEquals(path, entity.getName());
        Assert.assertEquals(EntityTypes.POLICY_TYPE, entity.getType());
        Assert.assertEquals(MappingActions.NEW_OR_UPDATE, entity.getMappingAction());
        Assert.assertEquals(path, entity.getMappingProperties().get(MappingProperties.MAP_TO));
        mappingProperties.put(MappingProperties.MAP_BY, MappingProperties.NAME);
        entity = EntityBuilderHelper.getEntityWithMappings(EntityTypes.POLICY_TYPE, path, id,
                document.getDocumentElement(), MappingActions.NEW_OR_UPDATE, mappingProperties);
        Assert.assertEquals(PathUtils.extractName(path), entity.getMappingProperties().get(MappingProperties.MAP_TO));

    }

    @Test
    public void testGetPath(){
        Folder rootFolder = new Folder();
        rootFolder.setName("root");
        String path = EntityBuilderHelper.getPath(rootFolder, "test");
        Assert.assertEquals("test", path);

        Folder subFolder = new Folder();
        subFolder.setParentFolder(rootFolder);
        subFolder.setName("subfolder");
        path = EntityBuilderHelper.getPath(subFolder, "test");
        Assert.assertEquals("subfolder/test", path);
    }
}
