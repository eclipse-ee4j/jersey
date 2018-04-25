[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Freemaker example
=================

This example demonstrates how to use Freemarker templating engine to
produce web pages via Jersey viewable support.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                    | Resource class       | HTTP methods
--------------------------- | -------------------- | --------------
**_/hello_**                | FreemarkerResource   | GET
**_/autoTemplate_**         | FreemarkerResource   | GET
**_/helloWithoutSuffix_**   | FreemarkerResource   | GET

See FreemarkerResource for details on how to use jersey-freemaker-webapp
module for generating web pages.

If you want to use freemarker in your project, don't forget to set
**"jersey.config.freemarker.TemplateBasePath"** init param, which will
be pointing to base directory where your templates are stored
(FreemarkerTest class does that).

Running the Example
-------------------

Run the tests of the example as follows:

>     mvn clean test

This deploys this example using [Grizzly](http://grizzly.java.net/) and
executes the Jersey Test Framework using the unit test framework.

You can see test classes at `src/main/test` subdirectory for detailed
information how to consume the service using Client API.

Run the example as follows:

>     mvn clean package jetty:run

This deploys current example using Jetty. You can access the application
at <http://localhost:8080/freemarker-webapp/hello>