/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.credentials;

import com.microsoft.azure.core.utils.KeyStoreCredential;
import com.microsoft.azure.core.utils.SSLContextFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * The Class CertificateCloudCredentials.
 */
public class CertificateCloudCredentials extends SubscriptionCloudCredentials {
    
    /** The subscription id. */
    private String subscriptionId;
    
    /** The key store credential. */
    private KeyStoreCredential keyStoreCredential;
    
    /** The uri. */
    private URI uri; 

    /**
     * Instantiates a new certificate cloud credentials.
     */
    public CertificateCloudCredentials() {
    }

    /**
     * Instantiates a new certificate cloud credentials.
     *
     * @param uri the uri
     * @param subscriptionId the subscription id
     * @param keyStoreCredential the key store credential
     */
    public CertificateCloudCredentials(URI uri, String subscriptionId,
            KeyStoreCredential keyStoreCredential) {
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.keyStoreCredential = keyStoreCredential;
    }

    /* (non-Javadoc)
     * @see com.microsoft.azure.credentials.SubscriptionCloudCredentials#getSubscriptionId()
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionId the new subscription id
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * Gets the key store credential.
     *
     * @return the key store credential
     */
    public KeyStoreCredential getKeyStoreCredential() {
        return keyStoreCredential;
    }

    /**
     * Sets the key store credential.
     *
     * @param keyStoreCredential the new key store credential
     */
    public void setKeyStoreCredential(KeyStoreCredential keyStoreCredential) {
        this.keyStoreCredential = keyStoreCredential;
    }
    
    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the new URI
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public void processRequest(HttpURLConnection connection) {
        SSLContext sslContext;
        try {
            sslContext = SSLContextFactory.create(this.getKeyStoreCredential());
            if (connection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection)
                     .setSSLSocketFactory(sslContext.getSocketFactory());
            }
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(CertificateCloudCredentials.class.getName()).log(
                    Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CertificateCloudCredentials.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }
}
