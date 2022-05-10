/*
 * Created 21.10.2009
 *
 * Copyright (c) 2009-2012 SLF4J.ORG
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Portions Copyright (c) Microsoft Corporation.
 * Licensed under the MIT License.
 */

package com.azure.android.core.logging.implementation;

import android.os.Build;

import java.util.StringTokenizer;

public final class LogUtils {
    private static final char CR = '\r';
    private static final char LF = '\n';

    private LogUtils() {
    }

    private static final int TAG_MAX_LENGTH = 23;

    /**
     * Returns the short logger tag (up to {@value #TAG_MAX_LENGTH} characters) for the given logger name if the
     * devices API level is <= 25, otherwise, the tag is unchanged. Traditionally loggers are named by fully-qualified
     * Java classes; this method attempts to return a concise identifying part of such names.
     */
    public static String ensureValidLoggerName(String name) {
        if (name == null) {
            return null;
        } else {
            name = name.trim();
        }

        if (name.length() > TAG_MAX_LENGTH && Build.VERSION.SDK_INT <= 25) {
            final StringTokenizer st = new StringTokenizer(name, ".");

            if (st.hasMoreTokens()) { // Note that empty tokens are skipped, i.e., "aa..bb" has tokens "aa", "bb".
                final StringBuilder sb = new StringBuilder();
                String token;

                do {
                    token = st.nextToken();
                    if (token.length() == 1) { // Token of one character appended as is.
                        sb.append(token);
                        sb.append('.');
                    } else if (st.hasMoreTokens()) { // Truncate all but the last token.
                        sb.append(token.charAt(0));
                        sb.append("*.");
                    } else { // Last token (usually class name) appended as is.
                        sb.append(token);
                    }
                } while (st.hasMoreTokens());

                name = sb.toString();
            }

            // Either we had no useful dot location at all or name still too long.
            // Take leading part and append '*' to indicate that it was truncated.
            if (name.length() > TAG_MAX_LENGTH) {
                name = name.substring(0, TAG_MAX_LENGTH - 1) + '*';
            }
        }

        return name;
    }

    /**
     * Removes CR, LF or CRLF pattern in the {@code logMessage}.
     * <p>
     * This is more performant than using {@code Pattern.compile("[\r\n]")}.
     *
     * @param logMessage The log message to sanitize.
     * @return The updated logMessage.
     */
    public static String removeNewLinesFromLogMessage(String logMessage) {
        if (logMessage == null || logMessage.isEmpty()) {
            return logMessage;
        }

        StringBuilder sb = null;
        int prevStart = 0;

        for (int i = 0; i < logMessage.length(); i++) {
            if (logMessage.charAt(i) == CR || logMessage.charAt(i) == LF) {
                if (sb == null) {
                    sb = new StringBuilder(logMessage.length());
                }

                if (prevStart != i) {
                    sb.append(logMessage, prevStart, i);
                }
                prevStart = i + 1;
            }
        }

        if (sb == null) {
            return logMessage;
        }
        sb.append(logMessage, prevStart, logMessage.length());
        return sb.toString();
    }
}
