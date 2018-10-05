# Encapsulated Assertion Configuration
This is a file containing the encapsulated assertions that are available in the gateway.
The encapsulated assertions configuration file is expected to be in the `config` directory. It should be either `encass.yml` or `encass.json`.

An example `encass.yml` file would look like:
```yaml
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
The above example will create one encapsulated assertion:
* An encapsulated assertion with its policy coming from the policy file located at: `example project/encass-policy.xml`
  * It will have arguments `hello` or type `string` and `hello-again` of type `message`
  * It will have results `goodbye` or type `string` and `goodbye-again` of type `message`
 
The same JSON representation would look like:
```json
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

# Environment
Encapsulated assertions cannot be created using environment properties.