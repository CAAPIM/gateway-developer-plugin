[![Build Status](https://travis-ci.org/CAAPIM/gateway-developer-plugin.svg?branch=master)](https://travis-ci.org/CAAPIM/gateway-developer-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.ca.apim.gateway%3Agateway-developer-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.ca.apim.gateway%3Agateway-developer-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.ca.apim.gateway/gateway-developer-plugin.svg)](https://search.maven.org/artifact/com.ca.apim.gateway/gateway-developer-plugin)
[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/ca/apim/gateway/gateway-developer-plugin/com.ca.apim.gateway.gateway-developer-plugin.gradle.plugin/maven-metadata.xml.svg?label=gradle-plugin)](https://plugins.gradle.org/plugin/com.ca.apim.gateway.gateway-developer-plugin)

# About
The Gateway Policy Plugin is used to develop Gateway configuration: it reads configuration files, converts them to the Gateway Restman bundle.

# Usage
Usage guide [API Gateway Policy Plugin.](https://techdocs.broadcom.com/content/broadcom/techdocs/us/en/ca-enterprise-software/layer7-api-management/gateway-policy-plugin/1-0.html)

# Building Your Gateway Project with the Plugin
The build is done using gradle. To build the plugin run ```./gradlew build```.

## Versioning
Versioning is done using the [gradle-semantic-build-versioning](https://github.com/vivin/gradle-semantic-build-versioning) plugin. 
Every time a pull request is merged into `master`, the patch version is updated. For example, if the current version is `1.3.2`, the next pull request merged into master will cause the version to be updated to `1.3.3`.
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
