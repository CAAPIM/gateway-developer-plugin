[![Build Status](https://travis-ci.org/CAAPIM/gateway-developer-plugin.svg?branch=master)](https://travis-ci.org/CAAPIM/gateway-developer-plugin)
[![Sonar Cloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.ca.apim.gateway%3Agateway-developer-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.ca.apim.gateway%3Agateway-developer-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.ca.apim.gateway/gateway-developer-plugin.svg)](https://search.maven.org/artifact/com.ca.apim.gateway/gateway-developer-plugin)
[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/ca/apim/gateway/gateway-developer-plugin/com.ca.apim.gateway.gateway-developer-plugin.gradle.plugin/maven-metadata.xml.svg?label=gradle-plugin)](https://plugins.gradle.org/plugin/com.ca.apim.gateway.gateway-developer-plugin)

# About
The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugins add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.gateway.gateway-developer-plugin" version "0.8.+"
    id "com.ca.apim.gateway.gateway-export-plugin" version "0.8.+"
}

repositories {
    // This is needed in order to get dependencies for the environment 
    // creator application that is bundled in the gw7 file.
    mavenCentral()
}

// The Gateway Export Config is needed by the gateway-export plugin in order to export from a gateway
GatewayExportConfig {
    folderPath = '/my-solution-folder'
}

// The Gateway Connection Config is required if setting mentioned in main [build.gradle](https://github.com/ca-api-gateway-examples/gateway-developer-example/blob/master/build.gradle) is not applicable to this folder.
GatewayConnection {
    url = 'https://<gateway-host>:8443/restman'
}
```

After this is added run `./gradlew build` in order to build a bundle and deployment package from a gateway solution located in `src/main/Gateway`. 
The build will result in a deployment bundle and a deployment package in `build/gateway`.
Run `gradle export` in order to export a gateway solution into `src/main/Gateway`.

**[See more details on the usage of the plugin in the Wiki](https://github.com/ca-api-gateway/gateway-developer-plugin/wiki)**

# Building the Plugin
The build is done using gradle. To build the plugin run ```./gradlew build```.

## Versioning
Versioning is done using the [gradle-semantic-build-versioning](https://github.com/vivin/gradle-semantic-build-versioning) plugin. 
Every time a pull request is merged into `master` the patch version will be updated. For example, if the current version is `1.3.2` the next pull request merged into master will cause the version to be updated to `1.3.3`.
In order to update the major or minor version put either `[major]` or `[minor]` into the commit message.

## Publish to Local
You can also publish the plugin to your local maven repository and print the published version by running:
```./gradlew build publishToMavenLocal printVersion```

## Publishing
The plugin is published to Bintray: [ca-api-gateway/gateway-developer-plugin](https://bintray.com/ca-api-gateway/gateway-developer-plugin). This then gets promoted to jCenter and Maven Central. 
For more details look at the [build.gradle](build.gradle) and [.travis.yml](/.travis.yml) files.

## How You Can Contribute
Contributions are welcome and much appreciated. To learn more, see the [Contribution Guidelines][contributing].

## License

Copyright (c) 2018 CA/Broadcom. All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the [LICENSE][license-link] file for details.


 [license-link]: /LICENSE
 [contributing]: /CONTRIBUTING.md
