package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class GenericEntityFilterTest {

    @Test
    public void testFilter() {
        GenericEntityFilter genericEntityFilter = new GenericEntityFilter();
        FilterConfiguration filterConfiguration = new FilterConfiguration();
        Map<String, Collection<String>> entityFilters = new HashMap<>();
        Collection<String> entityNames = new LinkedList<>();
        entityNames.add(HTTP2_CLIENT_NAME);
        entityFilters.put(genericEntityFilter.getFilterableEntityName(), entityNames);
        filterConfiguration.setEntityFilters(entityFilters);

        List<GenericEntity> filteredEntities = genericEntityFilter.filter("/", filterConfiguration, getTestBundle(), new Bundle());
        assertEquals(1, filteredEntities.size());

        entityFilters.put(genericEntityFilter.getFilterableEntityName(), Collections.emptySet());
        filterConfiguration.setEntityFilters(entityFilters);
        filteredEntities = genericEntityFilter.filter("/", filterConfiguration, getTestBundle(), new Bundle());
        assertEquals(0, filteredEntities.size());

        entityFilters.put(genericEntityFilter.getFilterableEntityName(), ImmutableSet.of("unknownClient"));
        filterConfiguration.setEntityFilters(entityFilters);
        try {
            genericEntityFilter.filter("/", filterConfiguration, getTestBundle(), new Bundle());
            fail("Filter should have failed to validate the required generic entities");
        } catch (EntityFilterException e) {
            assertEquals("Missing Generic Entity(s) with name: 'unknownClient'", e.getMessage());
        }
    }

    private Bundle getTestBundle() {
        Bundle bundle = new Bundle();
        bundle.setDependencyMap(new HashMap<>());

        bundle.getFolders().put(Folder.ROOT_FOLDER_NAME, Folder.ROOT_FOLDER);
        bundle.buildFolderTree();

        String serviceRef = "testService";
        String serviceId = "testServiceId";
        Service service = new Service();
        service.setName(serviceRef);
        service.setId(serviceId);
        service.setParentFolder(Folder.ROOT_FOLDER);
        bundle.getServices().put(serviceRef, service);

        GenericEntity genericEntity = new GenericEntity();
        genericEntity.setName(HTTP2_CLIENT_NAME);
        genericEntity.setEntityClassName("com.l7tech.external.assertions.http2.routing.model.Http2ClientConfigurationEntity");
        genericEntity.setValueXml(HTTP2_CLIENT_VALUE_XML);
        bundle.getGenericEntities().put(genericEntity.getName(), genericEntity);

        return bundle;
    }

    private static final String HTTP2_CLIENT_NAME = "http2client";
    private static final String HTTP2_CLIENT_VALUE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "    <java version=\"1.8.0_242\" class=\"java.beans.XMLDecoder\">\n" +
            "     <object class=\"com.l7tech.external.assertions.http2.routing.model.Http2ClientConfigurationEntity\">\n" +
            "      <void property=\"connectionTimeout\">\n" +
            "       <int>1000</int>\n" +
            "      </void>\n" +
            "      <void property=\"description\">\n" +
            "       <string>default</string>\n" +
            "      </void>\n" +
            "      <void property=\"id\">\n" +
            "       <string>6183c11a61d2a42729506f690aa8eab9</string>\n" +
            "      </void>\n" +
            "      <void property=\"name\">\n" +
            "       <string>default</string>\n" +
            "      </void>\n" +
            "      <void property=\"readTimeout\">\n" +
            "       <int>1001</int>\n" +
            "      </void>\n" +
            "      <void property=\"tlsCipherSuites\">\n" +
            "       <string>TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256</string>\n" +
            "      </void>\n" +
            "      <void property=\"tlsVersion\">\n" +
            "       <string>TLSv1.2</string>\n" +
            "      </void>\n" +
            "      <void property=\"valueXml\">\n" +
            "       <string></string>\n" +
            "      </void>\n" +
            "      <void property=\"version\">\n" +
            "       <int>2</int>\n" +
            "      </void>\n" +
            "     </object>\n" +
            "    </java>";
}
