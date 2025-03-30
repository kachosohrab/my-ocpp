# my-ocpp: A Spring Boot OCPP Server

## Introduction
This repository implements an **OCPP (Open Charge Point Protocol) server** using **Spring Boot** with **WebSocket communication**, **Redis** for session management, and **PostgreSQL** for persistent storage. The server facilitates secure interaction between charge points and the backend system, handling authentication, session tracking, transactions, and status notifications.

## What is OCPP?
OCPP (Open Charge Point Protocol) is a standardized communication protocol that enables charge points to interact with a central management system. It defines how charging stations send status updates, transactions, and notifications while supporting remote control capabilities.

## Documentation
Here’s a breakdown of the key concepts and where to find them:

1. **[System Components](docs/systemcomponents.md)**
    - Provides an overview of the core components of the OCPP server.

2. **[Transactions](docs/transactions.md)**
    - Explains how charging transactions are stored and managed in PostgreSQL.
    - Details the transaction lifecycle from start to completion.

3. **[Sessions](docs/sessions.md)**
    - Describes WebSocket-based session handling with Redis and PostgreSQL.
    - Explains how session expirations and heartbeats are managed.

4. **[Authentication](docs/authentication.md)**
    - Covers JWT-based authentication for API and WebSocket connections.
    - Explains how authentication filters and WebSocket interceptors enforce security.

5. **[Testing](docs/testing.md)**
    - Provides instructions on testing the OCPP server using API requests and WebSocket interactions.
    - Includes sample OCPP messages for BootNotification, Heartbeat, and Transactions.


## Further Research
For more details on OCPP, visit the official website:
- [OCPP Documentation](https://www.openchargealliance.org)


