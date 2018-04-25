[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey Spring Integration Spring Helloworld WebApp Example
==========================================================

This example demonstrates how to create simple Jersey application that
utilises Spring 3 DI features, packaged into WAR file.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                        | Resource class            | HTTP methods
------------------------------- | ------------------------- | --------------
**_/jersey-hello_**             | JerseyResource            | GET
**_/spring-hello_**             | SpringRequestResource     | GET
**_/spring-singleton-hello_**   | SpringSingletonResource   | GET

Application is configured by using web.xml, which registers
[javax.ws.rs.core.Application](https://jax-rs-spec.java.net/nonav/2.0/apidocs/javax/ws/rs/core/Application.html)
descendant to get classes and singletons from it (see class
MyApplication).

Running the Example
-------------------

Run the example as follows:

>     mvn clean package jetty:run

This deploys current example using Jetty web server. Once deployed, the
application is ready to serve the following resources:

-   <http://localhost:8080/helloworld-spring-webapp/jersey-hello>
-   <http://localhost:8080/helloworld-spring-webapp/spring-hello>
-   <http://localhost:8080/helloworld-spring-webapp/spring-singleton-hello>