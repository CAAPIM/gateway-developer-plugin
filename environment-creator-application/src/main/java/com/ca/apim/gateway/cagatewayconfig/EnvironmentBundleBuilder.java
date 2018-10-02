package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.*;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.StoredPasswordsLoader;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.io.IOUtils;

import java.nio.charset.Charset;
import java.util.Map;

public class EnvironmentBundleBuilder {

    private final Bundle bundle;
    private static final JsonTools jsonTools = JsonTools.INSTANCE;

    public EnvironmentBundleBuilder(Map<String, String> environmentProperties) {
        bundle = new Bundle();
        environmentProperties.entrySet().stream().filter(e -> e.getKey().startsWith("ENV.")).forEach(e -> addEnvToBundle(e.getKey(), e.getValue()));
    }

    private void addEnvToBundle(String key, String value) {
        if (key.startsWith("ENV.LISTEN_PORT.")) {
            ListenPort listenPort = jsonTools.readStream(IOUtils.toInputStream(value, Charset.defaultCharset()), JsonTools.JSON, new TypeReference<ListenPort>() {
            });
            bundle.getListenPorts().put(key.substring("ENV.LISTEN_PORT.".length()), listenPort);
        } else if (key.startsWith("ENV.IDENTITY_PROVIDER.")) {
            IdentityProvider identityProvider = jsonTools.readStream(IOUtils.toInputStream(value, Charset.defaultCharset()), JsonTools.JSON, new TypeReference<IdentityProvider>() {
            });
            bundle.getIdentityProviders().put(key.substring("ENV.IDENTITY_PROVIDER.".length()), identityProvider);
        } else if (key.startsWith("ENV.JDBC_CONNECTION.")) {
            JdbcConnection jdbcConnection = jsonTools.readStream(IOUtils.toInputStream(value, Charset.defaultCharset()), JsonTools.JSON, new TypeReference<JdbcConnection>() {
            });
            bundle.getJdbcConnections().put(key.substring("ENV.JDBC_CONNECTION.".length()), jdbcConnection);
        } else if (key.startsWith("ENV.TRUSTED_CERTIFICATE.")) {
            TrustedCert trustedCert = jsonTools.readStream(IOUtils.toInputStream(value, Charset.defaultCharset()), JsonTools.JSON, new TypeReference<TrustedCert>() {
            });
            bundle.getTrustedCerts().put(key.substring("ENV.TRUSTED_CERTIFICATE.".length()), trustedCert);
        } else if (key.startsWith("ENV.PASSWORD.")) {
            StoredPassword password = StoredPasswordsLoader.buildStoredPassword(key.substring("ENV.PASSWORD.".length()), value);
            bundle.getStoredPasswords().put(password.getName(), password);
        } else if (key.startsWith("ENV.")) {
            bundle.getEnvironmentProperties().put(key.substring("ENV.".length()), value);
        }
    }

    public Bundle getBundle() {
        return bundle;
    }
}
