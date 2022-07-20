# FIWARE TRUE CONNECTOR

[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/fiware-true-connector.svg)](https://opensource.org/licenses/AGPL-3.0)
[![CI](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/workflows/ECC/badge.svg)](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container/actions/workflows/ECC.yml)
[![Documentation badge](https://readthedocs.org/projects/fiware-true-connector/badge/?version=latest)](https://fiware-true-connector.readthedocs.io/en/latest/)
<br/>
**FIWARE TRUE** (**TRU**sted **E**ngineering) **Connector** (**FTC** in short) for the IDS (International Data Space) ecosystem.

The FIWARE TRUE Connector enables the trusted data exchange in order to be active part of an IDS Ecosystem, a virtual data space leveraging existing standards and technologies, as well as governance models well-accepted in the data economy, to facilitate secure and standardized data exchange and data linkage in a trusted business ecosystem. The connector is compliant with the latest IDS specifications and can be easily customized to fit a wide spread of scenarios thanks to the internal separation of Execution Core Container and Data App. It is integrable with a lot of existing IDS services and totally configurable in terms of internal/external data format (multipart/mixed, multipart/form, http-header) and protocols (HTTP, HTTPS, Web Socket over HTTPS, IDSCPv2).

![FIWARE TRUE Connector Architecture Overview](docs/img/FTC_in_action.png?raw=true "FIWARE TRUE Connector Architecture Overview")

The TRUE Connector is composed of three components:

* [Execution Core Container (ECC)](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container), open-source project designed by ENG. It is in charge of the data exchange through the IDS ecosystem representing data using the IDS Information Model and interacting with an external Identity Provider. It is also able to communicate with an IDS Broker for registering and querying information.
* [FIWARE Data Application](https://github.com/Engineering-Research-and-Development/true-connector-fiware_data_app), open-source project designed by ENG. It represents a trivial data application for generating and consuming data on top of the ECC component.
* [Usage-Control (UC) Data Application](https://github.com/Engineering-Research-and-Development/true-connector-uc_data_app), a customized version of the Fraunhofer IESE base application for integrating the MyData Framework (a Usage Control Framework designed and implemented by Fraunhofer IESE) in a connector.

![FIWARE TRUE Connector Architecture](docs/img/TRUE_Connector_Architecture.png?raw=true "FIWARE TRUE Connector Architecture")

| :books: [Documentation](https://fiware-true-connector.readthedocs.io/en/latest/) | :whale: [Docker Hub](https://hub.docker.com/u/rdlabengpa) | :dart: [Roadmap](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/roadmap.md) |
| -------------------------------------------------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |

## Documentation Contents

-   [Home - Getting Started](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/index.md)
-   [Tutorial](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/true_connector_tutorial.md)
-   [Create new docker image](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/docker_readme.md)
-   [User & Programmers Manual](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/user_and_programmers_manual.md)
-   [Usage Control Rules](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/usage_control_rules.md)
-   [Broker](https://github.com/Engineering-Research-and-Development/fiware-true-connector/blob/master/docs/broker.md)
## License

The FIWARE TRUE Connector is licensed under [Affero General Public License (GPL) version 3](./LICENSE).

© 2021 Engineering Ingegneria Informatica S.p.A.


### Are there any legal issues with AGPL 3.0? Is it safe for me to use?

No problem in using a product licensed under AGPL 3.0. Issues with GPL (or AGPL) licenses are mostly related with the
fact that different people assign different interpretations on the meaning of the term “derivate work” used in these
licenses. Due to this, some people believe that there is a risk in just _using_ software under GPL or AGPL licenses
(even without _modifying_ it).

For the avoidance of doubt, the owners of this software licensed under an AGPL 3.0 license wish to make a clarifying
public statement as follows:

"Please note that software derived as a result of modifying the source code of this software in order to fix a bug or
incorporate enhancements is considered a derivative work of the product. Software that merely uses or aggregates (i.e.
links to) an otherwise unmodified version of existing software is not considered a derivative work, and therefore it
does not need to be released as under the same license, or even released as open source."
