# OCPP Charging System Database Design

This document outlines the database design for the Open Charge Point Protocol (OCPP) system. The system consists of two primary entities: **ChargePoint** and **Connector**.

## 1. System Components

In a full OCPP implementation, there are typically three levels: **ChargingStation**, **ChargePoint**, and **Connector**. However, this design simplifies the hierarchy by using only **ChargePoint** and **Connector**.

### **ChargePoint**
A **ChargePoint** represents a physical charging unit that can have multiple connectors attached to it.

### **Connector**
A **Connector** represents an individual charging port within a **ChargePoint**. Each **Connector** has its own status and error codes.

## 2. Table Definitions

### **ChargePoint Table**

| Column Name       | Data Type      | Description                                                                 |
|-------------------|---------------|-----------------------------------------------------------------------------|
| **id**           | `BIGINT`       | Unique identifier for the charge point (Primary Key).                      |
| **serial_number**| `VARCHAR(255)` | Unique serial number of the charge point (Unique Constraint).              |
| **model**        | `VARCHAR(255)` | Model name of the charge point.                                            |
| **vendor**       | `VARCHAR(255)` | Vendor of the charge point.                                                |
| **firmware_version** | `VARCHAR(255)` | Firmware version of the charge point.                                    |
| **status**       | `VARCHAR(255)` | The current status of the charge point (e.g., AVAILABLE, FAULTED, CHARGING).|
| **last_heartbeat**| `TIMESTAMP`    | Timestamp of the last heartbeat sent by the charge point.                   |
| **last_updated** | `TIMESTAMP`    | Timestamp when the charge point record was last updated.                   |

### **Connector Table**

| Column Name       | Data Type      | Description                                                                 |
|-------------------|---------------|-----------------------------------------------------------------------------|
| **id**           | `UUID`         | Unique identifier for the connector (Primary Key).                         |
| **charge_point_id** | `VARCHAR(255)` | Foreign Key linking to the **ChargePoint** table's **serial_number**.     |
| **connector_id** | `INT`          | Unique identifier for the connector within the charge point.               |
| **status**       | `VARCHAR(255)` | The status of the connector (e.g., AVAILABLE, FAULTED, CHARGING).          |
| **error_code**   | `VARCHAR(255)` | Error code for the connector, if any.                                      |
| **last_updated** | `TIMESTAMP`    | Timestamp of the last update for the connector record.                     |

## 3. Database Relationships

### One-to-Many Relationship
- A **ChargePoint** can have multiple **Connectors**.
- The **Connector** table references the **ChargePoint** table through the **charge_point_id** column (which maps to **serial_number** in the **ChargePoint** table).
- The combination of **charge_point_id** and **connector_id** ensures that each connector is unique within a given charge point.

### Summary of Relationships
- **ChargePoint** → **Connector** (One-to-Many): A **ChargePoint** can have multiple **Connectors**, but each **Connector** belongs to exactly one **ChargePoint**.
- The **ChargePoint** table maintains general information about the charging station.
- The **Connector** table manages individual charging ports within the station, along with their statuses and errors.

This structure ensures a scalable and efficient database design for managing charge points and their connectors in an OCPP-compliant system.

