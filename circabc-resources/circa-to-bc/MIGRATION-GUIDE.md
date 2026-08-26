# CIRCA to CIRCABC Migration System

## Overview

The `circabc-resources/circa-to-bc` module implements the migration system that was used to transfer Interest Groups (IGs) and their content from the legacy **CIRCA** platform to **CIRCABC**. It provides a complete ETL (Extract, Transform, Load) pipeline with scheduling, validation, and tracing capabilities.

The system was originally designed by Yanick Pignot and is licensed under the EUPL V.1.0.

---

## Repository Structure

```
circabc-resources/circa-to-bc/
├── source/
│   ├── java/
│   │   ├── eu/cec/digit/circabc/
│   │   │   ├── migration/                    # Core migration framework
│   │   │   │   ├── aida/                     # AIDA user integration
│   │   │   │   ├── archive/                  # Iteration storage (FileArchiver)
│   │   │   │   │   └── impl/                 # RepositoryArchiver, MigrationIterationImpl
│   │   │   │   ├── entities/                 # XML data model
│   │   │   │   │   ├── adapter/              # JAXB adapters
│   │   │   │   │   └── generated/            # JAXB-generated classes (ImportRoot, etc.)
│   │   │   │   ├── journal/                  # MigrationTracer, JournalLine
│   │   │   │   │   └── etl/                  # ETLReport, PathologicUser
│   │   │   │   ├── listener/                 # Event listeners
│   │   │   │   ├── log4j/                    # Logging configuration
│   │   │   │   ├── post/                     # Post-migration processors
│   │   │   │   ├── processor/                # Import pre/run/post processors
│   │   │   │   ├── reader/                   # Data readers (FTP, NNTP, LDAP, DB)
│   │   │   │   ├── tools/                    # Utility classes
│   │   │   │   ├── validation/               # XML validation handlers
│   │   │   │   └── walker/                   # Tree walkers for XML processing
│   │   │   ├── repo/migration/               # Service implementations
│   │   │   │   ├── ExportServiceImpl.java
│   │   │   │   ├── ImportServiceImpl.java
│   │   │   │   ├── LdapUsersETLServiceImpl.java
│   │   │   │   ├── PlannedMigrationServiceImpl.java
│   │   │   │   ├── JavaXmlBinder.java        # JAXB marshalling/unmarshalling
│   │   │   │   └── ...
│   │   │   ├── repo/web/scripts/             # REST API web scripts
│   │   │   ├── service/migration/            # Service interfaces
│   │   │   │   ├── ExportService.java
│   │   │   │   ├── ImportService.java
│   │   │   │   ├── ETLService.java
│   │   │   │   ├── AidaMigrationService.java
│   │   │   │   └── jobs/                     # PlannedMigrationService, status classes
│   │   │   └── web/wai/                      # JSF Web UI
│   │   │       ├── bean/admin/               # Managed beans
│   │   │       │   ├── ManageExportationsBean.java
│   │   │       │   ├── ManageImportationsBean.java
│   │   │       │   ├── MigrationETLBean.java
│   │   │       │   ├── RunningImportsBean.java
│   │   │       │   ├── ExporterBean.java
│   │   │       │   ├── ImporterBean.java
│   │   │       │   └── UploadImportationFileBean.java
│   │   │       └── dialog/admin/migration/   # Dialog beans (newer UI)
│   │   │           ├── ExportIgDialog.java
│   │   │           └── ImportIgDialog.java
│   │   └── alfresco/extension/
│   │       ├── migration/
│   │       │   ├── circabc-exportation.properties
│   │       │   └── circabc-importation.properties
│   │       └── circabc-importation-customization-context.xml
│   └── web/WEB-INF/                          # JSP pages and faces config
```

---

## How the Migration Worked

### The Three-Phase Pipeline

The migration follows a strict three-phase pipeline. Each phase produces artifacts that feed the next:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   PHASE 1       │     │   PHASE 2       │     │   PHASE 3       │
│   EXPORT        │────▶│   ETL           │────▶│   IMPORT        │
│                 │     │   (Transform)   │     │                 │
│ Reads CIRCA     │     │ Validates users │     │ Creates nodes   │
│ via FTP/NNTP/   │     │ Maps to ECAS    │     │ in CIRCABC      │
│ LDAP/DB         │     │ Produces valid  │     │ Applies perms   │
│                 │     │ XML             │     │ Uploads content │
│ Output:         │     │ Output:         │     │ Output:         │
│ uploaded.xml    │     │ valid.xml       │     │ applied.xml     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

