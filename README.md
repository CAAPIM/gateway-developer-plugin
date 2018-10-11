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

**[See more details on the usage of the plugin in the Wiki](https://github.com/ca-api-gateway/gateway-developer-plugin/wiki)**

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
