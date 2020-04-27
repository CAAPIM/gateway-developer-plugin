package com.ca.apim.gateway.cagatewayconfig.beans.metadata;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;

public class MetadataFactory {

    private MetadataFactory() {}

    public static Metadata getMetadata(final GatewayEntity entity) {
        if (entity instanceof Encass) {
            final Encass encass = (Encass) entity;
            return new EncassMetadata.Builder(encass.getName())
                    .arguments(encass.getArguments()).results(encass.getResults()).build();
        }
        return null;
    }
}
