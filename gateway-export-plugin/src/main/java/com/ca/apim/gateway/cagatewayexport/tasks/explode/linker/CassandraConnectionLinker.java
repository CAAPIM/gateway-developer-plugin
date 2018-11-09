/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;

import javax.inject.Singleton;

@Singleton
public class CassandraConnectionLinker implements EntityLinker<CassandraConnection> {

    @Override
    public void link(CassandraConnection entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getPasswordId() == null) {
            return;
        }

        StoredPassword storedPassword = bundle.getEntities(StoredPassword.class).values().stream().filter(s -> entity.getPasswordId().equals(s.getId())).findFirst().orElse(null);
        if (storedPassword == null) {
            throw new LinkerException("Could not find Stored Password for Cassandra Connection: " + entity.getName() + ". Password ID: " + entity.getPasswordId());
        }

        entity.setStoredPasswordName(storedPassword.getName());
    }

    @Override
    public Class<CassandraConnection> getEntityClass() {
        return CassandraConnection.class;
    }

}
