/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.regex.Matcher;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.VariableUtils.extractVariableName;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerConstants.ENCRYPTED_PASSWORD_PREFIX;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerConstants.STORED_PASSWORD_PATTERN;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.ServiceLinker.getServicePath;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class JmsDestinationLinker implements EntityLinker<JmsDestination> {

    @Override
    public Class<JmsDestination> getEntityClass() {
        return JmsDestination.class;
    }

    @Override
    public void link(JmsDestination entity, Bundle bundle, Bundle targetBundle) {
        linkJndiStoredPassword(entity, bundle);
        linkDestinationStoredPassword(entity, bundle);
        linkInboundAssociatedService(entity, bundle);
        
        // (kpak) - link private key(s)
    }
    
    private void linkJndiStoredPassword(JmsDestination entity, Bundle bundle) {
        // JNDI password
        if (entity.getJndiPassword() != null) {
            if (entity.getJndiPassword().startsWith(ENCRYPTED_PASSWORD_PREFIX)) {
                // Ignore passwords that are L7C2 encoded. We can't decode them anyways.
                entity.setJndiPassword(null);
            } else {
                String storedPasswordReference = extractVariableName(entity.getJndiPassword());
                if (!isEmpty(storedPasswordReference)) {
                    Matcher matcher = STORED_PASSWORD_PATTERN.matcher(storedPasswordReference);
                    if (matcher.matches()) {
                        // the middle string between secpass and plaintext will be the stored password name
                        String storedPasswordName = matcher.group(1);
                        StoredPassword storedPassword = this.findStoredPasswordRef(entity, bundle, storedPasswordName);

                        // replace the expression with the password reference by name
                        entity.setJndiPasswordRef(storedPassword.getName());
                        entity.setJndiPassword(null);
                    }
                }
            }
        }
    }
    
    private void linkDestinationStoredPassword(JmsDestination entity, Bundle bundle) {
        // Destination password
        if (entity.getDestinationPassword() != null) {
            if (entity.getDestinationPassword().startsWith(ENCRYPTED_PASSWORD_PREFIX)) {
                // Ignore passwords that are L7C2 encoded. We can't decode them anyways.
                entity.setDestinationPassword(null);
            } else {
                String storedPasswordReference = extractVariableName(entity.getDestinationPassword());
                if (!isEmpty(storedPasswordReference)) {
                    Matcher matcher = STORED_PASSWORD_PATTERN.matcher(storedPasswordReference);
                    if (matcher.matches()) {
                        // the middle string between secpass and plaintext will be the stored password name
                        String storedPasswordName = matcher.group(1);
                        StoredPassword storedPassword = this.findStoredPasswordRef(entity, bundle, storedPasswordName);

                        // replace the expression with the password reference by name
                        entity.setDestinationPasswordRef(storedPassword.getName());
                        entity.setDestinationPassword(null);
                    }
                }
            }
        }
    }
    
    private void linkInboundAssociatedService(JmsDestination entity, Bundle bundle) {
        if (entity.getInboundDetail() == null || 
                entity.getInboundDetail().getServiceResolutionSettings() == null ||
                StringUtils.isEmpty(entity.getInboundDetail().getServiceResolutionSettings().getServiceRef())) {
            return;
        }
        
        final String serviceRef = entity.getInboundDetail().getServiceResolutionSettings().getServiceRef();

        final Service service =
                bundle.getEntities(Service.class).values()
                        .stream()
                        .filter(e -> e.getId().equals(serviceRef))
                        .findFirst()
                        .orElse(null);
        if (service == null) {
            throw new LinkerException("Could not find associated Service for inbound JMS Destination: " + entity.getName() + ". Service Path: " + serviceRef);
        }
        
        entity.getInboundDetail().getServiceResolutionSettings().setServiceRef(getServicePath(bundle, service));
    }
    
    @NotNull
    private StoredPassword findStoredPasswordRef(JmsDestination entity, Bundle bundle, String storedPasswordName) {
        final StoredPassword storedPassword =
                bundle.getEntities(StoredPassword.class).values()
                        .stream()
                        .filter(e -> e.getName().equals(storedPasswordName))
                        .findFirst()
                        .orElse(null);
        if (storedPassword == null) {
            throw new LinkerException("Could not find Stored Password for JMS Destination: " + entity.getName() + ". Password Name: " + storedPasswordName);
        }

        return storedPassword;
    }
}
