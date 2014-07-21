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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class XMLGregorianCalendarImpl extends XMLGregorianCalendar {
    private int origYear = DatatypeConstants.FIELD_UNDEFINED;
    private int origMonth = DatatypeConstants.FIELD_UNDEFINED;
    private int origDay = DatatypeConstants.FIELD_UNDEFINED;
    private int origHour = DatatypeConstants.FIELD_UNDEFINED;
    private int origMinute = DatatypeConstants.FIELD_UNDEFINED;
    private int origSecond = DatatypeConstants.FIELD_UNDEFINED;
    private BigDecimal origFracSeconds;
    private BigInteger origEon;
    private int origTimezone = DatatypeConstants.FIELD_UNDEFINED;
    
    /**
     * <p>The Year.</p>
     */
    private int year = DatatypeConstants.FIELD_UNDEFINED;

    /**
     * <p>The Month.</p>
     */
    private int month = DatatypeConstants.FIELD_UNDEFINED;

    /**
     * <p>The Day.</p>
     */
    private int day = DatatypeConstants.FIELD_UNDEFINED;     
    /**
     * <p>The Timezone.</p>
     */
    private int timezone = DatatypeConstants.FIELD_UNDEFINED;

    /**
     * <p>The Hour.</p>
     */
    private int hour = DatatypeConstants.FIELD_UNDEFINED;

    /**
     * <p>The Minute.</p>
     */
    private int minute = DatatypeConstants.FIELD_UNDEFINED;

    /**
     * <p>The second.</p>
     */
    private int second = DatatypeConstants.FIELD_UNDEFINED;
    
    /**
     * <p>The fractional second.</p>
     */
    private BigDecimal fractionalSecond = null;
    
    private BigInteger eon = null;
    
    public XMLGregorianCalendarImpl() {
        // Intentionally empty
    }
    
    public XMLGregorianCalendarImpl(String lexicalRepresentation)
        throws IllegalArgumentException {

        SimpleDateFormat format;
        
        String lexRep = lexicalRepresentation;
        int lexRepLength = lexRep.length();

        if (lexRep.indexOf('T') != -1) {
            format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        } else if (lexRepLength >= 3 && lexRep.charAt(2) == ':') {
            format = new SimpleDateFormat("HH:mm:ss.SSSZ");
        } else {
            throw new IllegalArgumentException("\"" + lexicalRepresentation + "\" is not a valid or supported representation of an XML Gregorian Calendar value.");
        }

        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(format.parse(lexicalRepresentation));
            initFromCalendar(cal);
        } catch (ParseException e) {
            // Do nothing
        }
    }
    
    public XMLGregorianCalendarImpl(GregorianCalendar cal) {
        initFromCalendar(cal);
    }
    
    private void initFromCalendar(Calendar cal) {
        int year = cal.get(Calendar.YEAR);
        if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
            year = -year;
        }

        this.setYear(year);
        this.setMonth(cal.get(Calendar.MONTH) + 1);
        this.setDay(cal.get(Calendar.DAY_OF_MONTH));
        this.setTime(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND),
            cal.get(Calendar.MILLISECOND));

        int offsetInMinutes = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
        this.setTimezone(offsetInMinutes);
    }
    
    public XMLGregorianCalendarImpl(BigInteger year,
            int month, int day, int hour, int minute, int second,
            BigDecimal fractionalSecond, int timezone) {
        setYear(year);
        setMonth(month);
        setDay(day);
        setTime(hour, minute, second, fractionalSecond);
        setTimezone(timezone);
    }

    @Override
    public void add(Duration arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        year = DatatypeConstants.FIELD_UNDEFINED;
        month = DatatypeConstants.FIELD_UNDEFINED;
        day = DatatypeConstants.FIELD_UNDEFINED;
        timezone = DatatypeConstants.FIELD_UNDEFINED;
        hour = DatatypeConstants.FIELD_UNDEFINED;
        minute = DatatypeConstants.FIELD_UNDEFINED;
        second = DatatypeConstants.FIELD_UNDEFINED;
        fractionalSecond = null;
        eon = null;
    }

    @Override
    public Object clone() {
        return new XMLGregorianCalendarImpl(
            getEonAndYear(),
            this.month,
            this.day, 
            this.hour,
            this.minute,
            this.second,
            this.fractionalSecond,
            this.timezone);
    }

    @Override
    public int compare(XMLGregorianCalendar xmlGregorianCalendar) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDay() {
        return day;
    }

    @Override
    public BigInteger getEon() {
        return eon;
    }

    @Override
    public BigInteger getEonAndYear() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigDecimal getFractionalSecond() {
        return fractionalSecond;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public int getMonth() {
        return month;
    }

    @Override
    public int getSecond() {
        return second;
    }

    @Override
    public TimeZone getTimeZone(int defaultZoneoffset) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTimezone() {
        return timezone;
    }

    @Override
    public QName getXMLSchemaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public XMLGregorianCalendar normalize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reset() {
        year = origYear;
        month = origMonth;
        day = origDay;
        hour = origHour;
        minute = origMinute;
        second = origSecond;
        fractionalSecond = origFracSeconds;
        eon = origEon;
        timezone = origTimezone;
    }

    @Override
    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public void setFractionalSecond(BigDecimal fractional) {
        this.fractionalSecond = fractional;
    }

    @Override
    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    public void setMillisecond(int millisecond) {
        if (millisecond == DatatypeConstants.FIELD_UNDEFINED) {
            fractionalSecond = null;
        }
        else {
            fractionalSecond = new BigDecimal((long) millisecond).movePointLeft(3);
        }
    }

    @Override
    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public void setMonth(int month) {
        this.month = month;
    }

    @Override
    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    public void setTimezone(int offset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setYear(BigInteger year) {
        this.year = year.intValue();
    }

    @Override
    public void setYear(int year) {
       this.year = year;
    }

    @Override
    public GregorianCalendar toGregorianCalendar() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GregorianCalendar toGregorianCalendar(TimeZone timezone,
            Locale aLocale, XMLGregorianCalendar defaults) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toXMLFormat() {
        // TODO Auto-generated method stub
        return null;
    }
}
