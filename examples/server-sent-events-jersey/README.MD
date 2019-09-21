[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Server-Sent Events (SSE) Example
===================================

This example demonstrates JAX-RS 2.0 server-sent events support sometimes also called one-way publish-subscribe model.

The full description how to create a support for server-sent events can be found in Jersey User Guide, chapter
[Server-Sent Events (SSE) Support](https://jersey.java.net/documentation/latest/sse.html).

It is highly recommended to look at details of SSE API Specification on
<http://www.w3.org/TR/2009/WD-eventsource-20091029/>

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                              | Resource class             | HTTP methods   | Description
------------------------------------- | -------------------------- | -------------- | ------------
**_server-sent-events_**              | ServerSentEventsResource   | GET            | Get entire EventOutput with all messages
**_server-sent-events_**              | ServerSentEventsResource   | POST           | Insert a new message in EventOutput
**_server-sent-events_**              | ServerSentEventsResource   | DELETE         | Reset EventOutput
**_server-sent-events/domains/{id}_** | BlockingPostChatResource   | POST           | Generate several messages with a delay in EventOutput and return it

Sample Response
---------------

A great example of Server-Sent Events is `server-sent-events/domains/{id}` which sends several messages with a delay 200ms
between each other.

>     curl -v -X POST http://localhost:8080/server-sent-events-jersey/domains/1 -H "Content-Type: text/event-stream"

Look at a console how events are handled one after another in the right order.

```
event: domain-progress
data: starting domain 1 ...

event: domain-progress
data: 50%

event: domain-progress
data: 60%

event: domain-progress
data: 70%

event: domain-progress
data: 99%

event: domain-progress
data: done
```

Running the Example
-------------------

Look at Jersey Documentation to learn how to consume Server-Sent Events using Jersey Client 
<https://jersey.java.net/documentation/latest/sse.html#d0e11786>

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.
