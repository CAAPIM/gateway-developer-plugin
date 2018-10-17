package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters.FilterTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class BundleFilterTest {

    @Test
    void filter() {
        BundleFilter bundleFilter = new BundleFilter(new EntityFilterRegistry(Collections.emptySet()));

        Bundle bundle = FilterTestUtils.getBundle();
        Bundle filteredBundle = bundleFilter.filter("/my/folder/path", bundle);

        assertEquals(1, filteredBundle.getEntities(Folder.class).size());
        assertNotNull(filteredBundle.getFolderTree());
    }
}