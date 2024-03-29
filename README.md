﻿# **OneNet Deployment guide on Users Premisses**
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

To proceed with the installation of ONENET, the user must use the *docker* folder of this repository that contains all the necessary configuration.

1. The first step is to clone this repository <https://github.com/european-dynamics-rnd/OneNet> , by typing:
```
cd /opt/onenet-true-connector/
git clone https://github.com/european-dynamics-rnd/OneNet.git
```

2. There is the docker-compose.yml file located under /opt/onenet-true-connector/docker that contains all the configuration of the onenet fiware true connector containers. Go to that file by typing the command
```
cd /opt/onenet-true-connector/docker
```

3. :warning: At this step you must request from us a file with ".env" name and put it on the same folder /opt/onenet-true-connector/docker.
Email us on the helias.karagozidis@eurodyn.com to get the ".env" file.

4. :information_source: Replace the following environment variables values with "your_ip_where_the_containers_will_be_installed":
   - MONGO_HOST_IP
   - CONTEXT_BROKER_IP

5. After puting the ".env" file in position you can start the containers with the below commands  

```
$docker-compose up –d
$docker-compose logs -f
```
5. If no errors are seen, this means that ONENET FIWARE TRUE CONNECTOR was successfully deployed on your premisses.

## **Connection Settings Configuration through the User Interface**

For this step you must open the Onenet User Interface. 
The user interface is in a container that was installed on your premisses on the previous step.
It can be accessed through the url <code>http://the_ip_where_the_containers_are_installed:30003</code>, so

1. Login to the Ui Application using the <code>username</code> & <code>password</code> that you received from the onenet administrator.

![](image3.png)

2. Navigate to the connector settings by the sidebar menu & define the urls of your <code>Local Api Url</code>, <code>Data App Url</code>, <code>Ecc Url</code> & <code>Broker Url</code>  .

![](settings2.png)

Those 4 onenet applications are running on the containers that you installed, so the urls must be configured accordingly as shown below 

##### Local Api Url 
The url must be <code>http://your_ip_where_the_containers_are_installed:30001/api</code>

##### Data App Url 
The Data App is also located on the <code>http://your_ip_where_the_containers_are:8484</code> but it cannot be used this way. 
Data App Must Be Publicly Exposed In A Static Ip Via Https, before saved on the connection settings. 
This happens because Data App is served as an endpoint for peer to peer file transfer between you and other Onenet users. 
So the url must be the <code>https://your_static_url_that_points_to_dataapp_container</code>

##### Ecc Url
The Ecc Url is also located on the <code>https://your_ip_where_the_containers_are:8889/data</code> but it cannot be used this way. 
Ecc Url Must Be Publicly Exposed In A Static Ip Via Https, before saved on the connection settings. 
This happens because Ecc Url is served as an endpoint for peer to peer file transfer between you and other Onenet users. 
So the url must be the <code>https://your_static_url_that_points_to_ecc_url_container</code>

##### Broker Url 
The url must be <code>http://your_ip_where_the_containers_are:1026</code>

#### Optional Nginx configuration
Optionally you can use nginx as web server.

The folder _docker/nginx-connector-config/_ contains a [_docker-compose.yml_](docker/nginx-connector-config/docker-compose.yml) file and an example of nginx configuration ([_nginx.conf_](docker/nginx-connector-config/nginx.conf)).

The [_docker-compose.yml_](docker/nginx-connector-config/docker-compose.yml) defines the nginx service to start (from the official image), the exposed ports and the volumes to mount.

```
version: "3"
services:
  nginx:
   image : nginx:latest
   ports :
       - "8080:8080"
       - "80:80"
       - "443:443"
   volumes:
       - ./ssl:/etc/nginx/ssl
       - ./nginx.conf:/etc/nginx/conf.d/default.conf
``` 

The [(_nginx.conf_)](docker/nginx-connector-config/nginx.conf) contains header management, SSL configuration and location directives necessary for the correct functioning of the connector.

In particular, for each service to be exposed, a location type directive must be defined with the following configurations (please replace the _**path**_ and _**uri**_ placeholders with your own values):
```
  location /<path> {
    proxy_pass <uri>;
    proxy_redirect off;
    proxy_set_header    Upgrade     $http_upgrade;
    proxy_set_header    Connection  "upgrade";
    proxy_set_header Host $host:$server_port;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Ssl on;
    proxy_set_header  X-Forwarded-Proto  https;
    rewrite ^/<path>/(.*)$ /$1 break;
  }
``` 

For further information, refer to the [Official Nginx Guide](https://nginx.org/en/docs/).
