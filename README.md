# cf-solace-messaging-demo

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

Clone this repo, then build all demo applications with:

	$ ./gradlew build

### Deploying to Cloud Foundry

You need to deploy both the Aggregator and Worker application to Cloud Foundry. The included manifest.yml can be used to deploy both applications using the Cloud Foundry CLI from the project root directory:

    $ cf push
    
## Demo Components

**TODO**

### Aggregator

application name: `cf-solace-messaging-demo-aggregator-app`

**TODO**

### Worker

application name: `cf-solace-messaging-demo-worker-app`

**TODO**

## Using the Demo

**TODO**


## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/mdspielman/cf-solace-messaging-demo/contributors) who participated in this project.

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