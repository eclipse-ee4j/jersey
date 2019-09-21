[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

SSE Item Store Jersey Example
=============================

An example demonstrating how Server Sent Events (SSE) Jersey support can
be used to notify clients about changes in server-side managed data
(collection of items). The example also outlines how SSE events can be
consumed using javascript browser-based clients.

Contents
--------

The example consists of a web-based client application and a server-side
application deployed on a [Jetty servlet container](http://www.eclipse.org/jetty/documentation/current/).

The server side part of the application consists of a JAX-RS resource
managing collection of string items and a simple HTML page that includes
a browser SSE client written in Javascript as well as a basic CSS
stylesheet. The SSE Javascript client connects to the JAX-RS resource
and transforms the streamed messages into HTML code that is rendered by
the browser. The javascript client also demonstrates how named and
unnamed SSE events are handled by HTML5 `EventSource` component. The
mapping of the URI path space of the server-side part of the application
is presented in the following table:

URI path                        |  Resource class      | HTTP methods
------------------------------- |  ------------------- | --------------
**_/resources/items_**          |  ItemStoreResource   | GET, POST
**_/resources/items/events_**   |  ItemStoreResource   | GET (SSE)

Application is configured to run using Jetty maven plugin under base
path `sse-item-store-webapp`.

Running the Example
-------------------

>     mvn clean compile jetty:run

The command above deploys the current example. After successful
deployment, you should be able to access the browser SSE client page at
<http://localhost:8080/sse-item-store-jersey-webapp/index.html>. To see the raw
SSE event stream, you may also point your browser directly at the
[`ItemStoreResource`](http://localhost:8080/sse-item-store-jersey-webapp/resources/items/events).

Deploying the example to another servlet container.
---------------------------------------------------

>     mvn clean package

The command above creates a Servlet 3.x compliant WAR located in the
target directory. The WAR can be then deployed to your Servlet 3.x
compliant container.

Running Test Client
-------------------

After deploying the application into a Servlet 3.x compliant container,
you can run the attached test by executing:

>     mvn test -Djersey.config.test.container.factory=org.glassfish.jersey.test.external.ExternalTestContainerFactory -Djersey.config.test.container.port=<port>
