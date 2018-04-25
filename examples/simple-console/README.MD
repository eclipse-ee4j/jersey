[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Simple Console Example
======================

This example demonstrates how to develop RESTful web services with the
embedded Grizzly server.

Contents
--------

The example consists of three Java classes and one HTML file:

`org.glassfish.jersey.examples.console.App`

Contains the main function which starts and stops the HTTP server.

`org.glassfish.jersey.examples.console.resources.Colours`

A resource class that can produce two different representations of a
list of colors. The list can be filtered by a client-supplied
query parameter. The resource references the Form resource using the
Path annotation declared on the Colours.getColours method.

`org.glassfish.jersey.examples.console.FormResource`

A resource class that produces a form in response to an HTTP GET,
processes the content of the form when submitted, and produces a
table of the submitted values in respose to the form being posted.

`form.html`

A static HTML file that is returned by the Form resource.

The mapping of the URI path space is presented in the following table:

URI path              | Resource class   | HTTP methods
--------------------- | ---------------- | --------------
**_/form_**           | Form             | GET, POST
**_/form/colours_**   | Colours          | GET

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   <http://127.0.0.1:9998/resources/form>

Fill in the form and submit it. Note that the pull-down list of colors
is populated dynamically. Enter part of a color name in the hint box to
see a filtered list of colors containing the hint.

You can see the complete list of colors at:

-   <http://127.0.0.1:9998/resources/form/colours>

Or see only those colors containing the letter 'r':

-   <http://127.0.0.1:9998/resources/form/colours?match=r>

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://127.0.0.1:9998/resources/application.wadl>