---

## Phase 1: Export

### What It Does

The Export service connects to a running CIRCA instance and extracts all data for selected Interest Groups into an XML file.

### Data Sources

| Source | Protocol | What is extracted |
|--------|----------|-------------------|
| File system | FTP | Library documents, Information pages, Meeting files |
| Database | JDBC (MySQL) | Metadata, configurations |
| Newsgroups | NNTP | Forum topics, messages, replies |
| Directory | LDAP | Users, groups, permissions |
| Configuration | FTP | IG settings (ig.cfg), logos |
| Logs | FTP | Access logs, activity history |

### Configuration (`circabc-exportation.properties`)

```properties
# FTP connection to CIRCA server
export.ftp.host=alpha1.cc.cec.eu.int
export.ftp.port=21
export.ftp.user=circadev
export.ftp.dataRoot=./www/data

# Database connection
export.db.jdbc.driver=org.gjt.mm.mysql.Driver
export.db.jdbc.url=jdbc:mysql://host:6030/irc

# NNTP (newsgroups)
export.nntp.host=alpha1.cc.cec.eu.int
export.nntp.port=6022

# LDAP (users/permissions)
export.user.ldap.host=alpha1.cc.cec.eu.int
export.user.ldap.port=6028
```

### Key Classes

- **`ExportService`** (interface) — defines `runExport(pairs, iterationName, description)`
- **`ExportServiceImpl`** — orchestrates all readers, produces `ImportRoot` XML
- **Readers**: `RemoteFileReader` (library/info), `SecurityReader`, `MetadataReader`, `UserReader`, `CalendarReader`, `NewsgroupReader`, `LogFileReader`

### Export Output

The export creates an **iteration** in the Alfresco repository under:
```
/Company Home/Data Dictionary/Migration History/<iteration-name>/original/uploaded.xml
```

The XML follows the `ImportRoot` schema with namespaces:
- `https://circabc.europa.eu/Import/NodesSchema/1.0` — node hierarchy
- `https://circabc.europa.eu/Import/UsersSchema/1.0` — user data
- `https://circabc.europa.eu/Import/ImportSchema/1.0` — import metadata

---

## Phase 2: ETL (Extract, Transform, Load)

### What It Does

The ETL phase validates and transforms user identities. CIRCA used its own user directory (internal LDAP); CIRCABC uses ECAS (EU Login). The ETL maps CIRCA usernames to their ECAS equivalents so that permissions, ownership, and authorship are preserved after migration.

### Where It Lives

- **Interface**: `eu.cec.digit.circabc.service.migration.ETLService`
- **Implementation**: `eu.cec.digit.circabc.repo.migration.LdapUsersETLServiceImpl`
- **UI Bean**: `eu.cec.digit.circabc.web.wai.bean.admin.MigrationETLBean`
- **Spring config**: `circabc-importation-customization-context.xml`

### The Two-Step ETL Process

#### Step 1: `proposeEtl(iterationName)` — Analyze Users

This method reads the exported XML and identifies all user references:

1. **Unmarshalls** the XML file (either `uploaded.xml` for first ETL, or previous `valid.xml` for re-ETL)
2. **Extracts all user IDs** from the XML tree using JXPath queries:
   - `.//*[owner]` — all nodes with an owner
   - `.//*[creator]` — all nodes with a creator
   - `.//*[modifier]` — all nodes with a modifier
   - `.//*[user]` — all elements with a single user reference (permissions, notifications)
   - `.//*[users]` — all elements with user lists (group members)
   - `.//*[author]` — all content nodes with an author
3. **Loads previously resolved users** from `previous_valid_users.properties` (cache from earlier ETL runs)
4. **For each user**, calls `checkPerson()` which attempts resolution in this order:

