# rd-profile-sync
Scheduled sync job between IDAM and User Profile

### Running unit tests tests:

If you have some time to spare, you can run the *unit tests* as follows:

```
./gradlew test
```

### Running mutation tests tests:

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/

### Contract testing with pact

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

