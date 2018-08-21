package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider;

import java.util.List;

/**
 * Created by chaoy01 on 2018-08-16.
 */
public abstract class IdentityProviderDetail {

    private List<String> serverUrls;
    private boolean useSslClientAuthentication;

    public List<String> getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(List<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public boolean isUseSslClientAuthentication() {
        return useSslClientAuthentication;
    }

    public void setUseSslClientAuthentication(boolean useSslClientAuthentication) {
        this.useSslClientAuthentication = useSslClientAuthentication;
    }
}
