# Product Requirements Document (PRD)

## Distributed Cloud Backup and Disaster Recovery System

**Document type:** Mini-project Product Requirements Document\
**Domain:** Distributed Computing Systems (DCS)\
**Version:** 1.0\
**Prepared on:** 28 October 2026\
**Project level:** Academic mini-project / proof of concept

------------------------------------------------------------------------

## 1. Project Overview

### 1.1 Project Title

**Distributed Cloud Backup and Disaster Recovery System**

### 1.2 Purpose

The system provides a small-scale distributed file backup service. A
user uploads a file through a web dashboard. The backend calculates a
SHA-256 checksum, stores file metadata in PostgreSQL, and replicates the
file to multiple independent storage nodes. The system monitors node
health using heartbeat messages. If a storage node becomes unavailable,
the system should still be able to retrieve a file from a healthy
replica and verify its integrity.

The project demonstrates core Distributed Computing Systems concepts
through a practical, observable workflow rather than attempting to
reproduce a production cloud storage platform.

### 1.3 Problem Statement

Files stored in a single location can become unavailable if the storage
location fails. A backup is also less useful if the system cannot
identify which copies are available or verify whether a recovered file
matches the original. This project addresses these issues through file
replication, node-health monitoring, checksum verification, and recovery
from an available replica.

### 1.4 Proposed Solution

Build a web application with: - A React dashboard for uploading files
and monitoring backups. - A Java 21 / Spring Boot REST API for file
operations and coordination. - PostgreSQL for file metadata, replica
records, node status, and operation history. - Multiple storage nodes,
preferably running as separate Docker Compose services. - SHA-256
checksums to verify file integrity. - Heartbeat-based health monitoring
to identify unavailable nodes. - A recovery workflow that selects a
healthy replica and verifies the recovered content.

### 1.5 Target Users

-   Students demonstrating distributed-system concepts.
-   An administrator or evaluator who monitors storage nodes and backup
    operations.
-   A user who needs to upload, list, download, and recover backed-up
    files.

------------------------------------------------------------------------

## 2. Goals and Success Criteria

### 2.1 Goals

1.  Upload and store files through a browser-based interface.
2.  Maintain at least two copies of each successfully replicated file on
    separate storage nodes.
3.  Record file metadata and replica locations in PostgreSQL.
4.  Calculate and store a SHA-256 checksum for each uploaded file.
5.  Monitor storage-node availability using heartbeat messages.
6.  Demonstrate recovery when one storage node is unavailable.
7.  Verify the recovered file against the original checksum.
8.  Provide a clear dashboard and a repeatable demonstration for a DCS
    viva.

### 2.2 Success Criteria

The project is considered successful when: - A user can upload a
supported file and see its status. - The backend records its name, size,
checksum, and replica locations. - Replication creates copies on
separate configured storage nodes. - The dashboard reflects node health
after missed heartbeats. - A file can be downloaded or recovered from a
healthy replica when another node is unavailable. - SHA-256 verification
reports whether the recovered file matches the recorded checksum. - API
and UI errors are displayed clearly rather than silently ignored. - The
application can be started using documented setup steps.

### 2.3 Non-Goals

The mini-project will not attempt to provide: - Production-grade public
cloud hosting. - End-to-end encryption, user billing, or commercial
multi-tenant storage. - Global geographic replication. - Guaranteed
durability under simultaneous loss of every replica. - Large-scale
orchestration, automatic capacity scaling, or enterprise access
control. - A claim of high availability when the coordinator or metadata
database itself is unavailable.

------------------------------------------------------------------------

## 3. Scope

### 3.1 Minimum Viable Product (MVP)

The MVP includes: - Dashboard and file upload. - File listing and
metadata. - SHA-256 checksum generation. - Replication to two
independent storage nodes. - Node heartbeat monitoring and health
status. - A controlled node-failure simulation. - Recovery/download from
a healthy replica. - Checksum verification after recovery. - Operation
status and basic logs.

### 3.2 Optional Enhancements

Only implement these after the MVP works: - Three or more storage
nodes. - Configurable replication factor. - Retry for failed replication
attempts. - Search, filtering, and sorting of files. - Backup history
and recovery history. - A simple storage-usage chart. - Docker health
checks and automatic service restart. - File deletion with replica
cleanup. - Downloadable recovery report.

------------------------------------------------------------------------

## 4. User Stories

1.  **Upload a file:** As a user, I want to upload a file so that the
    system can back it up.
