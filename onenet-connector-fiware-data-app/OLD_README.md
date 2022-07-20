# TRUE Connector FIWARE Data App

NOTE: At the moment of writing only HTTP is supported by the Orion Context Broker which means that all endpoints in the requests must be HTTP (Connection between DataApps and Brokers must be HTTP)

## Context Broker - Entities

There are docker files provided for Orion Context Broker, located in doc directory, one for consumer and other for provider. Start those 2 docker files simply by running following command:

```
docker-compose -f docker-compose-cb.yml up
```
for consumer, and

```
docker-compose -f docker-compose-provider-cb.yml up
```
for provider.

NOTE: on Unix based OS, you might need to have admin/sudo rights to run docker.


Once those 2 containers are up and running, you can access Orion API using following URL:

**Consumer Orion Context Broker**

```
http://localhost:1026/ngsi-ld/v1/entities
```

**Provider Orion Context Broker**

```
http://localhost:1027/ngsi-ld/v1/entities
```

First, create entity in Provider ContextBroker, using following CURL command (can be imported in Postman)

```
curl --location --request POST 'http://localhost:1027/ngsi-ld/v1/entities/' \
--header 'Content-Type: application/ld+json' \
--data-raw '{
    "id": "urn:ngsi-ld:Building:store001",
    "type": "Building",
    "category": {
    	"type": "Property",
        "value": ["commercial"]
    },
    "address": {
        "type": "Property",
        "value": {
            "streetAddress": "Bornholmer Straße 65",
            "addressRegion": "Berlin",
            "addressLocality": "Prenzlauer Berg",
            "postalCode": "10439"
        },
        "verified": {
			"type": "Property",
			"value": true
		}
    },
    "location": {
        "type": "GeoProperty",
        "value": {
             "type": "Point",
             "coordinates": [13.3986, 52.5547]
        }
    },
    "name": {
        "type": "Property",
        "value": "Bösebrücke Einkauf"
    },
    "@context": [
        "https://fiware.github.io/data-models/context.jsonld",
        "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld"
    ]
}'


```

You can verify that entity is created in Provider ContextBroker using provided URL 

```
http://localhost:1027/ngsi-ld/v1/entities/urn:ngsi-ld:Building:store001

```

Next step, create Registration on Consumer ContextBroker, by executing following CURL:

```
curl --location --request POST 'http://localhost:1026/ngsi-ld/v1/csourceRegistrations/' \
--header 'Content-Type: application/ld+json' \
--data-raw ' {
     "@context": [
			"https://fiware.github.io/data-models/context.jsonld",
			"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld"
		],
    "type": "ContextSourceRegistration",
    "information": [
        {
            "entities": [
                {
                    "type": "Building",
                    "id": "urn:ngsi-ld:Building:store001"
                }
            ]
        }
    ],
    "endpoint": "http://IPADRESS_FROM_CONSUMER_DATA_APP:8084/"
}'

```

This registration will redirect request for entity with id **urn:ngsi-ld:Building:store001** to the endpoint **http://IPADRESS_FROM_CONSUMER_DATA_APP:8084**,(for example Docker IP adress in format x.x.x.x) which iz FIWARE consumer dataApp, responsible for packing request and sending it to ECC, and unpacking response.

Once this registration is in place you can request entity with id **urn:ngsi-ld:Building:store001** from Consumer ContextBroker, by sending GET request to:

```
http://localhost:1026/ngsi-ld/v1/entities/urn:ngsi-ld:Building:store001

```

NOTE that the port is 1026, for Consumer ContextBroker.


## DataApp

2 properties are used to configure connection:

**application.fiware.contextBroker.provider.url=http://IPADRESS_FROM_PROVIDER_BROKER:1027** (for example Docker IP adress in format x.x.x.x)

Used to configure connection between provider DataApp and Provider Orion ContextBroker

**application.fiware.ecc.provider.url=https://ecc-provider:8889/data**

Configure B-endpoint of Provider Connector (aka Forward-To; this one can be HTTPS it depends on the connection between ECCs)



[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-fiware_data_app.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Docker badge](https://img.shields.io/docker/pulls/rdlabengpa/true-connector-fiware_data_app.svg)](https://hub.docker.com/r/rdlabengpa/true-connector-fiware_data_app)
<br/>

