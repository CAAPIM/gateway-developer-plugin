/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.ServiceLinker.getServicePath;

/**
 * Linker for ListenPort and TargetService.
 */
@Singleton
public class ListenPortLinker implements EntityLinker<ListenPortEntity> {

    @Override
    public Class<ListenPortEntity> getEntityClass() {
        return ListenPortEntity.class;
    }

    @Override
    public void link(ListenPortEntity entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getTargetServiceReference() == null || entity.getTargetServiceReference().isEmpty()) {
            return;
        }

        ServiceEntity service = bundle.getEntities(ServiceEntity.class).get(entity.getTargetServiceReference());
        if (service == null) {
            throw new LinkerException("Could not find Service for Listen Port: " + entity.getName() + ". Service Reference: " + entity.getTargetServiceReference());
        }

        entity.setTargetServiceReference(getServicePath(bundle, service));
    }
}
