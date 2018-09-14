# Trusted Certificates Configuration
This is a file containing the trusted certificates that are available in the gateway.
The trusted certs configuration file is expected to be in the `config` directory. It should be either `trusted-certs.yml` or `trusted-certs.json`.

An example `trusted-certs.yml` file would look like:
```yaml
  trusted-certs-demo:
    properties:
      verifyHostname: false
      trustedForSsl: true
      trustedAsSamlAttestingEntity: false
      trustAnchor: true
      revocationCheckingEnabled: true
      trustedForSigningClientCerts: true
      trustedForSigningServerCerts: true
      trustedAsSamlIssuer: false
    certificateData:
      issuerName: "CN%3Dgateway-dev"
      serialNumber: 7738683622989879330
      subjectName: "CN%3Dgateway-dev"
      encodedData: "<some base64 encoded data as a string>"
  ```
The above example will create one trusted certificate:
* with name: `trusted-certs-demo`
* with various properties about the certificate such as usage for Signing Client and Server certificates
* with issuer name: `"CN%3Dgateway-dev"`
* with serial number: `7738683622989879330`
* with subjectName: `"CN%3Dgateway-dev"`
* encoded data is the binary data of the cert which is base64 encoded
          
The same JSON representation would look like:
```json
{
  "trusted-certs-demo" : {
    "properties" : {
      "verifyHostname" : false,
      "trustedForSsl" : true,
      "trustedAsSamlAttestingEntity" : false,
      "trustAnchor" : true,
      "revocationCheckingEnabled" : true,
      "trustedForSigningClientCerts" : true,
      "trustedForSigningServerCerts" : true,
      "trustedAsSamlIssuer" : false
    },
    "certificateData" : {
      "issuerName" : "CN%3Dgateway-dev",
      "serialNumber" : 7738683622989879330,
      "subjectName" : "CN%3Dgateway-dev",
      "encodedData" : "<some base64 encoded data as a string>"
    }
  }
}
```