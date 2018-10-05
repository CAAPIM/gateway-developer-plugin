# Identity Providers Configuration
This is a file containing the identity providers that are exposed in the Gateway.
The identity providers configuration file is expected to be in the `config` directory. It should be either `identity-providers.yml` or `identity-providers.json`.

An example `identity-providers.yml` file for a BIND_ONLY_LDAP configuration would look like:
```yaml
  simple ldap:
    type: BIND_ONLY_LDAP
    properties:
      key1: "value1"
      key2: "value2"
    identityProviderDetail:
      serverUrls:
        - ldap://host:port
        - ldap://host:port2
      useSslClientAuthentication: false
      bindPatternPrefix: somePrefix
      bindPatternSuffix: someSuffix
  ```
  The above file has the following organization:
  ```yaml
  <name-of-id-provider>:
    <id-provider-description>
  ```
Possible arguments for id-provider-description:
* `type`: currently only supports BIND_ONLY_LDAP
* `serverUrls`: A list of LDAP or LDAPS directory service that the identity provider is configured to connect to. When configuring using the IPv6 address space, the host URL must be enclosed within '[ ]' if a literal IPv6 address is used, for example: 
`ldap://oracle.companyx.com:389 (no brackets required)`
`ldap://[2222::22]:389 (brackets required)`
* `useSslClientAuthentication`: set to true to use the Default SSL Key on the CA API Gateway
* `bindPatternPrefix`, `bindPatternSuffix`: Optional prefix or suffix for the authorization DN
* `properties`: A list of keys and values associated with the ID provider.

The same JSON representation would look like:
```json
{
  "simple ldap": {
    "type" : "BIND_ONLY_LDAP",
    "properties": {
      "key1":"value1",
      "key2":"value2"
    },
    "identityProviderDetail" : {
      "serverUrls": [
        "ldap://host:port",
        "ldap://host:port2"
      ],
      "useSslClientAuthentication":false,
      "bindPatternPrefix": "somePrefix",
      "bindPatternSuffix": "someSuffix"
    }
  }
}
```

# Environment
Identity provider configuration is environment configuration. It is not added to a deployment bundle and must be specified as environment.
In order to do so you can set an environment property with the name: `ENV.IDENTITY_PROVIDER.<name>` where `<name>` is the name of the identity provider.

## Examples
```
ENV.IDENTITY_PROVIDER.oauth='{
                               "type" : "BIND_ONLY_LDAP",
                               "properties": {
                                 "key1":"value1",
                                 "key2":"value2"
                               },
                               "identityProviderDetail" : {
                                 "serverUrls": [
                                   "ldap://host:port",
                                   "ldap://host:port2"
                                 ],
                                 "useSslClientAuthentication":false,
                                 "bindPatternPrefix": "somePrefix",
                                 "bindPatternSuffix": "someSuffix"
                               }
                             }'
```
This will create an LDAP identity provider called `oauth`.