[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

JSON-P JAX-RS JSON Provider Example
===================================

This example demonstrates how to produce/consume JSON representations
defined by [JSR-353](http://jcp.org/en/jsr/detail?id=353).

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                    | Resource class              | HTTP methods   | Allowed values
--------------------------- | --------------------------- | -------------- | ------------------------------
**_/document_**             | DocumentResource            | GET            |
**_/document_**             | DocumentResource            | DELETE         | 
**_/document_**             | DocumentResource            | POST           | `JsonObject`
**_/document/{id: \d+}_**   | DocumentResource            | GET            | 
**_/document/{id: \d+}_**   | DocumentResource            | DELETE         | 
**_/document/multiple_**    | DocumentResource            | POST           | `JsonArray` of `JsonObject`s
**_/document/filter_**      | DocumentFilteringResource   | POST           | `JsonArray` of string values

(See JsonProcessingResourceTest for more details.)

Running the Example
-------------------

Run the example as follows:

>     mvn clean package jetty:run

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://localhost:8080/jsonp-webapp/application.wadl>

This deploys current example using Jetty. You can access the application at <http://localhost:8080/jsonp-webapp/document>

You can access resources of this application also using curl:

### Store some documents.

>     curl -H "Content-Type: application/json" -X POST --data '[{"name":"Jersey","site":"http://jersey.java.net"},{"age":33,"phone":"158158158","name":"Foo"},{"name":"JSON-P","site":"http://jsonp.java.net"}]' http://localhost:8080/jsonp-webapp/document/multiple

### Retrieve stored documents.

>     curl http://localhost:8080/jsonp-webapp/document

### Retrieve a document with id 1.

>     curl http://localhost:8080/jsonp-webapp/document/1

### Retrieve `site` attributes with values of documents containing this attribute.

>     curl -H "Content-Type: application/json" -X POST --data '["site"]' http://localhost:8080/jsonp-webapp/document/filter
