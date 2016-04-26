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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.cerberus.crud.entity.TestBatteryContent;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ITestBatteryContentDAO;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TestBatteryContentWithDescription;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTestBatteryContent;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author memiks
 */
@Repository
public class TestBatteryContentDAO implements ITestBatteryContentDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTestBatteryContent factoryTestBatteryContent;

    private static final Logger LOG = Logger.getLogger(TestBatteryContentDAO.class);

    private final String OBJECT_NAME = "TestBatteryContent";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<TestBatteryContent> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t";

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public TestBatteryContent findTestBatteryContentByKey(Integer testBatteryContentID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t WHERE t.testbatterycontentID = ?";

        TestBatteryContent testBatteryContent = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, testBatteryContentID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        testBatteryContent = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContent;
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByTestBatteryName(String testBattery) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM testbatterycontent t where t.testbattery = ?";

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testBattery);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public List<TestBatteryContentWithDescription> findTestBatteryContentsWithDescriptionByTestBatteryName(String testBattery) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT tbc.*, tc.Description FROM testbatterycontent tbc inner join testcase tc on tbc.Test=tc.Test and tbc.TestCase=tc.TestCase"
                + " where tbc.testbattery = ?";

        List<TestBatteryContentWithDescription> testBatteryContentsWithDescriptionsList = new ArrayList<TestBatteryContentWithDescription>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testBattery);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsWithDescriptionsList.add(this.loadTestBatteryContentWithDescriptionFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsWithDescriptionsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsWithDescriptionsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsWithDescriptionsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsWithDescriptionsList;
    }

    @Override
    public boolean updateTestBatteryContent(TestBatteryContent testBatteryContent) {
        final StringBuffer query = new StringBuffer("UPDATE `testbatterycontent` set `testbattery` = ?, `Test` = ?, `TestCase` = ? WHERE `testbatterycontentID` = ?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBatteryContent.getTestbattery());
            preStat.setString(2, testBatteryContent.getTest());
            preStat.setString(3, testBatteryContent.getTestCase());
            preStat.setInt(4, testBatteryContent.getTestbatterycontentID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public List<TestBatteryContent> findTestBatteryContentsByCriteria(Integer testBatteryContentID, String testBattery, String test, String testCase) throws CerberusException {
        boolean throwEx = false;
        final StringBuffer query = new StringBuffer("SELECT * FROM testbatterycontent t WHERE 1=1");

        if (testBatteryContentID != null) {
            query.append(" AND t.testBatterycontentID = ?");
        }
        if (testBattery != null && !"".equals(testBattery.trim())) {
            query.append(" AND t.testBattery LIKE ?");
        }
        if (test != null && !"".equals(test.trim())) {
            query.append(" AND t.Test LIKE ?");
        }
        if (testCase != null && !"".equals(testCase.trim())) {
            query.append(" AND t.TestCase LIKE ?");
        }

        List<TestBatteryContent> testBatteryContentsList = new ArrayList<TestBatteryContent>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (testBatteryContentID != null) {
                preStat.setInt(index, testBatteryContentID);
                index++;
            }
            if (testBattery != null && !"".equals(testBattery.trim())) {
                preStat.setString(index, "%" + testBattery.trim() + "%");
                index++;
            }
            if (test != null && !"".equals(test.trim())) {
                preStat.setString(index, "%" + test.trim() + "%");
                index++;
            }
            if (testCase != null && !"".equals(testCase.trim())) {
                preStat.setString(index, "%" + testCase.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        testBatteryContentsList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    testBatteryContentsList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                testBatteryContentsList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            testBatteryContentsList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return testBatteryContentsList;
    }

    @Override
    public boolean createTestBatteryContent(TestBatteryContent testBatteryContent) {
        final StringBuffer query = new StringBuffer("INSERT INTO `testbatterycontent` (`testbattery`, `Test`, `TestCase`) VALUES (?, ?, ?)");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, testBatteryContent.getTestbattery());
            preStat.setString(2, testBatteryContent.getTest());
            preStat.setString(3, testBatteryContent.getTestCase());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    private TestBatteryContent loadFromResultSet(ResultSet rs) throws SQLException {
        Integer testbatterycontentID = ParameterParserUtil.parseIntegerParam(rs.getString("testbatterycontentID"), -1);
        String testbattery = ParameterParserUtil.parseStringParam(rs.getString("testbattery"), "");
        String test = ParameterParserUtil.parseStringParam(rs.getString("Test"), "");
        String testCase = ParameterParserUtil.parseStringParam(rs.getString("TestCase"), "");

        return factoryTestBatteryContent.create(testbatterycontentID, test, testCase, testbattery);
    }

    private TestBatteryContentWithDescription loadTestBatteryContentWithDescriptionFromResultSet(ResultSet rs) throws SQLException {
        Integer testbatterycontentID = ParameterParserUtil.parseIntegerParam(rs.getString("testbatterycontentID"), -1);
        String testbattery = ParameterParserUtil.parseStringParam(rs.getString("testbattery"), "");
        String test = ParameterParserUtil.parseStringParam(rs.getString("Test"), "");
        String testCase = ParameterParserUtil.parseStringParam(rs.getString("TestCase"), "");
        String description = ParameterParserUtil.parseStringParam(rs.getString("Description"), "");

        return new TestBatteryContentWithDescription(testbatterycontentID, test, testCase, testbattery, description);
    }

    @Override
    public boolean deleteTestBatteryContent(TestBatteryContent testBatteryContent) {
        final StringBuffer query = new StringBuffer("DELETE FROM `testbatterycontent` WHERE testbatterycontentID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, testBatteryContent.getTestbatterycontentID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestBatteryContentDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestBatteryContentDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public AnswerList readByTestBatteryByCriteria(String testBattery, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TestBatteryContent> testBatteryContentList = new ArrayList<TestBatteryContent>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testbatterycontent ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`testbatterycontentid` like ?");
            searchSQL.append(" or `testbattery` like ?");
            searchSQL.append(" or `test` like ?");
            searchSQL.append(" or `testcase` like ?)");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`?`)");
        }
        if (!StringUtil.isNullOrEmpty(testBattery)) {
            searchSQL.append(" and (`testbattery` = ? )");
        }
        query.append(searchSQL);

        if (!StringUtil.isNullOrEmpty(column)) {
            query.append(" order by `").append(column).append("` ").append(dir);
        }

        if ((amount <= 0) || (amount >= MAX_ROW_SELECTED)) {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
        } else {
            query.append(" limit ").append(start).append(" , ").append(amount);
        }

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int i = 1;
                if (!StringUtil.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                if (!StringUtil.isNullOrEmpty(individualSearch)) {
                    preStat.setString(i++, individualSearch);
                }
                if (!StringUtil.isNullOrEmpty(testBattery)) {
                    preStat.setString(i++, testBattery);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testBatteryContentList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (testBatteryContentList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(testBatteryContentList, nrTotalRows);
                    } else if (testBatteryContentList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(testBatteryContentList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(testBatteryContentList, nrTotalRows);
                    }

                } catch (SQLException exception) {
                    LOG.error("Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }

        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
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

        response.setResultMessage(msg);
        response.setDataList(testBatteryContentList);
        return response;
    }
}
