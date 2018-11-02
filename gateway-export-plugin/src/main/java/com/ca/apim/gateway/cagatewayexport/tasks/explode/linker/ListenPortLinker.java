/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.ServiceLinker.getServicePath;

/**
 * Linker for ListenPort and TargetService.
 */
@Singleton
public class ListenPortLinker implements EntityLinker<ListenPort> {

    @Override
    public Class<ListenPort> getEntityClass() {
        return ListenPort.class;
    }

    @Override
    public void link(ListenPort entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getTargetServiceReference() == null || entity.getTargetServiceReference().isEmpty()) {
            return;
        }

        Service service = bundle.getEntities(Service.class).get(entity.getTargetServiceReference());
        if (service == null) {
            throw new LinkerException("Could not find Service for Listen Port: " + entity.getName() + ". Service Reference: " + entity.getTargetServiceReference());
        }

        entity.setTargetServiceReference(getServicePath(bundle, service));
    }
}