2.  **View backups:** As a user, I want to see my uploaded files, sizes,
    checksums, and statuses.
3.  **View node health:** As an administrator, I want to see whether
    each storage node is healthy or unavailable.
4.  **Simulate a failure:** As an evaluator, I want to mark or stop a
    node so that I can demonstrate failure handling.
5.  **Recover a file:** As a user, I want the system to retrieve a file
    from a healthy replica if one node fails.
6.  **Verify integrity:** As a user, I want the system to verify the
    recovered file so that corruption can be detected.
7.  **Review operations:** As an evaluator, I want to see replication
    and recovery outcomes to understand what the system did.

------------------------------------------------------------------------

## 5. Functional Requirements

### FR-01: File Upload

-   The frontend must allow the user to choose and upload a file.
-   The backend must validate that a file was supplied and reject empty
    or invalid requests where appropriate.
-   The backend must generate a unique file identifier rather than
    relying on the original filename as a storage key.
-   The system must calculate the SHA-256 checksum of the uploaded
    content.
-   The UI must show upload progress or a clear pending/success/failure
    state.
-   The initial implementation should define a configurable file-size
    limit and communicate it to the user.

**Acceptance criteria:** Uploading a valid file creates a file record
and reports its checksum and replication status.

### FR-02: File Metadata

Store the following metadata in PostgreSQL: - Unique file ID. - Original
filename. - File size in bytes. - MIME type, if available. - SHA-256
checksum. - Upload timestamp. - Overall backup status. - Replica
locations and replica statuses.

The original filename must be treated as display metadata, not as a
trusted filesystem path.

**Acceptance criteria:** Refreshing the page does not remove the file
metadata.

### FR-03: File Replication

-   The coordinator must write the uploaded content to at least two
    independent storage nodes.
-   Each node must store the file using a safe unique identifier.
-   The coordinator must record which nodes contain a copy and the
    status of each copy.
-   A file must not be marked `REPLICATED` until the configured
    replication requirement has been met.
-   If a replica write fails, the system must record the failure and
    report a partial or failed backup status.
-   The implementation must not simulate replication by writing multiple
    database rows that point to the same physical copy.

**Acceptance criteria:** The same file content exists in separate node
storage locations and the dashboard shows the replica status for each
node.

### FR-04: SHA-256 Integrity Verification

-   Calculate SHA-256 from the original uploaded bytes.
-   Store the resulting checksum in PostgreSQL.
-   During recovery, calculate SHA-256 again from the recovered bytes.
-   Compare the calculated checksum with the recorded checksum.
-   Mark the recovery as successful only when the checksums match.
-   Report an integrity failure if they differ.

**Acceptance criteria:** An unchanged recovered file passes
verification. A deliberately altered test copy fails verification.

### FR-05: Heartbeat Failure Detection

-   Each storage node must expose a health endpoint or send periodic
    heartbeat messages.
-   The coordinator must track the last successful heartbeat time.
-   A node should be marked `UNAVAILABLE` after a configurable number of
    missed heartbeat intervals or timeout period.
-   The dashboard must display `HEALTHY`, `UNAVAILABLE`, or `UNKNOWN`.
-   Health status must be based on actual heartbeat or health-check
    results, not a hardcoded UI value.

**Acceptance criteria:** When a node is stopped or its health endpoint
becomes unreachable, its status changes to unavailable after the
configured timeout.

### FR-06: Failure Simulation

-   Provide a controlled way to stop a storage-node container or
    simulate an unavailable node.
-   If the application uses a simulation toggle rather than stopping a
    container, label it clearly as a simulated failure.
-   The simulation must affect recovery selection and must not merely
    change the displayed status.

**Acceptance criteria:** After a node is made unavailable, recovery does
not attempt to read the file from that node if a healthy replica is
available.

### FR-07: File Recovery and Download

-   The user must be able to request recovery or download of a stored
    file.
-   The coordinator must identify a healthy node with a replica.
-   It must read the file from that node and verify its checksum.
-   If verification passes, return the file to the user and record the
    result.
-   If no healthy replica is available, return a clear error and record
    the failed recovery attempt.
-   The original filename may be used for the download response, but
    must not control the server-side storage path.

**Acceptance criteria:** A file is recovered successfully while one of
its replica nodes is unavailable, provided another healthy replica
exists.

### FR-08: Dashboard

