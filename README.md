# cf-solace-messaging-demo

**TODO**

![](aggregator/src/main/resources/demo-overview.png)

## Common Setup

This demo depends on Solace Messaging for Cloud Foundry being installed and available in the Cloud Foundry marketplace. If that is not already done, use the following links:

* [PCF Tile Download from Pivotal Network](https://network.pivotal.io/)
* [Solace Messaging for Pivotal Cloud Foundry Documentation](http://docs.pivotal.io/solace-messaging/)

The demo applications specify a dependency on a Solace Messaging service instance named `solace-messaging-demo-instance`. To create the required Solace messaging service instance, do the following:

	cf create-service solace-messaging vmr-shared solace-messaging-demo-instance

### Building

Clone this repo, then build all demo applications with:

	$ ./gradlew build

### Deploying

You need to deploy both the Aggregator and Worker application to Cloud Foundry. You can do this as follows:

    cd aggregator
    cf push
    cd ..
    cd worker
    cf push

## Aggregator

application name: `cf-solace-messaging-demo-aggregator-app`

**TODO**

## Worker

application name: `cf-solace-messaging-demo-worker-app`

**TODO**

## Try out the Applications

**TODO**


## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Authors

See the list of [contributors](https://github.com/mdspielman/cf-solace-messaging-demo/contributors) who participated in this project.

## License

This project is licensed under the Apache License, Version 2.0. - See the [LICENSE](LICENSE) file for details.