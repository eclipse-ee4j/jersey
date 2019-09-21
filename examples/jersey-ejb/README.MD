[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey EJB Example
==================

This example demonstrates how to develop RESTful web service with a Servlet 3.0 and EJB 3.1 container.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                | Resource class             | HTTP methods   | Description
--------------------------------------- | -------------------------- | -------------- | ------------------------------------------------------------------------------------
**_/jersey-ejb_**                       | ---                        | GET            | Simple html/ajax client
**_/jersey-ejb/app/messages_**          | MessageBoardResourceBean   | GET            | Returns first ten messsages
**_/jersey-ejb/app/messages/{id}_**     | MessageBoardResourceBean   | GET            | Returns message with uniqueId = {id} or returns HTTP 404 if message does not exist
**_/jersey-ejb/app/messages/{id}_**     | MessageBoardResourceBean   | DELETE         | Deletes message with uniqueId = {id} or returns HTTP 404 if message does not exist
**_/jersey-ejb/app/messages/_**         | MessageBoardResourceBean   | POST           | Creates new message and allocates unique id for it (with message as request entity)

Running the Example
-------------------

This sample utilizes Java EE features in the GlassFish container

Build the application WAR file by executing maven 2 from the project directory:

>     mvn clean package

To deploy on a running GlassFish container, you would then typically need to launch:

>     $AS_HOME/bin/asadmin deploy target/jersey-ejb.war

Another option is to deploy using [GlassFish administrator console GUI](http://localhost:4848/)

You can access the application at:

-   <http://localhost:8080/jersey-ejb>
