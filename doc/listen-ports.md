# Listen Ports Configuration
This is a file containing the custom listen ports that are available in the gateway.
The listen ports configuration file is expected to be in the `config` directory. It should be either `listen-ports.yml` or `listen-ports.json`.

An example `listen-ports.yml` file would look like:
```yaml
  Custom HTTPS Port:
    protocol: "HTTPS"
    port: 12345
    enabledFeatures:
    - "Published service message input"
    tlsSettings:
      clientAuthentication: "REQUIRED"
      enabledVersions:
      - "TLSv1.2"
      enabledCipherSuites:
      - "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
      - "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"
      - "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"
      ...
      properties:
        usesTLS: true
    properties:
      threadPoolSize: "20"
  ```
The above example will create one listen port:
* with name: `Custom HTTPS Port`
* listening to port 12345
* using HTTPS protocol
* with one feature enabled `Published service message input`
* requiring tls authentication
* with only TLSv1.2 enabled
* restricting to some ciphers available (not all)
* enforcing tls via property
* setting a specific thread pool with 20 threads for processing requests
          
The same JSON representation would look like:
```json
{
  "Custom HTTPS Port" : {
      "protocol" : "HTTPS",
      "port" : 12345,
      "enabledFeatures" : [ "Published service message input" ],
      "tlsSettings" : {
        "clientAuthentication" : "REQUIRED",
        "enabledVersions" : [ "TLSv1.2" ],
        "enabledCipherSuites" : [ "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384" ],
        "properties" : {
          "usesTLS" : true
        }
      },
      "properties" : { 
         "threadPoolSize" : "20"
      }
    }
}
```