# friendship-api
This friendship-api is a library for connecting 2 users to each other as having established a friendship.  A user can send a request for a friendship with another user (friend). When the friend has accepted the friendship request then they are friends.

This api contains the business service for friendship api and the persistence entity with repository api.



This api is meant to be used either through a monolith web-application or deployed in a microservice.


To build: `./gradlew clean build`

To publish to local repository:
`./gradle publishToMavenLocal`
