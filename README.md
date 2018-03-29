[![Waffle.io - Columns and their card count](https://badge.waffle.io/ca-api-Gateway/Gateway-developer-plugin.svg?columns=all)](https://waffle.io/ca-api-Gateway/Gateway-developer-plugin)

# About
The Gateway developer plugin can be used to develop Gateway configuration.

# Usage
In order to use this plugin add the following you your gradle file:

```groovy
plugins {
    id "com.ca.apim.Gateway.Gateway-developer-plugin" version "0.3.00"
}
```

After this is added run `gradle build` in order to build a bundle from a gateway solution located in `src/main/Gateway`.

## Using the Gateway Developer Plugin
> Note: This plugin is still in an Beta stage

The plugin adds the following tasks:

### build-bundle
This build a Gateway configuration bundle from the source located in `src/main/Gateway`. This bundle can be used as a bootstrap bundle for a container Gateway. The built bundle is available here: `build/gateway/<project-name>.bundle`

## Currently Supported Entities
This plugin currently supports these entities:

Entity | Supported | Description
--- | --- | ---
Folder | Yes | 
Service | Yes | 
Policy | Yes |
EncapsulatedAssertion | Yes |
ClusterProperty | Yes |
 

## Expected Source Directory Organization
The Gateway solution directory (`src/main/Gateway` by default) expects the following organization:

* Gateway Solutions Directory
  * `config`
    * either `services.yml` or `services.json`
      * This is a file containing the services that are exposed in the gateway.
      * An example `services.yml` file would look like:
        * ```yaml
          example project/example.xml:
            httpMethods:
            - GET
            - POST
            - PUT
            - DELETE
            url: "/example"
          example project/example-project.xml:
            httpMethods:
            - POST
            - PUT
            url: "/example-project"
          ```
        * The above example will expose two services:
          * A service at `/example` using HTTP Method `GET`,`POST`, `PUT`, `DELETE` with its policy coming from the policy file located at: `example project/example.xml`
          * A service at `/example-project` using HTTP Method `POST`, `PUT` with its policy coming from the policy file located at: `example project/example-project.xml`
        * The above file has the following organization:
          * ```yaml
            <path-to-policy-file>:
              <service-description>
            ```
            where `<service-description>` lists `httpMethods` and the `url` the service is exposed at.
        * The same JSON representation would look like:
          * ```json
            {
              "example project/example.xml": {
                "httpMethods": [
                  "GET",
                  "POST",
                  "PUT",
                  "DELETE"
                ],
                "url": "/example"
              },
              "example project/example-project.xml": {
                "httpMethods": [
                  "POST",
                  "PUT"
                ],
                "url": "/example-project"
              }
            }
            ```
    * either `encass.yml` or `encass.json`
      * This is a file containing the encapsulated assertions that are available in the gateway.
      * An example `encass.yml` file would look like:
        * ```yaml
          example project/encass-policy.xml:
            arguments:
            - name: "hello"
              type: "string"
            - name: "hello-again"
              type: "message"
            results:
            - name: "goodbye"
              type: "string"
            - name: "goodbye-again"
              type: "message"
          ```
          * The above example will create one encapsulated assertion:
            * An encapsulated assertion with its policy coming from the policy file located at: `example project/encass-policy.xml`
              * It will have arguments `hello` or type `string` and `hello-again` of type `message`
              * It will have results `goodbye` or type `string` and `goodbye-again` of type `message`
          * The same JSON representation would look like:
            * ```json
              {
                "example project/encass-policy.xml": {
                  "arguments": [
                    {
                      "name": "hello",
                      "type": "string"
                    },
                    {
                      "name": "hello-again",
                      "type": "message"
                    }
                  ],
                  "results": [
                    {
                      "name": "goodbye",
                      "type": "string"
                    },
                    {
                      "name": "goodbye-again",
                      "type": "message"
                    }
                  ]
                }
              }
              ```
    * `global.properties`
      * This is a standard java properties file that contains the different cluster properties to create on the Gateway. An example `global.properties` file might look like:
        * ```properties
            my-global-property=This is a properties value
            another-property=\
              {"another":"properties",\
               "value":"0"\
              }
          ```
  * `policy`
    * The policy folder contains the different policies that are available. It can contain may subdirectories to help organize the policy.

## Customizing the Default Plugin Configuration
You can customize the source solution directory location and the location to put the built bundle file by setting the `GatewaySourceConfig`. For example:
```groovy
GatewaySourceConfig {
    solutionDir = new File("export/gateway/solution")
    builtBundle = new File("gateway/built-bundle.bundle")
}
```
The above will make the solution directory `export/gateway/solution` and will put the built bundle file in `gateway/built-bundle.bundle`.

# Building the Plugin
The build is done using gradle. To build the plugin run ```gradle build```. Once built it is available in the `build/libs` directory. 

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
