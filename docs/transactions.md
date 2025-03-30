# OCPP Transactions Database Design

This document describes how transactions are handled in the Open Charge Point Protocol (OCPP) system. Transactions occur at the **Connector** level since a single **ChargePoint** can have multiple connectors in use simultaneously.

## 1. Understanding Transactions in OCPP

A **transaction** in OCPP represents the charging session for an electric vehicle (EV). It begins when a user plugs into a connector and authenticates using an **idTag**, and it ends when the session is terminated (either by the user or due to an error/timeout). Transactions track energy consumption and session duration.

### **Key Aspects of Transactions**
- **Connector-Level Transactions**: Since a **ChargePoint** can have multiple connectors, transactions are managed at the **Connector** level.
- **Start and End Tracking**: The system records when the transaction starts (`startTime`) and when it ends (`endTime`), if applicable.
- **Energy Metering**: `meterStart` records the initial energy meter value, while `meterStop` (optional) records the value when the session ends.
- **User Authentication**: The `idTag` identifies the user who initiated the session.

## 2. Transaction Table Definition

| Column Name       | Data Type      | Description                                                                 |
|-------------------|---------------|-----------------------------------------------------------------------------|
| **id**           | `UUID`         | Unique identifier for the transaction (Primary Key).                        |
| **serial_number** | `VARCHAR(255)` | Foreign Key linking to the **ChargePoint** table's **serial_number**.      |
| **connector_id**  | `UUID`         | Foreign Key linking to the **Connector** table's **id**.                   |
| **idTag**        | `VARCHAR(255)` | Identifies the user who started the transaction.                           |
| **startTime**    | `TIMESTAMP`    | Timestamp of when the transaction started.                                 |
| **endTime**      | `TIMESTAMP`    | Timestamp of when the transaction ended (nullable).                        |
| **meterStart**   | `INT`          | Initial energy meter value at the start of the transaction.                |
| **meterStop**    | `INT`          | Final energy meter value at the end of the transaction (nullable).         |
| **last_updated** | `TIMESTAMP`    | Timestamp when the transaction record was last updated.                    |

## 3. Database Relationships

### One-to-Many Relationship
- A **Connector** can have multiple **Transactions**, but each **Transaction** belongs to exactly one **Connector**.
- A **ChargePoint** can have multiple **Transactions** through its **Connectors**.

### Summary of Relationships
- **ChargePoint** → **Connector** (One-to-Many): A **ChargePoint** has multiple **Connectors**.
- **Connector** → **Transaction** (One-to-Many): A **Connector** can have multiple **Transactions**, but each **Transaction** is associated with exactly one **Connector**.

## 4. Transaction Lifecycle
1. **Start**: A transaction begins when a user plugs in and authenticates.
2. **During Session**: The system records meter readings and updates transaction data.
3. **End**: The transaction ends when the user stops charging, disconnects, or an error occurs.
4. **Data Logging**: The system logs the session details, including duration and energy consumption.

This structure ensures an organized and scalable approach to handling charging transactions in an OCPP-compliant system.