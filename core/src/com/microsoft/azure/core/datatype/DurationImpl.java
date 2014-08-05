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
package com.microsoft.azure.core.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

import javax.xml.datatype.DatatypeConstants.Field;
import javax.xml.datatype.Duration;

public class DurationImpl extends Duration {
    /**
     * <p>Indicates if the duration is negative,
     * or positive.</p>
     */
    private int sign;
    
    /**
     * <p>Years of this <code>Duration</code>.</p>
     */
    private BigInteger years;

    /**
     * <p>Months of this <code>Duration</code>.</p>
     */
    private BigInteger months;

    /**
     * <p>Days of this <code>Duration</code>.</p>
     */
    private BigInteger days;

    /**
     * <p>Hours of this <code>Duration</code>.</p>
     */
    private BigInteger hours;

    /**
     * <p>Minutes of this <code>Duration</code>.</p>
     */
    private BigInteger minutes;

    /**
     * <p>Seconds of this <code>Duration</code>.</p>
     */
    private BigDecimal seconds;

    public DurationImpl(boolean isPositive, BigInteger years,
            BigInteger months, BigInteger days, BigInteger hours,
            BigInteger minutes, BigDecimal seconds) {
        if (isPositive) {
            this.sign = 1;
        } else {
            this.sign = -1;
        }
        
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }
    
    /**
     * <p>Constructs a new Duration object by specifying the duration
     * in milliseconds.</p>
     * 
     * @param durationInMilliSeconds
     *      The length of the duration in milliseconds.
     */
    protected DurationImpl(final long durationInMilliSeconds) {
        boolean isOverflow = false;
        long duration = durationInMilliSeconds;
        
        if (duration < 0) {
            this.sign = -1;
            if (duration == 0x8000000000000000L) {
                duration++;
                isOverflow = true;
            }
            duration *= -1;
        } else if (duration > 0) {
            this.sign = 1;
        } else {
            this.sign = 0;
        }

        this.years = null;
        this.months = null;

        this.seconds = BigDecimal.valueOf((duration % 60000L)
            + (isOverflow ? 1 : 0), 3);

        duration /= 60000L;
        this.minutes = (duration == 0) ? null : BigInteger.valueOf(duration % 60L);

        duration /= 60L;
        this.hours = (duration == 0) ? null : BigInteger.valueOf(duration % 24L);

        duration /= 24L;
        this.days = (duration == 0) ? null : BigInteger.valueOf(duration);
    }
    
    public DurationImpl(final String lexicalRepresentation) {
        int len = lexicalRepresentation.length();
        int index = 0;
        char c;
 
        if (len < 1) {
            return;
        }
 
        c = lexicalRepresentation.charAt(0);
        if (c == '-') {
            sign = -1;
            index++;
        }
        else if (c == '+') {
            index++;
            sign = 1;
        }
 
        if (len < index) {
            return;
        }
 
        c = lexicalRepresentation.charAt(index);
        if (c != 'P') {
            throw new IllegalArgumentException("Expected character 'P' at index " + index);
        }

        index++;
 
        days = BigInteger.valueOf(0);
        int n = 0;
        for (; index < len; index++) {
            c = lexicalRepresentation.charAt(index);
            if (c >= '0' && c <= '9') {
                n *= 10;
                n += ((int) (c - '0'));
            }
            else if (c == 'W') {
                days = BigInteger.valueOf(n * 7 + days.intValue());
                n = 0;
            }
            else if (c == 'D') {
                days = BigInteger.valueOf(n + days.intValue());
                n = 0;
            }
            else if (c == 'H') {
                hours = BigInteger.valueOf(n);
                n = 0;
            }
            else if (c == 'M') {
                minutes = BigInteger.valueOf(n);
                n = 0;
            }
            else if (c == 'S') {
                seconds = BigDecimal.valueOf(n);
                n = 0;
            }
            else if (c != 'T') {
                throw new IllegalArgumentException("Unexpected character '" + c + "' at index " + index);
            }
        }
    }

    @Override
    public Duration add(Duration duration) {
        // TODO: implement
        return null;
    }
    
    @Override
    public void addTo(Calendar calendar) {
        calendar.add(Calendar.YEAR, sign * years.intValue());
        calendar.add(Calendar.MONTH, sign * months.intValue());
        calendar.add(Calendar.DAY_OF_MONTH, sign * days.intValue());
        calendar.add(Calendar.HOUR, sign * hours.intValue());
        calendar.add(Calendar.MINUTE, sign * minutes.intValue());
        calendar.add(Calendar.SECOND, sign * seconds.intValue());
    }

    @Override
    public int compare(Duration duration) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Number getField(Field field) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getSign() {
        return sign;
    }

    @Override
    public int getYears() {
        return years.intValue();
    }
    
    @Override
    public int getMonths() {
        return months.intValue();
    }
    
    @Override
    public int getDays() {
        return days.intValue();
    }
    
    @Override
    public int getHours() {
        return hours.intValue();
    }
    
    @Override
    public int getMinutes() {
        return minutes.intValue();
    }
    
    @Override
    public int getSeconds() {
        return seconds.intValue();
    }
    
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isSet(Field field) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Duration multiply(BigDecimal factor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration negate() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Duration normalizeWith(Calendar startTimeInstant) {
        // TODO Auto-generated method stub
        return null;
    }
}
