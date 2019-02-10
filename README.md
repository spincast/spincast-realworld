# ![RealWorld Example App](logo.png)

> ### Spincast Framework codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.


# Intro


This codebase was created to demonstrate a fully fledged fullstack application built with **[Spincast Framework](https://www.spincast.org)** including CRUD operations, authentication, routing, pagination, and more. [Spincast](https://www.spincast.org) is a Java framework based on [Guice](https://github.com/google/guice).

We've gone to great lengths to adhere to the **[Spincast Framework](https://www.spincast.org)** community styleguides & best practices.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.


# How it works

An *embedded* PostgreSQL instance is provided and is used as the data source. You do not need to install/configure
anything else than the application itself... Everything is included in the application `.jar`.

If at some point you want to reset the data of the application, simply delete the "`dbData`" directory which is going to be 
created.


# Getting started

### Requirements

- JDK8
- Maven

### Starting the application

From the root of the project:

```
mvn clean package -DskipTests
```

Then:

```
java -jar target/spincast-realworld-1.0.0.jar
```

That's it!  

You can now start sending requests to the API, for example using Postman. 
The application starts on port `12345`, using SSL, and its root is: [https://localhost:12345/api](https://localhost:12345/api).

### Modifying the configurations

If you want to change the default configurations (which are provided in "`src/main/resources/app-config.yaml`", you can create a new "`target/app-config.yaml`"
file, next to "`target/spincast-realworld-1.0.0.jar`", and restart the application.


### Running tests

You can run tests using:

```
mvn test
```

You can also run the Postman tests provided by the specs by executing:
- `varia/run-api-tests.sh` (Linux/Mac)
- `varia/run-api-tests.bat` (Windows)

But, of course, the best way to run the tests is by opening the project in your favorite IDE and launch them manually... You can then see exactly how they were written:

- `/src/test/java/org/spincast/realworld/UsersTest.java` 
- `/src/test/java/org/spincast/realworld/ArticlesTest.java` 


# Analyzing the code

- Import the project *as a Maven project* in your IDE.
- The `main` method is located in the `org.spincast.realworld.App` class. Add
  a breakpoint and start a debug configuration to understand how a Spincast application
  is bootstrapped.
- Add breakpoints in the controllers (`org.spincast.realworld.controllers.*`) and
  start sending requests using Postman or your favorite tool.

  
  
-----