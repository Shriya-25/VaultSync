# VaultSync — Updated Mini-Project PRD and Implementation Plan

**Project title:** Distributed Cloud Backup and Disaster Recovery System  
**Short name:** VaultSync  
**Version:** 2.0 — Simplified Mini-Project Scope  
**Date:** 28 October 2026  
**Purpose:** Demonstrate core Distributed Computing Systems (DCS) concepts in a small, explainable working prototype.

---

## 1. Project Vision

VaultSync is a small web-based demonstration of how a file can be backed up across two independent storage nodes and recovered when one node becomes unavailable. The aim is **not** to build a production cloud-storage platform. The aim is to implement one complete, observable workflow that can be demonstrated in 2–3 minutes and explained clearly in a college viva.

### Problem
If a file exists in only one storage location, a failure of that location can make the file unavailable. A backup system should keep another copy, detect an unavailable storage node, recover from a healthy copy, and verify that the recovered content is unchanged.

### Core demonstration

1. Select and upload a file.
2. Calculate its SHA-256 checksum.
3. Replicate it to Node A and Node B, which have separate storage locations.
4. Show heartbeat-based node health.
5. Simulate or cause one node to become unavailable.
6. Recover the file from the healthy replica.
7. Recalculate SHA-256 and compare it with the original checksum.

The system must report success only when the recovery and checksum verification actually succeed.

---

## 2. Fixed Technology Stack

- **Frontend:** React.js, Vite, plain CSS
- **Backend/coordinator:** Java 21, Spring Boot, REST APIs
- **Metadata database:** PostgreSQL
- **Integrity:** SHA-256
- **Distributed concepts:** file replication, heartbeat failure detection, fault tolerance, replica-based recovery
- **Local orchestration:** Docker Compose
- **Version control:** Git and GitHub

Do not permanently replace PostgreSQL with H2 or replace the agreed stack with another technology. If PostgreSQL or Docker is not available during early UI work, frontend work may proceed independently. Any temporary local-development workaround must be documented and must not be mistaken for the final architecture.

### Smallest acceptable architecture

- **React frontend:** One main screen for uploading a file, seeing node status, and triggering the demonstration.
- **Spring Boot coordinator:** Upload endpoint, checksum calculation, metadata, replication coordination, heartbeat monitoring, and recovery selection.
- **Storage Node A:** A small independent service with its own storage directory.
- **Storage Node B:** A second independent service with a different storage directory.
- **PostgreSQL:** Stores file metadata, node status, replica records, and a minimal operation history.
- **Docker Compose:** Starts the coordinator, database, and both storage-node services together.

The two storage nodes must be independently addressable services for the final demonstration. Two folders in one coordinator process may be used only as an explicitly temporary prototype if necessary; it does not satisfy the final independent-node demonstration.

---

## 3. Scope Control

### In scope — required

1. Upload one file at a time.
2. Calculate and display SHA-256.
3. Store metadata in PostgreSQL.
4. Write separate physical copies to two independent storage nodes.
5. Track whether each replica write succeeded.
6. Check node health through heartbeat/health checks and a timeout.
7. Provide a controlled way to stop or simulate one node failure.
8. Recover a file from an available replica.
9. Verify recovered content using SHA-256.
10. Display clear operation results on the same page.
11. Start the complete system using documented commands and Docker Compose.

### Explicitly out of scope

- Multiple pages or a complex admin dashboard.
- Authentication, accounts, roles, billing, or multi-tenancy.
- Cloud-provider integration or public deployment requirements.
- Three or more nodes, configurable quorum, or configurable replication factor.
- Advanced analytics, charts, search, filters, notifications, or settings pages.
- Encryption, compression, deduplication, version history, scheduling, and retention policies.
- Enterprise monitoring, automatic scaling, distributed consensus, or production-grade guarantees.
- Sophisticated retry queues or background job infrastructure.

Do not add these features unless all required functionality is complete and there is time left.

---

## 4. Minimal User Interface

