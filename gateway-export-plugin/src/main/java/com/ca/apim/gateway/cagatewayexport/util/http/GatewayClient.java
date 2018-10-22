/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Base64.getEncoder;
import static org.apache.commons.lang3.RandomStringUtils.random;

public class GatewayClient {

    @SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
    private static final String KEY_PASSPHRASE_HEADER = "L7-key-passphrase";
    private static final Logger LOGGER = Logger.getLogger(GatewayClient.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final GatewayClient INSTANCE = new GatewayClient();

    /**
     * Make API Calls using the given API Caller.
     *
     * @param apiCaller The API Caller to make API calls from
     * @param userName  The user name for the gateway user.
     * @param password  The password for the gateway user.
     */
    public void makeGatewayAPICallsNoReturn(final APICallVoid apiCaller, final String userName, final String password) {
        makeGatewayAPICallsWithReturn(c -> {
            apiCaller.apply(c);
            return Void.TYPE;
        }, userName, password);
    }

    /**
     * Make API Calls using the given API Caller.
     *
     * @param apiCaller The API Caller to make API calls from
     * @param userName  The user name for the gateway user.
     * @param password  The password for the gateway user.
     * @param <R>       The return type of the APICaller
     * @return Returns the result from the api caller
     */
    public <R> R makeGatewayAPICallsWithReturn(final APICall<R> apiCaller, final String userName, final String password) {
        try (CloseableHttpClient client = GatewayClient.buildHTTPSClient(userName, password)) {
            return apiCaller.apply(client);
        } catch (IOException e) {
            throw new GatewayClientException("Exception making API calls", e);
        }
    }

    /**
     * Makes an API call using the given http client
     *
     * @param client The http client to make the API call using
     * @param uri    The uri of the API
     * @return The response body stream from the API response
     */
    public InputStream makeAPICall(final HttpClient client, final String uri) {
        final HttpResponse response;
        final HttpGet httpGet = new HttpGet(uri);

        // Generate a random passphrase with any type of char and using a secure random generator, in order to encrypt the secrets.
        final String encodedPassphrase = random(64, 0, 0, true, true, null, RANDOM);
        httpGet.addHeader(KEY_PASSPHRASE_HEADER, getEncoder().encodeToString(encodedPassphrase.getBytes(defaultCharset())));

        try {
            response = client.execute(httpGet);
        } catch (IOException e) {
            throw new GatewayClientException("Could not make an API Call to: " + uri, e);
        }

        final int statusCode = response.getStatusLine().getStatusCode();

        LOGGER.log(Level.FINE, "Status code is: {0} for uri: {1}", new Object[]{statusCode, uri});
        final InputStream inputStream;
        try {
            inputStream = response.getEntity().getContent();
        } catch (IOException e) {
            throw new GatewayClientException("Could not retrieve response body from API Call to: " + uri, e);
        }
        if (HttpStatus.SC_OK != statusCode) {
            throw new GatewayClientException("API Call to gateway returned status " + statusCode + " for uri: " + uri);
        }
        return inputStream;
    }

    private static CloseableHttpClient buildHTTPSClient(final String userName, final String password) {
        final CredentialsProvider provider = new BasicCredentialsProvider();
        final UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(userName, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        final SSLContext sslContext;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new GatewayClientException("Unexpected exception building a gateway https client", e);
        }
        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLContext(sslContext).build();
    }

    public interface APICallVoid {
        void apply(HttpClient httpClient) throws IOException;
    }

    public interface APICall<R> {
        R apply(HttpClient httpClient) throws IOException;
    }
}
