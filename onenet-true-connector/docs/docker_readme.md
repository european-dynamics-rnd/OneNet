## How to build a Docker image

Building a new Docker image for the TRUE Connector can be useful if you want to integrate your changes without relying
on Docker volumes.

Each component can be build as an indipendent Docker image, the operation is quite easy after clone the source code of:
* [ECC](https://github.com/Engineering-Research-and-Development/true-connector-execution_core_container)
* [UC](https://github.com/Engineering-Research-and-Development/true-connector-uc_data_app)
* [data App](https://github.com/Engineering-Research-and-Development/true-connector-fiware_data_app)


```bash
cd <path to source code root directory>
docker build -f Dockerfile -t <registry-name/hub-user>/<repo-name>:<version-tag> .

Example:
cd true-connector-execution_core_container
docker build -f Dockerfile -t johndoe/execution_core_container:1.0 .
```

Be aware that using an existing Docker Hub username is required to push the image to Docker Hub

Pushing docker image to remote repo can be performed using following command

```
docker push <registry-name/hub-user>/<repo-name>:<version-tag>

Example:
docker push -f Dockerfile -t johndoe/execution_core_container:1.0 .
```
