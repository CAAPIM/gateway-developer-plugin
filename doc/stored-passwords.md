# Stored Passwords Configuration
This is a standard java properties file that contains the stored passwords to be created on the Gateway. 
The passwords file is expected to be in the `config` directory. It should be called `stored-passwords.properties`.

An example `stored-passwords.properties` file might look like:
```properties
  PasswordName1=plaintextpassword1
  PasswordName2=plaintextpassword2
```

Currently, only simple passwords are supported. No support for passwords stored as private keys.