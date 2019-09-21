[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Entity Data Filtering
=====================

### *Entity Data Filtering Using custom annotations to filter entities*

This example demonstrates how to define custom entity filtering
annotations (views) and how to apply them on domain classes as well as
on JAX-RS resource classes or JAX-RS resource methods.

The full description how Entity Data Filtering can be found in Jersey User Guide, chapter
[Entity Data Filtering](https://jersey.java.net/documentation/latest/entity-filtering.html).
Sections relevant to this example (describing this exact example) are:

-   [Enabling and configuring Entity Filtering in your application](https://jersey.java.net/documentation/latest/entity-filtering.html#d0e13911)
-   [Components used to describe Entity Filtering concepts](https://jersey.java.net/documentation/latest/entity-filtering.html#d0e14024)
-   [Using custom annotations to filter entities](https://jersey.java.net/documentation/latest/entity-filtering.html#ef.annotations)

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                                   | Resource class     | HTTP methods   | Allowed values
-----------------------------------------  | ------------------ | -------------- | ----------------
**_/projects/{id}_**                       | ProjectsResource   | GET            | int
**_/projects_**                            | ProjectsResource   | GET            | N/A
**_/projects/detailed/{id}_**              | ProjectsResource   | GET            | int
**_/projects/detailed_**                   | ProjectsResource   | GET            | N/A
**_/tasks/{id}?detailed={true\|false}_**   | TasksResource      | GET            | int, boolean
**_/tasks_**                               | TasksResource      | GET            | N/A
**_/tasks/detailed_**                      | TasksResource      | GET            | N/A
**_/users/{id}?detailed={true\|false}_**   | UsersResource      | GET            | int, boolean
**_/tasks?detailed={true\|false}_**        | UsersResource      | GET            | N/A

Application is based on Grizzly container (see `App`). Everything needed
(resources/providers) is registered in `EntityFilteringApplication`.

Sample Response
---------------

Even though the same instance of, e.g. Project class, is used to create
response for both basic and detailed view the actual data sent over the
wire differ for each of these two views. For basic view it looks like:

```javascript
{
   "description" : "Jersey is the open source (under dual EPL+GPL license) JAX-RS 2.1 (JSR 370) production quality Reference Implementation for building RESTful Web services.",
   "id" : 1,
   "name" : "Jersey"
}
```

And for detailed view it looks like:

```javascript
{
   "description" : "Jersey is the open source (under dual EPL+GPL license) JAX-RS 2.1 (JSR 370) production quality Reference Implementation for building RESTful Web services.",
   "id" : 1,
   "name" : "Jersey",
   "tasks" : [ {
      "description" : "Entity Data Filtering",
      "id" : 1,
      "name" : "ENT_FLT"
   }, {
      "description" : "OAuth 1 + 2",
      "id" : 2,
      "name" : "OAUTH"
   } ],
   "users" : [ {
      "email" : "very@secret.com",
      "id" : 1,
      "name" : "Jersey Robot"
   } ]
}
```

Running the Example
-------------------

Run the example as follows:

>     mvn clean package exec:java

This deploys current example using Grizzly. You can access the
application at:

-   <http://localhost:8080/projects>
-   <http://localhost:8080/projects/detailed>
-   <http://localhost:8080/users>
-   <http://localhost:8080/users?detailed=true>
-   <http://localhost:8080/tasks>
-   <http://localhost:8080/tasks/detailed>

Using Jackson instead of MOXy
-----------------------------

This examples uses by default Entity Data Filtering feature together
with MOXy. To switch MOXy JSON provider to Jackson (2.x) JSON provider
simply

-   comment registration of MOXy ContextResolver, and\
     `register(new MoxyJsonConfig().setFormattedOutput(true).resolver())`
-   uncomment registration of JacksonFeature\
     `register(JacksonFeature.class)`

in `EntityFilteringApplication` class.
