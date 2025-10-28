# Glossary

> 📘 **Source**: This wiki page provides definitions for technical terms used throughout the XAI-Forge project

**Navigation**: [[Home]] > [[Reference & Resources]] > Glossary

## Table of Contents

1. [A](#a)
2. [B](#b)
3. [C](#c)
4. [D](#d)
5. [E](#e)
6. [F](#f)
7. [G](#g)
8. [H](#h)
9. [I](#i)
10. [J](#j)
11. [L](#l)
12. [M](#m)
13. [N](#n)
14. [O](#o)
15. [P](#p)
16. [R](#r)
17. [S](#s)
18. [T](#t)
19. [U](#u)
20. [V](#v)
21. [X](#x)

---

## A

**API (Application Programming Interface)**
A set of protocols and tools for building software applications. In XAI-Forge, REST APIs are used for communication between frontend and backend.

**Authentication**
The process of verifying the identity of a user. XAI-Forge uses JWT-based authentication.

**Authorization**
The process of determining what resources a user can access. XAI-Forge implements role-based access control.

**Async Processing**
A programming paradigm where operations can run independently without blocking the main thread. Used in XAI-Forge for ML model training.

---

## B

**Builder Pattern**
A creational design pattern that constructs complex objects step by step. Implemented in XAI-Forge with PredictionResponseBuilder and TrainRequestBuilder.

**BCrypt**
A password hashing function used for secure password storage in XAI-Forge.

---

## C

**CORS (Cross-Origin Resource Sharing)**
A security feature that allows web pages to make requests to a different domain. Configured in XAI-Forge for frontend-backend communication.

**CSV (Comma-Separated Values)**
A file format for storing tabular data. XAI-Forge accepts CSV files for dataset uploads.

**Classification**
A type of machine learning task where the goal is to predict categorical outcomes (e.g., spam/not spam).

**Concurrency**
The ability of a system to handle multiple operations simultaneously. XAI-Forge uses thread pools for concurrent ML operations.

---

## D

**DTO (Data Transfer Object)**
An object that carries data between processes. Used in XAI-Forge for API communication.

**Database Connection Pooling**
A technique for managing database connections efficiently. XAI-Forge uses HikariCP for connection pooling.

**Deadlock**
A situation where two or more processes are blocked waiting for each other. XAI-Forge implements deadlock detection and recovery.

---

## E

**Entity**
A Java class that represents a database table. XAI-Forge has User, Dataset, and MLModel entities.

**Exception Hierarchy**
A structured organization of exception classes. XAI-Forge implements 11 custom exceptions in a hierarchical structure.

**Explainable AI (XAI)**
A field of AI that focuses on making AI decisions understandable to humans. XAI-Forge uses LIME for explanations.

---

## F

**Factory Pattern**
A creational design pattern that provides an interface for creating objects. Implemented in XAI-Forge with AlgorithmFactory and ModelFactory.

**Feature**
An input variable used in machine learning models. XAI-Forge allows users to select features for model training.

**Frontend**
The user interface part of the application. XAI-Forge uses React for the frontend.

---

## G

**GitHub Actions**
A CI/CD platform for automating software workflows. XAI-Forge uses GitHub Actions for automated testing and deployment.

---

## H

**Hibernate**
An ORM (Object-Relational Mapping) framework for Java. Used in XAI-Forge for database operations.

**HikariCP**
A high-performance JDBC connection pool. Used in XAI-Forge for database connection management.

---

## I

**Isolation Level**
A database property that controls the visibility of changes made by one transaction to other transactions. XAI-Forge uses different isolation levels for different operations.

**Integration Test**
A type of testing that verifies the interaction between different components. XAI-Forge includes integration tests for API endpoints.

---

## J

**JPA (Java Persistence API)**
A Java specification for managing relational data. Used in XAI-Forge for database operations.

**JWT (JSON Web Token)**
A compact, URL-safe token used for authentication. XAI-Forge uses JWT for stateless authentication.

**JaCoCo**
A code coverage library for Java. Used in XAI-Forge for measuring test coverage.

---

## L

**LIME (Local Interpretable Model-agnostic Explanations)**
An algorithm for explaining individual predictions of machine learning models. Used in XAI-Forge for generating explanations.

**Logistic Regression**
A statistical method for analyzing datasets with categorical dependent variables. One of the ML algorithms supported by XAI-Forge.

---

## M

**Machine Learning (ML)**
A subset of AI that enables computers to learn and make decisions from data. XAI-Forge supports classification and regression ML tasks.

**Material-UI (MUI)**
A React component library that implements Google's Material Design. Used in XAI-Forge for the user interface.

**Maven**
A build automation tool for Java projects. Used in XAI-Forge for dependency management and building.

**Model**
A mathematical representation of a real-world process. XAI-Forge trains and stores ML models for predictions.

**Multipart File**
A file upload format that allows sending files over HTTP. Used in XAI-Forge for CSV file uploads.

---

## N

**Node.js**
A JavaScript runtime environment. Used in XAI-Forge for frontend development.

**npm**
A package manager for JavaScript. Used in XAI-Forge for frontend dependency management.

---

## O

**ORM (Object-Relational Mapping)**
A programming technique for converting data between incompatible type systems. XAI-Forge uses Hibernate as an ORM.

**Optimistic Locking**
A concurrency control mechanism that assumes conflicts are rare. Implemented in XAI-Forge using version fields.

---

## P

**PostgreSQL**
An open-source relational database management system. Used as the primary database in XAI-Forge.

**Prediction**
The output of a machine learning model for new input data. XAI-Forge generates predictions and explanations.

**Pessimistic Locking**
A concurrency control mechanism that assumes conflicts are common. Used in XAI-Forge for critical operations.

---

## R

**React**
A JavaScript library for building user interfaces. Used in XAI-Forge for the frontend.

**Regression**
A type of machine learning task where the goal is to predict continuous numerical values.

**Repository Pattern**
A design pattern that abstracts data access logic. Implemented in XAI-Forge with JPA repositories.

**REST (Representational State Transfer)**
An architectural style for designing web services. XAI-Forge uses REST APIs for communication.

---

## S

**Spring Boot**
A Java framework for building microservices and web applications. Used as the main framework in XAI-Forge.

**Spring Security**
A framework for securing Spring applications. Used in XAI-Forge for authentication and authorization.

**Strategy Pattern**
A behavioral design pattern that enables selecting algorithms at runtime. Implemented in XAI-Forge for ML algorithm selection.

**SQL Injection**
A code injection technique used to attack data-driven applications. XAI-Forge prevents SQL injection through parameterized queries.

---

## T

**Testcontainers**
A Java library that provides lightweight, throwaway instances of common databases. Used in XAI-Forge for integration testing.

**Thread Pool**
A collection of pre-created threads that can be reused for executing tasks. XAI-Forge uses thread pools for async operations.

**Tribuo**
Oracle's machine learning library for Java. Used in XAI-Forge for ML operations.

**Transaction**
A sequence of database operations that are treated as a single unit. XAI-Forge uses transactions for data consistency.

---

## U

**Unit Test**
A type of testing that verifies individual components in isolation. XAI-Forge includes unit tests for services and components.

**UUID (Universally Unique Identifier)**
A 128-bit identifier used for unique identification. Used in XAI-Forge for generating unique filenames.

---

## V

**Validation**
The process of checking data for correctness and completeness. XAI-Forge implements comprehensive input validation.

**Version Control**
A system for tracking changes to files over time. XAI-Forge uses Git for version control.

---

## X

**XAI (Explainable AI)**
The field of artificial intelligence focused on making AI decisions understandable to humans. XAI-Forge is built around this concept.

**XSS (Cross-Site Scripting)**
A security vulnerability that allows attackers to inject malicious scripts. XAI-Forge prevents XSS through input sanitization.

---

## Additional Terms

**Backend**
The server-side part of the application that handles business logic and data processing.

**Frontend**
The client-side part of the application that handles user interaction and presentation.

**Full-Stack**
A development approach that includes both frontend and backend development.

**Microservices**
An architectural approach where applications are built as a collection of loosely coupled services.

**Monolithic**
An architectural approach where applications are built as a single, unified unit.

**Production**
The live environment where the application runs for end users.

**Development**
The environment where developers build and test the application.

**Staging**
A pre-production environment used for final testing before deployment.

---

**Next**: [[WIKI_SETUP]] | **Previous**: [[Edge Cases|Edge-Cases]]  
**Related**: [[Architecture]], [[Advanced Concepts|Advanced-Concepts]], [[Technology Stack|Technology-Stack]]
