[![Waffle.io - Columns and their card count](https://badge.waffle.io/ca-api-Gateway/Gateway-developer-plugin.svg?columns=all)](https://waffle.io/ca-api-Gateway/Gateway-developer-plugin)
[![Build Status](https://travis-ci.org/ca-api-gateway/gateway-developer-plugin.svg?branch=master)](https://travis-ci.org/ca-api-gateway/gateway-developer-plugin)
[![Sonar Cloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.ca.apim.gateway%3Agateway-developer-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.ca.apim.gateway%3Agateway-developer-plugin)

# About
The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.Gateway.Gateway-developer-plugin" version "0.6.+"
}

repositories {
    // This is needed in order to get dependencies for the environment 
    // creator application that is bundled in the gw7 file.
    mavenCentral()
}
```

After this is added run `./gradlew build` in order to build a bundle and deployment package from a gateway solution located in `src/main/Gateway`.
The build will result in a deployment bundle and a deployment package in `build/gateway`

## What gets built
### Deployment Bundle
The deployment bundle is a bundle that contains the policies and other deployment artifact in the `src/main/Gateway`. 
This bundle can be referred to a a dependency in other solutions.

### Deployment Package
The deployment package is a gw7 file that contains the deployment bundle as well as all it's dependencies. 
This can be deployed to a Gateway to add this solution to the Gateway. 
Note that only one deployment package can be deployed to a gateway.

### Hierarchy of Artifacts
In order to better understand the relationship between different files and artifacts consider the following comparison to Java:

* `Policy` file **≈** `java` file
  * The policy files that are found in `src/main/Gateway/policy` folder are akin to java files. Policy files are the code that tells the gateway what to do when processing a massage. Policy files can refer to other policy files using `<L7p:Include>` or `<L7p:Encapsulated>`.
* `Item` xml within *bundle* xml **≈** `class` file
  * When you run `./gradlew build` the *policy* files get *compiled* into `<l7:Item>` sections in a *bundle*. These sections in a *bundle* are akin to `class` files. They are *compiled* versions of the *policy* files that are *linked* to the other *policies* that they refer to.
* `bundle` file **≈** `jar` file
  * A `bundle` file is akin to a `jar` file. It is a single file that contains all the compiled *policy* files. It can be deployed to a Gateway but it will not run unless you also deploy all of the dependent *bundles*.
* `gw7` file **≈** `fat jar` file
  * A `fat jar` is a *jar* file that also contains all the dependent *jars*. Similarly, a `gw7` file is a single file that contains the built *bundle* as well as all the *bundles* that it depends on. It is meant to be a single file that can be deployed to a Gateway in order to completely configure it.

## Dealing with Environment Configuration
For more information on environment see [applying-environment.md](doc/applying-environment.md).

## Gateway Developer Plugin additional Tasks
The plugin adds the following tasks:

### build-bundle
This builds a Gateway configuration bundle from the source located in `src/main/Gateway`. This bundle can be used as a dependency bundle for another gateway solution. The built bundle is available in: `build/gateway/bundle`

### package-gw7
This builds a Gateway deployment package from the source located in `src/main/Gateway`. The deployment package will also contain any dependent bundles as well as instructions on how to apply environment configuration tot he bundles. The built deployment package is available in: `build/gateway`

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
    * [trusted-certs.yml - Trusted Certificates Configuration](doc/trusted-certs.md#trusted-certificates-configuration)
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

## Publish to Local
You can also publish the plugin to your local maven repository by running:
```./gradlew publishToMavenLocal```

## Publishing
The plugin is published to Bintray: [ca-api-gateway/gateway-developer-plugin](https://bintray.com/ca-api-gateway/gateway-developer-plugin). This then gets promoted to jCenter and Maven Central. 
For more details look at the [build.gradle](build.gradle) and [.travis.yml](/.travis.yml) files.

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2018 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md
