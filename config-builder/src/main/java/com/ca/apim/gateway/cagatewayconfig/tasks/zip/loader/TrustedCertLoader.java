/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TrustedCertLoader implements EntityLoader {
    private static final String FILE_NAME = "trusted-certs";
    private static final TypeReference<HashMap<String, TrustedCert>> trustedCertTypeMapping = new TypeReference<HashMap<String, TrustedCert>>() {
    };
    private final JsonTools jsonTools;

    public TrustedCertLoader(JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        final Map<String, TrustedCert> trustedCerts = jsonTools.parseDocumentFileFromConfigDir(
                rootDir,
                FILE_NAME,
                trustedCertTypeMapping);
        if (trustedCerts != null) {
            bundle.putAllTrustedCerts(trustedCerts);
        }
    }
}
