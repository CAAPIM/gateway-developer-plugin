[![Waffle.io - Columns and their card count](https://badge.waffle.io/ca-api-gateway/gateway-developer-plugin.svg?columns=all)](https://waffle.io/ca-api-gateway/gateway-developer-plugin)
[![Build Status](https://travis-ci.org/ca-api-gateway/gateway-export-plugin.svg?branch=master)](https://travis-ci.org/ca-api-gateway/gateway-export-plugin)
[![Sonar Cloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.ca.apim.gateway%3Agateway-export-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.ca.apim.gateway%3Agateway-export-plugin)

# About
The Gateway export plugin can be used to extract configuration from a running Gateway.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.gateway.gateway-export-plugin" version "0.5.0"
}

GatewayConnection {
    url = 'https://<gateway-host>:8443/restman'
    folderPath = '/my-solution-folder'
}
```
After this is added run `gradle export` in order to export a gateway solution into `src/main/Gateway`.

# Using the Gateway Developer Plugin
> Note: This plugin is still in an Beta stage

The plugin adds the following tasks:

### export-raw
This will export a bundle from the gateway using the configuration from the GatewayConnection. It will save the exported bundle to: `build/gateway/raw-export.bundle`

### sanitize-export
This will sanitize an exported bundle. It will save the sanitized bundle to: `build/gateway/sanitized-export.bundle`

### export
This will export and explode the bundle to `src/main/gateway`. This can then be locally modified and checked into a version control repository.

### clean-export
This will delete everything in the `src/main/gateway` directory.

## Customizing the Default Plugin Configuration
You can customize the source solution directory location and the location to put the built bundle file by setting the `GatewaySourceConfig`. For example:
```groovy
GatewayExportConfig {
    solutionDir = new File("export/gateway/solution")
    rawBundle = new File("gateway/raw-bundle.bundle")
    sanitizedBundle = new File("gateway/sanitized-bundle.bundle")
}
```
The above will make the solution directort `export/gateway/solution`. It will put the raw bundle export file in `gateway/raw-bundle.bundle`. It will put the sanitized bundle file in `gateway/sanitized-bundle.bundle`.
 
## Requiring Entities on Export
You can require entities to export by adding them to the `GatewayExportConfig`. 
For example, to force the export of:
* listen ports with name `public-port` and `internal-access`
* certificates: `finance`, `accounting`
* JDBC Connection `app-db`

Add the following to your `GatewayExportConfig`:
 ```groovy
 GatewayExportConfig {
    exportEntities = [
        listenPorts: [ "public-port", "internal-access" ],
        certificates: [ "finance", "accounting" ],
        jdbcConnections: [ "app-db" ]
    ]
 }
 ```
 
The following entities are available:
* clusterProperties
* identityProviders
* jdbcConnections
* listenPorts
* privateKeys
* passwords
* certificates

## Entities Not Exported
There are some default entities that are not exported unless they are explicitly listed in the exportEntities:

| Type          | Name          |
| ------------- |-------------  |
| Identity Provider | Internal Identity Provider |
| Private Key      | *ssl* or *SSL*      |  
 
# Building the Plugin
The build is done using gradle. To build the plugin run ```gradle build```. Once built it is available in the `build/libs` directory. 

## Versioning
Versioning is done using the [gradle-semantic-build-versioning](https://github.com/vivin/gradle-semantic-build-versioning) plugin. 
Every time a pull request is merged into `master` the patch version will be updated. For example, if the current version is `1.3.2` the next pull request merged into master will cause the version to be updated to `1.3.3`.
In order to update the major or minor version put either `[major]` or `[minor]` into the commit message.

### publish to local
You can also publish the custom assertion builder to your local maven repository by running:
```gradle publishToMavenLocal```

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2018 CA. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md
