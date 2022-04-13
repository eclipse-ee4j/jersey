# Jakarta REST TCK

The **Jakarta REST TCK** is a standalone kit for testing compliance of an implementation with the Jakarta REST specification.


## Performing Test

While the test kit *is not dependent* of Maven, the most easy way to perform the tests is to create a copy of the sample Maven project found in the `jersey-tck` folder and adjust it to the actual needs of the implementation to be actually tested:
* Replace the dependency to Jersey by a dependency to the implementation to be actually tested.
* Execute `mvn verify`.
* Find the test result as part of Maven's output on the console or refer to the surefire reports.

**Note:** Certainly the same can be performed using *other* build tools, like Gradle, or even by running a standalone Jupiter API compatible test runner (e. g. JUnit 5 Console Runner), as long as the Jakarta REST TCK JAR file and the implementation to test are both found on the classpath.

**Hint:** The test project can safely get stored as part of the implementation, so it can be easily executed as part of the QA process.
