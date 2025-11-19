# Helm Chart Automation Setup

This document describes the automated Helm chart update process for Herocraft.

## Overview

When a backend change is merged to `master`, the CI/CD pipeline automatically:
1. Creates a new CalVer tag (e.g., `2025.11.19`)
2. Builds and releases a JAR file
3. Builds and pushes a multi-platform Docker image to GitHub Container Registry
4. **Automatically updates the Helm chart in the separate `helm-charts` repository**

## How It Works

The `update-helm-chart` job in `.github/workflows/docker-image.yml` performs the following:

1. **Checks out the helm-charts repository** using a Personal Access Token (PAT)
2. **Updates the image tag** in `charts/herocraft/values.yaml` to the new version
3. **Increments the chart version** in `charts/herocraft/Chart.yaml` (patch version)
4. **Commits and pushes** the changes back to the helm-charts repository

### Updated Files

- `charts/herocraft/values.yaml` - Updates the `image.tag` field
- `charts/herocraft/Chart.yaml` - Increments the `version` field

## Required Setup

### 1. Create a Personal Access Token (PAT)

You need to create a GitHub Personal Access Token with the following permissions:
- `repo` scope (full control of private repositories)

**Steps:**
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a descriptive name like "Herocraft Helm Chart Automation"
4. Select the `repo` scope
5. Generate the token and copy it

### 2. Add the Token as a Repository Secret

Add the PAT as a secret in the Herocraft repository:

1. Go to the Herocraft repository on GitHub
2. Navigate to Settings → Secrets and variables → Actions
3. Click "New repository secret"
4. Name: `HELM_CHARTS_PAT`
5. Value: Paste your PAT
6. Click "Add secret"

### 3. Ensure Correct Repository Structure

The automation assumes:
- Helm charts repository is named `helm-charts` under the same GitHub organization/user
- The chart is located at `charts/herocraft/` in the helm-charts repository
- The main branch is named `main` (not `master`)

## Workflow Trigger

The automation runs whenever:
- Code is pushed to the `master` branch
- The changes are NOT in `src/webapp/**` (frontend-only changes don't trigger it)

## What Happens After Automation

1. The helm-charts repository receives a new commit with updated versions
2. The existing `herocraft-chart-release.yaml` workflow in helm-charts automatically:
   - Detects the chart version change
   - Packages the chart
   - Creates a GitHub release
   - Updates the Helm chart repository index

## Versioning Strategy

- **Application Version (Image Tag)**: Uses CalVer format (YYYY.M.D) from the Herocraft repository
- **Chart Version**: Uses Semantic Versioning (X.Y.Z), automatically incremented on each update
  - Patch version increments automatically with each backend release
  - Minor/major versions should be incremented manually when making significant chart changes

## Manual Override

If you need to manually update the Helm chart:

1. Edit `charts/herocraft/values.yaml` and/or `charts/herocraft/Chart.yaml` in the helm-charts repo
2. Commit and push to `main` branch
3. The chart releaser will automatically package and publish the new version

## Troubleshooting

### "remote: Permission to helm-charts.git denied"
- Check that the `HELM_CHARTS_PAT` secret is correctly set
- Verify the PAT has `repo` scope
- Ensure the PAT hasn't expired

### "No changes to commit"
- This can happen if the version hasn't changed or if the workflow ran twice
- This is not an error - the workflow will exit gracefully

### Chart not releasing after update
- Check the helm-charts repository workflows to ensure `herocraft-chart-release.yaml` ran successfully
- Verify the chart version was actually incremented in `Chart.yaml`

## Example

When a commit is merged to `master` on 2025-11-19:

1. New tag created: `v2025.11.19`
2. Docker image pushed: `ghcr.io/sirlag/herocraft:2025.11.19`
3. Helm chart updated:
   - `values.yaml`: `tag: "2025.11.19"`
   - `Chart.yaml`: `version: 0.1.21` (incremented from 0.1.20)
4. Commit message: "Update herocraft to version 2025.11.19"
5. Chart automatically released by helm-charts repository workflow
