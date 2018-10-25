/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;

import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apim.gateway.cagatewayexport.util.gateway.VariableUtils.extractVariableName;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class JdbcConnectionLinker implements EntityLinker<JdbcConnectionEntity> {

    private Pattern secpassPattern = Pattern.compile("secpass.(.+?).plaintext");

    @Override
    public void link(JdbcConnectionEntity entity, Bundle bundle, Bundle targetBundle) {
        if (entity.getPassword() != null) {
            if (entity.getPassword().startsWith("$L7C2$")) {
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

    private void setPasswordRef(JdbcConnectionEntity entity, Bundle bundle, String storedPasswordName) {
        final StoredPasswordEntity storedPassword =
                bundle.getEntities(StoredPasswordEntity.class).values()
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
    public Class<JdbcConnectionEntity> getEntityClass() {
        return JdbcConnectionEntity.class;
    }
}
