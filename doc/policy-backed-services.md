# Policy Backed Services Configuration
This is a file containing the policy backed services that are available in the gateway.
The policy backed services configuration file is expected to be in the `config` directory. It should be either `policy-backed-services.yml` or `policy-backed-services.json`.

An example `policy-backed-services.yml` file would look like:
```yaml
  my-pbs:
    interfaceName: "com.l7tech.objectmodel.polback.BackgroundTask"
    operations:
    - policy: "example project/example-pbs.xml"
      operationName: "run"
  ```
The above example will create one policy backed service:
* A policy backed service with the name: `my-pbs`
  * It will use the policy backed interface `com.l7tech.objectmodel.polback.BackgroundTask`
  * For the single operation `run` it will execute the policy located at: `example project/example-pbs.xml`
          
The same JSON representation would look like:
```json
{
  "my-pbs": {
    "interfaceName": "com.l7tech.objectmodel.polback.BackgroundTask",
    "operations": [
      {
        "policy": "example project/example-pbs.xml",
        "operationName": "run"
      }
    ]
  }
}
```

# Environment
Policy Backed Services cannot be created using environment properties.