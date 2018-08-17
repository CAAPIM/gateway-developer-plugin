package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Created by chaoy01 on 2018-08-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes( {
        @JsonSubTypes.Type(value=BindOnlyLdapIdentityProviderDetail.class, name="BIND_ONLY_LDAP"),
        @JsonSubTypes.Type(value=LdapIdentityProviderDetail.class, name="LDAP")
})
public abstract class IdentityProviderDetail {

    //type is used for JsonSubType annotations. Currently supports "BIND_ONLY_LDAP" and "LDAP"
    private String type;
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
