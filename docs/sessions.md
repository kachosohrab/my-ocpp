# OCPP Sessions

## What are Sessions?
Each WebSocket connection corresponds to a single charging point. Authentication happens only once at the beginning of the session. All subsequent messages from that socket pertain to the same charging point.

## Session Management
To efficiently manage sessions, we use Redis to store the session ID and charge point serial number. This helps in:
- Associating incoming requests (e.g., StartTransaction, StopTransaction, StatusNotification) with the correct charging point.
- Handling heartbeat messages.

## Expiring Sessions
Each session is tracked using Redis keys with an expiration mechanism. Here’s how it works:

1. When a new session is created, an entry is stored in Redis:
   ```bash
   SET session:<uuid> <serial_number> EX 300
   ```
   `EX 300` ensures the key expires in **5 minutes**.

2. The charge point must send a heartbeat every 30 seconds. On each heartbeat:
   ```bash
   EXPIRE session:<uuid> 300
   ```
   This resets the expiration timer.

3. If a session does not receive a heartbeat for 5 minutes, Redis automatically removes the key, marking the session as expired.

## Redis Key Expiration Event Handling
To detect session expirations in Redis, we must enable keyspace notifications:
```bash
CONFIG SET notify-keyspace-events Ex
```
This allows the backend to listen for `__keyevent@0__:expired` events. When a session key expires, Redis publishes an event, and our system handles it accordingly.

In addition to Redis, we persist session details in PostgreSQL to ensure accurate session tracking and prevent inconsistencies during expirations. This redundancy helps maintain data integrity and enables reliable session recovery when needed.

## Database Table for Sessions
| Column        | Data Type       | Constraints                                      |
|--------------|----------------|--------------------------------------------------|
| id           | SERIAL          | PRIMARY KEY                                     |
| serial_number | VARCHAR(255)    | UNIQUE, NOT NULL, REFERENCES charge_point(serial_number) |
| session_id   | UUID            | UNIQUE, NOT NULL                                |

## Handling Expired Sessions
- Each WebSocket session is assigned a unique UUID for tracking.
- Redis automatically deletes session keys upon expiration, triggering an event.
- The backend listens for the `__keyevent@0__:expired` event and processes expired sessions.
- The WebSocket session associated with the UUID is forcibly closed to prevent dangling connections.
- The expired session entry is removed from PostgreSQL to maintain data consistency.
- The `charge_point` status is updated to `FAULTED`, ensuring inactive charge points are marked correctly.

## Closing Sessions Explicitly
If a client disconnects intentionally:
- The WebSocket session is closed, and its UUID is removed from active tracking.
- The corresponding session entry in Redis and PostgreSQL is deleted immediately.
- The charge point status is updated as necessary.

## Why UUIDs and Maps Are Used
- **UUIDs provide a unique identifier** for each session, ensuring session tracking remains accurate even if multiple connections exist.
- **A map (UUID to WebSocket session) helps efficiently track active sessions** and allows quick lookups for session management.
- **Upon expiration, the UUID map allows targeted cleanup**, ensuring that the correct session is removed from both Redis and PostgreSQL without affecting other active sessions.

This structured approach ensures that expired or disconnected sessions do not cause inconsistencies and that inactive charge points are correctly marked as unavailable or in an error state.

