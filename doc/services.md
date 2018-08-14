# Services Configuration
This is a file containing the services that are exposed in the Gateway.
The service configuration file is expected to be in the `config` directory. It should be either `services.yml` or `services.json`.

An example `services.yml` file would look like:
```yaml
  example project/example.xml:
    httpMethods:
    - GET
    - POST
    - PUT
    - DELETE
    url: "/example"
    properties:
      key: "value"
      key.1: "value.1"
  example project/example-project.xml:
    httpMethods:
    - POST
    - PUT
    url: "/example-project"
  ```
The above example will expose two services:
* A service at `/example`
  * Using HTTP Method `GET`,`POST`, `PUT`, `DELETE` 
  * Its policy coming from the policy file located at: `example project/example.xml`
  * Two service properties:
    * `key` = `value`
    * `key.1` = `value.1`
* A service at `/example-project` 
  * Using HTTP Method `POST`, `PUT` 
  * Its policy coming from the policy file located at: `example project/example-project.xml`

The above file has the following organization:
```yaml
<path-to-policy-file>:
  <service-description>
```
where `<service-description>` lists `httpMethods`, the `url` the service is exposed at and the `properties` it has.

The same JSON representation would look like:
```json
{
  "example project/example.xml": {
    "httpMethods": [
      "GET",
      "POST",
      "PUT",
      "DELETE"
    ],
    "url": "/example",
    "properties": [
      {"key":"value"},
      {"key.1":"value.1"}
    ]
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