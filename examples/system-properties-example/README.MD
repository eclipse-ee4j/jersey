[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Helloworld webapp Example
=========================

This example demonstrates how to manage system properties using REST API. System Properties Example
is able to expose all registered system properties, find out its values and even change a given
property value.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                        | Resource class          | HTTP methods
------------------------------- | ----------------------- | --------------
**_/properties_**               | PropertyNamesResource   | GET
**_/properties/{prop_name}_**   | PropertyResource        | GET
**_/properties/{prop_name}_**   | PropertyResource        | PUT

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   <http://localhost:8080/properties>