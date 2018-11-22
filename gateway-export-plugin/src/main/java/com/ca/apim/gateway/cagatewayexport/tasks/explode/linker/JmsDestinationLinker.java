/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;

import javax.inject.Singleton;

@Singleton
public class JmsDestinationLinker implements EntityLinker<JmsDestination> {
    
    @Override
    public Class<JmsDestination> getEntityClass() {
        return JmsDestination.class;
    }

    @Override
    public void link(JmsDestination entity, Bundle bundle, Bundle targetBundle) {
        // TODO
        // link private key(s)
        // service
        // secure password(s)

    }
}
