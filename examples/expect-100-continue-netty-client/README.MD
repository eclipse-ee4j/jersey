[//]: # " Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

jersey-example-expect-100-continue-netty-connector
==========================================================

This example demonstrates how to register and run Jersey Netty connector with Expect:100-continue feature on.
It also provides custom low-level Socket server to demonstrate how is request is captured and processed.  

Contents
--------

The server and client are operating on requests level, without exposing any Resources. Client only sends request in
form 
```json
{"message":"Hello from java client"}
```

Sample Response
---------------
Server in turn shows output which demonstrates Expect:100-continue presence and handling

```shell
==== DUMPING HEADERS ====
expect, 100-continue
transfer-encoding, chunked
host, 127.0.0.1:3000
content-type, application/json
accept, application/json
user-agent, jersey/2.47-snapshot (netty 4.1.112.final)
==== HEADERS DUMPED =====
==== DUMPING RESPONSE ====
HTTP/1.1 100 Continue
Connection: keep-alive


==== RESPONSE DUMPED =====
24
{"message":"Hello from java client"}
==== DUMPING RESPONSE ====
HTTP/1.1 204 No Content
Server: Socket Server v.0.0.1


```

Running the Example
-------------------

Run the example using provided ServerSocket container as follows:

>     mvn clean compile exec:java

Run the example using client as follows:

>     mvn clean package exec:java -Pclient

