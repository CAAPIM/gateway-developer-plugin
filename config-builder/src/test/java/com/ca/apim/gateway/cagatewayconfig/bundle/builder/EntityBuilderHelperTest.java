package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class EntityBuilderHelperTest {
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
}
