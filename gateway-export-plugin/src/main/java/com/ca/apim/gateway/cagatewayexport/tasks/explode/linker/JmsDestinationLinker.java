/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.VariableUtils.extractVariableName;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class JmsDestinationLinker implements EntityLinker<JmsDestination> {

    private static final Pattern secpassPattern = Pattern.compile("secpass.(.+?).plaintext");
    
    @Override
    public Class<JmsDestination> getEntityClass() {
        return JmsDestination.class;
    }

    @Override
    public void link(JmsDestination entity, Bundle bundle, Bundle targetBundle) {
        linkJndiStoredPassword(entity, bundle);
        linkDestinationStoredPassword(entity, bundle);
        
        // (kpak): link private key(s), service
    }
    
    private void linkJndiStoredPassword(JmsDestination entity, Bundle bundle) {
        // JNDI password
        if (entity.getJndiPassword() != null) {
            if (entity.getJndiPassword().startsWith("$L7C2$")) {
                // Ignore passwords that are L7C2 encoded. We can't decode them anyways.
                entity.setJndiPassword(null);
            } else {
                String storedPasswordReference = extractVariableName(entity.getJndiPassword());
                if (!isEmpty(storedPasswordReference)) {
                    Matcher matcher = secpassPattern.matcher(storedPasswordReference);
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
            if (entity.getDestinationPassword().startsWith("$L7C2$")) {
                // Ignore passwords that are L7C2 encoded. We can't decode them anyways.
                entity.setDestinationPassword(null);
            } else {
                String storedPasswordReference = extractVariableName(entity.getDestinationPassword());
                if (!isEmpty(storedPasswordReference)) {
                    Matcher matcher = secpassPattern.matcher(storedPasswordReference);
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
