[![Waffle.io - Columns and their card count](https://badge.waffle.io/ca-api-Gateway/Gateway-developer-plugin.svg?columns=all)](https://waffle.io/ca-api-Gateway/Gateway-developer-plugin)
[![Build Status](https://travis-ci.org/ca-api-gateway/gateway-developer-plugin.svg?branch=master)](https://travis-ci.org/ca-api-gateway/gateway-developer-plugin)
[![Sonar Cloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.ca.apim.gateway%3Agateway-developer-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.ca.apim.gateway%3Agateway-developer-plugin)

# About
The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.Gateway.Gateway-developer-plugin" version "0.5.0"
}
```

After this is added run `gradle build` in order to build a bundle from a gateway solution located in `src/main/Gateway`.

## Using the Gateway Developer Plugin
> Note: This plugin is still in an Beta stage

The plugin adds the following tasks:

### build-bundle
This build a Gateway configuration bundle from the source located in `src/main/Gateway`. This bundle can be used as a bootstrap bundle for a container Gateway. The built bundle is available in: `build/gateway/bundle`

## Currently Supported Entities
This plugin currently supports these entities:

Entity | Supported | Description
--- | --- | ---
Folder | Yes | 
Service | Yes | 
Policy | Yes |
EncapsulatedAssertion | Yes |
ClusterProperty | Yes |
Policy Backed Service | Yes |
Listen Port | Yes | 
Stored Passwords | Yes | 
JDBC Connections | Yes |  

## Expected Source Directory Organization
The Gateway solution directory (`src/main/Gateway` by default) expects the following organization:

* Gateway Solutions Directory
  * `config`
    * [services.yml - Services Configuration](doc/services.md#services-configuration)
    * [encass.yml - Encapsulated Assertions Configuration](doc/encapsulated-assertions.md#encapsulated-assertion-configuration)
    * [policy-backed-services.yml - Policy Backed Services Configuration](doc/policy-backed-services.md#policy-backed-services-configuration)
    * [listen-ports.yml - Listen Ports Configuration](doc/listen-ports.md#listen-ports-configuration)
    * [static.properties - Static Properties Configuration](doc/static-properties.md#static-properties-configuration)
    * [env.properties - Environment Properties Configuration](doc/environment-properties.md#environment-properties-configuration)
    * [stored-passwords.properties - Stored Passwords Configuration](doc/stored-passwords.md#stored-passwords-configuration)
    * [jdbc-connections.yml- JDBC Connections Configuration](doc/jdbc-connections.md#jdbc-connections-configuration)
  * `policy`
    * The policy folder contains the different policies that are available. It can contain may subdirectories to help organize the policy.

## Adding Bundle Dependencies
You can depend on other bundles by adding a dependency. For example:
```groovy
dependencies {
    bundle group: 'my-bundle', name: 'my-bundle', version: '1.0.00', ext: 'bundle'
}
```
The above will add a dependency on a bundle called 'my-bundle'. With this added you can reference encapsulated assertions and policies from the dependent bundles. 
Note that the `ext: 'bundle'` is required to specify that the bundle file extension is `.bundle` 

## Customizing the Default Plugin Configuration
You can customize the source solution directory location and the location to put the built bundle file by setting the `GatewaySourceConfig`. For example:
```groovy
GatewaySourceConfig {
    solutionDir = new File("export/gateway/solution")
    builtBundleDir = new File("gateway")
}
```
The above will make the solution directory `export/gateway/solution` and will put the built bundle file in `gateway` directory.

# Building the Plugin
The build is done using gradle. To build the plugin run ```./gradlew build```. Once built it is available in the `build/libs` directory. 

## Versioning
Versioning is done using the [gradle-semantic-build-versioning](https://github.com/vivin/gradle-semantic-build-versioning) plugin. 
Every time a pull request is merged into `master` the patch version will be updated. For example, if the current version is `1.3.2` the next pull request merged into master will cause the version to be updated to `1.3.3`.
In order to update the major or minor version put either `[major]` or `[minor]` into the commit message.

### publish to local
You can also publish the plugin to your local maven repository by running:
```gradle publishToMavenLocal```

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2018 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md
