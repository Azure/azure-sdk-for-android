### Azure Core Modules:

The following diagram shows the azure core modules and their dependencies. 

The minSdkVersion of all the modules except `azure-core-http-okhttp` is 15. The `azure-core-http-okhttp` uses minSdkVersion as 21 since `okhttp` is baselined to L21.

<img width="1170" alt="Modules" src="https://user-images.githubusercontent.com/1471612/108896719-21739a80-75ca-11eb-9b9e-3e1df65a56ec.png">
 