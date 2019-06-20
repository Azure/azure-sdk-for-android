package com.azure.mobile.azuredataandroidexample_java.model;

import com.azure.data.model.Document;
import com.azure.data.model.User;
import java.util.Date;

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */
public class MyDocument extends Document {

    String testString = "My Custom String";
    Integer testNumber = 0;
    Date testDate = new Date();
    Boolean testBool = false;
    int[] testArray = {1, 2, 3};
    User testObject = null;
}