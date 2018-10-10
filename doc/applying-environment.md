# What is Environment?
When a deployment package is built it contains the behaviour and configurations to apply to a Gateway. It is static configuration, think of it as code.
A deployment package does not contain any environment values. For example, it would not contain the database credentials for a database because those would be different in different environments.

Types of environment values include:
* global(cluster) properties
* context variables
* service properties
* JDBC connections
* listen ports
* identity providers
* passwords
* certificates
* private keys
* etc...

If your solution requires environment values, the need for them will be added to the deployment package but they will not have any values.

# Providing Environment to a Gateway
In General Environment can be providable in 2 ways.

1. Via Environment Variables
2. Via Files on Disk - This is not yet supported
> Both approaches are needed in order to support different platforms and docker environments.

## Providing Environment via Environment Variables
The [third factor in building twelve-factor apps](https://12factor.net/config) talks about all configuration coming from environment.
This means giving values for the different environment properties above using Environment variables. 

### Usage
Our convention is that environment properties for the CA API Gateway are prefixed with `ENV.<TYPE>.`. Where `<TYPE>` is the type of environment property.

For example in order to set a global property pass an environment variable like so: `docker run -e ENV.PROPERTY.my-global-property='foo' caapim/gateway`

For more complex properties like Identity Providers. The property value can either be the traditional entity xml or the newer yml or json representation. The property key is prefixed with the entity type and suffixed with the value type: `ENV.IDENTITY_PROVIDER.myLDAP.json`. For example:
```
ENV.IDENTITY_PROVIDER.myLDAP.json='{
                                 "type": "BIND_ONLY_LDAP",
                                 "properties": {
                                   "key1": "value1",
                                   "key2": "value2"
                                 },
                                 "identityProviderDetail": {
                                   "serverUrls": [
                                     "ldap://host:port",
                                     "ldap://host:port2"
                                   ],
                                   "useSslClientAuthentication": false,
                                   "bindPatternPrefix": "somePrefix",
                                   "bindPatternSuffix": "someSuffix"
                                 }
                               }'
```

There are also properties that are binary. Like in the case of private keys. These values will be passed as base64'd values. For example,
`ENV.PRIVATE_KEY.my-key='amFzaGRramFzZGphc2xrZGprbHNkamFzamRqZHM='`

## Providing Environment via Files on Disk
Another way to provide environment is by providing it as files on disk. These will be in:
`/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env`
In that directory can be the same configurations files as you would see in the `src/main/gateway/config` directory.

### Usage
For example, in order to provide a JDBC configuration you could mount the `jdbc-connections.yml` file to `/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/jdbc-connections.yml`

## Providing Environment using Both Methods
You can mix both methods to provide environment values. If you are using both methods and there is a value specified both from environment properties and in a configuration file the value from the environment properties will be preferred.

## Examples
See the following docker-compose file:
```yaml
version: '3.4'
services:
  gateway-dev:
    hostname: gateway-hostname
    image: caapim/gateway:9.3.00
    ports:
      - "8443:8443"
    volumes:
      - ./build/gateway/gateway-developer-example.gw7:/opt/docker/rc.d/gateway-developer-example.gw7
      - ./env/listen-ports.yml:/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/listen-ports.yml
    secrets:
      - source: passwords
        target: /opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/stored-passwords.properties
    environment:
      ENV.CONTEXT_PROPERTY.message-variable: "docker A message Variable"
      ENV.PROPERTY.my-global-property: "docker global1"
      ENV.SERVICE_PROPERTY.db.type: "docker mongo"
      ENV.IDENTITY_PROVIDER.myLDAP.json: '{
                                 "type": "BIND_ONLY_LDAP",
                                 "properties": {
                                   "key1": "value1",
                                   "key2": "value2"
                                 },
                                 "identityProviderDetail": {
                                   "serverUrls": [
                                     "ldap://host:port",
                                     "ldap://host:port2"
                                   ],
                                   "useSslClientAuthentication": false,
                                   "bindPatternPrefix": "somePrefix",
                                   "bindPatternSuffix": "someSuffix"
                                 }
                               }'
secrets:
  passwords:
    file: ./docker/passwords.properties
```

# How it Works
When a container Gateway is started an Environment bundle is created from specified environment properties and available files on disk. 
Once the environment bundle is created the deployment bundles are scanned to make sure that all required environment is available in the environment bundle. 
If it is the gateway will start and load the environment bundle along with all the deployment bundles.