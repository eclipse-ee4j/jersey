[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

MOXy JAX-RS JSON Provider Example
=================================

This example demonstrates how to produce/consume JSON representations from Java objects.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path       | Resource class   | HTTP method
-------------- | ---------------- | -------------
**_/test_**    | JsonResource     | GET
**_/test_**    | JsonResource     | POST

(See JsonResourceTest for more details.)

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/)

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://localhost:9998/jsonmoxy/application.wadl>

The resource is available at

-   <http://localhost:9998/jsonmoxy/test>