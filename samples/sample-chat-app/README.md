## Test android app for Azure Communication Chat Service common functionality.

Setup:
1. Open the whole repository in Android Studio. 
2. Click Gradle Sync.
3. Open sample-chat-app/src/main/java/com/azure/android/communication/chat/sampleapp/MainActivity.
3. Replace the firstUserId and secondUserId with valid user identifiers.
4. Replace the userAccessToken with an access token generated for the first user.
5. Run "sample-chat-app" from the IDE by clicking the run button on the top right corner.

Functions:
* Use the button 'Start real time notifications', to start the real time notifications. (Implementation pending)
* Use the button 'Register real time notifications listener' to register a listener for 'chatMessageReceived' events. (Implementation pending)
* Use the button 'Unregister real time notifications listener' to unregister a listener for 'chatMessageReceived' events. (Implementation pending)

> After registering to the event, you need to use postman or JS client to create a thread with the same token you used in the test app, then send a message to the thread. Observe log and app UI to see if the test app receives real time notifications.

* Use the button 'Test basic operations' to run chat normal operations such as create chat thread, send chat message, etc.
