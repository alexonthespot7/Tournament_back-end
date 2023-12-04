# Tournament_back-end
> This Java Spring Boot application serves as the server-side component for a comprehensive tournament management system.<br>
> It provides a robust backend infrastructure to support various functionalities for users and administrators.<br>
> The front-end side of this project is available [here](https://github.com/alexonthespot7/Tournament_front-end) 

## Table of Contents
* [Usage Guide](#usage-guide)
* [Features](#features)
* [Technologies Used](#technologies-used)
* [Documentation](#documentation)
* [License](#license)

## Usage Guide
1. Clone the project <br>```git clone https://github.com/alexonthespot7/Tournament_back-end.git```<br>
2. Set the environmental variables<br>
To activate smtp service functionality you will need to set the following env variables with some real data.<br>For that run the following commands in the command line:<br>
    ```$Env:SPRING_MAIL_HOST="your_smtp_host"```<br>
    ```$Env:SPRING_MAIL_USERNAME="your_smtp_username"```<br>
    ```$Env:SPRING_MAIL_PASSWORD="your_smtp_password"```<br>
3. Run the following command in a terminal window (in the complete) directory:<br>
```./mvnw spring-boot:run```<br>
5. Navigate to localhost:8080

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

## Documentation
The documentation for this project is made with Swagger and can be accessed after launching the project at the following endpoint: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## License
This project is under the MIT License.