The dashboard should show: - Total backed-up files. - Total storage
nodes. - Number of healthy and unavailable nodes. - Recent upload,
replication, and recovery activity. - File name, size, upload time,
checksum (possibly shortened in the table), and backup status. - Replica
status for each file. - Actions to upload, download/recover, and inspect
details.

The dashboard must display actual API data. Do not use hardcoded success
metrics.

### FR-09: Error Handling

The system must handle: - Unsupported or missing upload data. -
File-size limit violations. - Database connection failures. -
Storage-node timeouts. - Replication failures. - Missing file IDs. - No
healthy replica available. - Checksum mismatch. - Invalid API requests.

Errors should be logged on the backend and translated into
understandable UI messages. Do not expose stack traces or secrets to the
browser.

### FR-10: Operation History

Record basic events such as: - File uploaded. - Replica write succeeded
or failed. - Node became unavailable or healthy. - Recovery started,
succeeded, or failed. - Checksum verification passed or failed.

Each event should include a timestamp, operation type, relevant file or
node ID when applicable, status, and a short message.

------------------------------------------------------------------------

## 6. Non-Functional Requirements

### NFR-01: Usability

The UI must be responsive, readable, and straightforward to use during a
live demonstration.

### NFR-02: Reliability

A failed replica write must not be reported as a successful fully
replicated backup. Recovery must use a healthy available replica and
verify integrity.

### NFR-03: Performance

For the mini-project, small test files should upload, replicate, and
recover within a reasonable time on a development machine. Measure
actual timings rather than promising a fixed production SLA.

### NFR-04: Security

-   Validate file size and input values.
-   Use generated storage identifiers and prevent path traversal.
-   Restrict CORS to the configured frontend origin during development.
-   Do not expose database credentials in frontend code.
-   Do not expose arbitrary filesystem paths through API parameters.
-   For a classroom prototype, document whether authentication is
    omitted; do not imply that the system is production-secure.

### NFR-05: Maintainability

Keep frontend, backend, storage-node behavior, database configuration,
and Docker configuration separated into understandable modules.

### NFR-06: Portability

Document required versions and provide Docker Compose configuration
where feasible.

------------------------------------------------------------------------

## 7. Proposed Architecture

### 7.1 Components

1.  **React + Vite frontend:** Displays files, node health, backup
    status, and recovery actions.
2.  **Spring Boot coordinator/API:** Validates requests, calculates
    checksums, coordinates replication, selects healthy replicas, and
    performs recovery.
3.  **PostgreSQL database:** Stores file metadata, node records, replica
    records, and operation history.
4.  **Storage Node A:** Stores file copies and exposes a health
    endpoint.
5.  **Storage Node B:** Stores replica copies independently from Node A.
6.  **Heartbeat monitor:** Checks node health at a configurable interval
    and updates node status.
7.  **Docker Compose:** Starts the frontend, backend, database, and
    storage nodes as separate services.

### 7.2 Typical Upload Flow

1.  User selects a file in the React interface.
2.  Frontend sends a multipart upload request to Spring Boot.
3.  Backend validates the request and calculates SHA-256.
4.  Backend creates a metadata record or tracks the operation as
    pending.
5.  Backend writes the file to Storage Node A and Storage Node B.
6.  Backend records replica outcomes in PostgreSQL.
7.  Backend marks the backup as replicated only when the replication
    requirement is met.
8.  Frontend refreshes and displays the final status.

### 7.3 Typical Recovery Flow

1.  User selects a file and requests recovery/download.
2.  Backend looks up the file metadata and replica locations.
3.  Backend excludes nodes marked unavailable and checks candidate
    replicas.
4.  Backend reads the content from a healthy node.
5.  Backend calculates SHA-256 and compares it with the stored checksum.
6.  If the checksum matches, backend returns the file and records
    success.
7.  If it does not match, backend records an integrity failure and tries
    another healthy replica if one is available.

### 7.4 Failure Scenario

-   Initially, both storage nodes are healthy and contain a copy.
-   Stop Storage Node A or make its health endpoint unavailable.
-   The heartbeat monitor marks Node A unavailable after the timeout.
-   Request the file again.
-   The coordinator selects Storage Node B.
-   The recovered file passes SHA-256 verification and can be
    downloaded.

### 7.5 Architecture Limitation

In the basic mini-project, the coordinator and PostgreSQL database may
remain single points of failure. The project demonstrates replicated
file storage and recovery from an individual storage-node failure; it
does not claim full system availability if every component fails.

------------------------------------------------------------------------

