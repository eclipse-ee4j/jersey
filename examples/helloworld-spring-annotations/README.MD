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
utilises Spring 3 DI features based on Spring annotations, packaged into WAR file.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                   | Resource class            | HTTP methods
------------------------------------------ | ------------------------- | --------------
**_/spring-resource_**                     | SpringRequestResource     | GET
**_/spring-resource/goodbye_**             | SpringRequestResource     | GET
**_/spring-resource/norwegian-goodbye_**   | SpringRequestResource     | GET

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.

-   <http://localhost:8080/base/spring-resource>
-   <http://localhost:8080/base/spring-resource/goodbye>
-   <http://localhost:8080/base/spring-resource/norwegian-goodbye>