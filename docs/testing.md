# Testing the OCPP Server

## Overview
This guide explains how to test the OCPP server using different endpoints for authentication, monitoring, and WebSocket communication.

## Prerequisites

Before running the server, ensure that you fill in the following values in the `application.properties` file to properly configure the necessary services:

```properties
redis.host={redis.host}
redis.port={redis.port}
redis.username={redis.username}
redis.password={redis.password}
redis.timeout=6000

spring.datasource.url={postgres.url}
spring.datasource.username={postgres.username}
spring.datasource.password={postgres.password}
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Server Endpoints
The server has three main endpoints:
- **`/ocpp`** - WebSocket endpoint for charge point communication.
- **`/auth`** - Generates a test authentication token.
- **`/api`** - Provides monitoring capabilities.

## Generating a Test Authentication Token
To generate a test token, send the following request:
```bash
curl --location 'http://localhost:8080/auth/generate-test-token?sub=testTokenName&expirySeconds=600'
```
**Note:** This authentication mechanism is only for testing, so `sub` can be any value. In a real system, an authentication service would validate credentials before issuing a token.

## Using the Authentication Token
Once the token is generated, use it in the `Authorization` header as `Bearer <token>` for API and WebSocket requests.

## Monitoring Charge Points
To monitor charge point transactions, use the following API request:
```bash
curl --location 'http://localhost:8080/api/transactions/charge-point?serialNumber=2322212&startTime=2020-03-28T12%3A50%3A00Z&endTime=2025-03-28T12%3A50%3A00Z' \
--header 'Authorization: Bearer <token>'
```
Here, `serialNumber` refers to the serial number of a specific charge point. This request retrieves transactions within the specified timeframe.

## Connecting to WebSocket
To establish a WebSocket connection as a charge point, connect to:
```text
ws://localhost:8080/ocpp
```
**Note:** Each connection must include the header `Authorization: Bearer <token>` for authentication.

Once connected, the first message should be a `BootNotification`, followed by other necessary messages such as `Heartbeat`, `StatusNotification`, `StartTransaction`, and `StopTransaction`.

## Sample OCPP Messages
Below are example messages that can be sent over WebSocket:

### Boot Notification
```json
[2, "1234", "BootNotification", {
  "chargePointModel": "ModelX",
  "chargePointVendor": "VendorY",
  "firmwareVersion": "1.0.0",
  "chargePointSerialNumber": "2322212"
}]
```

### Heartbeat
```json
[2, "5678", "Heartbeat", {}]
```

### Start Transaction
```json
[2, "9012", "StartTransaction", {
  "connectorId": 1,
  "idTag": "ABC123",
  "meterStart": 1000,
  "timestamp": "2025-03-28T12:40:00Z"
}]
```

### Stop Transaction
```json
[2, "3456", "StopTransaction", {
  "transactionId": "tsd1324-4msd23fasdfadg23",
  "meterStop": 1500,
  "timestamp": "2025-03-28T13:00:00Z",
  "reason": "Local"
}]
```

### Status Notification
```json
[2, "7890", "StatusNotification", {
  "connectorId": 1,
  "status": "Available",
  "errorCode": "NoError",
  "timestamp": "2025-03-28T13:10:00Z"
}]
```

## OCPP Message Structure
OCPP messages follow a standard format:
```json
[<messageType>, "<uniqueId>", "<actionName>", <payload>]
```
- **`messageType`**: Defines the type of message:
  - `2`: Call (request from client to server)
  - `3`: CallResult (server response to a Call message)
  - `4`: CallError (error response)
- **`uniqueId`**: A unique identifier for tracking requests and responses.
- **`actionName`**: Specifies the type of request (e.g., `BootNotification`, `Heartbeat`).
- **`payload`**: Contains the request data in JSON format.

### Understanding Message Types
OCPP messages have three main types:
- **Call (`2`)**: A request message from a charge point to the server.
- **CallResult (`3`)**: A response from the server to a Call message.
- **CallError (`4`)**: A response indicating an error in a Call message.

This demo project **only supports Call messages (type `2`)**, meaning all test messages must follow this format. The server does not handle CallResult (`3`) or CallError (`4`).

### Message Format Basis
The choice of these message formats is based on the OCPP message standard, ensuring compatibility with the OCPP protocol.

## Time Formats in OCPP
OCPP messages use timestamps in the ISO 8601 UTC format:
```text
YYYY-MM-DDTHH:MM:SSZ
```
For example:
```text
2025-03-28T12:40:00Z
```
- `T` separates the date from the time.
- `Z` indicates that the time is in UTC (Coordinated Universal Time).
- This format ensures synchronization across systems operating in different time zones.

By following these guidelines, you can effectively test the OCPP server and simulate charge point interactions.

