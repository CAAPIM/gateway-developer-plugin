# Trusted Certificates Configuration
This is a file containing the trusted certificates that are available in the gateway.
The trusted certs configuration file is expected to be in the `config` directory. It should be either `trusted-certs.yml` or `trusted-certs.json`.

## Different ways to load a trusted certificate
1. Using an url. Currently only loads the leaf certificate if a certificate chain is present.
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
    url: https://www.google.ca
```
2. Specifying a file. The certificate file is expected to be in the `config/certificates` directory. Each certificate file can only contain one certificate. Certificate chains will be supported in the future.
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
    file: testCert.pem
```
3. Specifying the certificateData field and filling out:
 - the issuer name
 - serial number
 - subject name
 - and encoded data which is the binary data of the cert in base64 encoding.
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