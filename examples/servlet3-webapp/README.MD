[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Servlet 3 Example
===================

This example demonstrates how to use advantages of Servlet v3 and how to configure Jersey application with this version of Jersey.
JAX-RS resource returns the usual text

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path             | Resource class      | HTTP methods
-------------------- | ------------------- | ------------
**_/cat_**           | CatResource         |  GET
**_/dog_**           | DogResource         |  GET

Running the Example
-------------------

Run the example using Jetty  as follows:

>   mvn clean package jetty:run

Go to the URL:

-   <http://localhost:9998/animals/cat>
-   <http://localhost:9998/animals/dog>
