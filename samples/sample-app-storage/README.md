# Android Sample app for Azure Blob Storage Alpha
## Prerequisites
- [Azure Subscription](https://azure.microsoft.com/free/)
- [Create a Storage Account](https://docs.microsoft.com/azure/storage/common/storage-account-create?tabs=azure-portal)

## Setup
### Create a User in your Azure Active Directory
1. In the main top search bar, search and select "Users".
2. Click on "+ New User". Once there, select "Create User".
3. Provide a "User name" to create a new user in the Active Directory. Let say the AD Domain associated with the subscription is "testemailhotmail.onmicrosoft.com" then for a user with name "John Doe" this step creates a user that is identified as "JohnDoe@testemailhotmail.onmicrosoft.com" in the AD Domain.
4. You can either set a password or let Azure auto-generate one for you. If you choose the latter, you can see it by checking the "Show Password" box. 
5. Click on "Create".
> If the subscription is associated with your Org/Company Azure Active Directory, then you may not be able to create a user like above unless you're the admin of the Org, this option is mostly disabled in such Active Directories.
 
## Give the user access to a Storage Account
The user now needs the necessary permissions enabled in the Storage Acount so that they can access it after logging in to the Android application:
 
1. Go to the Storage Account that needs to be accessed from the Android app.
2. Select "Access Control (IAM)" from the left pane.
3. Select "Add role assignment" and create a role assignment with following settings:
    - "Role" as "Storage Blob Data Contributor".
    - "Assign Access To" as "Azure AD user, group or Service Principal".
    - "Select" as the user previously created e.g. "JohnDoe@testemailhotmail.onmicrosoft.com".
4. Save this Role Assignment.

The selected role will enable the user to perform "Read, Write and Delete operations" on Blobs and Containers in this Storage Account.

### Create an App registration and give it access to a Storage Account
1. In the main top search bar in the Azure Portal, search and select "App registrations".
2. Choose "+ New registration" and create one with following settings:
    - Provide a name in "Name" field e.g. android-storage-app-1.
    - Set "Supported Account Types" to "Accounts in this organizational directory only (Default Directory only - Single tenant)".
    This will register the app and take you to the app (e.g. android-storage-app-1) details page.
3. In the left pane, under "Manage", select "Authentication". We will now can create an authentication configuration for the Android platform. If you don't see a "Platform Configuration" section in this page then click the "Try out new experience" tab on the top.
    1. Click on "Add Platform" and choose Android:
        1. Provide your Android app's Package Name (e.g. "com.example.storage.msal").
        2. For "Signature Hash", generate a Signature Hash using your computer and set it here. The Portal shows instructions to generate a "Development Signature Hash".
        3. Select "Configure" and set following values:
            - Package name (same as the value above)
            - Signature Hash (same as the value provided above)
            - A redirect URI
            - MSAL Configuration
            Click "Done"
    2. In the "Authentication configuration" set the following:
        - "Supported Account Types" to "Accounts in this organizational directory only (Default Directory only - Single tenant)".
        - Under "Advanced settings", select "Yes" for "Treat application as public client".
    4. Click "Save" on the top tab.
4. In the left pane, under "Manage", select "API Permissions". Under the list of "Configured permissions":
    1. Choose "+ Add permission" and select "Azure Storage".
    2. Check "user_impersonation Access Azure Storage" and choose "Add permission".
5. One back on the "API permission" page, you should see an entry for "Azure Storage" under "Configured permissions".
6. Click on "Grant admin consent for <Active Directory Name>" (e.g. "Grant admin consent for Default Directory").
> It takes some time to complete the consent process, once done you should see "Granted For <Aactive Directory Name>" next to the "Configured permissions" entry for Azure Storage.

### Set the following values in your app's configuration files
1. Replace the following values in `src/main/res/raw/authorization_configuration.json` with your App Registration details:
    - `client_id`
    - `hash`
    - `tenant_id`
    
 > Keep in mind that the hash contents should be URL-safe, so a hash like `mysamplehash1234/morechars5=` would need to look like this: `mysamplehash1234%2Fmorechars5%3D`.

2. Replace the following values in `src/main/res/raw/storage_configuration.json` with your Storage Account details:
    - `storage_account_name`
    - `container_name`
    
3. Replace the `hash` in the `<>` section of your `src/main/AndroidManifest.xml`.
> The hash contents *DO NOT NEED TO BE* URL-safe, so a hash like `mysamplehash1234/morechars5=` would not need to be changed.

### Run the application and login
Run the application using Android Studio. The following functionality is currently available:

#### Listing blobs
Use the *List blobs* button to list all the blobs in the specified container. You will be taken to a login page where you will need to enter the details from the User created above. After logging in, you will be able to see all the existing blobs in the container.

##### Download a blob
Tap a blob to download and open it (if the format is supported by an app installed on the device).

#### Upload a blob
Use the *Upload blob* to upload a file in the system to the specified container. You will be taken to a valid content provider to select a file with. Once selected, if you have not logged in, you will be taken to a login page where you will need to enter the details from the User created above. After logging in, the blob upload will begin.
