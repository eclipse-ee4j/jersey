[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

JSONB Example
=============

This example demonstrates how to use JSONB with resource classes.

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                | Resource class           | HTTP methods
--------------------------------------- | ------------------------ | --------------
**_/jsonb/cats/one_**                   | JsonbResource            | GET
**_/jsonb/cats/all_**                   | JsonbResource            | GET
**_/jsonb/cats/add_**                   | JsonbResource            | POST
**_/jsonb/cats/addAll_**                | JsonbResource            | POST

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container. You can access the application at:

-   <http://localhost:8080/json-binding-webapp/cats/one>
-   <http://localhost:8080/json-binding-webapp/cats/all>

or you can post an xml entity:

>    curl -v -X POST http://localhost:8080/json-binding-webapp/cats/add -H "Content-Type:application/json" -d '
>   {"color":"white","sort":"maine coon","catName":"Darwin", "domesticated":"true"}'

You can also use the built war file to deploy the app to a container of your choice.