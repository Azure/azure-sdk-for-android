## Test android app for Azure Communication Chat Service common functionality.

Setup:
1. Open the whole repository in Android Studio. 
2. Click Gradle Sync.
3. Open `sample-chat-app/src/main/java/com/azure/android/communication/chat/sampleapp/MainActivity`.
4. Replace the `firstUserId` and `secondUserId` with valid user identifiers.
5. Replace the `firstUserAccessToken` with an access token generated for the first user.
6. If you want to enable the FCM push notification feature, use the following instructions:
    1. Follow [this link](https://firebase.google.com/docs/cloud-messaging/android/client#create_a_firebase_project) to create your own Firebase project, register this test app with Firebase, then download the Firebase config file _"google-services.json"_ to replace the existing empty file with the same name under the _"sample-chat-app"_ folder.
    2. Follow [this link](https://docs.microsoft.com/azure/notification-hubs/notification-hubs-android-push-notification-google-fcm-get-started#configure-a-hub) to create a Notification Hub under the same Azure Subscription as your ACS resource and link your Firebase project to this Notification Hub. 
    3. Follow [this link](https://docs.microsoft.com/azure/communication-services/concepts/notifications#notification-hub-provisioning) to link your ACS resource with your ACS resource.
7. Run the _"sample-chat-app"_ from the IDE by clicking the 'Run' button in the top right corner.

Functions:
* Use the button 'Start Realtime Notifications', to start the realtime notifications.
* Use the button 'Register Realtime Notifications listener' to register listeners for all events.
* Use the button 'Unregister Realtime Notifications listener' to unregister listeners for all events.

* Use the button 'Start Push Notifications', to start FCM push notifications.
* Use the button 'Register Push Notifications listener' to register listeners for all FCM push notifications events.
* Use the button 'Stop Push Notifications' to stop FCM push notifications.


> After registering a listener for a specific type of event, you'll need to use Postman or a JS client to create a thread with the same token you used in the test app and then send a message to the thread. Observe logs and your app's UI to see if the test app receives realtime/push notifications.
