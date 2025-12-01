# Change Management Process

## 1. Introduction
This document establishes the formal Change Management process for the **Ecommerce Microservice Backend** project. The objective is to ensure that all changes to the production environment are assessed, approved, implemented, and reviewed in a controlled manner to minimize risk and impact on services.

## 2. Scope
This process applies to all changes affecting:
- Application source code and APIs.
- Infrastructure configurations (Kubernetes, Docker, Terraform).
- Database schemas and migrations.
- CI/CD pipeline configurations.

## 3. Versioning Strategy
The project adheres to **Semantic Versioning 2.0.0** (`MAJOR.MINOR.PATCH`):

| Component | Description | Example |
| :--- | :--- | :--- |
| **MAJOR** | Incompatible API changes or architectural shifts. | `v1.0.0` -> `v2.0.0` |
| **MINOR** | New functionality added in a backwards-compatible manner. | `v1.1.0` -> `v1.2.0` |
| **PATCH** | Backwards-compatible bug fixes. | `v1.0.1` -> `v1.0.2` |

## 4. Change Lifecycle

### 4.1. Request for Change (RFC)
All changes originate as a **Work Item** (Issue/Ticket) in the project management system (GitHub Issues/Jira).
- **Types:** Feature, Bugfix, Hotfix, Chore.
- **Required Info:** Description, Acceptance Criteria, Priority.

### 4.2. Development & Branching (GitFlow)
- **`main`**: Production-ready state. Protected branch.
- **`develop`**: Integration branch for ongoing development.
- **`feature/*`**: Short-lived branches for new features.
- **`hotfix/*`**: Critical fixes branched directly from `main`.

### 4.3. Review & Approval
Changes are submitted via **Pull Request (PR)** to the `develop` branch.
**Approval Criteria:**
1.  **Automated Checks:** CI pipeline must pass (Unit Tests, Security Scan, E2E).
2.  **Peer Review:** Minimum of 1 approval from a senior developer.
3.  **Code Quality:** No new critical issues in SonarQube/Linting.

### 4.4. Release Preparation
1.  A release candidate is prepared by merging `develop` into `main`.
2.  **Release Notes** are automatically generated based on PR titles and labels.
3.  A **Git Tag** (`vX.Y.Z`) is created to mark the release artifact.

### 4.5. Deployment
Deployment is triggered automatically by the creation of the Git Tag via GitHub Actions.

## 5. Audit & Traceability
All changes are traceable via Git history.
- **Who:** Author of the commit.
- **What:** Diff of the code changes.
- **Why:** Linked Issue/Ticket ID in the PR description.
- **When:** Timestamp of the merge/deployment.
