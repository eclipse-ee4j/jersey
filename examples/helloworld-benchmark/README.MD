[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # " "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Distribution License v. 1.0, which is available at "
[//]: # " http://www.eclipse.org/org/documents/edl-v10.php. "
[//]: # " "
[//]: # " SPDX-License-Identifier: BSD-3-Clause "

Hello World JMH Example
=======================

### *How to write JAX-RS Micro-Benchmarks*

This example demonstrates how to write simple micro-benchmarks, based on
JMH, for JAX-RS application. This particular example shows how to obtain
throughput of a simple 'Hello World' application.

The application consists of one resource, `HelloWorldResource` with
three resource methods (one method per HTTP GET, POST and PUT methods).
To make it more interesting the resource also acts like sub-resource
locator which allows us to measure the difference between processing of
normal resource methods and sub-resource locators.

This application is solely focused on showing how to write benchmarks
and it doesn't start any server that would allow one to send request to
the `HelloWorldResource`.

Main class that illustrates writing benchmarks for JAX-RS applications
is `HelloWorldBenchmark`. Measurements are not based on any container
such as Servlet or Grizzly container. Requests are directly invoked on
`ApplicationHandler` class from Jersey Server module to eliminate any
network influences. Still, all JAX-RS features, like filters and
interceptors, are taken into consideration when the benchmark is
executed.

It's also possible to run the benchmark on older versions of Jersey,
e.g. 2.15, which had known performance issues with sub-resource
locators. The recommended approach is to run the benchmarks without any
changes at first and then modify the `pom.xml` to switch version of
Jersey to 2.15 and run the benchmarks again to see the (big) difference.
Look for `jersey-bom` artifact in `pom.xml` to see how the switch should
be done.

Sample Results
--------------

The execution takes around 2 minutes and you can see partial results
(for each combination of parameters) of every iteration. At the end JMH
gives you whole summary. For example, Jersey 2.16 gives the following
results:

    # Run complete. Total time: 00:01:41

    Benchmark                    (method)              (path)   Mode  Cnt      Score      Error  Units
    HelloWorldBenchmark.measure       GET          helloworld  thrpt    8  74343.864 ± 4814.979  ops/s
    HelloWorldBenchmark.measure       GET  helloworld/locator  thrpt    8  54137.102 ± 8996.766  ops/s
    HelloWorldBenchmark.measure      POST          helloworld  thrpt    8  45173.853 ± 5349.363  ops/s
    HelloWorldBenchmark.measure      POST  helloworld/locator  thrpt    8  37144.797 ± 4699.782  ops/s
    HelloWorldBenchmark.measure       PUT          helloworld  thrpt    8  45945.974 ± 4116.752  ops/s
    HelloWorldBenchmark.measure       PUT  helloworld/locator  thrpt    8  36345.667 ± 5929.480  ops/s

Running the Example
-------------------

There are two ways how to run the micro-benchmark. Either via Maven
`exec:exec`

>     mvn clean install exec:exec

or you can build the benchmark JAR at first

>     mvn clean install

and then invoking it using `java` command

>     java -jar target/benchmark.jar

Resources
---------

JMH has excellent overview of it's features presented as list of
different samples. You can find them on [JMH
Samples](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/).
The general overview of JMH is available at their [home
page](http://openjdk.java.net/projects/code-tools/jmh/).
