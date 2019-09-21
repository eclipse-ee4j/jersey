[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Message Streaming Jersey Example
================================

An example demonstrating how Server Sent Events (SSE) Jersey support can
be used to create a message aggregating and streaming resource. The
example also demonstrates how to consume data provided by a [Twitter
Streaming API](https://dev.twitter.com/docs/streaming-apis) using Jersey
`ChunkedInput` client-side support.

Contents
--------

The example consists of a Swing GUI client application and a server-side
application deployed on a [Grizzly container](http://grizzly.java.net).

The Swing client application opens in a single window split into
multiple configuration panels. The top-left panel allows to select which
aggregators should be executed. It is possible to select a twitter
message aggregator and/or a fake test message aggregator. The panel
below provides means for adding new keywords tracked by the Twitter
message aggregator (if enabled). All actively tracked keywords are
listed in the right-side list box which supports removal of the keys
using a `Delete` key on your keyboard. The last panel in the Swing
client window contains a Start/Stop button for starting or stopping the
selected aggregators as well as a text area for displaying the messages
that the aggregators have sent to the message aggregating resource
deployed on the server-side of the application.

The server side part of the application consists of a message
aggregating JAX-RS resource and a simple HTML page that includes a
browser SSE client written in Javascript as well as a basic CSS
stylesheet. The SSE Javascript client connects to the JAX-RS message
aggregating resource and transforms the streamed message data into HTML
code that is rendered by the browser. The mapping of the URI path space
of the server-side part of the application is presented in the following
table:

URI path                           | Resource class          | HTTP methods
---------------------------------- | ----------------------- | --------------
`/aggregator-api/message/stream`   | MessageStreamResource   | GET, PUT

Application is programmatically configured to run on Grizzly container
under base paths `aggregator` and `aggregator-api`.

Running the Example
-------------------

Before running the example, you need to copy the
`twitter-api.properties` file available in the root directory of this
example to your user home directory. Once done, you need to edit the
file in your user home directory and provide a valid twitter account
credentials. This step is necessary as the Twitter streaming API is
protected using HTTP Basic Authentication mechanism and only an
authenticated twitter account owner can access the API. The
TwitterAggregator class in the example will use the credentials provided
in the file to access the Twitter Streaming API.

Once you have set up your Twitter credentials, you're ready to run the
example. You can run the example by executing a main class via maven as
follows:

>     mvn clean compile exec:java

This deploys the current example as well as it starts the Swing client
application. You should be able to see the main Swing application window
open as you run the application as well as you should see the note in
the command line stating that the Grizzly container has started and the
server side part of the application has been deployed.

After successful deployment, you should be able to access the browser
SSE client page at

-   <http://localhost:8080/aggregator/index.html>

To see the raw SSE event stream, you may also point your browser
directly at the

-   [MessageStreamResource](http://localhost:8080/aggregator-api/message/stream)

Note that you first need to start the message aggregators via the Swing
client application in order to be able to receive any SSE events from
the message streaming resource.
