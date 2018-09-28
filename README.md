# dropbox-tags-api

This project extends the functionality of dropbox allowing the consumer of the API to tag files and search filtering by those tags. In order to provide the new functionalities, a spring boot app has been created. This app interacts with dropbox and solr to get files and index them so the client can execute the queries through the endpoints provided.

## Getting started

These instructions will get you a copy of the project on a local machine ready for testing.

### Prerequisites

Docker

### Configuration

The API is configurable via environment variables passed in the Dockerfile

* SOLR_HOST: URL of the solr instance
* ZIP_SIZE: Indicates the max size the zip file containing the dropbox files requested can have
* DROPBOX_ACCESS_TOKEN: Token needed to connect with dropbox

### Starting the app

To get everything up and running quickly, a docker-compose file has been created. This allows us to have the API and a solr instance ready for testing in an easy way. It's possible to just use the API docker image and configure the solr host property to point to another solr instance. In that case, you can ignore the following instructions. The first thing we need to do to have solr configured with our own conf files, is to create a volume and copy the conf files of the project, so they're available when we run the docker-compose command. We need to run the following commands:

```
docker volume create dropboxFile
docker create --rm --name copier --user root -v dropboxFile:/d solr chown -R 8983:8983 /d
docker cp config/dropboxFile/conf copier:/d/
docker cp config/dropboxFile/core.properties copier:/d/
docker start copier
```

Afterwards, we can run the docker-compose command to get everything up

```
docker-compose up
```

### API endpoints

Once everything is up and running, the API endpoints are ready to be consumed. The main endpoints are:

Lists all the files indexed in solr:
```
(GET) http://localhost:8080/api/files
```

Retrieves the information about the file with the id indicated:
```
(GET) http://localhost:8080/api/files/{id}
```

Allows the modification of the attributes of the file with the id indicated. We can add/remove tags using this endpoint:
```
(PATCH) http://localhost:8080/api/files/{id}
```

Retrieves the files tagged with the tags passed by parameter
```
(GET) http://localhost:8080/api/files/search/findByTagsIn?tags=cv,engineer,application
```

Returns a zip file with the files tagged with the tags passed by parameter
```
(GET) http://localhost:8080/api/zip?tags=cv,engineer,application
```
