/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;

import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.VariableUtils.extractVariableName;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class JdbcConnectionLinker implements EntityLinker<JdbcConnection> {

    private Pattern secpassPattern = Pattern.compile("secpass.(.+?).plaintext");

    @Override
    public void link(JdbcConnection entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getPassword() != null) {
            if (entity.getPassword().startsWith("$L7C2$")) {
                // Ignore passwords that are L7C2 encoded. We can't decode them anyways.
                entity.setPassword(null);
            } else {
                String storedPasswordReference = extractVariableName(entity.getPassword());
                if (!isEmpty(storedPasswordReference)) {
                    Matcher matcher = secpassPattern.matcher(storedPasswordReference);
                    if (matcher.matches()) {
                        // the middle string between secpass and plaintext will be the stored password name
                        String storedPasswordName = matcher.group(1);
                        setPasswordRef(entity, bundle, storedPasswordName);
                    }
                }
            }
        }
    }

    private void setPasswordRef(JdbcConnection entity, Bundle bundle, String storedPasswordName) {
        final StoredPassword storedPassword =
                bundle.getEntities(StoredPassword.class).values()
                        .stream()
                        .filter(e -> e.getName().equals(storedPasswordName))
                        .findFirst()
                        .orElse(null);
        if (storedPassword == null) {
            throw new LinkerException("Could not find Stored Password for JDBC Connection: " + entity.getName() + ". Password Name: " + storedPasswordName);
        }

        // replace the expression with the password reference by name
        entity.setPasswordRef(storedPassword.getName());
        entity.setPassword(null);
    }

    @Override
    public Class<JdbcConnection> getEntityClass() {
        return JdbcConnection.class;
    }
}
