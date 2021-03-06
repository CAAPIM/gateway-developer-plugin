package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters.FilterTestUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BundleFilterTest {

    @Test
    void filter() {
        BundleFilter bundleFilter = new BundleFilter(new EntityFilterRegistry(Collections.emptySet()));

        Bundle bundle = FilterTestUtils.getBundle();
        Bundle filteredBundle = bundleFilter.filter("/my/folder/path", new FilterConfiguration(), bundle);

        assertEquals(1, filteredBundle.getEntities(Folder.class).size());
        assertNotNull(filteredBundle.getFolderTree());
    }
}