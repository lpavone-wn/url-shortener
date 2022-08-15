# Shorty - URL Shortener
An application for shortening URLs for use on Twitter, or other use cases, 
where characters are limited.

## Dependencies
- Maven (tested with 3.8.6)
- JDK 17

## Using the Project

### Building the Application
To build the application run:
```
mvn package
```

### Running the Application
#### Executable Jar
The application is built by Maven as an executable jar file that can be run 
locally. To run the application, run:
```
java -jar target/url-shortener-1.0.jar
```

### Web App
The web application is accessible at http://localhost:8080

## Design
The application has been named "Shorty" to avoid calling it "URL Shortener" 
everywhere, and to provide a context to use for the REST api.

### Helidon Web Server
The application runs on Helidon SE Web server using JAXRS to implement the 
REST API, and using Helidon's static content support to serve the UI.

Helidon is a lightweight web server that runs of Netty, see https://helidon.io.

### REST API
The REST API for the application is based around a "Url" resource.

#### OpenAPI
An OpenApi specification is in the META-INF directory of the source resources.
The api can be fetched from the server at http://localhost:8080/openapi using 
Helidon's built in OpenAPI support.

#### Context root
The context root is "/shorty/v1", to handle versioning of the API.

#### Url Resource
##### Fields
**token** - the token for the url

**shortUrl** - the short url

**url** - the original url

**expiry** - the expiry date of the url

##### Endpoints
**GET /url/{token}** - Get the url by token

200 - success returning url

400 - missing or empty token

404 - url not found for the given token

**POST /url** - Create a new url, returning the json representation of the url

201 - success creating url

400 - empty or missing fields in request body

**DELETE /url/{token}** - delete the url by token

204 - successfully deleted url

404 - url not found for the given token

### Implementation

#### JAXRS
JAXRS is used to implement the REST API. Endpoints can be found in **UrlEndpoints** class.

#### ServerMain
The main class configures and starts the Helidon Web Server. Helidon's built-in 
routing support is used to redirect shortened url requests in ServerMain. The 
static content routing, OpenAPI support and JAXRS application are also registered with the Web 
Server in ServerMain.

#### URLService
The URLService is the controller for creating, fetching and deleting URLs. It 
contains the logic for generating the tokens for the URL's, and calculating 
expiry dates.

URLService is a singleton, as we only need a single instance of the class for 
the application. Alternatively may have used dependency injection for "Inversion 
of Control" pattern, but since @Autowired was ruled out in the guidelines, 
injection seemed to similar. So using Singletons rather than something like
@ApplicationScoped CDI beans.

URLService handles cleanup of expired URL's using a scheduled thread executor 
that runs periodically to purge any expired tokens.

##### Config Properties
**shorty.token.token-length** - The length of the url token. Defaults to 7.

**shorty.token.token-characters** - The characters that can be used in the generated token.

#### URLRepository
The URLRepository is used to persist short URL's, with operations to create, 
get, and delete URL's, as well as purge expired URL's.

##### MapUrlRepository
The MapUrlRepository is the currently the sole implementation of UrlRepository.
It stores the url data in-memory in a ConcurrentHashMap. Avoided adding dependency 
on a database to avoid any potential complications for the reviewers.

### Future Enhancements
- Replace MapUrlRepository with an implementation backed by a database for 
persistent storage.
- Switching to using a database would mean an auto generated id might be used 
as part of the token generation to avoid conflicts, and remove the inefficient 
method of avoiding clashes by generating the token again if a token already 
exists in the repository.
- A cache for frequently accessed URL's could be introduced to boost performance, 
with UrlService first reading from the cache before checking the UrlRepository.
- Improve the UI.

## Testing
JUnit 5 is used for unit and integration testing.

### Unit Testing
A small number of unit tests are included to cover testing of the algorithm for  
generating tokens. There is not much business logic beyond this to test, so 
integration testing of the rest api has been favoured over unit testing.

To run the unit tests, run:
```
mvn test
```

### Integration Testing
The integration tests start the web server and test the REST API against the 
running server. The server is started in BeforeAll and shutdown in AfterAll.

To run the integration tests, run:
```
mvn integration-test
```
