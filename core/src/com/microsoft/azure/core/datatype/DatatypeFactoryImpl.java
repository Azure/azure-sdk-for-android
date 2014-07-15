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
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

public class DatatypeFactoryImpl extends DatatypeFactory {
    public static DatatypeFactory newInstance() throws DatatypeConfigurationException {
        return new DatatypeFactoryImpl();
    }

    @Override
    public Duration newDuration(String lexicalRepresentation) {
        return new DurationImpl(lexicalRepresentation);
    }

    @Override
    public Duration newDuration(long durationInMilliSeconds) {
        return new DurationImpl(durationInMilliSeconds);
    }

    @Override
    public Duration newDuration(boolean isPositive, BigInteger years,
            BigInteger months, BigInteger days, BigInteger hours,
            BigInteger minutes, BigDecimal seconds) {

        return new DurationImpl(isPositive, years,
            months, days, hours,
            minutes, seconds);
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar() {
        return new XMLGregorianCalendarImpl();
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar(String lexicalRepresentation) {
        return new XMLGregorianCalendarImpl(lexicalRepresentation);
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar(GregorianCalendar cal) {
        return new XMLGregorianCalendarImpl(cal);
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar(BigInteger year,
            int month, int day, int hour, int minute, int second,
            BigDecimal fractionalSecond, int timezone) {

        return new XMLGregorianCalendarImpl(year,
                month, day, hour, minute, second,
                fractionalSecond, timezone);
    }
}
