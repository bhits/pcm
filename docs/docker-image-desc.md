# Short Description
PCM provides service to manage patient provider lists and enables patients to manage their e-consents.

# Full Description

# Supported Source Code Tags and Current `Dockerfile` Link

[`2.4.0 (latest)`](https://github.com/bhits-dev/pcm/releases/tag/2.4.0), [`2.3.0`](https://github.com/bhits-dev/pcm/releases/tag/2.3.0), [`2.2.0`](https://github.com/bhits-dev/pcm/releases/tag/2.2.0), [`2.1.0`](https://github.com/bhits-dev/pcm/releases/tag/2.1.0), [`2.0.0`](https://github.com/bhits-dev/pcm/releases/tag/2.0.0)

[`Current Dockerfile`](../pcm/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits-dev/pcm).

# What is PCM?

The Patient Consent Management (PCM) Service is one of the core components of the Consent2Share (C2S) application. The PCM provides service for patients to manage their electronic consents including consent create, consent edit, consent delete, consent eSignature and patient provider list management. An electronic patient consent is a digital agreement created and electronically signed by the patient to do the following:

1. which *sensitive categories* of health information he or she wishes to share,
2. which *purposes* the medical information may be used,
3. identify the provider *from* whom the information can be disclosed,
4. identify the provider *to* whom the information can be disclosed,
5. record the date when the consent *goes into effect*,
6. identify the *expiration date*.


For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits.github.io/consent2share/).

# How to use this image

## Start a PCM instance

Be sure to familiarize yourself with the repository's [README.md](https://github.com/bhits-dev/pcm) file before starting the instance.

`docker run  --name pcm -d bhitsdev/pcm:latest <additional program arguments>`

*NOTE: In order for this Service to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and the support level infrastructure. Please refer to the Consent2Share Deployment Guide in the corresponding Consent2Share release (see [Consent2Share Releases Page](https://github.com/bhits-dev/consent2share/releases)) for instructions to setup the Consent2Share infrastructure.*

## Configure

The Spring profiles `application-default` and `docker` are activated by default when building images.

This service can run with the default configuration which is from three places: `bootstrap.yml`, `application.yml`, and the data which the [`Configuration Server`](https://github.com/bhits-dev/config-server) reads from the `Configuration Data Git Repository`. Both `bootstrap.yml` and `application.yml` files are located in the class path of the running application.

`docker run -d bhitsdev/pcm:latest --spring.datasource.password=strongpassword`

## Environment Variables

When you start the PCM image, you can edit the configuration of the PCM instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name pcm -e JAR_FILE="pcm-latest.jar" -v "/path/on/dockerhost/pcm-latest.jar:/pcm-latest.jar" -d bhitsdev/pcm:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name pcm -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhitsdev/pcm:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup an application argument. The default value is "--spring.profiles.active=application-default, docker".

`docker run --name pcm -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=application-default,ssl,docker" -d bhitsdev/pcm:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.13.0.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](https://github.com/bhits-dev/pcm/blob/master/LICENSE) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhitsdev/pcm](https://github.com/bhits-dev/pcm) GitHub repository. Be sure to familiarize yourself with the repository's README.md file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits-dev/pcm/issues).