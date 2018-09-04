/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Created by chaoy01 on 2018-08-17.
 */
@JsonTypeName("BIND_ONLY_LDAP")
public class BindOnlyLdapIdentityProviderDetail extends IdentityProviderDetail {

    private String bindPatternPrefix;
    private String bindPatternSuffix;

    public String getBindPatternPrefix() {
        return bindPatternPrefix;
    }

    public void setBindPatternPrefix(String bindPatternPrefix) {
        this.bindPatternPrefix = bindPatternPrefix;
    }

    public String getBindPatternSuffix() {
        return bindPatternSuffix;
    }

    public void setBindPatternSuffix(String bindPatternSuffix) {
        this.bindPatternSuffix = bindPatternSuffix;
    }
}
