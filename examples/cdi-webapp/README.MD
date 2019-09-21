[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey CDI Integration CDI Beans WebApp Example
===============================================

This example demonstrates how to develop RESTful web service with CDI
managed beans and a Servlet 3.0 Web container.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                              | Description                                                                                 | Expected Results
------------------------------------- | ------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------
**_/cdi-webapp/helloworld_**          | A managed bean with no use of injection whatsoever                                          | Hello World
**_/cdi-webapp/singleton_**           | Shows injection of context objects into the fields of a managed bean.                       | OK GET /cdi-webapp/singleton
**_/cdi-webapp/singleton/counter_**   | Shows injection of context objects into the fields of a managed bean.                       | standalone Grizzly (no resource injection support) - 0 initial increment value, that gets incremented with each other request
                                      |                                                                                             | Java EE compliant AS - 42 initial increment value
**_/cdi-webapp/other/C/D_**           | Shows injection of context objects and path parameters into the fields of a managed bean.   | INTERCEPTED: OK GET /cdi-webapp/other, c=C, d=D
**_/cdi-webapp/echofield/b?a=a_**     | Shows injection of path and query parameters into the fields of a managed bean.             | ECHO a b
**_/cdi-webapp/echo/a_**              | A managed bean that uses (but does not inject) a path parameter.                            | ECHO a

Running the Example
-------------------

This example should work on any Java EE 7 compliant application server.
It has been tested on a standalone GlassFish 4 instance. The easiest way
to get the application running there is to build it and deploy as
follows:

    mvn clean package
    $AS_HOME/asadmin deploy target/cdi-webapp.war

Another option, introduced in Jersey 2.15, is to run this example in
Grizzly HTTP server. To get the application running there you just
invoke the following command:

    mvn clean compile exec:java

Since Weld Servlet support is provided also for Apache Tomcat server,
there is yet another way how to deploy the application. Use the
`tomcat-packaging` maven profile to get the WAR archive packaged in a
way that makes it ready for Tomcat 7+ deployment:

    mvn -Ptomcat-packaging clean package
    cp target/cdi-webapp.war $CATALINA_HOME/webapps

After you successfully deploy the application, visit the following URLs:

-   <http://localhost:8080/cdi-webapp/helloworld>
-   <http://localhost:8080/cdi-webapp/singleton>
-   <http://localhost:8080/cdi-webapp/singleton/counter>
-   <http://localhost:8080/cdi-webapp/other/C/D>
-   <http://localhost:8080/cdi-webapp/echofield/b?a=a>
-   <http://localhost:8080/cdi-webapp/echo/a>