## 8. Technology Stack

  -----------------------------------------------------------------------
  Layer                   Technology              Purpose
  ----------------------- ----------------------- -----------------------
  Frontend                React.js                User interface

  Frontend tooling        Vite                    Development server and
                                                  build

  Styling                 CSS                     Responsive dashboard

  Backend                 Java 21                 Backend runtime

  Backend framework       Spring Boot             REST API and
                                                  application logic

  Database                PostgreSQL              Metadata and operation
                                                  history

  API communication       REST APIs / JSON        Frontend-to-backend
                                                  requests and service
                                                  coordination

  Integrity               SHA-256                 Detect content changes

  Distributed storage     Separate storage        Store independent file
                          nodes/services          replicas

  Failure detection       Heartbeat / periodic    Detect unavailable
                          health checks           nodes

  Fault tolerance         Replica-based recovery  Recover from a healthy
                                                  copy

  Containerization        Docker Compose          Run services together
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 9. Suggested Database Design

The following is a suggested logical schema. Column types and
constraints can be adapted during implementation.

### 9.1 `files`

-   `id`: primary key or UUID.
-   `original_filename`: original display name.
-   `size_bytes`: file size.
-   `content_type`: MIME type, if known.
-   `sha256_checksum`: checksum of the original file.
-   `backup_status`: `PENDING`, `REPLICATED`, `PARTIAL`, or `FAILED`.
-   `created_at`: upload timestamp.

### 9.2 `storage_nodes`

-   `id`: primary key.
-   `node_name`: human-readable node name.
-   `base_url` or service identifier: internal node address.
-   `status`: `HEALTHY`, `UNAVAILABLE`, or `UNKNOWN`.
-   `last_heartbeat_at`: timestamp of the last successful health check.
-   `updated_at`: last status update time.

### 9.3 `file_replicas`

-   `id`: primary key.
-   `file_id`: foreign key to `files`.
-   `node_id`: foreign key to `storage_nodes`.
-   `storage_key`: generated identifier used by the node.
-   `replica_status`: `PENDING`, `AVAILABLE`, or `FAILED`.
-   `created_at`: replication timestamp.

Add a uniqueness constraint on `(file_id, node_id)` if only one replica
per file per node is supported.

### 9.4 `operation_logs`

-   `id`: primary key.
-   `file_id`: nullable reference to a file.
-   `node_id`: nullable reference to a node.
-   `operation_type`: upload, replication, heartbeat, recovery, or
    verification.
-   `status`: success, failure, or warning.
-   `message`: short diagnostic message.
-   `created_at`: event timestamp.

------------------------------------------------------------------------

## 10. Suggested REST API

These routes are recommendations; use consistent names throughout the
implementation.

  -------------------------------------------------------------------------------------
  Method                  Endpoint                              Purpose
  ----------------------- ------------------------------------- -----------------------
  `POST`                  `/api/files`                          Upload a file and
                                                                initiate replication

  `GET`                   `/api/files`                          List backed-up files

  `GET`                   `/api/files/{id}`                     Get file metadata and
                                                                replica status

  `GET`                   `/api/files/{id}/download`            Download a file from a
                                                                healthy replica after
                                                                verification

  `POST`                  `/api/files/{id}/recover`             Run an explicit
                                                                recovery and
                                                                verification operation

  `GET`                   `/api/nodes`                          List storage nodes and
                                                                their health

  `GET`                   `/api/nodes/{id}/health`              Check an individual
                                                                node's health

  `GET`                   `/api/operations`                     List recent operation
                                                                history

  `POST`                  `/api/nodes/{id}/simulate-failure`    Trigger a clearly
                                                                labelled
                                                                development-only
                                                                failure simulation, if
                                                                implemented

  `POST`                  `/api/nodes/{id}/simulate-recovery`   Restore a simulated
                                                                node to service, if
                                                                implemented
  -------------------------------------------------------------------------------------

For production deployment, failure simulation endpoints must be
protected or removed. Do not expose arbitrary host-level
container-control commands through a public API.

------------------------------------------------------------------------

## 11. Frontend Pages and UI Requirements

### 11.1 Dashboard

-   Summary cards: total files, healthy nodes, unavailable nodes, recent
    recoveries.
-   Node-health panel.
-   Recent operation list.
-   A visible upload action.

### 11.2 Backed-Up Files

-   Table of filename, size, upload date, checksum, and backup status.
-   Actions to inspect, recover, or download a file.
-   Empty state when no files have been uploaded.
-   Loading and error states.

### 11.3 File Details

