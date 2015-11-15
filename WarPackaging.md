# WAR packaging #

Adding WAR packaging is actually very simple process, slightly different for servler 2.5 and 3.0 compatible servers:

## Servlet 2.5 ##

Add to your project **war/WEB-INF/web.xml** file containing:
```
<?xml version="1.0" ?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
        version="2.5">

  <display-name>${project.name}</display-name>

  <listener>
      <listener-class>play.core.server.servlet25.Play2Servlet</listener-class>
  </listener>

  <servlet>
    <servlet-name>play</servlet-name>
    <servlet-class>play.core.server.servlet25.Play2Servlet</servlet-class>
  </servlet>

  <servlet-mapping>
    <servlet-name>play</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>

</web-app>
```

Add dependency:
```
    <dependencies>
        ...
        <dependency>
            <groupId>com.github.play2war</groupId>
            <artifactId>play2-war-core-servlet25_2.10</artifactId>
            <version>1.2</version> <!-- version "1.2" for Play! 2.2.x, version "1.3-beta2" for Play! 2.3.x -->
        </dependency>
    </dependencies>
```

Configure maven-war-plugin plugin:
```
    <build>
        ...
        <plugins>
            ....
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>2.6</version>
                <configuration>
                    <filteringDeploymentDescriptors>true</filteringDeploymentDescriptors>
                    <primaryArtifact>false</primaryArtifact>
                    <warSourceDirectory>${basedir}/war</warSourceDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>make-war</id>
                        <phase>package</phase>
                        <goals>
                            <goal>war</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

See working [example project](http://play2-maven-plugin.googlecode.com/svn/tags/test-projects-1.0.0-beta3/play23/war/helloworld-war-servlet-2.5) tested with Apache Tomcat 6.0.33 and 7.0.52.

## Servlet 3.0 ##

Add dependency:
```
    <dependencies>
        ...
        <dependency>
            <groupId>com.github.play2war</groupId>
            <artifactId>play2-war-core-servlet30_2.10</artifactId>
            <version>1.2</version> <!-- version "1.2" for Play! 2.2.x, version "1.3-beta2" for Play! 2.3.x -->
        </dependency>
    </dependencies>
```


Configure maven-war-plugin plugin:
```
    <build>
        ...
        <plugins>
            ....
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>2.6</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                    <primaryArtifact>false</primaryArtifact>
                    <warSourceDirectory>${basedir}/war</warSourceDirectory>
                </configuration>
                <executions>
                    <execution>
                        <id>make-war</id>
                        <phase>package</phase>
                        <goals>
                            <goal>war</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

See working [example project](http://play2-maven-plugin.googlecode.com/svn/tags/test-projects-1.0.0-beta3/play23/war/helloworld-war-servlet-3.0) tested with Apache Tomcat 7.0.52 and 8.0.3.

## Attaching WAR file to project build artifacts ##

If you want to install/deploy generated WAR file, you have to attach it to the project. Do it by adding:
```
    <build>
        ...
        <plugins>
            ....
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>1.9.1</version>
                <executions>
                    <execution>
                        <id>attach-war</id>
                        <phase>package</phase>
                        <goals>
                            <goal>attach-artifact</goal>
                        </goals>
                        <configuration>
                             <artifacts>
                                 <artifact>
                                     <file>${project.build.directory}/${project.build.finalName}.war</file>
                                     <type>war</type>
                                 </artifact>
                             </artifacts>
                         </configuration>
                     </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```