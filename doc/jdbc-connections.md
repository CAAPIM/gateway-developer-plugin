# JDBC Connections Configuration
This is a file containing the JDBC Connections that are available in the gateway.
The JDBC Connections configuration file is expected to be in the `config` directory. It should be either `jdbc-connections.yml` or `jdbc-connections.json`.

An example `jdbc-connections.yml` file would look like:
```yaml
  MySQL:
    driverClass: "com.mysql.jdbc.Driver"
    jdbcUrl: "jdbc:mysql://localhost:3306/ssg"
    user: "gateway"
    passwordRef: "gateway"
    minimumPoolSize: 3
    maximumPoolSize: 15
    properties:
      EnableCancelTimeout: "true"
  ```
The above example will create one JDBC Connection:
* with name: `MySQL`
* with driver class `com.mysql.jdbc.Driver`
* with user `gateway`
* referring to the stored password named `gateway` (see Stored Passwords documentation [here](stored-passwords.md))
* with minimum pool size `3` (this is the default value, if not specified)
* with maximum pool size `15` (this is the default value, if not specified)
* setting connection property EnableCancelTimeout to `true`
          
The same JSON representation would look like:
```json
{
  "MySQL" : {
    "driverClass" : "com.mysql.jdbc.Driver",
    "jdbcUrl" : "jdbc:mysql://localhost:3306/ssg",
    "user" : "gateway",
    "passwordRef" : "gateway",
    "minimumPoolSize" : 3,
    "maximumPoolSize" : 15,
    "properties" : {
      "EnableCancelTimeout" : "true"
    }
  }
}
```

# Environment
JDBC Connection configuration is environment configuration. It is not added to a deployment bundle and must be specified as environment.
In order to do so you can set an environment property with the name: `ENV.JDBC_CONNECTION.<name>` where `<name>` is the name of the JDBC Connection.

## Examples
```
ENV.JDBC_CONNECTION.my-db='{
                             "driverClass" : "com.mysql.jdbc.Driver",
                             "jdbcUrl" : "jdbc:mysql://localhost:3306/ssg",
                             "user" : "gateway",
                             "passwordRef" : "gateway",
                             "minimumPoolSize" : 3,
                             "maximumPoolSize" : 15,
                             "properties" : {
                               "EnableCancelTimeout" : "true"
                             }
                           }'
```
This will create a JDBC Connection called `my-db`.