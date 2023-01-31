# GeoVER: Geo-warning for the transport sector using extended reality in the air traffic application area


## Prerequisites

Before getting started, make sure that tinyFaaS is running and here is the [tinyFaaS git repository](https://github.com/OpenFogStack/tinyFaaS).

Run the following commands in tinyFaas to start the Management Service and Reverse Proxy.

```bash
make
```

OR

```bash
docker build -t tinyfaas-mgmt ./src/
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -p 8080:8080 --name tinyfaas-mgmt -d tinyfaas-mgmt tinyfaas-mgmt
```

## Getting Started
You could follow these steps to simply get started:

1. Start GeoBroker Server by run the main method of *de.hasenburg.geobroker.server.main.Server.kt*.
2. Start the Rule Manager main method in *de.hasenburg.geover.rulemanager.kt*, and input the subscription and function path.
3. Using the Simple Publishing Client in *de.hasenburg.geobroker.client.main.simplepublishclinet.kt* to publish events to geobroker server.
4. The matching events will be sent to tinyFaaS and the user defined function would process the rules to these matching events.


## Introduction
Here mainly 4 files: BridgeBuilder, CmdToTinyFaaS, RuleManager, UserSpecifiedRule

### Bridge Builder
The GeoBroker Configuration could be defined here with *GeoBroker host, GeoBroker port and tinyFaaS base url*. Default configuration is: *GEOBROKER_HOST = "localhost", GEOBROKER_PORT = 5559, TINYFAAS_BASE_URL = "http://localhost:80/"*.
- For every subscription, a Simple Client is created with the unique id in *TinyFaaSClient, functionName, subscription topic, geofence and the system nanotime*.
- Send connect payload and subscribe payload are send afterwards, if there is/are publish payload received then the matching events would be sent to tinyFaaS in Json format.

### UserSpecificRule
Give an entry for users to send the subscription and function, and for GeoVER the functions are mainly about rules.

### Rule Manager
Creat/Delete rules here, and upload/delete the user function to tinyFaaS.

#### Define Subscription
You need to input the Topic and Location(latitude, longitude and radius for the circle area) to define the subscription.
Subscription topics should be defined in the format like */a/b/c*, the lat/lon/radius should be in double.

#### Function operation
To upload the user defined function to tinyFaaS, the absolute file path is needed. 
To delete the function, the function name is needed. The function name is topic without slash, for example, if the topic is */berlin/1/test*, then the function name would be *"berlin1test"* .


### CmdToTinyFaas
The cmd commands that needed to send to tinyFaaS are defined in this file and the absolute path of tinyFaaS scripts needs to be defined first.
















