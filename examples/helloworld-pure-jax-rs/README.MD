[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Hello World Example
===================

This example demonstrates Hello World example. JAX-RS resource returns the usual text `Hello World!`.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path             | Resource class      | HTTP methods | Notes
-------------------- | ------------------- | ------------ | --------------------------------------------------------
**_/helloworld_**    | HelloWorldResource  |  GET         |  Returns `Hello World!`

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   <http://localhost:8080/helloworld>
