[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

OSGi HttpService Example
========================

This example demonstrates how to develop a simple OSGi bundle
registering a Jersey based RESTful web service as an [OSGi
HttpService](http://www.osgi.org/javadoc/r4v42/org/osgi/service/http/HttpService.html)

Contents
--------

The example bundle (see the `bundle` module) consists of just one Jersey resource:

`org.glassfish.jersey.examples.osgihttpservice.StatusResource`

that produces a textual response to an HTTP GET

The mapping of the URI path space is presented in the following table:

URI path        | Resource class   | HTTP method
--------------- | ---------------- | -------------
**_/status_**   | StatusResource   | GET

Running the Example
-------------------

To run the example, you would need to build the OSGi bundle in the
`bundle` module and install it to an OSGi runtime (e.g. Apache Felix)
together with other OSGi bundles. Look at the attached `functional-test`
module for details on the programatical runtime configuration

To build the bundle jar file and run the tests, you can just launch

>     mvn clean install