Build **one compact, responsive page**. The UI is a control panel for the DCS demonstration, not a commercial product dashboard.

### Required sections

1. **Header:** VaultSync and a short subtitle such as “Distributed Backup Demo”.
2. **Two small status cards:** Files backed up; healthy nodes (for example, 2/2).
3. **Storage Nodes:** Node A and Node B, each showing live health and last heartbeat if available.
4. **Upload File:** File picker, selected filename/size, upload action, and calculated checksum after upload.
5. **Demonstration controls:** Replicate File (if replication is a separate action), Simulate/Restore Node, and Recover & Verify. Disable actions when their prerequisites are not met.
6. **Compact backup list:** Filename, checksum (shortened visually with a way to inspect the full value), replica status, and last recovery result.
7. **Result message:** Plain success/failure feedback explaining what happened.

### UI rules

- Keep the page simple, with a light background and restrained indigo accents.
- Avoid a complex sidebar, large tables, charts, search, filters, notifications, and decorative metrics.
- Use real backend data once the API exists. Clearly label temporary UI preview data before integration.
- Never show “replicated”, “recovered”, or “healthy” as a successful real operation based only on frontend state or hardcoded values.
- Provide loading, empty, success, and error states, but no elaborate animations.

---

## 5. Functional Requirements and Acceptance Criteria

### FR-1 — Upload and checksum

The user can select a file and send it to the backend. The backend validates the request, assigns a unique ID, calculates SHA-256, and returns the file metadata and checksum.

**Pass condition:** A valid upload produces a stored file record and the displayed checksum matches a locally calculated SHA-256 for the same file bytes.

### FR-2 — Two-node replication

The coordinator writes the same file content to Node A and Node B through their service interfaces. Each node stores the file independently using the unique file ID as its storage key. The coordinator records each write result.

**Pass condition:** The file exists in both independent node storage locations, both replica records are accurate, and the UI reports replication success only after both writes succeed.

### FR-3 — Heartbeat and failure detection

Each storage node exposes a health endpoint. The coordinator checks node health periodically or at a clearly documented interval and tracks the last successful heartbeat. A node becomes unavailable after a configured timeout.

**Pass condition:** When a node is stopped or made unreachable, its status changes to unavailable based on failed health checks/heartbeat timeout—not a hardcoded frontend change. A development-only simulation endpoint is acceptable if it causes the backend to stop treating that node as available and is clearly labelled as a simulation.

### FR-4 — Recovery and integrity verification

The coordinator chooses a healthy replica, retrieves the file, recalculates SHA-256, and compares it with the checksum stored for the original upload. It must not report successful recovery if no healthy replica is available or the checksum differs.

**Pass condition:** Recovery succeeds with one node unavailable when the other has a valid copy. The returned checksum matches. If the only available copy is corrupted or no copy is available, the operation reports failure.

### FR-5 — Metadata and operation result

PostgreSQL stores the original filename, file ID, size, checksum, upload timestamp, replica locations/statuses, and minimal operation outcomes. Keep the schema small and use clear statuses.

Suggested statuses:
- Node: `HEALTHY`, `UNAVAILABLE`, `UNKNOWN`
- Replica: `AVAILABLE`, `FAILED`
- Backup: `PENDING`, `REPLICATED`, `PARTIAL`, `FAILED`
- Recovery: `SUCCESS`, `FAILED`, `INTEGRITY_MISMATCH`

**Pass condition:** Refreshing the frontend does not erase saved file metadata or replica status.

---

## 6. Minimal Data Model

Use only the tables needed for the demonstration.

### `files`
- `id` — UUID or equivalent unique identifier
- `original_filename`
- `size_bytes`
- `sha256_checksum`
- `backup_status`
- `created_at`

### `storage_nodes`
- `id`
- `name` (Node A / Node B)
- `endpoint`
- `status`
- `last_heartbeat`

### `file_replicas`
- `id`
- `file_id`
- `node_id`
- `storage_key`
- `status`
- `created_at`
- Unique constraint on (`file_id`, `node_id`)

