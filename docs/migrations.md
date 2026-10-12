# Database Migration Guide & Runbook

This document details database management rules, schema versioning best practices, production backup requirements, and disaster recovery procedures for `order-service`.

---

## 1. Core Principles
1. **Flyway as Single Source of Truth**: `spring.jpa.hibernate.ddl-auto` is set to `none` (or `validate`) in all runtime environments. Auto-DDL is strictly forbidden.
2. **Backward Compatibility (Expand & Contract)**:
   - Every schema change must be non-breaking to the currently running version of the service.
   - New columns must either be nullable or carry explicit default values.
   - Renaming or dropping columns requires a multi-step migration (Deprecate -> Stop Reading/Writing -> Drop in subsequent release).

---

## 2. Pre-Migration Checklist (Production)

Before applying migrations in Production:
- [ ] **Automated Backup / Snapshot**:
  - AWS RDS: Trigger a manual snapshot before triggering deployment:
    ```bash
    aws rds create-db-snapshot \
      --db-instance-identifier <RDS_INSTANCE_IDENTIFIER> \
      --db-snapshot-identifier pre-migration-backup-$(date +%Y%m%d%H%M%S)
    ```
- [ ] **Verify Migration Locally / Staging**: Test migration scripts on clean staging replica.
- [ ] **Locking & Performance Review**: Confirm that changes on large tables do not cause long exclusive table locks.

---

## 3. Failure Scenarios and Recovery Procedures

### Scenario A: Migration Fails During Application Startup
When Flyway encounters an error, it records a row in `flyway_schema_history` marked with `success = false` and terminates startup immediately.

#### Step 1: Inspect Failure Details
Inspect the error message from service logs or directly query:
```sql
SELECT installed_rank, version, description, type, script, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 5;
```

#### Step 2: Fix Schema State / Rollback Script
- If the statement failed before altering schema: Fix the underlying SQL script or database permissions.
- If DDL was partially applied: Manually revert the partial DDL change on the database.

#### Step 3: Repair Flyway Metadata
Run the Flyway repair command or clean the failed migration record:
```bash
# Using Flyway CLI or Maven plugin
mvn flyway:repair -pl order-service -Dflyway.url="${DB_URL}" -Dflyway.user="${DB_USER}" -Dflyway.password="${DB_PASSWORD}"
```
Or manually remove the failed record:
```sql
DELETE FROM flyway_schema_history WHERE version = '<FAILED_VERSION>' AND success = false;
```

#### Step 4: Re-run Deployment
Re-trigger the deployment workflow after fixing the SQL issue.

### Scenario B: Catastrophic Data Inconsistency
If a migration caused data corruption:
1. Immediately invoke the rollback deployment pipeline or scale down service replicas.
2. Restore the database from the pre-migration RDS snapshot taken before deployment.
3. Repoint the service to the restored database instance.