```
┌─────────────────────────────────────────────────────────────────┐
│                    User Resolution Strategy                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. Already transformed?                                          │
│     └─ Check previous_valid_users.properties cache                │
│        └─ If found → verify still exists in ECAS → VALID         │
│                                                                   │
│  2. No person data at all?                                        │
│     ├─ Looks like a UID? → try ECAS lookup by UID                 │
│     └─ Otherwise → try ECAS lookup by CN (common name)            │
│        ├─ Found → PATHOLOGIC (needs confirmation)                 │
│        └─ Not found → PATHOLOGIC (no data)                        │
│                                                                   │
│  3. Has person data (email, userId):                              │
│     ├─ Email empty?                                               │
│     │   ├─ UID ends with @cec? → try ECAS by UID                 │
│     │   └─ Otherwise → PATHOLOGIC (empty email)                   │
│     ├─ Email invalid? → PATHOLOGIC (invalid email)                │
│     └─ Email valid? → query ECAS by email                         │
│         ├─ 0 results → PATHOLOGIC (no user found)                 │
│         ├─ 1 result → check moniker matches circa username        │
│         │   ├─ Matches → VALID (auto-resolved)                    │
│         │   └─ Doesn't match → PATHOLOGIC (needs confirmation)    │
│         └─ >1 results → PATHOLOGIC (too many users)               │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

5. **Returns an `ETLReport`** containing:
   - `transformationElements` — list of successfully resolved users (CIRCA ID → ECAS ID mapping)
   - `pathologicUsers` — list of users that need manual intervention, each with:
     - The original `Person` object
     - A reason message (e.g., "No user found with this email")
     - Proposed ECAS matches (if any)

#### Step 2: `applyEtl(report)` — Generate Output Files

After the admin reviews pathologic users and confirms/corrects mappings, `applyEtl` is called:

1. **Builds a map** of valid users (CIRCA ID → `TransformationElement`)
2. **Re-reads the XML** from the iteration
3. **Generates `valid.xml`** — walks the entire XML tree and:
   - Replaces all owner/creator/modifier/user references with the resolved ECAS UIDs
   - For authors: replaces with "Firstname Lastname" from ECAS
   - Removes permission entries for unresolved users (they go to residual)
   - Updates the `<persons>` section with resolved user data
   - Adds version history and statistics
4. **Generates `residual.xml`** — contains only elements with unresolved users:
   - Nodes whose owner/creator/modifier couldn't be resolved
   - Permission entries for unresolved users
   - The pathologic persons list
   - Preserves the tree structure (parents are included for context)
5. **Stores resolved mappings** in `previous_valid_users.properties` for future ETL runs

### What the Admin Does in the UI (MigrationETLBean)

The ETL dialog is a multi-step wizard:

1. **Select iteration** — dropdown shows iterations where `isIterationReadyForTransformation() == true`
2. **Click "Propose"** — triggers `proposeEtl()`, displays results in two tables:
   - **Valid users table** — auto-resolved users (green)
   - **Pathologic users table** — problematic users (red) with:
     - The CIRCA username and email
     - The reason for failure
     - Proposed ECAS matches (if any)
     - Search fields to query ECAS manually (by UID, email, moniker, CN)
3. **Admin resolves pathologic users**:
   - Select a proposed match → moves user to valid list
   - Search ECAS manually → select from results
   - Mark as "ignore" → user stays in residual
4. **Click "Apply"** — triggers `applyEtl()`, stores `valid.xml` and `residual.xml`

### Key ECAS/LDAP Queries Used

```java
// Find user by email
userService.getLDAPUserIDByMail(email)

// Find user by UID, moniker, email, or CN (flexible search)
userService.getLDAPUserIDByIdMonikerEmailCn(uid, moniker, email, cn, true)

// Get full user data by UID
userService.getLDAPUserDataByUid(uid)
```

### ETL Output

Creates a `transformed_<timestamp>/` subfolder inside the iteration:
```
/Company Home/Data Dictionary/Migration History/<iteration-name>/
  ├── original/
  │   └── uploaded.xml
  └── transformed_2024-03-15/
      ├── valid.xml          ← import-ready (all users resolved to ECAS)
      └── residual.xml       ← unresolved users (for next ETL round)
