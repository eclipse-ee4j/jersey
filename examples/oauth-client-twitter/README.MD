[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

OAuth 1 Client Sample - Twitter Client
======================================

This example demonstrates how to develop a simple web app client
utilizing JAX-RS client API and Jersey OAuth support.

Contents
--------

This example consists of the main class (`App`) and two JAXB beans used
for unmarshaling the responses from Twitter (`Status` and `User`).

Running the Example
-------------------

### Registering with Twitter

Before running the example, you have to register your own application
with Twitter (to obtain consumer key and consumer secret you have to use
to configure this client). You can do it as follows:

1.  Go to [Twitter Developers Page](http://dev.twitter.com/). You need
    to sign in to the Twitter (if you are new to Twitter you need to
    sign up). Then in the right upper corner you will see your profile
    photo and drop down list in which you select "My Application". Click
    on **Create a new application** button.

2.  Fill out the "Create an application" form - you have to pick a
    unique application name (e.g. app12345678), put in some description,
    put in something for app website (e.g. http://jersey.java.net),
    Accept terms by selecting **Yes, I agree** in "Twitter Content"
    checkbox, enter captcha.

3.  Click **Create your twitter application** button.

4.  You will be presented with consumer key, consumer secret and other
    details for your registered application.

### Running for the First Time

Enter the following on the command-line:

>    mvn clean compile exec:java -DconsumerKey=<consumer key assigned to your app> -DconsumerSecret=<consumer secret assigned to your app>

### Subsequent Runs

After you run the application for the first time, it stores the consumer
key, secret and token and token secret in a property file for future
use. So, for subsequent runs you don't have to specify these as system
properties. The following command is good enough for running the
application after it was built, run for the first time and the property
file was created:

>    mvn exec:java

Troubleshooting
---------------

### HTTP Proxy

Behind a HTTP proxy, you will need to setup the following system
properties to communicate with the Twitter server:

-   http.proxyHost - the name of the proxy host.
-   http.proxyPort - the port of the proxy host.
-   http.proxyAuth - the user name and password to use when connecting
    to the proxy; this string should be the user name and password
    separated by a colon (e.g., rickhall:mypassword).

These system properties can be set directly on the command line when
starting the JVM using the standard "-D&lt;prop&gt;=&lt;value&gt;"
syntax.
