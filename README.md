# Getting started

You need Java 11 installed.

    ./mvn spring-boot:run
    
To test that it works, open a browser tab at http://localhost:8080/tags. 
Alternatively, you can run

    curl http://localhost:8080/tags    

# Try it out with [Docker](https://www.docker.com/)

You need Docker installed.
	
	docker-compose up -d
	
# Try it out with a frontend

The entry point address of the backend API is at http://localhost:8080/api

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./mvn test	
