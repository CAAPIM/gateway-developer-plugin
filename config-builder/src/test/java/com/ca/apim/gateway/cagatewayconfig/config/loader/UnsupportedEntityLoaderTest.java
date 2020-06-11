package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UnsupportedEntityLoaderTest {

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testNoServices(TemporaryFolder temporaryFolder) {
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(JsonTools.INSTANCE, new IdGenerator());

        Bundle bundle = new Bundle();
        unsupportedEntityLoader.load(bundle, temporaryFolder.getRoot());
        assertTrue(bundle.getServices().isEmpty());
    }

    @Test
    void testEntityInfo() {
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(JsonTools.INSTANCE, new IdGenerator());
        assertEquals("unsupported-entities", unsupportedEntityLoader.getFileName());
        assertEquals("UNSUPPORTED", unsupportedEntityLoader.getEntityType());
        assertEquals(UnsupportedGatewayEntity.class, unsupportedEntityLoader.getBeanClass());
    }
}
