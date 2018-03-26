[![Waffle.io - Columns and their card count](https://badge.waffle.io/ca-api-gateway/gateway-developer-plugin.svg?columns=all)](https://waffle.io/ca-api-gateway/gateway-developer-plugin)

# About
The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.gateway.gateway-export-plugin" version "0.2.00"
}

GatewayConnection {
    url = 'https://<gateway-host>:8443/restman'
    folderPath = '/my-solution-folder'
}
```
After this is added run `gradle export` in order to export a gateway solution into `src/main/Gateway`.

# Using the Gateway Developer Plugin
> Note: This plugin is still in an Alpha stage

The plugin adds the following tasks:

### export-raw
This will export a bundle from the gateway using the configuration from the GatewayConnection. It will save the exported bundle to: `build/gateway/raw-export.bundle`

### sanitize-export
This will sanitize an exported bundle. It will save the sanitized bundle to: `build/gateway/sanitized-export.bundle`

### export
This will export and explode the bundle to `src/main/gateway`. This can then be locally modified and checked into a version control repository.

### clean-export
This will delete everything in the `src/main/gateway` directory.

## Currently Supported Entities
This plugin currently supports these entities:

Entity | Supported | Description
--- | --- | ---
Folder | Yes | 
Service | Yes | 
Policy | No | This will be the next supported entity

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
 
# Building the Plugin
The build is done using gradle. To build the plugin run ```gradle build```. Once built it is available in the `build/libs` directory. 

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
