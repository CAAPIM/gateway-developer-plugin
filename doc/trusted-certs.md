# Trusted Certificates Configuration
This is a file containing the trusted certificates that are available in the gateway.
The trusted certs configuration file is expected to be in the `config` directory. It should be either `trusted-certs.yml` or `trusted-certs.json`.

## Different ways to load a trusted certificate
### Using an url with optional port.
The name of the trusted certificate must be prefixed with `https://`. Currently only loads the leaf certificate if a certificate chain is present. If the port is not specified, the default https port 443 will be used.
```yaml
  https://www.ca.com:8443:
    verifyHostname: false
    trustedForSsl: true
    trustedAsSamlAttestingEntity: false
    trustAnchor: true
    revocationCheckingEnabled: true
    trustedForSigningClientCerts: true
    trustedForSigningServerCerts: true
    trustedAsSamlIssuer: false
```
### Loading a certificate from a file.

The certificate file should follow 3 rules:
1. The certificate file is expected to be in the `config/certificates` directory
2. The certificate file should have the same name as the trusted cert config, followed by the appropriate certificate extension.
3. The certificate file should only contain information for one certificate. Certificate chains are currently not supported but will be supported in the future.
     
For example, in the given yaml file below. A file called trusted-certs-demo.pem, trusted-certs-demo.der, trusted-certs-demo.crt, or trusted-certs-demo.cer should be in the `config/certificates` directory   
```yaml
  trusted-certs-demo:
    verifyHostname: false
    trustedForSsl: true
    trustedAsSamlAttestingEntity: false
    trustAnchor: true
    revocationCheckingEnabled: true
    trustedForSigningClientCerts: true
    trustedForSigningServerCerts: true
    trustedAsSamlIssuer: false
```
The above example will create one trusted certificate:
* with name: `trusted-certs-demo`
* with various properties about the certificate such as usage for Signing Client and Server certificates
          
The same JSON representation would look like:
```json
{
  "trusted-certs-demo" : {
      "verifyHostname" : false,
      "trustedForSsl" : true,
      "trustedAsSamlAttestingEntity" : false,
      "trustAnchor" : true,
      "revocationCheckingEnabled" : true,
      "trustedForSigningClientCerts" : true,
      "trustedForSigningServerCerts" : true,
      "trustedAsSamlIssuer" : false
  }
}
```