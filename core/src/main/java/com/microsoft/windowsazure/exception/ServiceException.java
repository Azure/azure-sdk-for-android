/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.exception;

import com.microsoft.windowsazure.core.utils.BOMInputStream;
import com.microsoft.windowsazure.core.utils.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The Service Exception indicates an error while executing a service operation.
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = -4942076377009150131L;

    private int httpStatusCode;
    private String httpReasonPhrase;
    private String serviceName;

    private String errorCode;
    private String errorMessage;
    private Map<String, String> errorValues;
    private String rawResponseBody;

    public ServiceException() {
        super();

        init();
    }

    public ServiceException(final String message) {
        super(message);
        init();
    }

    public ServiceException(final String message, final Throwable cause) {
        super(message, cause);
        init();
    }

    public ServiceException(final Throwable cause) {
        super(cause);
        init();
    }

    private void init() {
        errorValues = new HashMap<String, String>();
    }

    @Override
    public String getMessage() {
        final StringBuffer buffer = new StringBuffer(50);
        buffer.append(super.getMessage());

        if (this.rawResponseBody != null) {
            buffer.append("\nResponse Body: ");
            buffer.append(this.rawResponseBody);
        }

        return buffer.toString();
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(final int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpReasonPhrase() {
        return httpReasonPhrase;
    }

    public void setHttpReasonPhrase(final String httpReasonPhrase) {
        this.httpReasonPhrase = httpReasonPhrase;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getErrorValues() {
        return errorValues;
    }

    public void setErrorValues(final Map<String, String> errorValues) {
        this.errorValues = errorValues;
    }

    public String getErrorValue(final String name) {
        return errorValues.get(name);
    }

    public void setErrorValue(final String name, final String value) {
        this.errorValues.put(name, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRawResponseBody(final String body) {
        this.rawResponseBody = body;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }

    public static ServiceException create(
            final String requestContent,
            final int responseStatusCode,
            final String responseMessage,
            final String responseContentType,
            final InputStream responseStream,
            final String defaultTo) {
        
        ServiceException serviceException;

        if (responseContentType.equals("application/json")
                || responseContentType.equals("text/json")) {
            throw new UnsupportedOperationException();
        } else if (responseContentType.equals("application/xml")
                || responseContentType.equals("text/xml")) {
            serviceException = createFromXml(requestContent,
                    responseMessage, responseStatusCode, responseContentType, responseStream);
        } else if ("Json".equals(defaultTo)) {
            throw new UnsupportedOperationException();
        } else {
            serviceException = createFromXml(requestContent,
                    responseMessage, responseStatusCode, responseContentType, responseStream);
        }

        return serviceException;
    }

    public static ServiceException createFromXml(
            final String requestContent,
            final String responseMessage,
            final int responseStatusCode,
            final String responseContentType,
            final InputStream responseStream) {
        
        String content;
        try {
            content = StreamUtils.toString(responseStream);
        } catch (IOException e) {
            return new ServiceException(e);
        }

        ServiceException serviceException;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory
                    .newDocumentBuilder();
            Document responseDoc = documentBuilder.parse(new BOMInputStream(new ByteArrayInputStream(content.getBytes())));
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            String code = xpath.compile("/Error/Code/text()").evaluate(
                    responseDoc);
            String message = xpath.compile("/Error/Message/text()").evaluate(
                    responseDoc);

            serviceException = new ServiceException(buildExceptionMessage(code,
                    message));

            serviceException.setErrorCode(code);
            serviceException.setErrorMessage(message);
        } catch (XPathExpressionException e) {
            return new ServiceException(content);
        } catch (ParserConfigurationException e) {
            return new ServiceException(content);
        } catch (SAXException e) {
            return new ServiceException(content);
        } catch (IOException e) {
            return new ServiceException(content);
        }

        serviceException.setHttpStatusCode(responseStatusCode);
        serviceException.setHttpReasonPhrase(responseMessage);

        return serviceException;
    }

    private static String buildExceptionMessage(
            final String code,
            final String message) {
        return (code != null && message != null) ? code + ": " + message
                : (message != null) ? message
                    : (code != null) ? code
                        : "Invalid operation";
    }
}
