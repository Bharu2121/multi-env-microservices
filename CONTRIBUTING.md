# Contributing Guidelines

Thank you for contributing to the Multi-Environment Microservices project.

## Branching Model

We follow a GitFlow-inspired branching strategy:

```
feature/*  --->  develop  --->  release/*  --->  main (tagged vX.Y.Z)
                   ^                                |
                   |--- hotfix/* <------------------|
```

1. **`feature/*`**:
   - Branched from `develop`.
   - Used for individual features or enhancements (e.g., `feature/user-roles`).
   - Merged back into `develop` via Pull Request after CI passes.

2. **`develop`**:
   - The integration branch for current sprint features.
   - Constantly deployed to DEV and QA.

3. **`release/*`**:
   - Branched from `develop` when preparing for a production release (e.g., `release/v1.0.0`).
   - Only bugfixes and documentation are permitted on release branches.
   - Deployed to Staging for E2E qualification.

4. **`main`**:
   - Reflects the latest production release.
   - Every merge into `main` is tagged with semantic versioning (`vX.Y.Z`).

5. **`hotfix/*`**:
   - Branched directly from `main` to address critical production issues.
   - Merged back to both `main` and `develop`.

---

## Pull Request Guidelines

- All pull requests must target `develop` (or `main` in the case of hotfixes).
- All unit and integration tests must pass.
- Database migrations must follow the backward compatibility rules documented in `docs/migrations.md`.
- No secrets, real passwords, or hardcoded API keys may be introduced.
