[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Multipart Web app Example
=========================

This example demonstrates how to develop RESTful web service with
demonstrating JAX-RS Integration with MIME MultiPart Message Formats and
an EE 6 compliant Web container.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                     | Description                                    | Sample request using curl
---------------------------- | ---------------------------------------------- | -----------------------------------------------------------------------------------------------
**_/form/part_**             | POST message returning entire string           | `curl -X POST -F "part=part1"  http://localhost:8080/multipart-webapp/form/part`
**_/form/part-file-name_**   | POST message returning part filename string.   | Be sure to execute this curl from project directory where pom.xml resides
                             |                                                | `curl -X POST -F "part=@pom.xml"  http://localhost:8080/multipart-webapp/form/part-file-name`
**_/form/xml-jaxb-part_**    | POST message returning xml jaxb part string.   | No curl sample available, please check test sources.

Running the Example
-------------------

You can run the example using Jetty as follows:

> `mvn clean package jetty:run`

Following steps are using [cURL](http://curl.haxx.se/) command line tool: