# OneNet Connector FIWARE Data App

## Docker setting

The Fiware Data APP Docker image is available in a private docker registry (hosted by ENG). Please add the registry in your [docker settings](https://docs.docker.com/registry/insecure/).


```
{
  "insecure-registries" : ["109.232.32.194:5000"]
}
```


## Start services
```

cd onenet-true-connector/docker

docker-compose up -d
```

## API Examples

### Create Entity
```
curl --location --request POST 'http://<DATAAPP_IP_ADDRESS>:8084/createentity' `
--header 'Content-Type: application/ld+json' `
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
            "streetAddress": "Via Emanuele Gianturco 15",
            "addressRegion": "Campania",
            "addressLocality": "Napoli",
            "postalCode": "80142"
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
             "coordinates": [14.286190426655033, 40.845961239858234]
        }
    },
    "name": {
        "type": "Property",
        "value": "Engineering Ingegneria Informatica"
    },
    "@context": [
        "https://fiware.github.io/data-models/context.jsonld",
        "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld"
    ]
}'
```

### Registration

```
curl --location --request POST 'http://<DATAAPP_IP_ADDRESS>:8084/registration' `
--header 'Content-Type: application/ld+json' `
--data-raw '{
    "entityId": "urn:ngsi-ld:Building:store001",
    "eccUrl": "http://<PROVIDER_IP_ADDRESS>:8889/data",
    "brokerUrl": "http://<PROVIDER_IP_ADDRESS>:1026"
}'
```

### Get Entity

```
curl --location --request GET 'http://<DATAAPP_IP_ADDRESS>:8084/getentity/urn:ngsi-ld:Building:store001' `
--header 'host;' `
--header 'accept: application/json'
```

