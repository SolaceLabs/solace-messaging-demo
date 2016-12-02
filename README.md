# Horizontal Scaling of Aggregated Microservices
## Using Solace Messaging in Cloud Foundry

The goal of this demo is to illustrate a real world example of using messaging between microservices in the cloud. This demo illustrates the Aggregator Microservice Design Pattern using an event driven architecture with messaging to communicate between Microservices, specifically Solace Messaging in Pivotal Cloud Foundry. 

## Contents

* [Overview](#overview)
* [Deploying to Cloud Foundry](#deploying-to-cloud-foundry)
* [Demo Components](#demo-components)
* [Using the Demo](#using-the-demo)
* [Contributing](#contributing)
* [Authors](#authors)
* [License](#license)
* [Resources](#resources)

---

## Overview
 
![Architecture Overview](resources/demo-overview.png)

As illustrated in the architecture diagram above, this demo is composed of the following parts:

1. A RESTful Web Application receives incoming requests. In this demo, the job requests are number of work units, rate, and delay characteristics.
2. The work requests are sent to a non-exclusive queue in the Solace Messaging Service.
3. The worker applications process messages from the Solace queue. The application can be horizontally scaled using Cloud Foundry to increase the overall processing throughput of the system.
4. When a Worker is done with a work request, it sends the response. For the purposes of this demo, the Aggregator Application tracks and displays status of each job in the summary table below.
5. The Aggregator Application and Worker Applications depend on a Solace Messaging Service for Cloud Foundry.
6. All of the apps are pushed to Cloud Foundry through the CLI or Pivotal Apps Manager.

## Deploying to Cloud Foundry

### Common Setup

This demo depends on Solace Messaging for Cloud Foundry being installed and available in the Cloud Foundry marketplace. If that is not already done, use the following links:

* [PCF Tile Download from Pivotal Network](https://network.pivotal.io/)
* [Solace Messaging for Pivotal Cloud Foundry Documentation](http://docs.pivotal.io/solace-messaging/)

The demo applications specify a dependency on a Solace Messaging service instance named `solace-messaging-demo-instance`. To create the required Solace messaging service instance, do the following:

	cf create-service solace-messaging shared solace-messaging-demo-instance

### Building

Clone this GitHub repository and build. For example:

```
  git clone https://github.com/SolaceLabs/sl-cf-solace-messaging-demo.git
  cd sl-cf-solace-messaging-demo
  ./gradlew build
```

### Deploying to Cloud Foundry

You need to deploy both the Aggregator and Worker application to Cloud Foundry. The included manifest.yml can be used to deploy both applications using the Cloud Foundry CLI from the project root directory:

    $ cf push
    
## Demo Components

As shown in the overview, this demo is made up of two Cloud Foundry applications. Both are Spring Boot Java Applications which make use of the [Solace Messaging Spring Cloud Connectors Extention](https://github.com/SolaceLabs/sl-spring-cloud-connectors) to establish the connection to Solace Messaging. The two applications are:

* Aggregator
* Worker

### Aggregator

application name: `cf-solace-messaging-demo-aggregator-app`

This application is a web based application that serves a simple web page. In that web pages you can enter requests which for this demo are described as `jobs`. The application then sends the requests as messages on the Solace Messaging service and it waits and correlates the subsequent replies.

### Worker

application name: `cf-solace-messaging-demo-worker-app`

The worker application is a Solace Messaging application that binds and listens to a Solace Non-Exclusive Queue. When it receives requests, it parses them, finds the work contents and simulates work by sleeping based on the parameters in the request. Once done it sends a correlated response back to the Solace Messaging Service to notify the Aggregator application.

## Using the Demo

### Accessing the Web UI

You can access the demo Web UI, by browsing to the Aggregator application's URL. You can find this URL from Pivotal Apps Manager or through the Cloud Foundry CLI as follows:

	$ cf apps
	Getting apps in org demo / space demo as demoUser...
	OK

	name                                      requested state   instances   memory   disk   urls
	cf-solace-messaging-demo-aggregator-app   started           1/1         512M     1G     cf-solace-messaging-demo-aggregator-app.cloudfoundry.io
	cf-solace-messaging-demo-worker-app       started           1/1         512M     1G     cf-solace-messaging-demo-worker-app.cloudfoundry.io

In this case the URL would be:

	cf-solace-messaging-demo-aggregator-app.cloudfoundry.io

The Web UI will look similar to this:

![Web UI Image](resources/web-ui.png)

### Adding a job

To add a new job, you fill in the web form by adding the following:

* Unique Job ID
* Work Count - This equates to number of messages to sent.
* Send Rate - How fast the Aggregator should send the messages. Sending faster than the workers can process creates a work backlog in the Solace queue. This is evident in the UI through larger than expected latency. 
* Min Delay - The lower bound for work delays. For each work message, the Aggregator will assign a random delay between the min and max.
* Max Delay - The upper bound for work delays.

### Understanding the Demo

Once a Job is added to the system, you will see it in the summary table at the bottom. Under Job Requests, the Aggregator status is reflected. The Job Summaries, provides insight into how quickly responses are coming back from the Worker Applications. If you create a scenario where the work is being sent faster than it can be produced (the send rate is faster than the min delay), the you can use the CF scale command to scale the Worker applications and increase the overall throughput of the system. This is the horizontal scaling aspect of the demo.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/SolaceLabs/sl-cf-solace-messaging-demo/graphs/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.

## References

Here are some interesting links if you're new to these concepts:

* [Martin Fowler on Microservices](http://martinfowler.com/articles/microservices.html)
* [A intro to Microservices Design Patterns](http://blog.arungupta.me/microservice-design-patterns/)
* [REST vs Messaging for Microservices](http://www.slideshare.net/ewolff/rest-vs-messaging-for-microservices)
* [Pivotal Network](https://network.pivotal.io/)
* [Solace Messaging for Pivotal Cloud Foundry Documentation](http://docs.pivotal.io/solace-messaging/)
* [The Solace Developer Portal](http://dev.solacesystems.com/)