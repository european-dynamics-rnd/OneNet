# FIWARE TRUE Connector Tutorial

This is a step-by-step tutorial that will introduce in detail how to exchange data in a trusted environment using the FIWARE TRUE Connector.

## What is IDS?

The International Data Space (IDS) is a virtual data space leveraging existing standards and technologies, as well as governance models well-accepted in the data economy, to facilitate secure and standardized data exchange and data linkage in a trusted business ecosystem.
It thereby provides a basis for creating smart-service scenarios and facilitating innovative cross-company businessprocesses, while at the same time guaranteeing data sovereignty for data owners.

## Components

The FIWARE TRUE Connector is composed by:

-   **Execution core container (ECC)**, representing the connector exchanging data.
-   **FIWARE data app**, it is in charge of processing incoming request and provided the relative responses.
-   **Usage Control data app (UC)**, it will check if who are requesting the data has the grants to use that in a well defined policy.
	(The FIWARE TRUE Connector integrates the [Fraunhofer MyData Framework](https://www.mydata-control.de/) for implementing the Usage Control. Details about the PMP and PEP components can be found [here](usage_control_rules.md))

![TRUE Connector Architecture](img/TRUE_Connector_Architecture.png?raw=true "TRUE Connector Architecture")

The connector can be run as consumer (send the request to the provider to obtain some data) or provider (provide the data to the consumers if allowed from the policies in UC).

## Services

### Supported Identity Providers
An Identity Provider offers a service to create, maintain, manage, monitor, and validate identity information of and for participants.

If it is needed to run the connector in developer mode please set the following properties as false:

```
CACHE_TOKEN=false
FETCH_TOKEN_ON_STARTUP=false
application.isEnabledDapsInteraction=false
```

This allow you to skip  the Daps interaction during the process in the development phase, otherwise you need to have a certificate provided from the CA offering Identity provider service.


The FIWARE TRUE Connector is able to interact with the following Identity Providers:

* **AISECv1** put the certificate in the *cert* folder, edit related settings (i.e., *application.keyStoreName*, *application.keyStorePassword*) (in the *.env*) and set the *application.dapsVersion* (in the *resources/application-docker.properties*) to *v1*
* **AISECv2** put the certificate in the *cert* folder,edit related settings (i.e., *application.keyStoreName*, *application.keyStorePassword*) (in the *.env*) and set the *application.dapsVersion* (in the *resources/application-docker.properties*) to *v2*
* **ORBITER** put the certificate in the *cert* folder, edit related settings (i.e., *application.daps.orbiter.privateKey*, *application.daps.orbiter.password*) (in the *.env*) and set the *application.dapsVersion* (in the *resources/application-docker.properties*) to *orbiter*


The *application.dapsUrl* (in the *resources/application-docker.properties*) property must be set properly in order to address the right DAPS server.

### Clearing House
The Clearing House is an intermediary that provides clearing and settlement services for all financial and data exchange transactions (logs all activities performed in the course of a data exchange).
The FIWARE TRUE Connector supports the communication with the ENG Clearing House for registering transactions, available as a service at:

```
CLEARING_HOUSE=http://109.232.32.193:8280/data
```

### Broker
The Broker is an intermediary that stores and manages information about the data sources available.
The FIWARE TRUE Connector integrates some endpoints for interacting with an IDS Broker described in [Broker](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/broker.md) section

## Flow

The communication/message exchange starts from the data app level, that will expose APIs. Then the data app will forward the message to ECC that is in charge of enstablish a trusted communication with the other connector and services (CH, Broker, Identity Providers).
Click [here](user_and_programmers_manual.md) for the manual explaining how to performe a test.
