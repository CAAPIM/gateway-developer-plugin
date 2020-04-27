package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityAnnotation;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.metadata.BundleMetadata;
import com.ca.apim.gateway.cagatewayconfig.beans.metadata.Metadata;
import com.ca.apim.gateway.cagatewayconfig.beans.metadata.MetadataFactory;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BundleMetadataBuilder {

    public BundleMetadata build(final String bundleName, final String bundleVersion, final GatewayEntity entity) {
        final Encass encass = (Encass) entity;
        String name = bundleName;
        String description = StringUtils.EMPTY;
        for (EntityAnnotation annotation : encass.getAnnotations()) {
            if (AnnotationConstants.ANNOTATION_TYPE_BUNDLE.equals(annotation.getType())) {
                if (StringUtils.isNotBlank(annotation.getName())) {
                    name = annotation.getName();
                }
                description = annotation.getDescription();
            }
        }
        BundleMetadata.Builder builder = new BundleMetadata.Builder("encass", encass.getGuid(), name,
                bundleVersion);
        builder.description(description);
        final List<Metadata> desiredEntities = new ArrayList<>();
        desiredEntities.add(MetadataFactory.getMetadata(entity));

        return builder.definedEntities(desiredEntities).build();
    }
}
