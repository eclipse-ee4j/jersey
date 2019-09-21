[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Groovy Example
==============

This example demonstrates how to develop RESTful hello world web service
using Groovy and the embedded Grizzly server.

Contents
--------

The example consists of just one Jersey resource:

`org.glassfish.jersey.examples.groovy.GroovyResource`

hat produces a textual response to an HTTP GET.

The mapping of the URI path space is presented in the following table:

URI path      | Resource class   | HTTP methods
-----------   | ---------------- | --------------
**_/groovy_** | GroovyResource  | GET

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.

From a web browser, visit:

-   <http://localhost:9998/groovy>
