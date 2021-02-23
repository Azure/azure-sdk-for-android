
# Azure SDK for Android (vNext)

### License

Azure SDK for Android is licensed under the [MIT](https://github.com/Azure/azure-sdk-for-android/blob/master/LICENSE.txt) license.

### Design documents:

#n | Topic | Link
-- | --- | --- 
1 | HttpClient API | [reference](https://gist.github.com/anuchandy/ce2319492824d548b5ed00a0529eb4ba)
2 | HttpPipeline Chain Design | [reference](https://gist.github.com/anuchandy/f5339a661912d766214fc37570de8c7a)
3 | RestProxy API | [reference](https://gist.github.com/anuchandy/5aa3c0f3bc164cfc6137b397c0a775ea)


### Presentations:

MD file used to present the journey from v1 (master) to v2 (this branch), and design choices made.

#n | presentation | Link
-- | --- | --- 
1 | talk 1 | [reference](https://gist.github.com/anuchandy/6d960e29e66d9574e7cf5c9731037cb1)

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2FREADME.png)


### Modules (MinAPILevel 15):

The following diagram shows the core modules and their dependencies. 

The minSdkVersion of all the modules except `azure-core-http-okhttp` is 15. The `azure-core-http-okhttp` uses minSdkVersion as 21 since `okhttp` is baselined to L21.

<img width="1170" alt="Modules" src="https://user-images.githubusercontent.com/1471612/108790251-1882ba80-7531-11eb-8dbc-923ce7c66bcd.png">

> Note: We'll remove `azure-core-serde` and will rename `azure-core-serde-jackson` to `azure-core-jackson`.
 
