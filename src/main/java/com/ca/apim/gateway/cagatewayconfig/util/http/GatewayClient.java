/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.http;

import com.ca.apim.gateway.cagatewayconfig.tasks.export.ExportTask;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class GatewayClient {
    private static final Logger LOGGER = Logger.getLogger(ExportTask.class.getName());
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
        try {
            response = client.execute(new HttpGet(uri));
        } catch (IOException e) {
            throw new GatewayClientException("Could not make an API Call to: " + uri, e);
        }

        final int statusCode = response.getStatusLine().getStatusCode();

        LOGGER.log(Level.FINE, "Status code is: %d for uri: %s", new Object[]{statusCode, uri});
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
