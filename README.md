# About

The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.gateway.gateway-developer-plugin" version "0.1.00"
}

GatewayConnection {
    url = 'https://<gateway-host>:8443/restman'
    folderPath = '/my-solution-folder'
}
```

# Using the Gateway Developer Plugin
> Note: This plugin is still in an Alpha stage

The plugin adds the following tasks:

## export-raw
This will export a bundle from the gateway using the configuration from the GatewayConnection. It will save the exported bundle to: `build/gateway/raw-export.bundle`

## sanitize-export
This will sanitize an exported bundle. It will save the sanitized bundle to: `build/gateway/sanitized-export.bundle`

## export
This will export and explode the bundle to `src/main/gateway`. This can then be locally modified and checked into a version control repository.

## zip
This zips the exported configuration into a bundle that can be used as a bootstrap bundle for a container gateway. The zipped bundle is available here: `build/gateway/zipped.bundle`

# Currently Supported Entities
This plugin currently supports these entities:

Entity | Supported | Description
--- | --- | ---
Folder | Yes | 
Service | Yes | 
Policy | No | This will be the next supported entity

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