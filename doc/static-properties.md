# Static Properties Configuration
This is a standard java properties file that contains the different cluster properties to create on the Gateway. 
The service configuration file is expected to be in the `config` directory. It should be called `static.properties`.

An example `static.properties` file might look like:
```properties
  my-static-property=This is a properties value
  another-property=\
    {"another":"properties",\
     "value":"0"\
    }
```