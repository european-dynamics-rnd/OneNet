# **OneNet Deployment guide on Users Premisses**
## **Prerequisites and Installation**
The hardware and operating system prerequisites are:

- A 2-core processor 
- 4GB RAM Memory
- 50GB of disk space or more

The software prerequisites include:

- Centos 7 or Windows Server Operative System (OS);
- docker and docker-compose;

Onenet software and its components will be delivered utilizing the Docker containers functionalities. Firstly, the Docker platform has to be downloaded and installed accordingly to the OS of the server to host the deployment. 

### **For Windows server**

1. Install Docker from <https://hub.docker.com/?overlay=onboarding> (First you will need to create an account on the same page)
1. After the installation if Docker has not started automatically open start Menu, type Docker and select the Docker icon that appears.

![](image1.png)

*Figure 1. Launching Docker (Windows Server)*

3. Docker may take some time to start. When it has successfully started you should able to see the icon on the bottom right of your screen

![](image2.png)

*Figure 2. Docker Successfully Started (Windows Server)*

**Troubleshooting:**

- Docker in order to run requires specific support from the CPU but most recent PCs should support it. 
- Hyper-V should be installed and enabled in windows and Virtualization should be enabled in your BIOS.  Please also consult the following page <https://docs.docker.com/docker-for-windows/troubleshoot/#virtualization/> .

### **For Linux server**

1. Update the apt package index and install packages to allow apt to use a repository over HTTPS. 

For 64-bit version of CentOS type:
```
$ sudo yum update

$ sudo yum install \
 apt-transport-https \
 ca-certificates \
 curl \
 gnupg-agent \
 software-properties-common
```

For 64-bit version of one of Ubuntu versions (Groovy 20.10, Focal 20.04 (LTS), Bionic 18.04 (LTS), Xenial 16.04 (LTS)) type:
```
$ sudo apt-get update

$ sudo apt-get install \
apt-transport-https \
ca-certificates \
curl \
gnupg-agent \
software-properties-common
```
2. Add Docker’s official GPG key

`$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -`

3. Verify the key by searching for the last 8 characters of the fingerprint.

`sudo apt-key fingerprint <last 8 characters of the fingerprint >`

4. Install Docker. According to the Docker installation[^1] we should:
- Set up the Docker repository.

For 64-bit version of CentOS type:
```
$sudo yum install -y yum-utils
$sudo yum-config-manager \
  --add-repo \
  https://download.docker.com/linux/centos/docker-ce.repo
```
For 64-bit version of Ubuntu use the following command to set up the stable repository:
```
$ sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
$(lsb_release -cs) \
   stable"
```
- Install the latest version of Docker Engine and *containerd*.

For 64-bit version of CentOS type:

`$sudo yum install docker-ce docker-ce-cli containerd.io`

For 64-bit versions of Ubuntu type:

`$ sudo apt-get install docker-ce docker-ce-cli containerd.io`

- Start and enable docker by typing:
```
$sudo systemctl start docker
$sudo systemctl enable docker
```
- Verify that Docker Engine is installed correctly by running the hello-world image:
  
`$ sudo docker run hello-world`

This command downloads a test image and runs it in a container. When the container runs, it prints an informational message and exits


5. Install Docker compose. Docker Compose is a tool for defining and running multi-container Docker applications. With Compose, we use a YAML Ain't Markup Language (YAML) file to configure our application’s services. Then, with a single command, we create and start all the services from our configuration. According to the docker compose installation  we should:
- Download the current stable release of Docker Compose, by typing:
  
`$ curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose`

- Apply executable permissions to the binary, by typing:

`$sudo chmod +x /usr/local/bin/docker-compose`

## **Onenet Containers Installation on Docker**

