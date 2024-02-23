[//]: # " Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Client Configured by Property File Example
==========================================

This example demonstrates configuration of a Client using property file.
The property file microprofile-config.properties is consumed by 
Microprofile Config implementation and the properties from the 
property file are set to Jersey Configuration.

The following properties are defined in `microprofile-config.properties` property file:
  * jersey.config.client.connector.provider - jersey property name for the connector to be used
  * entity.value - user defined property to be be sent as an echo message  


Contents
--------

The mapping of the URI path space is presented in the following table:

URI path             | Resource class      | HTTP methods | Notes
-------------------- | ------------------- |--------------| --------------------------------------------------------
**_/helloworld_**    | HelloWorldResource  | POST         |  Echoes the message sent

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.

-   <http://localhost:8080/base/helloworld>
