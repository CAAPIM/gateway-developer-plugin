/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.Set;

public abstract class LdapIdentityProviderDetail implements IdentityProviderDetail {

    private Set<String> serverUrls;
    private boolean useSslClientAuthentication;

    public Set<String> getServerUrls() {
        return serverUrls;
    }

    public void setServerUrls(Set<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public boolean isUseSslClientAuthentication() {
        return useSslClientAuthentication;
    }

    public void setUseSslClientAuthentication(boolean useSslClientAuthentication) {
        this.useSslClientAuthentication = useSslClientAuthentication;
    }
}
