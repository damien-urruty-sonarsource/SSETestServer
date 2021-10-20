# SSETestServer

This server is written using Tomcat 8.5.68, which is the version currently in use in SonarQube.

The server serves two different routes:

* `/eventStream`: this endpoint streams a ping message every 5sec, respecting the [SSE protocol](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events). The stream remains open for 10min.
* `/`: serves [index.html](index.html) that connects through an `EventSource` to the previous endpoint and logs the received events in the browser console

## Start the server:

> ./gradlew run

The server should be started on `localhost:8080`.

A command line client can be found at [SSETestClient](https://github.com/damien-urruty-sonarsource/SSETestClient).
