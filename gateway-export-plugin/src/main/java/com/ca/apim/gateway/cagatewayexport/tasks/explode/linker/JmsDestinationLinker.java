/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.VariableUtils.extractVariableName;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerConstants.ENCRYPTED_PASSWORD_PREFIX;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerConstants.STORED_PASSWORD_PATTERN;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.ServiceLinker.getServicePath;
import static java.util.Optional.ofNullable;
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
        verifyPrivateKeys(entity, bundle);
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
            throw new LinkerException("Could not find associated Service for inbound JMS Destination: " + entity.getName() + ". Service path: " + serviceRef);
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
            throw new LinkerException("Could not find Stored Password for JMS Destination: " + entity.getName() + ". Password name: " + storedPasswordName);
        }

        return storedPassword;
    }
    
    private void verifyPrivateKeys(JmsDestination entity, Bundle bundle) {
        // Verify that referenced private keys exists in the bundle.
        Set<Object> aliases = ofNullable(entity.getAdditionalProperties()).orElseGet(HashMap::new)
                .entrySet().stream()
                .filter(map ->
                        JNDI_CLIENT_AUT_KEYSTORE_ALIAS.equals(map.getKey()) || 
                                DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS.equals(map.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        
        if (aliases.isEmpty()) {
            return;
        }
        
        for (Object alias : aliases) {
            String cast = (String) alias;
            if (null == bundle.getEntities(PrivateKey.class).get((cast))) {
                throw new LinkerException("Could not find Private Key for JMS Destination: " + entity.getName() + ". Private Key alias: " + cast);   
            }
        }
    }
}