ONENET is available online in six specified co-existing docker images to efficiently distribute the software avoiding the likelihood of collisions. Therefore, there is one image specified to host the backend application (*dras-backend*) as well as one for the frontend application (*dras-frontend*). Two other docker images are created to host the ONENET database (*dras-db*) and the PhpMyAdmin application (*phpmyadmin*) to be able to access the database from a UI for administrative purposes, also an image with Redis server (*redis*) is created and used by ONENET for caching and the sixth docker image provides access and navigation to the log files of ONENET and is used for debugging purposes.

To proceed with the installation of ONENET, the user must use the *docker* folder that contains all the necessary configuration of the backend, frontend and the database.

6. Next step is to deploy Onenet using Docker Compose. First step is to clone the <https://github.com/european-dynamics-rnd/OneNet>  repository, by typing:
```
cd /opt/onenet-true-connector/
git clone https://github.com/european-dynamics-rnd/OneNet.git
```
7. Use the following yml file (docker-compose.yml), located under */opt/onenet/onenet-true-connector/docker to configure all aspects of the onenet fiware true connector container:
```
version: '2.2'
name: "onenet-fiware-true-connector"
services:
  ecc-provider: 
    image: rdlabengpa/ids_execution_core_container:v1.9.3
    ports:
      - "${PROVIDER_PORT}:${INTERNAL_REST_PORT}"  #Port for exposing HTTP endpoints 
      - 8889:8889  #Exposed port for receiving data from another connector (REST)
      - 8086:8086  #Exposed port for receiving data from another connector (WS)
    environment:
       - "SPRING_PROFILES_ACTIVE=docker"  
       - DATA_APP_ENDPOINT=${PROVIDER_DATA_APP_ENDPOINT}      #Data APP enpoint for consuming received data 
       - MULTIPART_EDGE=${PROVIDER_MULTIPART_EDGE}                      #Data APP endpoint multipart/mixed content type
       - MULTIPART_ECC=${MULTIPART_ECC} 
       - REST_ENABLE_HTTPS=${REST_ENABLE_HTTPS}
       - IDSCP2=${IDSCP2}
       - WS_EDGE=${PROVIDER_WS_EDGE}
       - WS_ECC=${WS_ECC}
       - UC_DATAAPP_URI=${PROVIDER_UC_DATAAPP_URI}
       - CLEARING_HOUSE=${CLEARING_HOUSE}
       - PUBLIC_PORT=${PROVIDER_PORT}
       - BROKER_URL=${BROKER_URL}
       - DISABLE_SSL_VALIDATION=${DISABLE_SSL_VALIDATION}
       - CACHE_TOKEN=${CACHE_TOKEN}
       - FETCH_TOKEN_ON_STARTUP=${FETCH_TOKEN_ON_STARTUP}
       - SERVER_SSL_ENABLED=${SERVER_SSL_ENABLED}
       - KEYSTORE_NAME=${KEYSTORE_NAME}
       - KEY_PASSWORD=${KEY_PASSWORD}
       - KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
       - ALIAS=${ALIAS}
       - DAPS_KEYSTORE_NAME=${DAPS_KEYSTORE_NAME}
       - DAPS_KEYSTORE_PASSWORD=${DAPS_KEYSTORE_PASSWORD}
       - DAPS_KEYSTORE_ALIAS=${DAPS_KEYSTORE_ALIAS}
       - TRUSTORE_NAME=${TRUSTORE_NAME}
       - TRUSTORE_PASSWORD=${TRUSTORE_PASSWORD}
       - TZ=Europe/Rome                
    volumes:
      - ./ecc_resources_provider:/config
      - ./ecc_cert:/cert
    extra_hosts:
      - "ecc-consumer:172.17.0.1"

  uc-dataapp-provider:
    image: rdlabengpa/ids_uc_data_app:v0.0.6
    environment:
      - TZ=Europe/Rome 
    ports:
      - "9552:9555"
      - "8043:43"
    volumes:
      - ./policies:/policies
      - ./uc-dataapp_resources:/config

  be-dataapp-provider:
    # image: rdlabengpa/ids_fiware_data_app:v0.0.2
    image: 109.232.32.194:5000/onenet-connector-fiware-data-app:0.1
    environment:
       - "SPRING_PROFILES_ACTIVE=docker"  
       - DATA_APP_MULTIPART=${PROVIDER_MULTIPART_EDGE}
       - SERVER_SSL_ENABLED=${SERVER_SSL_ENABLED}
       - KEYSTORE_NAME=${KEYSTORE_NAME}
       - KEY_PASSWORD=${KEY_PASSWORD}
       - KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
       - ALIAS=${ALIAS}
       - TZ=Europe/Rome 
       - ECC_HOSTNAME=ecc-provider
       - IP_PROVIDER_CONTEXT_BROKER=${LOCAL_IP_PROVIDER_CONTEXT_BROKER}
       - ECC_PORT=8889
    ports:
      - "8083:8083"
      - "9000:9000"
    volumes:
      - ./be-dataapp_resources:/config
      - ./be-dataapp_data_receiver:/data
      - ./ecc_cert:/cert

  ecc-consumer: 
    image: rdlabengpa/ids_execution_core_container:v1.9.3
    ports:
      - "${CONSUMER_PORT}:${INTERNAL_REST_PORT}"  #Port for exposing HTTP endpoints 
      - 8890:8889  #Exposed port for receiving data from another connector (REST)
      - 8087:8086  #Exposed port for receiving data from another connector (WS)
    environment:
       - "SPRING_PROFILES_ACTIVE=docker"  
       - DATA_APP_ENDPOINT=${CONSUMER_DATA_APP_ENDPOINT}    #Data APP enpoint for consuming received data 
       - MULTIPART_EDGE=${CONSUMER_MULTIPART_EDGE}          #Data APP endpoint multipart/mixed content type
       - MULTIPART_ECC=${MULTIPART_ECC}
       - REST_ENABLE_HTTPS=${REST_ENABLE_HTTPS}
       - IDSCP2=${IDSCP2}
       - WS_EDGE=${CONSUMER_WS_EDGE}
       - WS_ECC=${WS_ECC}
       - CLEARING_HOUSE=${CLEARING_HOUSE}
       - UC_DATAAPP_URI=${CONSUMER_UC_DATAAPP_URI}
       - PUBLIC_PORT=${CONSUMER_PORT}
       - BROKER_URL=${BROKER_URL}
       - DISABLE_SSL_VALIDATION=${DISABLE_SSL_VALIDATION}
       - CACHE_TOKEN=${CACHE_TOKEN}
       - FETCH_TOKEN_ON_STARTUP=${FETCH_TOKEN_ON_STARTUP}
       - SERVER_SSL_ENABLED=${SERVER_SSL_ENABLED}
       - KEYSTORE_NAME=${KEYSTORE_NAME}
       - KEY_PASSWORD=${KEY_PASSWORD}
       - KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
       - ALIAS=${ALIAS}
       - DAPS_KEYSTORE_NAME=${DAPS_KEYSTORE_NAME}
       - DAPS_KEYSTORE_PASSWORD=${DAPS_KEYSTORE_PASSWORD}
       - DAPS_KEYSTORE_ALIAS=${DAPS_KEYSTORE_ALIAS}
       - TRUSTORE_NAME=${TRUSTORE_NAME}
       - TRUSTORE_PASSWORD=${TRUSTORE_PASSWORD}
       - TZ=Europe/Rome                
    volumes:
      - ./ecc_resources_consumer:/config
      - ./ecc_cert:/cert
    extra_hosts:
      - "ecc-provider:172.17.0.1"

  uc-dataapp-consumer:
    image: rdlabengpa/ids_uc_data_app:v0.0.6
    environment:
      - TZ=Europe/Rome  
    ports:
      - "9553:9555"
      - "8044:43"
    volumes:
      - ./policies:/policies
      - ./uc-dataapp_resources:/config

  be-dataapp-consumer:
    # image: rdlabengpa/ids_fiware_data_app:v0.0.2
    image: 109.232.32.194:5000/onenet-connector-fiware-data-app:0.1
    environment:
       - "SPRING_PROFILES_ACTIVE=docker"  
       - DATA_APP_MULTIPART=${CONSUMER_MULTIPART_EDGE}
       - SERVER_SSL_ENABLED=${SERVER_SSL_ENABLED}
       - KEYSTORE_NAME=${KEYSTORE_NAME}
       - KEY_PASSWORD=${KEY_PASSWORD}
       - KEYSTORE_PASSWORD=${KEYSTORE_PASSWORD}
       - ALIAS=${ALIAS}
       - TZ=Europe/Rome     
       - ECC_HOSTNAME=ecc-consumer
       - IP_PROVIDER_CONTEXT_BROKER=${LOCAL_IP_PROVIDER_CONTEXT_BROKER}       
       - ECC_PORT=8887
    ports:
      - "8084:8083"
      - "9001:9000"
    volumes:
      - ./be-dataapp_resources:/config
      - ./be-dataapp_data_sender:/data
      - ./ecc_cert:/cert
  orion:
    image: fiware/orion-ld:0.7.0
    hostname: orion
    container_name: fiware-orion
    expose:
      - "1026"
      - "8083"
    ports:
      - "1026:1026"
    depends_on:
      - mongo-db
    #command: -dbhost mongo-db -logLevel DEBUG
    #command: -statCounters -dbhost mongo-db -logLevel DEBUG -forwarding
    command: -dbhost 'mongo-db' -db orion -dbuser onenet -dbpwd true2022 -statCounters -logLevel DEBUG -forwarding

  mongo-db:
    image: mongo:3.6
    hostname: mongo-db
    container_name: db-mongo
    environment:
       - MONGO_INITDB_DATABASE=orion
       - MONGO_INITDB_ROOT_USERNAME=onenet
       - MONGO_INITDB_ROOT_PASSWORD=true2022               
    ports:
      - "27017:27017" 
    networks:
      - default
    command: --auth --nojournal
#    volumes:
#      - mongo-db:/data
    volumes:
#      - mongo-db:/data
      - ./data-consumer:/data/db
      - ./mongo-init.js:/docker-entrypoint-initdb.d/mongo-init.js:ro
  local_api:
    build:
      context: local-api
      args:
        SOFIA_URI: https://onenet-ngsi-ld.eurodyn.com/api
      dockerfile: Dockerfile
    image: local-api
    container_name: local-api
    ports:
      - "30001:30001"
      - "30000:30000"
    restart: unless-stopped
volumes:
  mongo-db: ~
```
8. Finally execute:
   
