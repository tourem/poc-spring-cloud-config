## Configuration management with Spring Cloud Config

Spring Cloud Config project provides a mechanism of configuration in a distributed system.

The Spring Cloud Services Config Server externalizes configuration information of an application and serves out this configuration using a REST based interface. It has the ability to store and serve encrypted configuration values and this project will go into the details of how to provision and test this feature.

#### Projects

<table>
 <tr>
 <th>Project</th><th> Description</th>
</tr>
<tr>
<td><b>config-client</b></td>
<td>client application</td>
</tr>
<tr>
<td><b>config-server</b></td>
<td>server application</td>
</tr>
	
</table>

#### Spring Cloud Config Server Application

To make our SpringBoot application as a SpringCloud Config Server, we just need to add @EnableConfigServer annotation to the main entry point class and configure spring.cloud.config.server.git.uri property pointing to the git repository.

Spring Cloud Config Server exposes the following REST endpoints to get application specific configuration properties:

```sh
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```

Here {application} refers to value of spring.config.name property, {profile} is an active profile and {label} is an optional git label (defaults to “master”).

In addition to the application specific configuration files like service-B.properties, service-A.properties, you can create application.properties file to contain common configuration properties for all applications. As you might have guessed you can have profile specific files like application-dev.properties, application-prod.properties.


#### Spring Cloud Config Client

Usually in SpringBoot application we configure properties in application.properties. But while using Spring Cloud Config Server we use bootstrap.properties or bootstrap.yml file to configure the URL of Config Server and Spring Cloud Config Client module will take care of starting the application by fetching the application properties from Config Server.


#### Dynamic configuration

Let us see how we can update the configuration properties of config-client at runtime without requiring to restart the application.

Update the config-client-dev.yml in git repository and commit the changes. Now if you access http://localhost:8888/env you will still see the old properties.

In order to reload the configuration properties we need to do the following:

- Mark Spring beans that you want to reload on config changes with @RefreshScope
- Request using POST method

```sh
curl -X POST http://root:s3cr3t@localhost:8888/refresh
```

But issuing /refresh requests manually is tedious and impractical in case of large number of applications and multiple instances of same application. We will cover how to handle this problem using Spring Cloud Bus.

#### Secure properties with spring cloud config
Secure properties: Almost every application has some kind of configuration that can’t be exposable, some are very sensitive and should be limited access. we generally pronounce it secure properties. 

First, generate a key store. assuming you are aware of keytool.

```sh
keytool -genkeypair -alias config-server-key 
-keyalg RSA -keysize 4096 -sigalg SHA512withRSA        
-dname 'CN=Config Server,OU=Spring Cloud,O=Larbotech'        
-keypass my-k34-s3cr3t -keystore config-server.jks        
-storepass my-s70r3-s3cr3t
```
Then place config-server.jks to resource folder in cloud config server project, after that edit bootstrap.properties(or .yaml). and add below properties.


```yaml
encrypt:
  keyStore:
    location: classpath:config-server.jks
    password: my-s70r3-s3cr3t
    alias: config-server-key
    secret: my-k34-s3cr3t
```


<table>
 <tr>
 <th>Properties</th><th> Description</th>
</tr>
<tr>
<td><b>encrypt.keyStore.location</b></td>
<td>Contains a resource(.jks file) location</td>
</tr>
<tr>
<td><b>encrypt.keyStore.password</b></td>
<td>Holds the password that unlocks the keystore</td>
</tr>
<tr>
<td><b>encrypt.keyStore.alias</b></td>
<td>Identifies which key in the store to use</td>
</tr>
<tr>
<td><b>encrypt.keyStore.secret</b></td>
<td>secret to encrypt or decrypt</td>
</tr>
</table>

There is encryption and decryption endpoint expose by config server. 

Let’s take a simple example, where we will try to encrypt and decrypt ‘password-secret’.

```sh
curl -X POST --data-urlencode secret-en-prod! http://root:s3cr3t@localhost:8888/encrypt
```

#### In a case when a client wants to decrypt configuration locally

First,

If you want to encrypt and decrypt endpoint not work, Then comment all properties that start with encrypt.* and include the new line as below

```yaml
spring.cloud.config.server.encrypt.enabled=false
```

Include keystore(.jks) file in client project and update below properties in bootstrap.properties(or .yaml) file

```yaml
encrypt:
  keyStore:
    location: classpath:config-server.jks
    password: my-s70r3-s3cr3t
    alias: config-server-key
    secret: my-k34-s3cr3t
```
That’s all, now client project not going to connect with the server to decrypt properties.