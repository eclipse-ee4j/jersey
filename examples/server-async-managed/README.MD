[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Server Asynchronous Managed Example
===================================

This example demonstrates JAX-RS 2.0 server-side non-blocking API using **ManagedAsync** annotation.

The full description how to create an asynchronous resource can be found in Jersey User Guide, chapter 
[Asynchronous Services and Clients](https://jersey.java.net/documentation/latest/async.html).

Indicates that the resource method to which the annotation has been applied
 * should be executed on a separate thread managed by an internal Jersey

The  goal of the sample is to demonstrate that with limited I/O processing threads
on the server the synchronous execution of a long-running task may lead to resource
starvation caused by I/O processing threads being blocked by the long-running
operation, unable to serve new incoming requests.

OTOH, when the same long-running operation is executed asynchronously, the I/O
threads do not need to block while waiting for the long-running operation to finish
and the overall throughput of the system is greatly increased.

- Look at the another example how to create a non-blocking API without **ManagedAsync**
annotation <https://github.com/jersey/jersey/tree/master/examples/server-async>
which also shown the way how to provide an immediate response for a client

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                        | Resource class            | HTTP methods
--------------------------------| --------------------------| --------------
**_chat_**                      | ChatResource              | POST
**_chat_**                      | ChatResource              | GET
**_managedasync/longrunning_**  | BlockingPostChatResource  | POST

Sample Response
---------------

This section shortly describes what really happens during the calls in this chat example.

We can start with POST request:
```
curl -v -X POST http://localhost:8080/base/chat -H "Content-Type: application/json" -d '{
    "author": "Anonymous Author",
    "message": "Secret Message"
}'
```

We can notice that we haven't got any response yet. The called method is annotated **@ManagedAsync**
which means that the I/O thread delegates a method processing to another Thread-Pool and is ready
to accept another incoming request. But the processing is still stop. Why? **AsyncResponse** was added
to the blocking queue and now it's waiting for GET call which will take the response and will finish the processing
of the preceding call.

```
curl -v -X GET http://localhost:8080/base/chat
```

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.
