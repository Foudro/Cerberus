/**
 * Cerberus  Copyright (C) 2013 - 2016  vertigo17
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.crud.entity;

import java.util.List;

/**
 * @author bcivel
 */
public class TestCaseCountryProperties {

    private String test;
    private String testCase;
    private String country;
    private String property;
    private String type;
    private String database;
    private String value1;
    private String value2;
    private int length;
    private int rowLimit;
    private String nature;
    private MessageEvent result;
    private TestCaseCountry testCaseCountry;
    private List<TestCaseCountry> tccList;

    public List<TestCaseCountry> getTccList() {
        return tccList;
    }

    public void setTccList(List<TestCaseCountry> tccList) {
        this.tccList = tccList;
    }
    
    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public String getCountry() {
        return country;
    }

    public TestCaseCountry getTestCaseCountry() {
        return testCaseCountry;
    }

    public void setTestCaseCountry(TestCaseCountry testCaseCountry) {
        this.testCaseCountry = testCaseCountry;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int getRowLimit() {
        return rowLimit;
    }

    public void setRowLimit(int rowLimit) {
        this.rowLimit = rowLimit;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTestCase() {
        return testCase;
    }

    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public MessageEvent getResult() {
        return result;
    }

    public void setResult(MessageEvent result) {
        this.result = result;
    }

    public boolean hasSameKey(TestCaseCountryProperties obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        if ((this.test == null) ? (obj.test != null) : !this.test.equals(obj.test)) {
            return false;
        }
        if ((this.testCase == null) ? (obj.testCase != null) : !this.testCase.equals(obj.testCase)) {
            return false;
        }
        if ((this.country == null) ? (obj.country != null) : !this.country.equals(obj.country)) {
            return false;
        }
        if ((this.property == null) ? (obj.property != null) : !this.property.equals(obj.property)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.test != null ? this.test.hashCode() : 0);
        hash = 83 * hash + (this.testCase != null ? this.testCase.hashCode() : 0);
        hash = 83 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 83 * hash + (this.property != null ? this.property.hashCode() : 0);
        hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 83 * hash + (this.database != null ? this.database.hashCode() : 0);
        hash = 83 * hash + (this.value1 != null ? this.value1.hashCode() : 0);
        hash = 83 * hash + (this.value2 != null ? this.value2.hashCode() : 0);
        hash = 83 * hash + this.length;
        hash = 83 * hash + this.rowLimit;
        hash = 83 * hash + (this.nature != null ? this.nature.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestCaseCountryProperties other = (TestCaseCountryProperties) obj;
        if ((this.test == null) ? (other.test != null) : !this.test.equals(other.test)) {
            return false;
        }
        if ((this.testCase == null) ? (other.testCase != null) : !this.testCase.equals(other.testCase)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.property == null) ? (other.property != null) : !this.property.equals(other.property)) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.database == null) ? (other.database != null) : !this.database.equals(other.database)) {
            return false;
        }
        if ((this.value1 == null) ? (other.value1 != null) : !this.value1.equals(other.value1)) {
            return false;
        }
        if ((this.value2 == null) ? (other.value2 != null) : !this.value2.equals(other.value2)) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        if (this.rowLimit != other.rowLimit) {
            return false;
        }
        if ((this.nature == null) ? (other.nature != null) : !this.nature.equals(other.nature)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestCaseCountryProperties{" + "test=" + test + ", testCase=" + testCase + ", country=" + country + ", property=" + property + ", type=" + type + ", database=" + database + ", value1=" + value1 + ", value2=" + value2 + ", length=" + length + ", rowLimit=" + rowLimit + ", nature=" + nature + '}';
    }
    
    
}
