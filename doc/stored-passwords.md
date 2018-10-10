# Stored Passwords Configuration
This is a standard java properties file that contains the stored passwords to be created on the Gateway. 
The passwords file is expected to be in the `config` directory. It should be called `stored-passwords.properties`.

An example `stored-passwords.properties` file might look like:
```properties
  PasswordName1=plaintextpassword1
  PasswordName2=plaintextpassword2
```

Currently, only simple passwords are supported. No support for passwords stored as private keys.

# Environment
Passwords are environment configuration. They are not added to a deployment bundle and must be specified as environment.
In order to do so you can set an environment property with the name: `ENV.PASSWORD.<name>` where `<name>` is the name of the password.

## Examples
```
ENV.PASSWORD.db-password=my-pass
```
This will create an password called `db-password`.