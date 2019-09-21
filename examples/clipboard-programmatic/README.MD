[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Clipboard Example
=================

Very basic programmatically created resource example showcases CRUD functionality
implemented via HTTP POST, GET, PUT and DELETE methods.
A simple clipboard is simulated which is capable of handling text data only.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                  | Resource class   | HTTP methods
------------------------- | ---------------- | --------------
**_/clipboard_**          | programmatically | GET
**_/clipboard_**          | programmatically | POST
**_/clipboard_**          | programmatically | PUT
**_/clipboard_**          | programmatically | DELETE

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the Clipboard example using [Grizzly](http://grizzly.java.net/)

You can post any text:

```
curl -v -X POST http://localhost:8080/base/clipboard -H "Content-Type:text/plain" -d 'The urgent task'
```

After you successfully deploy the application and post entities, visit the following URLs:

-   <http://localhost:8080/base/clipboard>