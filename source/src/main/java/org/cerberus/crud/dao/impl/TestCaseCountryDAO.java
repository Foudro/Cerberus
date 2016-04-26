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
package org.cerberus.crud.dao.impl;

import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestCaseCountryDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 28/Dez/2012
 * @since 2.0.0
 */
@Repository
public class TestCaseCountryDAO implements ITestCaseCountryDAO {

    /**
     * Description of the variable here.
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestCaseCountry factoryTestCaseCountry;

    private static final Logger LOG = Logger.getLogger(TestCaseCountryDAO.class);

    private final String OBJECT_NAME = "TestCaseCountry";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public TestCaseCountry findTestCaseCountryByKey(String test, String testCase, String country) throws CerberusException {
        final String query = "SELECT * FROM testcasecountry tcc WHERE test = ? AND testcase = ? AND country = ? ";
        TestCaseCountry result = null;
        boolean throwException = false;

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setString(3, country);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = factoryTestCaseCountry.create(test, testCase, country);
                    } else {
                        throwException = true;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwException) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public List<TestCaseCountry> findTestCaseCountryByTestTestCase(String test, String testCase) {
        List<TestCaseCountry> result = null;
        final String query = "SELECT * FROM testcasecountry tcc WHERE test = ? AND testcase = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    result = new ArrayList<TestCaseCountry>();

                    while (resultSet.next()) {
                        String country = resultSet.getString("Country");
                        result.add(factoryTestCaseCountry.create(test, testCase, country));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public void insertTestCaseCountry(TestCaseCountry testCaseCountry) throws CerberusException {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO testcasecountry (`test`, `testCase`, `country`) ");
        query.append("VALUES (?,?,?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, testCaseCountry.getTest());
                preStat.setString(2, testCaseCountry.getTestCase());
                preStat.setString(3, testCaseCountry.getCountry());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public void deleteTestCaseCountry(TestCaseCountry tcc) throws CerberusException {
        boolean throwExcep = false;
        final String query = "DELETE FROM testcasecountry WHERE test = ? and testcase = ? and country = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, tcc.getTest());
                preStat.setString(2, tcc.getTestCase());
                preStat.setString(3, tcc.getCountry());

                throwExcep = preStat.executeUpdate() == 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseCountryDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseCountryDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.CANNOT_UPDATE_TABLE));
        }
    }

    @Override
    public AnswerList readByTestTestCase(String system, String test, String testCase) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestCaseCountry> testCaseCountryList = new ArrayList<TestCaseCountry>();
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcasecountry tcc ");
        if (!Strings.isNullOrEmpty(system)) {
            query.append(" LEFT OUTER JOIN testcase tc on tc.test = tcc.test and tc.testcase = tcc.testcase  ");
            query.append(" LEFT OUTER JOIN application app on app.application = tc.application ");
        }

        query.append(" WHERE 1=1");

        if (!Strings.isNullOrEmpty(system)) {
            query.append(" AND app.`system` = ?");
        }
        if (!Strings.isNullOrEmpty(test)) {
            query.append(" AND tcc.test = ?");
        }
        if (!Strings.isNullOrEmpty(testCase)) {
            query.append(" AND tcc.testcase = ?");
        }

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            try {
                int i = 1;
                if (!Strings.isNullOrEmpty(system)) {
                    preStat.setString(i++, system);
                }
                if (!Strings.isNullOrEmpty(test)) {
                    preStat.setString(i++, test);
                }
                if (!Strings.isNullOrEmpty(testCase)) {
                    preStat.setString(i++, testCase);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testCaseCountryList.add(this.loadFromResultSet(resultSet));
                    }

                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                    answer = new AnswerList(testCaseCountryList, testCaseCountryList.size());

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    @Override
    public AnswerItem readByKey(String test, String testCase, String country) {
        AnswerItem answer = new AnswerItem();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        TestCaseCountry result = new TestCaseCountry();
        final String query = "SELECT * FROM testcasecountry tcc WHERE test = ? AND testcase = ? AND country = ?";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);

            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                preStat.setString(3, country);
                ResultSet resultSet = preStat.executeQuery();

                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer.setItem(result);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                    }
                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the entry!"));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Unable to retrieve the list of entries!"));
        } finally {
            try {
                if (!this.databaseSpring.isOnTransaction()) {
                    if (connection != null) {
                        connection.close();
                    }
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        answer.setResultMessage(msg);
        return answer;
    }

    private TestCaseCountry loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tcc.test") == null ? "" : resultSet.getString("test");
        String testcase = resultSet.getString("tcc.testcase") == null ? "" : resultSet.getString("testcase");
        String country = resultSet.getString("tcc.country") == null ? "" : resultSet.getString("country");

        return factoryTestCaseCountry.create(test, testcase, country);
    }

}
