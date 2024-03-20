[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

HelloWorld OSGi Example
=======================

This example demonstrates how to develop a simple OSGi WAR bundle
containing a RESTful hello world web service

Contents
--------

The example WAR (see the `war-bundle` module) consists of two Jersey
resources:

`org.glassfish.jersey.examples.osgi.helloworld.resource.HelloWorldResource`

that produces a textual response to an HTTP GET

`org.glassfish.jersey.examples.osgi.helloworld.resource.AnotherResource`

that produces a different textual response to an HTTP GET. The
purpose of this resource is to show how to define multiple web
resources within a web application.

The mapping of the URI path space is presented in the following table:

URI path           | Resource class       | HTTP method
------------------ | -------------------- | -------------
**_/helloworld_**  | HelloWorldResource   | GET
**_/another_**     | AnotherResource      | GET

Running the Example
-------------------

To run the example, you would need to build the WAR file and install it
to an OSGi runtime (e.g. Apache Felix) together with other OSGi modules.
Look at the attached `functional-test` module for details on the
programatical runtime configuration. To build the war archive and run
the tests, you can just launch

>     mvn clean install
