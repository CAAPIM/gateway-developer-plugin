/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.connection;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.ConnectionUtils.initSSLContext;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Base64.getEncoder;
import static java.util.logging.Level.FINE;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.auth.AuthScope.ANY;
import static org.apache.http.impl.client.HttpClientBuilder.create;

public class GatewayClient {

    @SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
    private static final String KEY_PASSPHRASE_HEADER = "L7-key-passphrase";
    private static final Logger LOGGER = Logger.getLogger(GatewayClient.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final GatewayClient INSTANCE = new GatewayClient();

    /**
     * Make API Calls using the given RequestBuilder to get request configuration.
     *
     * @param requestBuilder The Request Builder where method and URI have to be previously set
     * @param userName       The user name for the gateway user.
     * @param password       The password for the gateway user.
     * @return Returns the result from the api caller
     */
    public InputStream makeGatewayAPICall(final RequestBuilder requestBuilder, final String userName, final String password) {
        try (CloseableHttpClient client = buildHTTPSClient(userName, password)) {
            return makeAPICall(client, requestBuilder);
        } catch (IOException e) {
            throw new GatewayClientException("Exception making API calls", e);
        }
    }

    private static InputStream makeAPICall(final HttpClient client, final RequestBuilder requestBuilder) throws IOException {
        // Generate a random passphrase with any type of char and using a secure random generator, in order to encrypt the secrets.
        final String encodedPassphrase = random(64, 0, 0, true, true, null, RANDOM);
        requestBuilder.addHeader(KEY_PASSPHRASE_HEADER, getEncoder().encodeToString(encodedPassphrase.getBytes(defaultCharset())));

        final HttpResponse response;
        final HttpUriRequest request = requestBuilder.build();
        final String uri = request.getURI().toString();

        try {
            response = client.execute(request);
        } catch (IOException e) {
            throw new GatewayClientException("Could not make an API Call (" + request.getMethod() + ") to: " + uri, e);
        }

        final int statusCode = response.getStatusLine().getStatusCode();

        LOGGER.log(FINE, "Status code is: {0} for uri: {1}", new Object[]{ statusCode,  uri });
        final InputStream responseStream;
        try {
            responseStream = response.getEntity().getContent();
        } catch (IOException e) {
            throw new GatewayClientException("Could not retrieve response body from API Call (" + request.getMethod() + ") to: " + uri, e);
        }

        byte[] responseBytes = toByteArray(responseStream);
        if (SC_OK != statusCode) {
            throw new GatewayClientException("API Call (" + request.getMethod() + ") to gateway returned status " + statusCode + " for uri: " + uri + "\nResponse:\n\n" +new String(responseBytes));
        }
        return new ByteArrayInputStream(responseBytes);
    }

    private static CloseableHttpClient buildHTTPSClient(final String userName, final String password) {
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(userName, password);
        provider.setCredentials(ANY, credentials);
        final SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            initSSLContext(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new GatewayClientException("Unexpected exception building a gateway https client", e);
        }
        return create()
                .setDefaultCredentialsProvider(provider)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(sslContext)
                .build();
    }

    /**
     * Format the url with the required parts for restman endpoint.
     * @param url the url, full or partial
     * @return the formatted url
     */
    public static String getRestmanBundleEndpoint(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.contains("restman")) {
            url += "restman/";
        }
        return  url + "1.0/bundle";
    }
}