```

### Re-running ETL

The ETL can be run multiple times on the same iteration:
- First run: reads from `original/uploaded.xml`
- Subsequent runs: reads from the latest `transformed_*/valid.xml`
- Each run creates a new `transformed_<date>/` subfolder
- The `previous_valid_users.properties` cache grows with each run, making subsequent runs faster

### When Is an Iteration Ready for ETL?

`isIterationReadyForTransformation()` returns `true` when:
```java
originalFileNodeRef != null                              // has an uploaded file
AND (transformationDates.size() - importedDates.size() == 0)  // all transforms have been imported
```

This means: either it's a fresh iteration (no transforms yet), or the previous transform was already imported and a new ETL cycle can begin.

---

## Phase 3: Import

### What It Does

The Import service reads the `valid.xml` and creates the corresponding structure in CIRCABC: categories, interest groups, spaces, documents, forums, permissions, etc.

### Import Modes

| Mode | Description |
|------|-------------|
| **Validate** | Checks XML structure, verifies content URLs are accessible |
| **Dry Run** | Simulates the full import without persisting changes |
| **Run** | Executes the actual import, creates nodes in CIRCABC |

### Processing Pipeline

The import uses a configurable processor chain:

1. **Pre-processors** — validation, XML transformation
2. **Run processors** — actual node creation (or simulation for dry-run)
3. **Post-processors** — cleanup, link resolution, finalization

### Configuration (`circabc-importation.properties`)

```properties
# Repository location for iteration storage
import.xml.archive.space=Migration History

# Concurrency lock (prevents parallel imports on cluster)
import.xml.archive.lock.time.sec=43200

# Error handling
import.fail.on.error=false

# Thread pool
import.mt.maxthread=3

# Cross-instance content fetching credentials
circabc.import.remote.username=admin
circabc.import.remote.password=
```

### Key Classes

- **`ImportService`** (interface) — defines `validate`, `dryRun`, `run`, `getIterations`
- **`ImportServiceImpl`** — orchestrates the processor pipeline
- **`ImportIgDialog`** / **`ManageImportationsBean`** — UI for triggering imports

### Import Output

Creates an `import_<timestamp>/` subfolder:
```
/Company Home/Data Dictionary/Migration History/<iteration-name>/
  ├── original/
  ├── transformed_2024-03-15/
  └── import_2024-03-16/
      ├── transformed.xml    ← pre-processed version
      ├── applied.xml        ← with Alfresco NodeRef IDs added
      └── log.xml            ← detailed import journal
```

If import fails, the folder is renamed to `__failed_import_<timestamp>/`.

---

## Iteration Storage Model

### Repository Location

All iterations are stored in the Alfresco repository at:
```
/Company Home/Data Dictionary/Migration History/
```

This is configured by `import.xml.archive.space` property and managed by the `RepositoryArchiver` bean.

### Iteration Folder Structure

```
Migration History/                              ← root archive space
├── planned_iteration_import.properties         ← scheduled import jobs
├── planned_iteration_export.properties         ← scheduled export jobs
├── planned_user_export.properties              ← scheduled user exports
├── planned_export_statistics.properties        ← scheduled statistics
├── previous_valid_users.properties             ← cache of resolved users
│
├── circabc-export-MyIG-20240315-100000/        ← iteration folder
│   ├── original/                               ← Phase 1 output
│   │   ├── uploaded.xml                        ← the export XML
│   │   └── iglogs.properties                   ← IG log entries
│   ├── transformed_2024-03-15/                 ← Phase 2 output
│   │   ├── valid.xml                           ← import-ready XML
│   │   └── residual.xml                        ← unresolved items
│   └── import_2024-03-16/                      ← Phase 3 output
│       ├── transformed.xml
│       ├── applied.xml
│       └── log.xml
│
└── another-iteration/
    └── ...
