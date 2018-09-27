# Applying Environment During Gateway Bootstrap

This feature is about applying environment during gateway bootstrap.
Essentially, when a gateway is bootstrapped with a solution built from the Gateway Developer Gradle plugin how will environment properties be applied.

Environment properties include:
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

## Providing Environment
In General Environment should be providable in 2 way.

1. Via Environment Variables
2. Via Files on Disk
> Both approaches are needed in order to support different platforms and docker environments.

### Providing Environment via Environment Variables
The [third factor in building twelve-factor apps](https://12factor.net/config) talks about all configuration coming from environment.
This mean giving values for the different environment properties above using Environment variables. 

#### Usage
Our convention is that environment properties for the CA API Gateway are prefixed with `ENV.`. 

For example in order to set a global property pass an environment variable like so: `docker run -e ENV.my-global-property='foo' caapim/gateway`

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

### Providing Environment via Files on Disk
Another way to provide environment is by providing it as files on disk. These will be in:
`/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env`
In that directory can be the same configurations files as you would see in the `config` directory.

#### Usage
For example, in order to provide a JDBC configuration you could mount the `jdbc.yml` file to `/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/jdbc.yml`

### Providing Environment using Both Methods
You can mix both methods to provide environment values. If you are using both methods and there is a value specified both from environment properties and in a configuration file the value from the environment properties will be preferred.

### Examples
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
      - ./env/listenports.yml:/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/listenports.yml
    secrets:
      - source: passwords
        target: /opt/SecureSpan/Gateway/node/default/etc/bootstrap/env/passwords.properties
    environment:
      ENV.local.env.var: "docker local environment variable"
      ENV.message-variable: "docker A message Variable"
      ENV.gateway.my-global-property: "docker global1"
      ENV.gateway.another.global: "docker global2"
      ENV.empty-value: ""
      ENV.service.property.db.type: "docker mongo"
      ENV.JDBC_CONNECTION.my-jdbc: ""
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

## Technical Implementation
The technical implementation will be achieved by adding in a simple JAVA application to do environment configuration processing. It will be built by reusing code from the gradle plugins.
This application will be called from a pre-boot script. It will:

1. read environment properties
2. read configuration files
3. Build an environment bundle and put it in the bootstrap directory
4. De-Templatize the solution bundle and dependency bundles with environment values for things like service properties, and environment context variables.

### Environment Application Details

Options for distributing the application:
1. bundle full application and dependencies within the gradle plugin
   * Disadvantages
     * This greatly increases the size of the gradle plugin
     * Potential issues with redistributing 3rd partly libraries
   * Positives
     * Produced a very reproducible build
    * doesn't require end plugin used to have any other dependencies.
2. bundle application but not dependencies
   * Disadvantages
     * Requires end user to have a dependency on maven central (or other repo with sources)
   * Positives
     * doesn't increase repo size by much
3. Publish application to bintray and download from there during build
   * Disadvantages
     * Requires end user to have a dependency on bintray
   * Positives
     * no impact to plugin size

### GW7 Package Contents
* opt
  * docker
    * rc.d
      * apply-environment.sh - This script will execute the ApplyEnvironment.jar to apply environment specific values
        * This script will execute ...
      * apply-environment
        * ApplyEnvironment.jar
        * lib
          * ...  
        * bundles
          * <solution_name.bundle>
          * <dependency_bundles>
 
 