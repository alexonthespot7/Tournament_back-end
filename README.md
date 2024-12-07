# Tournament_back-end
> This Java Spring Boot application serves as the server-side component for a comprehensive tournament management system.<br>
> It provides a robust backend infrastructure to support various functionalities for users and administrators.<br>
>
> The front-end side of this project is available [here](https://github.com/alexonthespot7/Tournament_front-end)<br>


## Table of Contents
* [Usage Guide](#usage-guide)
* [Features](#features)
* [Technologies Used](#technologies-used)
* [Dependencies](#dependencies)
* [Documentation](#documentation)
* [Testing](#testing)
* [License](#license)

## Usage Guide
1. Clone the project <br>```git clone https://github.com/alexonthespot7/Tournament_back-end.git```<br>
2. Set the environmental variables (optional)<br>
    1. To activate smtp service functionality you will need to set the following env variables with some real data.<br>For that run the following commands in the command line (or change them in the application.properties file):<br>
    ```$Env:SPRING_MAIL_HOST="your_smtp_host"```<br>
    ```$Env:SPRING_MAIL_USERNAME="your_smtp_username"```<br>
    ```$Env:SPRING_MAIL_PASSWORD="your_smtp_password"```<br>
    2. You can use app without smtp service with some restrictions:<br>To signup user will need to ask admin to verify their account. The reset password functionality will also be unavailable
3. Run the following command in a terminal window (in the complete) directory:<br>
```./mvnw spring-boot:run```<br>
4. Navigate to localhost:8080

\* The default verification link sent to the user's email address uses the default React web address (http://localhost:3000) as its base.<br> You have the option to modify this base URL in the MailService class

## Features
- Restful Endpoints: Provides RESTful API endpoints for seamless communication with the front-end application.

- Authentication with JWT: Implements JSON Web Token (JWT) for secure authentication between the server-side and client-side applications.

- Bracket Tracking: Users can easily follow the tournament's progress through intuitive bracket visualization.
  
- Round and Competitor Details: Access comprehensive information about rounds, competitors, and their progression through stages.

- Personal Data Management: Users can conveniently manage their personal data within the application.

- Bracket Creation: Administrators can create tournament brackets efficiently using the application's interface.

- User Management: Add, edit, or remove user accounts as ADMIN, providing a streamlined process for user administration.

- Result Management: Set and update results for rounds and stages as ADMIN, ensuring accurate progress tracking within the tournament.

## Technologies Used
- Java Spring Boot
- RESTful APIs
- JWT (JSON Web Token)
- smtp

## Dependencies
- **spring-boot-starter-web**: Starter for building web applications using Spring MVC.

- **spring-boot-devtools**: Provides development-time tools to enhance developer productivity. Automatically triggers application restarts, among other features.
- **spring-boot-starter-data-jpa**: Starter for using Spring Data JPA for database access.
- **h2**: H2 Database Engine, an in-memory relational database for development and testing purposes.
- **spring-boot-starter-security**: Starter for enabling Spring Security and authentication/authorization features.
- **spring-boot-starter-mail**: Starter for sending emails using Spring's JavaMailSender.
- **jjwt-api**: JSON Web Token (JWT) API provided by JJWT library.
- **jjwt-impl**: Implementation of the JSON Web Token (JWT) provided by JJWT library (runtime dependency).
- **jjwt-jackson**: Jackson integration for JSON Web Token (JWT) provided by JJWT library (runtime dependency).
- **spring-boot-starter-test**: Starter for testing Spring Boot applications.
- **spring-security-test**: Spring Security testing support for integration testing.
- **junit-jupiter-api**: JUnit 5 API for writing tests.
- **junit-jupiter-engine**: JUnit 5 test engine implementation.
- **spring-boot-starter-validation**: Starter for using validation in Spring Boot applications.
- **springdoc-openapi-starter-webmvc-ui**: Starter for adding OpenAPI 3 documentation and Swagger UI support to your Spring Boot application.

## Documentation
The documentation for this project is made with Swagger and can be accessed after launching the project at the following endpoints: 
1. [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html): if you're running the app on your pc.
2. [https://tournament-back-end.onrender.com/swagger-ui.html](https://tournament-back-end.onrender.com/swagger-ui.html): deployed app.

## Testing
### Usage Guide
1. Clone the project <br>```git clone https://github.com/alexonthespot7/Tournament_back-end.git```<br>
2. Run the following command in a terminal window (in the complete) directory:<br>
```./mvnw test```<br>
### Info
1. Controllers testing.
2. Repositories testing: CRUD functionalities + custom queries.
3. Rest endpoints methods testing.

## License
This project is under the MIT License.
