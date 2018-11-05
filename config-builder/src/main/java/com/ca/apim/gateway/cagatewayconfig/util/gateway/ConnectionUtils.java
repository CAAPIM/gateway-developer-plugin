/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class ConnectionUtils {

    /**
    * SSLSocketFactory used to retrieve certificates even if they are not in the trust store,
     * to avoid SSL handshake exceptions upon socket connection. This socket factory should only be used to read cert
     * information, and should not be used for persistent TLS connections.
    * @return the sslSocketFactory that accepts all connections.
     */
    public static SSLSocketFactory createAcceptAllSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        @Override
                        @SuppressWarnings("squid:S4424")
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                            //Intentionally blank as this object should only be used to obtain cert information upon SSL handshake
                        }

                        @Override
                        @SuppressWarnings("squid:S4424")
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                            //Intentionally blank as this object should only be used to obtain cert information upon SSL handshake
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
            }}, null);
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new EntityBuilderException("Error creating socket factory: " + e.getMessage(), e);
        }
    }

    private ConnectionUtils(){}
}
