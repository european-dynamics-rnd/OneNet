# Welcome to FIWARE TRUE (TRUsted Engineering) Connector

The **FIWARE TRUE Connector** (**FTC** in short) is a connector for the IDS (International Data Space) ecosystem. FTC enables the trusted data exchange in order to be active part of an IDS Ecosystem, a virtual data space leveraging existing standards and technologies, as well as governance models well-accepted in the data economy, to facilitate secure and standardized data exchange and data linkage in a trusted business ecosystem. The connector is compliant with the latest IDS specifications and can be easily customized to fit a wide spread of scenarios thanks to the internal separation of Execution Core Container and Data App. It is integrable with a lot of existing IDS services and totally configurable in terms of internal/external data format (multipart/mixed, multipart/form, http-header) and protocols (HTTP, HTTPS, Web Socket over HTTPS, IDSCPv2).

![FIWARE TRUE Connector Architecture Overview](img/FTC_in_action.png?raw=true "FIWARE TRUE Connector Architecture Overview")

The TRUE Connector is composed of three components:

* [Execution Core Container (ECC)](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container), open-source project designed by ENG. It is in charge of the data exchange through the IDS ecosystem representing data using the IDS Information Model and interacting with an external Identity Provider. It is also able to communicate with an IDS Broker for registering and querying information.
* [FIWARE Data Application](https://github.com/Engineering-Research-and-Development/true-connector-fiware_data_app), open-source project designed by ENG. It represents a trivial data application for generating and consuming data on top of the ECC component.
* [Usage-Control (UC) Data Application](https://github.com/Engineering-Research-and-Development/true-connector-uc_data_app), a customized version of the Fraunhofer IESE base application for integrating the MyData Framework (a Usage Control Framework designed and implemented by Fraunhofer IESE) in a connector.

![FIWARE TRUE Connector Architecture](img/TRUE_Connector_Architecture.png?raw=true "FIWARE TRUE Connector Architecture")

The FTC Execution Core Container is the core component of an IDS Connector enabling:

* the data exchange between connectors, using several protocols like HTTP, HTTPS, WS over HTTPS, IDSCPv2
* the data (IDS Message header and payload) representation using multipart/mixed, multipart/form and http-header
* interaction with the several Identity Providers (Fraunhofer AISECv1, Fraunhofer AISECv2, Orbiter) and DAPS Service for requiring and validating a token
* communication with the ENG Clearing House for registering transactions
* the interaction with the IDS Metadata Broker 
* the integration of the Usage Control App in order to enforce the contract rules.


# Getting Started

As first step we suggest you to follow the introduction available in the [Tutorial](true_connector_tutorial.md) section, then the guidelines to perfome a test in the [manual](user_and_programmers_manual.md).

If you want to use your own certificate for the AISEC Fraunhofer DAPS server:

* Put **DAPS certificates** into the cert folder and edit related settings (i.e., *application.keyStoreName*, *application.keyStorePassword*) into the *resources/application.properties* file

You can find a complete docker-compose example in the the docker folder. If you want to run the application:

```
cd fiware-true-connector/docker/
docker-compose up -d
```
