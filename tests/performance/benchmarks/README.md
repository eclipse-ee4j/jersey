[//]: # " Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. "
[//]: # "  "
[//]: # " This program and the accompanying materials are made available under the "
[//]: # " terms of the Eclipse Public License v. 2.0, which is available at "
[//]: # " http://www.eclipse.org/legal/epl-2.0. "
[//]: # "  "
[//]: # " This Source Code may also be made available under the following Secondary "
[//]: # " Licenses when the conditions for such availability set forth in the "
[//]: # " Eclipse Public License v. 2.0 are satisfied: GNU General Public License, "
[//]: # " version 2 with the GNU Classpath Exception, which is available at "
[//]: # " https://www.gnu.org/software/classpath/license.html. "
[//]: # "  "
[//]: # " SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 "

## How to run?

To run all benchmarks execute:

`mvn clean install exec:exec` or `mvn clean install && java -jar target/benchmarks.jar`

To run specific benchmark, e.g. `JacksonBenchmark`:

`mvn clean install && java -cp target/benchmarks.jar org.glassfish.jersey.tests.performance.benchmark.JacksonBenchmark`

## Where to find more info/examples?

JMH page: http://openjdk.java.net/projects/code-tools/jmh/

JMH examples: http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/