```
$docker-compose up –d
$docker-compose logs -f
```
9. If no errors are seen, this means that ONENET FIWARE TRUE CONNECTOR was successfully deployed.

## **Central Registry Ui Configuration**

### **Certificate Installation**

1. The Local API component runs locally on each Users Premisses as a web service on the port 30000 (which is similar to deploying a local server) and the communication is done through the browser. Central Registry API uses encrypted communication (https) but because Local API is deployed locally on users premisses its certificate also needs to be installed on the same PC before it can be used.

- Download the certificate from https://onenet-ngsi-ld.eurodyn.com/installation-files/feg-local.crt.

- Double click the downloaded file and choose Install Certificate on the window that pops up

![](image6.png)

- Follow the configuration shown below in order to install the certificate on the window that pops up

![](image7.png)
![](image8.png)
![](image9.png)
![](image10.png)
![](image11.png)

- Restart Your Browser
<br>
### **Local Applications subscription To the Central Registry**
<br>
2. Login to the Ui Central Registry From <https://onenet-ngsi-ld.eurodyn.com> using the username & password that you received from the onenet administrator.

![](image3.png)


[^1]: <https://docs.docker.com/engine/install/centos/> 

3. Navigate to the connector settings by the sidebar menu & define the local installation urls of your <b>Local API</b>, <b>Fiware Url</b> & <b>Data App</b> .

![](image4.png)