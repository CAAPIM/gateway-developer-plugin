/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;

import javax.inject.Singleton;

@Singleton
public class CassandraConnectionLinker implements EntityLinker<CassandraConnectionEntity> {

    @Override
    public void link(CassandraConnectionEntity entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getPasswordId() == null) {
            return;
        }

        StoredPasswordEntity storedPassword = bundle.getEntities(StoredPasswordEntity.class).get(entity.getPasswordId());
        if (storedPassword == null) {
            throw new LinkerException("Could not find Stored Password for Cassandra Connection: " + entity.getName() + ". Password ID: " + entity.getPasswordId());
        }

        entity.setPasswordName(storedPassword.getName());
    }

    @Override
    public Class<CassandraConnectionEntity> getEntityClass() {
        return CassandraConnectionEntity.class;
    }

}
