# Release Process and Promotion Guide

This document describes the promotion lifecycle from code change to production deployment.

---

## 1. Release Lifecycle Overview

| Stage | Trigger Branch / Event | Target Environment | Automated Gates |
|---|---|---|---|
| **Build & CI** | Pull Request (`develop`/`main`) | CI Runner | Unit tests, linting, test reports |
| **Release Artifact** | Merge to `main` | AWS ECR | Builds image tagged with git commit SHA |
| **DEV Promotion** | Automatic on image build | Dev ECS Cluster | Basic container health check |
| **QA Promotion** | Sequential after DEV | QA ECS Cluster | Integration & API tests |
| **Staging Promotion**| Sequential after QA | Staging ECS Cluster | End-to-End tests & smoke check |
| **Production** | Manual review approval | Production ECS Cluster | Pre-migration snapshot check, post-deploy smoke check |

---

## 2. Production Promotion Steps

1. Create a `release/vX.Y.Z` branch from `develop`.
2. Open a Pull Request from `release/vX.Y.Z` into `main`.
3. Once CI passes and peer review approves, merge into `main`.
4. The `Release & Image Build` workflow builds the Docker images once and tags them with the git SHA.
5. In GitHub Actions, navigate to the `Multi-Environment Promotion Pipeline`.
6. When the workflow pauses at the `production` environment, designated team reviewers review the release notes, diff, and smoke results.
7. Approve the deployment.
8. If post-deploy smoke tests fail, the automated `rollback-prod` job will automatically revert to the previous task definition.
9. Tag `main` with `vX.Y.Z` upon successful deployment verification.
