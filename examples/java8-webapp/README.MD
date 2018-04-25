[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey and Java 8 Examples Features and Types
=============================================

This example demonstrates the usage of Java 8's new constructs and types.

Currently demonstrated are:

-   Default methods in interfaces - acting as JAX-RS Resource Methods

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                         | Resource class           | HTTP method   | description
-------------------------------- | ------------------------ | ------------- | ------------------------------------------
**_/j8/default-method_**         | DefaultMethodInterface   | GET           | returns `interface-root` in `text/plain`
**_/j8/default-method/path_**    | DefaultMethodInterface   | GET           | returns `interface-path` in `text/plain`
**_/j8/default-method/class_**   | DefaultMethodResource    | GET           | returns `class` in `text/plain`
**_/j8/lambdas/{p}_**            | LambdaResource           | GET           | returns `{p}-lambdaized` in `text/plain`

Application is Servlet 3 based, web.xml-less. Everything needed
(resources/providers) is registered in the `Java8Application` class.

Running the Example
-------------------

Run the example as follows:

>     mvn clean package jetty:run

This deploys current example using Jetty. You can access the application at:

-   <http://localhost:8080/j8/default-method>
-   <http://localhost:8080/j8/default-method/path>
-   <http://localhost:8080/j8/default-method/class>
-   <http://localhost:8080/j8/lambdas/test>
