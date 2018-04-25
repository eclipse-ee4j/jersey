[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Jersey HTTP PATCH
=================

### *Jersey HTTP PATCH Support Example*

This example demonstrates how to implement generic support for
[HTTP PATCH Method (RFC-5789)](https://tools.ietf.org/html/rfc5789)
via JAX-RS reader interceptor. The example has been created based on
[this Gerard Davison's blogentry] (http://kingsfleet.blogspot.co.uk/2014/02/transparent-patch-support-in-jax-rs-20.html).
The patch format supported by this example is [JSON Patch (RFC-6902)](http://tools.ietf.org/html/rfc6902).

Contents
--------

The mapping of the URI path space is presented in the following table:

URI path                 | Resource class      | HTTP methods
------------------------ | ------------------- | --------------
**_/patchable-state_**   | PatchableResource   | GET, PATCH

As you can see in the table, there is only a single resource deployed in
this application, that supports `GET` and `PATCH` methods. The resource
represents a *patchable* state, which consists of 3 properties:

-   a `title` text property,
-   a `message` text property, and
-   a `list` property that represents a list/array of text strings.

This state can be patch via HTTP GET method by sending the desired
patch/diff in the JSON Patch format as defined in RFC-6902, for example:

```javascript
[
  {
    "op" : "replace",
    "path" : "/message",
    "value" : "patchedMessage"
  }, {
    "op" : "add",
    "path" : "/list/-",
    "value" : "one"
  }
]
```

This patch will instruct the resource to replace it's `message` property
content with a new `"patchedMessage"` text and also to append a new
`"one"` string value to the list of valued contained in the `list`
property.

(See `HttpPatchTest` for more details.)

Sample Response
---------------

You can apply a patch using curl:

>     curl -v -X PATCH http://localhost:8080/http-patch/patchable-state -H "Content-Type:application/json-patch+json" -d '[{
>        "op": "replace",
>        "path": "/message",
>        "value": "patchedMessage"
>     }, {
>        "op": "add",
>        "path": "/list/-",
>        "value": "one"
>     }]'

The application will answer with a patched object:

```javascript
{
   "list" : [ "one" ],
   "message" : "patchedMessage",
   "title" : ""
}
```

Running the Example
-------------------

Run the example as follows:

>     mvn clean compile exec:java

This deploys the example using [Grizzly](http://grizzly.java.net/) container.

A [WADL description](http://wadl.java.net/#spec) may be accessed at the URL:

-   <http://localhost:8080/http-patch/application.wadl>

The resource is available at

-   <http://localhost:8080/http-patch/patchable-state>
