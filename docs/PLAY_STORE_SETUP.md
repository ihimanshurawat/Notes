# Google Play Store CI/CD Deployment Guide

This guide walks you through setting up the automated publishing pipeline for the **Notes** Android application using GitHub Actions.

---

## 1. Overview of the Pipeline

The workflow defined in [`.github/workflows/deploy_play_store.yml`](../.github/workflows/deploy_play_store.yml) automates the entire release cycle:
- **Trigger**: Runs automatically whenever changes to `app/build.gradle` are pushed to the `master` branch (e.g. on version update), or manually via GitHub's **Actions** tab (`workflow_dispatch`).
- **Safety Check**: Checks if `versionCode` in `app/build.gradle` actually increased. If it didn't increase, the release step is safely skipped.
- **Build & Sign**: Decodes your signing keystore from GitHub Secrets, signs the release App Bundle (`.aab`), and securely wipes the keystore from disk.
- **Publish**: Deploys the `.aab` to Google Play Store using the Google Play Developer API.
- **Tag & Release**: Creates a GitHub Release and Git tag (e.g. `v2.0`) with the `.aab` attached.

---

## 2. Required GitHub Secrets

Navigate to your GitHub repository:
**Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.

Add the following 5 secrets:

| Secret Name | Description | Example / How to generate |
|-------------|-------------|---------------------------|
| `PLAY_KEYSTORE_BASE64` | Base64-encoded Android release Keystore (`.jks` or `.keystore`) | See [Step 3](#3-generating-play_keystore_base64) below |
| `KEYSTORE_PASSWORD` | Password of your keystore | `your_keystore_password` |
| `KEY_ALIAS` | Key alias in your keystore | `your_key_alias` |
| `KEY_PASSWORD` | Password of your key alias | `your_key_password` |
| `PLAY_SERVICE_ACCOUNT_JSON` | Entire JSON content of Google Cloud Service Account key | See [Step 4](#4-setting-up-google-play-developer-api--service-account) below |

---

## 3. Generating `PLAY_KEYSTORE_BASE64`

Run the following command on your local machine to convert your `.jks` file to a Base64 string:

**macOS / Linux:**
```bash
base64 -i /path/to/your/release-key.jks | tr -d '\n' | pbcopy
```
*(On Linux, replace `pbcopy` with `xclip -selection clipboard` or print it to a file: `base64 -w 0 release-key.jks > keystore_base64.txt`)*

Paste the copied string directly as the value of `PLAY_KEYSTORE_BASE64`.

---

## 4. Reinstating a Removed App & Play Console Setup

> [!NOTE]
> **New Application ID (`com.himanshurawat.notesapp`)**:
> The package name has been updated to `com.himanshurawat.notesapp`. Since this is a fresh application ID:
> 1. Create a **New App** in the [Google Play Console](https://play.google.com/console) named **Notes** with default language and app details.
> 2. Ensure **Google Play App Signing** is enabled (Google manages your app signing key, and uses your keystore as the upload key).
> 3. Generate your new release keystore (see Step 3) and save its credentials securely.
> 4. Google Play Developer API requires the very first `.aab` to be uploaded **once manually** in the Google Play Console UI before API uploads can be processed.

### Step 4.1: Enable API Access in Google Play Console
1. Go to the [Google Play Console](https://play.google.com/console).
2. On the left menu, select **API access** (under *Developer account*).
3. Choose an existing Google Cloud Project or click **Create new project** and link it.

### Step 4.2: Create a Service Account in Google Cloud Console
1. In the **API access** page, scroll to **Service accounts** and click **Create new service account** (or visit [Google Cloud IAM & Admin Console](https://console.cloud.google.com/iam-admin/serviceaccounts)).
2. Click **+ Create Service Account**.
   - Name: `github-actions-play-deployer`
   - Description: `Uploads AABs to Google Play from GitHub Actions`
3. Grant the service account the role **Service Account User** (or continue without extra GCP roles, permissions are handled in Play Console).
4. Click **Done**.

### Step 4.3: Create and Download the JSON Key
1. In Google Cloud Console, click on your newly created service account.
2. Go to the **Keys** tab -> **Add Key** -> **Create new key**.
3. Choose **JSON** and click **Create**.
4. Save the downloaded `.json` file.
5. Copy the **entire contents** of this `.json` file and paste it into GitHub Secrets as `PLAY_SERVICE_ACCOUNT_JSON`.

### Step 4.4: Grant Permissions in Google Play Console
1. Return to the [Google Play Console](https://play.google.com/console) -> **API access**.
2. Locate the newly created service account under **Service accounts** and click **Grant access** (or find it in **Users and permissions**).
3. Under **App permissions**, select the **Notes** app (`com.himanshurawat.notesapp`).
4. Under **Account permissions** / **App permissions**, ensure the following are enabled:
   - **Release apps to testing tracks**
   - **Manage testing tracks and edit tester lists**
   - **Release to production, exclude devices, and use Play App Signing** (if you plan to publish to production)
5. Click **Invite user** or **Save changes**.

---

## 5. How to Release a New Version

1. Open [`app/build.gradle`](../app/build.gradle).
2. Increment `versionCode` and update `versionName`:
   ```groovy
   defaultConfig {
       ...
       versionCode 7       // Incremented from 6
       versionName "2.1"   // Updated version string
   }
   ```
3. Update release notes in [`distribution/whatsnew/whatsnew-en-US`](../distribution/whatsnew/whatsnew-en-US) if desired.
4. Commit and push to `master`:
   ```bash
   git add app/build.gradle distribution/whatsnew/whatsnew-en-US
   git commit -m "Bump version to 2.1 (code 7)"
   git push origin master
   ```
5. GitHub Actions will automatically:
   - Notice the version update.
   - Build and sign the release AAB.
   - Upload the AAB to the Google Play Store **internal** track.
   - Create a GitHub Release with the AAB attached.

---

## 6. Manual Triggering via GitHub Actions

You can also deploy manually at any time without committing:
1. Go to your repository on GitHub.
2. Click the **Actions** tab.
3. In the left sidebar, click **Deploy to Google Play**.
4. Click **Run workflow**:
   - Choose the target track (`internal`, `alpha`, `beta`, or `production`).
   - Choose release status (`completed`, `draft`, etc.).
   - Check `Force deploy` if you want to deploy without bumping `versionCode`.
5. Click **Run workflow**.
