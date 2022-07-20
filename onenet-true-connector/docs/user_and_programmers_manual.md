## How to Configurate and Run

The configuration should be performed customizing the following variables in the **.env** file:

* **DATA_APP_ENDPOINT=192.168.56.1:8084/data** DataAPP endpoint for receiveing data (F endpoint in the above picture)
* **MULTIPART_EDGE=mixed** DataAPP A-endpoint Content Type (choose *mixed* for Multipart/mixed or *form* for Multipart/form-data or *http-header* for Multipart/http-header) 
* **MULTIPART_ECC=mixed** Execution Core Container B-endpoint Content Type (choose *mixed* for Multipart/mixed or *form* for Multipart/form-data or *http-header* for Multipart/http-header) 
* Edit external ports if need (default values: **8086** for **WS over HTTPS**, **8090** for **http**, **8889** for **B endpoint**, **29292** for **IDSCP2**)
* Forward-To protocol validation can be changed by editing **application.validateProtocol**. Default value is *true* and Forward-To URL must be set like http(https,wss)://example.com, if you choose *false* Forward-To URL can be set like http(https,wss)://example.com or just example.com and the protocol chosen (from application.properties)will be automatically set (it will be overwritten! example: http://example.com will be wss://example if you chose wss in the properties).
* For websocket configuration, in DataApp resource folders, configure *config.properties* file, set following fields

```
server.ssl.key-password=changeit
server.ssl.key-store=/cert/ssl-server.jks
```
Or leave default values, if certificate and its password are correct.

## Endpoints
The TRUE Connector will use two protocols (http and https) as described by the Docker Compose File.
It will expose the following endpoints:

```
/proxy 
```
to receive data incomming request, and based on received request, forward request to Execution Core Connector (the P endpoint in the above picture)

``` 
/data 
```
to receive data (IDS Message) from a sender connector (the B endpoint in the above picture)
Furthermore, just for testing it will expose (http and https):

```
/about/version 
```
returns business logic version 

## Configuration
The ECC supports three different way to exchange data:

*  **REST endpoints** enabled if *WS_EDGE=false* and *WS_ECC=false*
*  **IDSCP2** enabled if *IDSCP2=true* and WS_ECC = false </br>For *WS_EDGE=true* (use websocket on the edge, false for REST on the edge) 
*  **Web Socket over HTTPS** enabled if *WS_EDGE=true* and *WS_ECC=true* and *IDSCP2=false* for configuration which uses web socket on the edge and between connectors.

For trusted data exchange define in *.env* the SSL settings:

*  KEYSTORE-NAME=changeit(JKS format)
*  KEY-PASSWORD=changeit
*  KEYSTORE-PASSWORD=changeit
*  ALIAS=changeit

## How to Test
The reachability could be verified using the following endpoints:

*  **http://{IP_ADDRESS}:{HTTP_PUBLIC_PORT}/about/version**

Keeping the provided docker-compose, for Data Provider URL will be:

*  **http://{IP_ADDRESS}:8090/about/version**

For Data Consumer, with provided docker-compose file:

*  **http://{IP_ADDRESS}:8091/about/version**


## How to Exchange Data

For details on request samples please check following link [Basic Data App Usage](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/README.md)

Be sure to use correct configuration/ports for sender and receiver Data App and Execution Core Container (check .env file).

Default values:

```
DataApp URL: https://{IPADDRESS}:8084/proxy 
"Forward-To": "https://{RECEIVER_IP_ADDRESS}:8889/data",
```

For WSS flow:

```
DataApp URL: https://{IPADDRESS}:8084/proxy
"multipart": "wss",
"Forward-To": "wss://ecc-provider:8086/data",
"Forward-To-Internal": "wss://ecc-consumer:8887",
```

### WebSocket 

On the following link, information regarding WebSocket Message Streamer implementation can be found here [WebSocket Message Streamer library](https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer).

### IDSCP2
Follow the REST endpoint or WS examples, put the server hostname/ip address in the Forward-To header (*wss/https://{RECEIVER_IP_ADDRESS/Hostname}:{WS_PUBLIC_PORT}*).