-   Full checksum.
-   Replica list and the node associated with each replica.
-   Current replica status.
-   Recovery/verification result.

### 11.4 Storage Nodes

-   Node name and status.
-   Last successful heartbeat.
-   Number of replicas stored, if accurately available.
-   A development-only failure/recovery control, if implemented.

### 11.5 Activity History

-   Timestamp.
-   Operation.
-   Related file or node.
-   Outcome and concise message.

------------------------------------------------------------------------

## 12. Testing Plan

### Test 1: Successful Upload

**Action:** Upload a small text or PDF file.\
**Expected:** File metadata is stored, checksum is calculated, and
replication status reflects actual outcomes.

### Test 2: Replication

**Action:** Inspect both storage nodes after upload.\
**Expected:** Independent copies exist on the configured nodes and
replica records match the real storage state.

### Test 3: Heartbeat Detection

**Action:** Stop or disconnect one storage node.\
**Expected:** Its status changes to unavailable after the configured
timeout.

### Test 4: Recovery After Node Failure

**Action:** Request a file when Node A is unavailable and Node B has a
valid replica.\
**Expected:** The system recovers from Node B without reading from Node
A.

### Test 5: Checksum Verification

**Action:** Recover an unchanged replica.\
**Expected:** The calculated checksum matches the original checksum.

### Test 6: Corrupted Replica

**Action:** Modify a test replica's contents.\
**Expected:** Checksum verification fails and the system does not report
a verified recovery from that replica.

### Test 7: No Available Replica

**Action:** Make all nodes holding the file unavailable.\
**Expected:** The system reports that recovery is not possible and
records the failure.

### Test 8: Invalid Upload

**Action:** Submit a missing, empty, or oversized upload according to
the defined rules.\
**Expected:** The backend rejects the request safely and returns a
useful error.

### Test 9: Persistence

**Action:** Restart the application while retaining the configured
database and storage volumes.\
**Expected:** Existing metadata and files remain available.

### Test 10: UI Error States

**Action:** Stop the backend or make an API request fail.\
**Expected:** The frontend displays a clear error and does not show
false success.

------------------------------------------------------------------------

## 13. Implementation Plan for a One-Day Mini-Project

The following schedule is a target, not a guarantee. Prioritize the
end-to-end workflow over visual polish.

### Phase 1 --- Project Setup

-   Create the React/Vite frontend.
-   Create the Spring Boot project.
-   Configure PostgreSQL.
-   Create a shared environment/configuration file strategy.

### Phase 2 --- Core File Operations

-   Implement upload and list APIs.
-   Store metadata.
-   Calculate SHA-256.
-   Confirm that a file can be downloaded.

### Phase 3 --- Replication

-   Implement two independent storage nodes.
-   Store replicas on both nodes.
-   Persist replica status.
-   Display replica locations in the UI.

### Phase 4 --- Failure Detection and Recovery

-   Implement health endpoints and periodic checks.
-   Display health status.
-   Test node failure.
-   Recover from the remaining healthy replica.
-   Verify the recovered checksum.

### Phase 5 --- Integration and Demonstration

-   Connect every UI action to the actual backend.
-   Test the complete scenario from the browser.
-   Add error handling and a README.
-   Avoid adding optional features until the required workflow works.

**Priority order if time runs short:** upload → checksum → two
independent copies → recovery from one unavailable node → checksum
verification → dashboard polish.

------------------------------------------------------------------------

## 14. DCS Concepts Demonstrated

### Distributed Storage

The file content is stored on multiple independent storage nodes rather
than relying on one physical copy.

### Replication

The system writes the same logical file to multiple nodes to improve the
chance that a copy remains available after an individual node fails.

### Heartbeat Failure Detection

Periodic health checks provide evidence that a node is reachable. Missed
heartbeats or failed checks cause the coordinator to mark it unavailable
after a timeout.

### Fault Tolerance

If one storage node fails, the system can continue a recovery operation
using another healthy node containing a valid replica.

### Data Integrity

SHA-256 provides a content fingerprint. Matching checksums provide
strong evidence that the recovered bytes match the original bytes; they
do not prove that a file is authentic or that the original was
trustworthy.

### REST-Based Communication

The frontend communicates with the coordinator through HTTP REST APIs.
The coordinator communicates with storage services through defined
internal endpoints or storage adapters.

### Containerization

Docker Compose can run the coordinator, database, and storage nodes as
separate services, making the distributed layout easier to reproduce and
demonstrate.

