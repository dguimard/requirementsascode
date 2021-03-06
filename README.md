# requirements as code 
[![Build Status](https://travis-ci.org/bertilmuth/requirementsascode.svg?branch=master)](https://travis-ci.org/bertilmuth/requirementsascode)

This project simplifies the development of message-driven applications.

It provides a builder API to create handlers for many types of messages at once.

You can [customize message handling](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeexamples/crosscuttingconcerns) in a simple way, for example for measuring performance, or for logging purposes.

For more advanced cases that depend on the application's state, like Process Managers and Sagas,
you can create a [model with flows](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeexamples/helloworld).
It's a simple alternative to state machines, understandable by developers and business people alike.

For the long term maintenance of your application, you can [generate documentation](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeextract) from the models inside the code without the need to add comments to it.

# getting started
At least Java 8 is required to use requirements as code, download and install it if necessary.

Requirements as code is available on Maven Central.

If you are using Maven, include the following in your POM, to use the core:

``` xml
  <dependency>
    <groupId>org.requirementsascode</groupId>
    <artifactId>requirementsascodecore</artifactId>
    <version>1.2.4</version>
  </dependency>
```

If you are using Gradle, include the following in your build.gradle, to use the core:

```
compile 'org.requirementsascode:requirementsascodecore:1.2.4'
```
# how to use requirements as code
Here's what you need to do as a developer.

## Step 1: Build a model defining the message types to handle, and the methods that react to a message:
``` java
Model model = Model.builder()
	.user(<command class>).system(<command handler, i.e. lambda, method reference, consumer or runnable)>)
	.user(..).system(...)
	...
.build();
```

The order of the statements has no significance.
For handling events instead of commands, use `.on()` instead of `.user()`.
For handling exceptions, use the specific exception's class or `Throwable.class` as parameter of `.on()`.
Use `.condition()` before `.user()`/`.on()` to define an additional precondition that must be fulfilled.
You can also use `condition(...)` without `.user()`/`.on()`, meaning: execute at the beginning of the run, or after a step has been run,
if the condition is fulfilled.

## Step 2: Create a runner and run the model:
``` java
ModelRunner runner = new ModelRunner().run(model);
```

## Step 3: Send messages to the runner, and enjoy watching it react:
``` java
runner.reactTo(<Message POJO Object> [, <Message POJO Object>,...]);
```
To customize the behavior when the runner reacts to a message, use `modelRunner.handleWith()`.
By default, if a message's class is not declared in the model, the runner consumes it silently.
To customize that behavior, use `modelRunner.handleUnhandledWith()`.
If an unchecked exception is thrown in one of the handler methods and it is not handled by any 
other handler method, the runner will rethrow it.

# hello world
Here's a complete Hello World example:

``` java
package hello;

import org.requirementsascode.Model;
import org.requirementsascode.ModelRunner;

public class HelloUser {
	public static void main(String[] args) {
		new HelloUser().buildAndRunModel();
	}
	
	private void buildAndRunModel() {
		Model model = Model.builder()
			.user(RequestHello.class).system(this::displayHello)
			.user(EnterName.class).system(this::displayName)
		.build();

		new ModelRunner().run(model)
			.reactTo(new RequestHello(), new EnterName("Joe"));		
	}

	public void displayHello(RequestHello requestHello) {
		System.out.println("Hello!");
	}

	public void displayName(EnterName enterName) {
		System.out.println("Welcome, " + enterName.getUserName() + ".");
	}

	class RequestHello {}
	
	class EnterName {
		private String userName;

		public EnterName(String userName) {
			this.userName = userName;
		}

		public String getUserName() {
			return userName;
		}
	}
}
```

# event queue for non-blocking handling
The default mode for the ModelRunner is to handle messages in a blocking way. 
Instead, you can use a simple event queue that processes events one by one in its own thread:

``` java
Model model = ...;
ModelRunner modelRunner = new ModelRunner();
modelRunner.run(model);

EventQueue queue = new EventQueue(modelRunner::reactTo);
queue.put(new String("I'm an event, react to me!"));
```

The constructor argument of `EventQueue` specifies that each event that's `put()` will be placed in the queue, and then forwarded to `ModelRunner.reactTo()`.
Note that you can forward events to any other consumer of an object as well.
You have to call `queue.stop()` to terminate the event queue thread before exiting your application.

# publishing events
When you use the `system()` method, you are restricted to just consuming messages.
But you can also publish events with `systemPublish()`, like so:

``` java
	private void buildAndRunModel() {
		Model model = Model.builder()
			.on(EnterName.class).systemPublish(this::publishNameAsString) 
			.on(String.class).system(this::displayNameString) 
		.build();		
		
		Optional<Object> userName = new ModelRunner().run(model)
			.reactTo(new EnterName("Joe"));	
	}
	
	private String publishNameAsString(EnterName enterName) {
		return enterName.getUserName();
	}
	
	public void displayNameString(String nameString) {
		System.out.println("Welcome, " + nameString + ".");
	}
```

As you can see, `publishNameAsString()` takes a command object as input parameter, and returns an event to be published. In this case, a String.
By default, the model runner takes the returned event and publishes it to the model. 
In the example, this will print "Welcome, Joe."

This behavior can be overriden by specifying a custom event handler on the ModelRunner with `publishWith()`.
For example, you can use `modelRunner.publishWith(queue::put)` to publish events to an event queue.

# documentation
* [Examples for building/running state based use case models](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeexamples/helloworld)
* [How to generate documentation from models](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeextract)
* [Cross-cutting concerns example](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeexamples/crosscuttingconcerns)

# publications
* [Simplifying an event sourced application](https://dev.to/bertilmuth/simplifying-an-event-sourced-application-1klp)
* [Kissing the state machine goodbye](https://dev.to/bertilmuth/kissing-the-state-machine-goodbye-34n9)
* [The truth is in the code](https://medium.freecodecamp.org/the-truth-is-in-the-code-86a712362c99)
* [Implementing a hexagonal architecture](https://dev.to/bertilmuth/implementing-a-hexagonal-architecture-1kgf)

# subprojects
* [requirements as code core](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodecore): create and run models. 
* [requirements as code extract](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeextract): generate documentation from the models (or any other textual artifact).
* [requirements as code examples](https://github.com/bertilmuth/requirementsascode/tree/master/requirementsascodeexamples): example projects illustrating the use of requirements as code.

# build from sources
Use Java >=11 and the project's gradle wrapper to build from sources.

# related topics
* The work of Ivar Jacobson on Use Cases. As an example, have a look at [Use Case 2.0](https://www.ivarjacobson.com/publications/white-papers/use-case-ebook).
* The work of Alistair Cockburn on Use Cases, specifically the different goal levels. Look [here](http://alistair.cockburn.us/Use+case+fundamentals) to get started, or read the book "Writing Effective Use Cases".