```

### How Iterations Are Detected

The `RepositoryArchiver.getIterations()` method:
1. Gets the root space (`Migration History`)
2. Lists all **subfolders** (each is a potential iteration)
3. For each subfolder, calls `fillIterationSequences()` which scans child folders:
   - Folder named `original` → sets `originalFileNodeRef` (looks for `uploaded.xml` inside)
   - Folder name containing `transformed_` → adds to `transformationDates` map
   - Folder name containing `import_` → adds to `importedDates` map
   - Folder name containing `__failed_import_` → adds to `failedImportation` map

### Readiness Checks

**Ready for ETL** (`isIterationReadyForTransformation`):
```
originalFileNodeRef != null
AND (transformationDates.size() - importedDates.size() == 0)
```
Meaning: has an original file AND all previous transforms have been imported (or no transforms yet).

**Ready for Import** (`isIterationReadyForMigration`):
```
transformationDates.size() > 0
AND (transformationDates.size() - importedDates.size() == 1)
```
Meaning: has at least one ETL transform AND exactly one transform that hasn't been imported yet.

---

## Copying an Iteration Between Environments (Dev → Test)

### Why Simple File Copy Doesn't Work

The import dropdown only shows iterations that pass `isIterationReadyForMigration()`. This requires:
1. The iteration folder exists under `Migration History`
2. It contains a subfolder named `original` with a file named `uploaded.xml`
3. It contains at least one subfolder whose name contains `transformed_` (e.g., `transformed_2024-03-15`)
4. Inside that transform folder, there must be a file named `valid.xml`
5. There must be NO `import_` subfolder (or one fewer `import_` folder than `transformed_` folders)

### Correct Procedure via FTP

When copying from dev to test via FTP (connecting to Alfresco FTP at `/Alfresco/Data Dictionary/Migration History/`):

1. Copy the **entire iteration folder** including all subfolders:
   ```
   <iteration-name>/
     ├── original/
     │   ├── uploaded.xml
     │   └── iglogs.properties    (optional)
     └── transformed_<date>/
           └── valid.xml
   ```

2. Make sure folder names are **exact**:
   - `original` (not "Original", not "orig")
   - `transformed_` prefix followed by a parseable date (e.g., `transformed_2024-03-15`)
   - File inside original must be named `uploaded.xml`
   - File inside transformed must be named `valid.xml`

3. Do NOT copy any `import_` subfolders (those indicate already-imported iterations)

### Alternative: Cross-Instance Import

Configure the test environment to fetch content from dev remotely:

On **dev** (`alfresco-global.properties`):
```properties
circabc.export.alfresco.base.url=https://circabc.development.europa.eu
```

On **test** (`alfresco-global.properties`):
```properties
circabc.import.remote.username=admin
circabc.import.remote.password=<dev-admin-password>
```

Then export on dev. The XML will contain absolute URLs pointing to dev. When importing on test, the system fetches content from dev using the configured credentials.

---

## Scheduling (PlannedMigrationService)

Jobs can be scheduled for deferred execution:

| Job Type | What It Does |
|----------|--------------|
| `IterationExportStatus` | Exports selected IGs at a scheduled time |
| `IterationImportStatus` | Imports a named iteration at a scheduled time |
| `UserExportStatus` | Bulk user export by email query |
| `StatisticExportStatus` | Exports migration statistics |

Jobs are persisted in `.properties` files under the Migration History root space and executed by a Quartz-like scheduler.

### Job Lifecycle

```
PENDING → WAITING → RUNNING → COMPLETED
                            └→ FAILED
```

---

## XML Data Model (ImportRoot)

The migration XML uses JAXB-generated classes from XSD schemas:

```xml
<importRoot>
  <circabc>                    <!-- Node hierarchy -->
    <categoryHeaders>
      <categoryHeader>
        <category>
          <interestGroup>
            <library>...</library>
            <newsgroup>...</newsgroup>
            <information>...</information>
            <directory>...</directory>
            <events>...</events>
          </interestGroup>
        </category>
      </categoryHeader>
    </categoryHeaders>
  </circabc>
  <persons>                    <!-- User definitions -->
    <person>...</person>
  </persons>
  <logFile>...</logFile>       <!-- Activity logs -->
  <versionHistory>...</versionHistory>
  <statistics>...</statistics>
</importRoot>
```

The `JavaXmlBinder` class handles marshalling (Java → XML) and unmarshalling (XML → Java) using JAXB with UTF-8 encoding and XML character sanitization.

---

## Key Design Decisions

1. **Repository-based storage** — Iterations are stored as Alfresco nodes, not filesystem files. This enables versioning, permissions, and cluster-safe access.

2. **Immutable pipeline** — Each phase produces new artifacts without modifying previous ones. This allows re-running any phase.

3. **Fail-safe imports** — `import.fail.on.error=false` allows partial imports. Failed items are logged but don't stop the process.

4. **Cluster-safe locking** — A 12-hour lock prevents concurrent imports across cluster nodes.

5. **Processor chain** — Import uses configurable pre/run/post processors, making it extensible without modifying core logic.

6. **Tracing** — `MigrationTracer<T>` captures every action with status (SUCCESS/FAIL) for audit and debugging.