------------------------------------------------------------------------

## 15. Viva Questions and Short Answers

**Q1. Why is this a distributed system?**\
Because file copies are stored and accessed across independent storage
services, and the coordinator communicates with those services over
defined interfaces.

**Q2. Why do we replicate files?**\
Replication provides additional copies so a file can remain recoverable
when an individual storage node fails.

**Q3. What is fault tolerance in this project?**\
The ability to recover a file from a healthy replica when another node
holding that file is unavailable.

**Q4. What is a heartbeat?**\
A periodic health signal or check used to determine whether a storage
node is responding.

**Q5. Can heartbeat monitoring perfectly detect failures?**\
No. A slow network or overloaded node can look like a failure. Timeouts
provide an operational estimate, not absolute proof that a node has
crashed.

**Q6. Why use SHA-256?**\
It produces a fixed-length checksum that can be recalculated to detect
accidental or unexpected changes in file contents.

**Q7. Why use PostgreSQL?**\
It stores structured metadata, node information, replica mappings, and
operation history.

**Q8. What happens if one node fails?**\
The coordinator marks it unavailable after health checks fail and
chooses a healthy replica for recovery.

**Q9. What happens if all replicas fail?**\
Recovery cannot succeed until a valid copy becomes available.
Replication reduces risk but cannot guarantee survival after loss of
every copy.

**Q10. Is the whole application highly available?**\
Not necessarily. If the coordinator or metadata database is a single
point of failure, the whole application may become unavailable even when
file replicas still exist.

**Q11. Is Docker Compose itself the replication mechanism?**\
No. Docker Compose runs the services. The application's replication
logic writes and tracks the file copies.

**Q12. What is the difference between replication and backup?**\
Replication maintains copies across nodes; a backup often also preserves
recoverable historical versions. This MVP focuses on replicated file
copies and recovery, not full versioned backup retention.

------------------------------------------------------------------------

## 16. Demo Script

Use a small test file and perform these steps in order:

1.  Open the dashboard and show both storage nodes as healthy.
2.  Upload a sample file.
3.  Show the generated SHA-256 checksum and successful replica records.
4.  Show that the file exists on two independent storage nodes.
5.  Stop or simulate the failure of Storage Node A.
6.  Wait for heartbeat monitoring to mark Node A unavailable.
7.  Request recovery of the uploaded file.
8.  Show that the system selects Storage Node B.
9.  Show successful checksum verification.
10. Download the recovered file and open it to confirm its contents.
11. Explain what happens if no healthy replica remains.

Do not claim a test passed unless you have run it successfully.

------------------------------------------------------------------------

## 17. Risks and Mitigations

  -----------------------------------------------------------------------
  Risk                                Mitigation
  ----------------------------------- -----------------------------------
  The full stack is too large to      Implement the smallest end-to-end
  complete in one day                 workflow first

  Docker or PostgreSQL setup takes    Get the backend working locally
  too long                            first; retain the intended
                                      distributed design in the final
                                      setup

  Replicas accidentally share one     Use separate storage directories
  physical location                   or, preferably, separate storage
                                      containers and volumes

  UI displays false node status       Fetch status from real health-check
                                      results

  Recovery reads from a failed node   Filter unavailable nodes before
                                      selecting a replica

  Corrupt content is returned as      Verify SHA-256 before reporting
  successful recovery                 recovery success

  File upload blocks or exhausts      Set a file-size limit and use safe
  memory                              file-handling practices

  Failure simulation is mistaken for  Clearly distinguish simulated
  real failure detection              failure from actual health-check
                                      failure

  Coordinator or database becomes     Document this as an MVP limitation
  unavailable                         
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 18. Final Deliverables

The completed mini-project should include: - React frontend source
code. - Java 21 / Spring Boot backend source code. - PostgreSQL schema
or migration scripts. - Storage-node implementation or configuration. -
Docker Compose configuration, if used. - README with setup
instructions. - Architecture diagram. - API endpoint list. - Test cases
and observed results. - Screenshots of upload, replication, node
failure, recovery, and checksum verification. - A concise viva
explanation.

------------------------------------------------------------------------

## 19. Definition of Done

The project is ready for submission when the group can demonstrate the
following workflow without manually editing database records:

**Upload a file → calculate SHA-256 → replicate to independent nodes →
detect a node failure → recover from a healthy replica → verify the
checksum → display the result.**

This workflow is the core deliverable. Additional features should only
be included if they work reliably and can be explained by every group
member.
