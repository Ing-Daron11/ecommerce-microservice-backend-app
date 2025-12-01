# Rollback & Disaster Recovery Plan

## 1. Objective
This document defines the standard operating procedures for reverting changes in the production environment to restore service stability in the event of a deployment failure or critical defect.

## 2. Rollback Triggers
A rollback must be initiated if any of the following criteria are met within the **Monitoring Window** (1 hour post-deployment):
- **Service Availability** drops below 99.9%.
- **Error Rate (HTTP 5xx)** exceeds 1% of total traffic.
- **Critical Functional Failure** (e.g., users cannot complete checkout).
- **Security Breach** introduced by the new version.

## 3. Version Control Rollback Strategy (GitHub)

Since our deployments are triggered by Git Tags, the rollback process involves reverting the codebase to the previous stable tag.

### 3.1. Scenario: Reverting a Release
**Context:** Release `v1.2.0` is faulty. The previous stable release is `v1.1.0`.

**Procedure:**
1.  **Identify Stable Version:** Confirm `v1.1.0` is the target restoration point via GitHub Releases history.
2.  **Revert `main` Branch:**
    - Do **NOT** delete the faulty tag `v1.2.0` (maintain audit trail).
    - Create a revert commit on `main` that undoes the changes of `v1.2.0`.
    ```bash
    git revert -m 1 <merge-commit-hash-of-v1.2.0>
    ```
3.  **Create Patch Release:**
    - Tag this new state as `v1.2.1` (Patch release).
    - Push the tag to trigger the deployment pipeline.
    ```bash
    git tag v1.2.1
    git push origin v1.2.1
    ```

### 3.2. Scenario: Hotfix (Emergency Fix)
**Context:** A critical bug is found in `v1.2.0`, but a full rollback is not feasible (e.g., database migration issues).

**Procedure:**
1.  Create a `hotfix/v1.2.1` branch from `main`.
2.  Implement the minimal fix required to restore stability.
3.  Merge to `main` via Pull Request.
4.  Tag as `v1.2.1` and deploy.

## 4. Infrastructure Rollback (Kubernetes)

For immediate service restoration while the Git process is executed, use Kubernetes native rollback capabilities.

**Command:**
```bash
# Revert to the previous revision
kubectl rollout undo deployment/<service-name> -n <namespace>
```

**Verification:**
```bash
kubectl rollout status deployment/<service-name> -n <namespace>
```

## 5. Database Rollback Policy
- **Forward-Only Strategy:** Whenever possible, fix data issues with a new migration script (fix-forward) rather than reverting schema changes, to avoid data loss.
- **Restore from Backup:** In catastrophic scenarios (data corruption), restore the database from the Point-in-Time Recovery (PITR) backup taken immediately before the deployment window.

## 6. Post-Rollback Actions
1.  **Incident Report:** Create a Post-Mortem document analyzing the root cause.
2.  **Cleanup:** Ensure the `develop` branch is synchronized with the reverted state of `main`.
3.  **Communication:** Notify stakeholders that the system has been restored to version `vX.Y.Z`.
