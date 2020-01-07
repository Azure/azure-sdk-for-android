

### Introduction

This is a sample application demo interactive login in Android, where one can login using a email and access the Azure storage.

Note: This app requires some configuration, following sections walk you through how to set it up.

### Azure Subscription and Azure Active Directory:

An Azure Subscription is associated with an Azure AD Directory. Each such Directory has a Name and a Domain. In azure portal, the Azure Subscription details page 
(Home -> Subscriptions -> click on `<Subscription-Name>`) display the associated AD Directory, it can look like "Default Directory (anuchandyhotmail.onmicrosoft.com)" where Directory name is "Default Directory" and Domain is "anuchandyhotmail.onmicrosoft.com". The default Domain name is often derived from the email associated with the subscription, e.g. anuchandy@hotmail.com
result in "anuchandyhotmail.onmicrosoft.com" AD Domain name.

A Microsoft FTE using Internal Microsoft Azure Subscriptions usually see AD Directory associated to such subscriptions as "Microsoft (microsoft.onmicrosoft.com)" where Directory name 
is "Microsoft" and Domain is "microsoft.onmicrosoft.com". Multiple Subscriptions can share the same AD Directory. If you have access to multiple Internal Azure Subscriptions
then you will see many of them associated with "Microsoft (microsoft.onmicrosoft.com)" AD Directory.

When you get an Azure Subscription for personnal use, to have the full contol on it make sure it does not get associated with Org/Comany AD Directory, such association can happen if you use
company provided email when signing up for Subscription, this can result in issues like loosing access when you leave the company, even when you belongs to the Org this limits you from doing some admin operations on your personnal subscription. As an example: An FTE at Microsoft can claim an Azure Subscription with some free credit for personnel use. (from https://my.visualstudio.com/Benefits). When claiming/activating this Azure subscription, it's recommended to not to associate it with "Microsoft (microsoft.onmicrosoft.com)" AD Directory, this can happen if employee used 
@microsoft.com email when the activation process ask for login. The recommendation is to - Add an alternate MSA account (live.com, hotmail.com) for Azure and use that email while activating. In case you already associated it with "Microsoft (microsoft.onmicrosoft.com)" then you can cancel the subscription (from Azure portal), once cancellation is done, it can be claimed/activated again but under alternate MSA account (from https://my.visualstudio.com/Benefits as described above).


The sample application demo interactive login, where you can login using a user email and access the Azure storage. 

Let's create a user.

### Creating User in AD Directory: 

1. In the main top search bar, search and select "Users". This will bring up "Users" page.
2. Choose "+ New User". From "Create User" - "Invite User" options choose "Create User".
3. provide a user-name to create a new user in the AD Directory. Let say AD Domain associated with the subscription is "anuchandyhotmail.onmicrosoft.com" then for a user-name "chris" this step creates
   a user that is identified as "chris@anuchandyhotmail.onmicrosoft.com" in the AD Domain.
4. We will use this user email to login to android application.

Note: If the subscription is associated with the Org/Company AD Directory, then you may not be able to create a user like above unless you're admin of the Org, this option is mostly disabled in such AD Directories. This is one of
the restriction when we talked about Azure Subscription and AD Directory previously.

### Enabling User to access Storage:

This user need necessary permission enabled in the storage that he want to access after login to Android application. Let's enable access:

1. Go to the storage account that needs to be accessed from Android app
2. Select "Access Control (IAM)" from the left pane
3. Select "Add role assignment" and create role assigment with following settings:
    A. "Role" as "Storage Blob Data Contributor"
    B. "Assign Access To" as "Azure AD user, group or Service Principal"
    C. "Select" as the user previously created e.g. "chris@anuchandyhotmail.onmicrosoft.com"
 and Save this Role

The selected role will enable the user to perform "Read, Write and Delete operations" on Blobs and Containers in this storage account.

Now we created user in AD Domain and enabled access.

### Android Application AD User and AD Application: 

When user sign-in to the Android application using this email, the application needs to contact the Azure AD on user's behalf and retrieve the Bearer Token to call Storage API.
This requires an "Azure AD Application" to be registered in the same Azure AD (where we created the user) for the Android app. We will use the identity associated this 
"Azure AD Application" to configure the Android app.

Let's create an "AD Application" in the same AD Domain.
 
### Creating AD Application: 

1. In the main top search bar, search and select "App registrations". This will bring up "App registrations" page
2. Choose "+ New registration", this will take you to "Register an application" page, create one with following settings:
     1. Provide a name in "Name" field e.g. android-storage-app-1
     2. Choose "Supported Account Types" as "Accounts in this organizational directory only (Default Directory only - Single tenant)"
     3. Click "Register"

This will register the app and take you to app (e.g. android-storage-app-1) details page.

3. In the left pane, under "Manage", select "Authentication" to get "Authentication configuration" page. Now we can create authentication configuration for 
    Android platform. If you don't see "Platform Configuration" section in this page then click on the top "Try out new experience" tab.
    1. Click on "Add Platform" and choose Android:
        
            A. Provide Package Name as "com.anuchandy.learn.msal"
            B. For "Signature Hash", generate a Signature Hash using your computer and set it here. [Portal shows instructions to generate "Developement Signature Hash"].
            C. Select "Configure", this will display the "Android Configuration" page, from this page grab following configurations:
                    a. Package name     (same as the value provided in A)
                    b. Signature Hash   (same as the value provided in B)
                    c. Redirect URI
                    d. MSAL Configuration
            Click "Done"
    2. In "Authentication configuration" page under "Supported Account Types" choose "Accounts in this organizational directory only (Default Directory only - Single tenant)"
    3. In "Authentication configuration" page under "Advanced settings", select "Yes" for "Treat application as public client"
    4. "Save" the configuration by choosing Save from top tab

4. In the left pane, under "Manage", select "API Permissions" to get "API permission" page, that will list "Configured permissions".
    1. Choose "+ Add permission" and select "Azure Stroage"
    2. Check "user_impersonation Access Azure Storage" and choose "Add permission"
    3. Now you're back to "API permission" page and should see an entry for "Azure Storage" under "Configured permissions"
    4. Click on "Grant admin consent for <AD Directory Name>" (e.g. for me it was 'Grant admin consent for Default Directory' where 'Default Directory' is name of my AD Directory)
    5. It takes some time to complete the consent process, once done you should see "Granted For <AD Directory Name>" next to "Configured permissions" entry for storage

Note: If the subscription is associated with the Org/Company AD Directory, then you may not be able to grant access like above unless you're admin of the Org, this option is mostly disabled in such AD Directories. This is another restriction when we talked about Azure Subscription and AD Directory previously.

### Configure the Android Application with AD Application

1. Open app/res/raw/auth_config.json and replace it's content with "MSAL Configuration" from 3.1.C.d
2. Open AndroidManifest.xml, set value of manifest::package attribute as "Package Name" from 3.1.C.a

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.anuchandy.learn.msal">
```

Update host path attribute of activity::intent-filter::data node with Package Name (from 3.1.C.a) and Signature Hash (from from 3.1.C.b)

```xml
<data android:scheme="msauth"
    android:host="com.anuchandy.learn.msal"
    android:path="/<Singature-Hash>" />
```

3. Open MainActivity.java, replace `<storage-account>` with name of your storage account.
   This application try to list content of a container with name `firstcontainer` in this storage account, so create such a container in your account. 





