[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

JSONP Example
=============

This example demonstrates how to use JSONP wrapper in order to serve not
only JSON but also a JavaScript expression representing a function call
with the original JSON data as a parameter. The technique is well
described e.g. at <http://en.wikipedia.org/wiki/JSON#JSONP>.

A simple web application is presented with just one resource: a change
record list. A client can obtain the list using XML or JSON or
JavaScript representation. The last change record is also available in
all three formats as a subresource of the previously mentioned list
resource.

Contents
--------

The change record list web resource is implemented by
`org.glassfish.jersey.examples.jsonp.ChangeListResource` class.

The mapping of the URI path space is presented in the following table:

URI path                | Resource class   | HTTP methods
----------------------- | ---------------- | --------------
**_/changes_**          | ChangeList       | GET
**_/changes/latest_**   | ChangeList       | GET

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the JSONP example using [Grizzly](http://grizzly.java.net/)

A [WADL description](http://wadl.java.net/#spec) may be then accessed at the URL:

- <http://localhost:8080/jsonp/application.wadl>

Following steps are using [cURL](http://curl.haxx.se/) command line tool:

Get the list of changes in JSON format:

>     curl -HAccept:application/json http://localhost:8080/jsonp/changes

Get the list of changes in XML format:

>     curl -HAccept:application/xml http://localhost:8080/jsonp/changes

Get the list of changes as a JavaScript callback:

>     curl -HAccept:application/x-javascript http://localhost:8080/jsonp/changes

You can also see test classes at `src/main/test` subdirectory for detail
information how to consume the service using Jersey client API

### JavaScript Client

After starting the service and refreshing this page, a list of changes
should be displayed below:

`You need to run the service first!`