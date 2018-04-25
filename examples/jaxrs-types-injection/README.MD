[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

JAX-RS Types Injection Example
==============================

This example demonstrates injection of various runtime information into Jersey/JAX-RS resources.

A simple application consists of two simple resources: One is a typical
JAX-RS `@Path` annotated resource. The second resource is created
programmatically using the new Jeresy 2 programmatic resource binding
API. Both resources inject the same information and produce the report
that is returned in the response back to the client.

Contents
--------

The JAX-RS `@Path` annotated web resource is implemented by the

`org.glassfish.jersey.examples.jaxrstypeinjection.JaxrsInjectionReportingResource`

class. The programmatic resource is implemented by the

`org.glassfish.jersey.examples.jaxrstypeinjection.JaxrsInjectionReportingInflector` class.

The mapping of the URI path space is presented in the following table:

URI path                                                                                 | Resource class                     | HTTP methods
---------------------------------------------------------------------------------------- | ---------------------------------- | --------------
**_/annotated/instance/{p1}/{p2}?q1=<int_value>&q2=<string_value>&q2=<string_value>_**   | JaxrsInjectionReportingResource    | GET
**_/annotated/method/{p1}/{p2}?q1=<int_value>&q2=<string_value>&q2=<string_value>_**     | JaxrsInjectionReportingResource    | GET
**_/programmatic/{p1}/{p2}?q1=<int_value>&q2=<string_value>&q2=<string_value>_**         | JaxrsInjectionReportingInflector   | GET

Please note that the value of the `q1` URI query parameter is expected

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the JAX-RS Types Injection Example using [Grizzly](http://grizzly.java.net/)

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://localhost:8080/jaxrs-type-injection/application.wadl>

The resources could be accessed via

-   <http://localhost:8080/jaxrs-type-injection/annotated/instance/p1/p2?q1=123&q2=abc&q2=def>
-   <http://localhost:8080/jaxrs-type-injection/annotated/method/p1/p2?q1=123&q2=abc&q2=def>
-   <http://localhost:8080/jaxrs-type-injection/programmatic/p1/p2?q1=123&q2=abc&q2=def>