A separate operation-log table is optional. If time is short, return clear operation results from the API and keep a small recent-results list without building a logging subsystem.

---

## 7. Minimal REST API

Keep endpoints small and consistent. Names may be adjusted slightly during implementation, but avoid creating extra APIs without a clear need.

- `POST /api/files` — upload a file, calculate checksum, coordinate replica writes, and save metadata.
- `GET /api/files` — list saved files and replica statuses.
- `GET /api/files/{id}` — get details for one file.
- `GET /api/files/{id}/recover` or `POST /api/files/{id}/recover` — recover from a healthy replica and verify SHA-256. Prefer `POST` if recovery is treated as an operation that may create a recovered output.
- `GET /api/nodes` — return node health and last heartbeat.
- `GET /api/health` — coordinator health check.

Only add development-only endpoints for simulating/restoring node availability if needed for the classroom demonstration. Clearly label and protect them as demo controls; do not present them as production administration features.

Each API should return meaningful HTTP status codes and a short error message. Avoid overengineering response wrappers.

---

## 8. DCS Concepts to Demonstrate in the Viva

- **Distribution:** The coordinator communicates with two independently running storage-node services.
- **Replication:** Each uploaded file is copied to two different storage locations.
- **Heartbeat failure detection:** Periodic health checks/heartbeats help identify an unavailable node after a timeout.
- **Fault tolerance:** One node can become unavailable while the other replica remains usable.
- **Recovery:** The coordinator selects an available replica instead of relying on the failed node.
- **Data integrity:** SHA-256 detects whether recovered bytes match the original content.
- **Metadata coordination:** PostgreSQL records where replicas are stored and their statuses.

Do not claim that this prototype survives simultaneous failure of both nodes, database failure, or coordinator failure.

---

## 9. Updated Implementation Phases

**Work one phase at a time. At the end of every phase, stop, report what changed and what was tested, and wait for explicit approval before starting the next phase.** Do not let the coding agent implement all phases in one pass.

### Phase 0 — Environment and project baseline (already mostly completed)
- Confirm repository, Java, Maven, Node/npm, PostgreSQL, and Docker availability.
- Preserve the existing Git repository and remote.
- Do not change unrelated Git settings.
- Record any missing dependencies honestly.

**Exit gate:** Tooling status is documented; no unnecessary setup changes.

### Phase 1 — Minimal frontend only (current phase; simplify the existing UI)
- Reduce the current oversized UI to the one-page layout in Section 4.
- Use React, Vite, and plain CSS.
- Keep temporary preview data clearly labelled.
- Do not fake backend operations.
- Build and run the frontend and fix errors.

**Exit gate:** A compact responsive page runs, with the required sections and no unnecessary product features. Stop for review.

### Phase 2 — Spring Boot API skeleton
- Create the Java 21 Spring Boot coordinator.
- Add a health endpoint and minimal REST structure.
- Configure CORS for the Vite development server.
- Add configuration through application properties/environment variables.
- Add a simple consistent error response.

**Exit gate:** The app starts and the health endpoint responds. No replication workflow yet. Stop for review.

### Phase 3 — PostgreSQL metadata
- Configure PostgreSQL as the final metadata database.
- Add only the `files`, `storage_nodes`, and `file_replicas` tables/entities and repositories.
- Add initial Node A/Node B records if needed.
- Confirm records persist across backend restarts.

**Exit gate:** PostgreSQL connection and basic create/read operations work. Do not substitute H2 as the final database. Stop for review.

### Phase 4 — Two independent storage-node services
- Implement the smallest storage-node service contract: health endpoint, write file, and read file by storage key.
- Run Node A and Node B as separate processes/services with separate storage directories and ports; final packaging will run them as separate Compose services.
- Verify the same file can be written and read independently from both nodes.

**Exit gate:** Both node services are independently reachable and store separate copies. Stop for review.

