## Test android app for Azure Communication Chat Service real time notifications

Setup:
1. Open the whole repo in Android Studio. 
2. Click gradle sync.
3. Replace the token in MainActivity with your user token.
4. You should be able to run sample-chat-app from IDE by clicking run start button on top right corner.

Functions:
* Button 'Start real time notification', to start the real time notification
* Button 'Register real time notification' to register a listener to 'chatMessageReceived' event
* Button 'Unregister real time notification' to unregister a listener to 'chatMessageReceived' event

After registering to the event, you need to use postman or JS client to create a thread with the same token you used in the test app, then send a message
to the thread. Observe log and app UI to see if the test app receives real time notifications.

