package com.ca.apim.gateway.cagatewayconfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.google.common.collect.ImmutableMap;

public class BuildPropfileBundleTask extends DefaultTask {
	Bundle createBundle (Map<String, String> environmentProperties, PropfileBundleConfig config) {
        EnvironmentBundleCreator bundleCreator = InjectionRegistry.getInjector().getInstance(EnvironmentBundleCreator.class);
        return bundleCreator.createEnvironmentBundle(
                environmentProperties,
                sanitizeDirPath(config.getBundleDirPath()),
                sanitizeDirPath(config.getTemplatizedDirPath()),
                sanitizeDirPath(config.getConfigDirPath()),
                EnvironmentBundleCreationMode.APPLICATION,
                "_0_env.req.bundle");
	}

	/*
	 * Prepend the subproject name if it's a relative path, and ensure that the directory exists.
	 * Otherwise return null.
	 */
	String sanitizeDirPath(String dirPath) {
		if (dirPath == null || dirPath.trim().length() == 0)
			return null;

		if (dirPath.startsWith("/")) {
			File dir = new File(dirPath);
			if (dir.isDirectory())
				return dir.getAbsolutePath();

			System.out.println("directory " + dir.getAbsolutePath() + " is not a directory or does not exist");
			return null;
		}

		File dir = new File(getProject().getName(), dirPath);
		if (dir.isDirectory())
			return dir.getAbsolutePath();

		System.out.println("directory " + dir.getAbsolutePath() + " is not a directory or does not exist");
		return null;
	}

	@TaskAction
	public void perform () {
		System.out.println("build propfile bundle: perform()");
		PropfileBundleConfig config = getProject().getExtensions().getByType(PropfileBundleConfig.class);

//        ImmutableMap<String, String> props = ImmutableMap.<String, String>builder()
//                .put(
//                        "ENV.IDENTITY_PROVIDER.simple ldap", "{\n" +
//                                "    \"type\" : \"BIND_ONLY_LDAP\",\n" +
//                                "    \"environmentVariables\": {\n" +
//                                "      \"key1\":\"value1\",\n" +
//                                "      \"key2\":\"value2\"\n" +
//                                "    },\n" +
//                                "    \"identityProviderDetail\" : {\n" +
//                                "      \"serverUrls\": [\n" +
//                                "        \"ldap://host:port\",\n" +
//                                "        \"ldap://host:port2\"\n" +
//                                "      ],\n" +
//                                "      \"useSslClientAuthentication\":false,\n" +
//                                "      \"bindPatternPrefix\": \"somePrefix\",\n" +
//                                "      \"bindPatternSuffix\": \"someSuffix\"\n" +
//                                "    }\n" +
//                                "  }")
//                .put("ENV.LISTEN_PORT.Custom HTTPS Port", "{\n" +
//                        "      \"protocol\" : \"HTTPS\",\n" +
//                        "      \"port\" : 12345,\n" +
//                        "      \"enabledFeatures\" : [ \"Published service message input\" ],\n" +
//                        "      \"tlsSettings\" : {\n" +
//                        "        \"clientAuthentication\" : \"REQUIRED\",\n" +
//                        "        \"enabledVersions\" : [ \"TLSv1.2\" ],\n" +
//                        "        \"enabledCipherSuites\" : [ \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\" ],\n" +
//                        "        \"environmentVariables\" : {\n" +
//                        "          \"usesTLS\" : true\n" +
//                        "        }\n" +
//                        "      },\n" +
//                        "      \"environmentVariables\" : { \n" +
//                        "         \"threadPoolSize\" : \"20\"\n" +
//                        "      }\n" +
//                        "    }")
//                .put("ENV.JDBC_CONNECTION.my-jdbc", "{\n" +
//                        "    \"driverClass\" : \"com.mysql.jdbc.Driver\",\n" +
//                        "    \"jdbcUrl\" : \"jdbc:mysql://localhost:3306/ssg\",\n" +
//                        "    \"user\" : \"gateway\",\n" +
//                        "    \"passwordRef\" : \"gateway\",\n" +
//                        "    \"minimumPoolSize\" : 3,\n" +
//                        "    \"maximumPoolSize\" : 15,\n" +
//                        "    \"properties\" : {\n" +
//                        "      \"EnableCancelTimeout\" : \"true\"\n" +
//                        "    }\n" +
//                        "  }")
//                .put("ENV.CERTIFICATE.my-cert", "{\n" +
//                        "      \"verifyHostname\" : false,\n" +
//                        "      \"trustedForSsl\" : true,\n" +
//                        "      \"trustedAsSamlAttestingEntity\" : false,\n" +
//                        "      \"trustAnchor\" : true,\n" +
//                        "      \"revocationCheckingEnabled\" : true,\n" +
//                        "      \"trustedForSigningClientCerts\" : true,\n" +
//                        "      \"trustedForSigningServerCerts\" : true,\n" +
//                        "      \"trustedAsSamlIssuer\" : false,\n" +
//                        "      \"certificateData\" : {\n" +
//                        "           \"issuerName\" : \"my-cert\",\n" +
//                        "           \"serialNumber\" : \"123\",\n" +
//                        "           \"subjectName\" : \"my-cert\",\n" +
//                        "           \"encodedData\" : \"my-cert-data\"\n" +
//                        "      }\n" +
//                        "  }")
//                .put("ENV.PASSWORD.my_password", "my_secret_password")
//                .put("ENV.SERVICE_PROPERTY.my-gateway-api.myEnvironmentVariable", "my-service-property-value")
//                .put("ENV.CONTEXT_VARIABLE_PROPERTY.anotherEnvVar", "context-variable-value")
//                .build();
		String bundleDirPath = sanitizeDirPath(config.getBundleDirPath());
		if (bundleDirPath == null)
			return;

		String templateDirPath = sanitizeDirPath(config.getTemplatizedDirPath());
		if (templateDirPath == null)
			return;

		String configDirPath = sanitizeDirPath(config.getConfigDirPath());
		if (configDirPath == null)
			return;

		Map<String, String> props = new HashMap<String, String>();
		EnvironmentBundleCreator bundleCreator = InjectionRegistry.getInjector().getInstance(EnvironmentBundleCreator.class);
		bundleCreator.createEnvironmentBundle(
				props, bundleDirPath, templateDirPath, configDirPath,
				EnvironmentBundleCreationMode.APPLICATION,
				"_0_env.req.bundle");
//        EntityTypeRegistry entityTypeRegistry = InjectionRegistry.getInjector().getInstance(EntityTypeRegistry.class);
//        entityTypeRegistry.getEntityTypeMap().forEach((k, info) -> System.out.println(
//        		"entity type " + k + " has info " + info));
	}
}