### Phase 5 — Upload, SHA-256, and replication
- Connect the upload API to the coordinator and storage nodes.
- Calculate SHA-256 on the backend.
- Write the file to both nodes and persist metadata/replica statuses.
- Report `REPLICATED` only when both writes succeed; otherwise report partial/failed status.
- Connect the frontend upload and file list to real API data.

**Exit gate:** Uploading one file creates two physical copies and persistent metadata, and the UI shows the real checksum and statuses. Stop for review.

### Phase 6 — Heartbeat, failure, recovery, and verification
- Add periodic node health checks/heartbeat timeout.
- Reflect real node status in the UI.
- Add one clear demo control to make one node unavailable and restore it.
- Recover from the healthy replica and verify SHA-256.
- Handle no-healthy-replica and checksum-mismatch cases honestly.

**Exit gate:** The full core demonstration works: upload → replicate → make one node unavailable → recover → checksum matches. Stop for review.

### Phase 7 — Docker Compose and final testing
- Add a Compose file for PostgreSQL, coordinator, Node A, Node B, and frontend if appropriate.
- Keep service configuration and ports simple.
- Run a clean startup test using the documented commands.
- Test both healthy-node recovery and unavailable-replica failure cases.
- Verify the UI has no misleading placeholder statuses after backend integration.

**Exit gate:** The project can be started reproducibly and the main workflow passes. Stop for review.

### Phase 8 — Submission and viva preparation
- Write a concise README with architecture, prerequisites, commands, API list, and known limitations.
- Add one simple architecture diagram if time permits.
- Prepare a 2–3 minute demo sequence and short explanations of each DCS concept.
- Include only test results that were actually run.

**Exit gate:** Another student can follow the README to run the project and reproduce the demonstration.

---

## 10. Core Test Checklist

- [ ] Valid file upload returns file ID and SHA-256.
- [ ] Invalid/empty upload produces a clear error.
- [ ] File content is stored separately on Node A and Node B.
- [ ] PostgreSQL contains the file and replica records.
- [ ] Both healthy nodes are reported healthy.
- [ ] Making Node A unavailable changes its status after the configured timeout.
- [ ] Recovery succeeds from Node B while Node A is unavailable.
- [ ] Recovered SHA-256 matches the original SHA-256.
- [ ] No available replica produces a recovery failure, not a false success.
- [ ] A corrupted replica fails checksum verification if corruption testing is implemented.
- [ ] Restarting the frontend does not erase saved metadata.
- [ ] Docker Compose starts the required services using the documented commands.

---

## 11. Two-to-Three-Minute Demonstration Script

1. Open the VaultSync page and show Node A and Node B as healthy.
2. Upload a small sample file and point out its SHA-256 checksum.
3. Show that replication completed on both independent nodes.
4. Make Node A unavailable and wait for the heartbeat timeout/status change.
5. Click Recover & Verify.
6. Show that the coordinator retrieved the file from Node B and the SHA-256 matches.
7. Explain that the demo shows replication, failure detection, fault tolerance, recovery, and integrity verification. Mention that simultaneous loss of both replicas is outside the prototype's scope.

---

## 12. One-Day Priority Rule

Prioritize a working core workflow over styling or optional features. If time becomes limited, keep the page minimal and reduce optional logging or UI polish—not checksum verification, real separate replicas, failure detection, or recovery. Do not claim completion of a feature that has not been implemented and tested.

## 13. Definition of Done

VaultSync is ready for the mini-project demonstration when:

- [ ] The simple frontend is connected to the real backend.
- [ ] Java 21 Spring Boot and PostgreSQL are used.
- [ ] Two independent storage-node services store the same uploaded file.
- [ ] The backend calculates and persists SHA-256.
- [ ] Node health is determined by heartbeat/health checks.
- [ ] One unavailable node does not prevent recovery from the other replica.
- [ ] Recovery is marked successful only after checksum verification passes.
- [ ] Docker Compose and README instructions are tested.
- [ ] The project can be explained clearly as a DCS concept demonstration, not a production cloud service.
