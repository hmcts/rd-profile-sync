# rd-profile-sync

## Purpose

Scheduled sync job between IDAM and User Profile

Architecture and Designs :

Profile Sync Low Level Design. Please refer to the confluence
https://tools.hmcts.net/confluence/display/RTRD/Profile+Sync+API+-+Low+Level+Design

Profile Sync API High Level Design. Please refer to the confluence
https://tools.hmcts.net/confluence/display/RTRD/Profile+Sync+-+High+Level+Design


### Prerequisites

To run the project you will need to have the following installed:

* Java 17
* Docker

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

While not essential, it is highly recommended to use the pre-push git hook included in this repository to ensure that all tests are passing. This can be done by running the following command:
`$ git config core.hooksPath .githooks`

### Environment Vars

If running locally for development or testing you will need to set the following environment variables

* export REDIRECT-URI=<The Environment you want to connect. Please check with the dev team for more information.>
* export AUTHORIZATION=<The actual authorization. Please check with the dev team for more information.>
* export client-authorization=<The actual client-authorization. Please check with the dev team for more information.>
* export totp_secret=<The actual totp_secret. Please check with the dev team for more information.>


### Running the application

Please Make sure you are connected to the VPN before running application
(https://portal.platform.hmcts.net/vdesk/webtop.eui?webtop=/Common/webtop_full&webtop_type=webtop_full)

To run the API quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh install
```
or

```
docker-compose up
```


After, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```


### Running integration tests:


You can run the *integration tests* as follows:

```
./gradlew integration
```


### Running unit tests tests:

If you have some time to spare, you can run the *unit tests* as follows:

```
./gradlew test
```

### Contract testing with pact
    
To generate the json inside target/pacts directory you need to run the tests first.
This file is not committed to the repo.

To publish against remote broker:
`./gradlew pactPublish`

Turn on VPN and verify on url `https://pact-broker.platform.hmcts.net/`
The pact contract(s) should be published


To publish against local broker:
Uncomment out the line found in the build.gradle:
`pactBrokerUrl = 'http://localhost:9292'`
comment out the real broker

Start the docker container from the root dir run
`docker-compose -f broker-compose.yml up`

Publish via the gradle command
`./gradlew pactPublish`

Once Verify on url `http://localhost:9292/`
The pact contract(s) should be published

Remember to return the localhost back to the remote broker

for more information, Please refer to the confluence on how to run and publish PACT tests.
https://tools.hmcts.net/confluence/display/RTRD/PACT+testing

