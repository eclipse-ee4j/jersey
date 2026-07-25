## Authentication and Authorization

### Motivation

Propose a starting point of an application that needs authentication and authorization.


### Introduction

The client can be authenticated by traditional username and password or (for now) Facebook OAuth. The result of a successful login results in a JWT token response, that can be used by the client to make a request to private resources.

The JWT token is issued with the subject (username or your user business key) and roles claims. Those claims will dictate if the client can be authorized to access application resources.

The application was designed and developed with repository pattern. In this, we have implemented the repository in JPA, but can be easily changed to fit your needs.

You can import the Postman collection to try the available resources.

You have to set the facebook Oauth client id and client secrete under settings OAuthSettings class.

Hope to be useful.
