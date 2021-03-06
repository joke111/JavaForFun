1. This project does not work on gradle 3.1  but it does on maven :)  
   Gradle is a pain in ass when working with hierarchical projects. :(


2. You can find documentation:  
   [http://docs.spring.io/spring-cloud-stream/docs/current/reference/html/](http://docs.spring.io/spring-cloud-stream/docs/current/reference/html/)  
   [https://github.com/mbogoevici/spring-cloud-stream](https://github.com/mbogoevici/spring-cloud-stream)

3. Examples:  
   [https://github.com/spring-cloud/spring-cloud-stream-samples](https://github.com/spring-cloud/spring-cloud-stream-samples)


4. Issues while debugging:

* Debugging from Eclipse/IntelliJ does not work because it needs to search for resources in `/BOOT-INF/lib` and `/BOOT-INF/classes` directories.  
  By default Eclipse/IntelliJ do not know anything about those folders :(

* `spring-boot-maven-plugin` creates `META-INF/MANIFEST.MF` in `target/some-project.jar` file whith this value:
   `Main-Class: org.springframework.boot.loader.JarLauncher`

* `JarLauncher` used when running from console with `java -jar` contains: [JarLauncher.java](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/JarLauncher.java)

  ```java
  static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
  static final String BOOT_INF_LIB = "BOOT-INF/lib/";
  ```


* The best you can do for debugging spring boot applications is running from console in this way:  
`java -jar -Djaxp.debug=1 -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n target/spring-stream-kafka-consumer-1.0-SNAPSHOT.jar`

   And then you must use the remote debugging option from Eclipse/IntelliJ


5. Consumer with health indicator:  
   See: [http://docs.spring.io/spring-cloud-stream/docs/current/reference/html/_health_indicator.html](http://docs.spring.io/spring-cloud-stream/docs/current/reference/html/_health_indicator.html)  

   You can retrieve information from this URL (do not forget you must comment out the web-environment property in `application.yml` of `spring-stream-kafka-consumer`):  
   GET http://localhost:8080/health  
   ```javascript
   {
        "status": "UP",
        "diskSpace": {
            "status": "UP",
            "total": 39235076096,
            "free": 23638888448,
            "threshold": 10485760
        },
        "binders": {
            "status": "UP",
            "kafka": {
                "status": "UP",
                "healthIndicator": {
                    "status": "UP"
                }
            }
        }
    }
    ```
