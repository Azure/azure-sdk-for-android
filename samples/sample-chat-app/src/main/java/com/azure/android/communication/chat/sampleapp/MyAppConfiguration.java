// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.communication.chat.sampleapp;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.azure.android.communication.chat.implementation.notifications.fcm.RegistrationRenewalWorkerFactory;
import com.azure.android.communication.common.CommunicationTokenCredential;
import com.azure.android.core.logging.ClientLogger;

import java9.util.function.Consumer;

public class MyAppConfiguration extends Application implements Configuration.Provider {
    public static String FIRST_USER_ID;

    public static String SECOND_USER_ID = "";

    public static String FIRST_USER_ACCESS_TOKEN;

    public static String ACS_ENDPOINT;

    public static CommunicationTokenCredential COMMUNICATION_TOKEN_CREDENTIAL;

    private ClientLogger logger = new ClientLogger(MyAppConfiguration.class);

    private final static String AZURE_FUNCTION_URL = "<replace_with_azure_function_endpoint>";

    Consumer<Throwable> exceptionHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) {
            logger.warning("Registration failed for push notifications!", throwable);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            //First user context
            //userTokenClient.getNewUserContext();
            ACS_ENDPOINT = "https://jimin-chat-test2.unitedstates.communication.azure.com";
            FIRST_USER_ID = "8:acs:553186a0-b99f-45b6-ae8d-8a51ba3200f2_0000001d-d7f1-04d2-85f4-343a0d00e951";
            SECOND_USER_ID = "8:acs:553186a0-b99f-45b6-ae8d-8a51ba3200f2_0000001e-4023-45a3-ec8d-084822006df6";
            FIRST_USER_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjYwNUVCMzFEMzBBMjBEQkRBNTMxODU2MkM4QTM2RDFCMzIyMkE2MTkiLCJ4NXQiOiJZRjZ6SFRDaURiMmxNWVZpeUtOdEd6SWlwaGsiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjU1MzE4NmEwLWI5OWYtNDViNi1hZThkLThhNTFiYTMyMDBmMl8wMDAwMDAxZC1kN2YxLTA0ZDItODVmNC0zNDNhMGQwMGU5NTEiLCJzY3AiOjE3OTIsImNzaSI6IjE3MDkwOTE0OTIiLCJleHAiOjE3MDkxNzc4OTIsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQiLCJyZXNvdXJjZUlkIjoiNTUzMTg2YTAtYjk5Zi00NWI2LWFlOGQtOGE1MWJhMzIwMGYyIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTcwOTA5MTQ5Mn0.Q-SQ_ncctaX-CTuhyShCieH9OEwBKv78K7dMdebFU13ORFaQG_EsYr0sJtbN8FesWebAQ5wjccXbqbk2Uj6F7Y057nQ1VlBO3wQXDGR0ymLyrIPUEsHM8PzBVToQxlvQynsz7WM2L2NZqgr8pLCI4SjNW0CgWNn2Wd9ygdr5SWSRv66DXwsQEBYsLCr2tee_FhU14srqP897-h1k7YYBbusSFFQ-uWTkcumrdMtgxFB6qvomj0SlebDxmQNu092aNJWVaLoiRYXf2YGAlSE54d4jiRqltSCtlr721gugF8dqmabWIncjrQGOiEKFeIFr3Tlo02N3Uc9ias3oddfJyA";
//          FIRST_USER_ID = "8:acs:553186a0-b99f-45b6-ae8d-8a51ba3200f2_0000001e-2b46-c7bf-28c5-593a0d0082bb";
//          FIRST_USER_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjYwNUVCMzFEMzBBMjBEQkRBNTMxODU2MkM4QTM2RDFCMzIyMkE2MTkiLCJ4NXQiOiJZRjZ6SFRDaURiMmxNWVZpeUtOdEd6SWlwaGsiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoiYWNzOjU1MzE4NmEwLWI5OWYtNDViNi1hZThkLThhNTFiYTMyMDBmMl8wMDAwMDAxZS0yYjQ2LWM3YmYtMjhjNS01OTNhMGQwMDgyYmIiLCJzY3AiOjE3OTIsImNzaSI6IjE3MDc0NDA2NzciLCJleHAiOjE3MDc1MjcwNzcsInJnbiI6ImFtZXIiLCJhY3NTY29wZSI6ImNoYXQiLCJyZXNvdXJjZUlkIjoiNTUzMTg2YTAtYjk5Zi00NWI2LWFlOGQtOGE1MWJhMzIwMGYyIiwicmVzb3VyY2VMb2NhdGlvbiI6InVuaXRlZHN0YXRlcyIsImlhdCI6MTcwNzQ0MDY3N30.kA-iEMQdqp3dMShyE1iaWwTooixnodrtsAh8n0-B8P3OadvYNaLHNsBnh_Xls3zI2S0aZUiX1XJE3X9QhE3lp9upwYapIqSF13dDFrv2Tpte6j6ruw-3Uvt4RQcKyatZdl5gwQVRqoMaS6m3rkJn9_QJVlvAOdMDnAqevPsr8gfLCoCeE5gAVlRr7azeTj7KBX3_iWAQynkh42kEL9ixu6boHqHa1iqwVu4w3f8uckq3gxNhZa2ChB6WELIhNBKw7YbeFyINI_5W4EuQMNL35bMsxzqwmkhnqHWTPnww7ZHjFClXdX2CcjhimhufZtbfio812NRwpoAwDN7G-VRMFA";
            COMMUNICATION_TOKEN_CREDENTIAL = new CommunicationTokenCredential(FIRST_USER_ACCESS_TOKEN);
            //Second user context
            //userTokenClient.getNewUserContext();
            //SECOND_USER_ID = userTokenClient.getUserId();
        } catch (Throwable throwable) {
            //Your handling code
            logger.logThrowableAsError(throwable);
        }
        WorkManager.initialize(getApplicationContext(), getWorkManagerConfiguration());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().
            setWorkerFactory(new RegistrationRenewalWorkerFactory(COMMUNICATION_TOKEN_CREDENTIAL, exceptionHandler)).build();
    }
}
