// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package azandroid.storage.applicationcontext;

public class Settings {
    private String connectionString;

    public void setConnectionString(String connectionString){
        this.connectionString  = connectionString;
    }

    public String getConnectionString() {
        return this.connectionString;
    }
}
