/*
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
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ILabelDAO;
import org.cerberus.crud.dao.ITestCaseDAO;
import org.cerberus.crud.dao.ITestCaseLabelDAO;
import org.cerberus.crud.entity.Label;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCase;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.log.MyLogger;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.SqlUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Used to manage TestCase table
 *
 * @author Tiago Bernardes
 * @version 1.0, 18/12/2012
 * @since 0.9.0
 */
@Repository
public class TestCaseDAO implements ITestCaseDAO {

    /**
     * Class used to manage connection.
     *
     * @see org.cerberus.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryTCase factoryTestCase;
    
    private static final Logger LOG = Logger.getLogger(TestCaseDAO.class);

    private final String OBJECT_NAME = "TestCase";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    /**
     * Get summary information of all test cases of one group.
     * <p/>
     * Used to display list of test cases on drop-down list
     *
     * @param test Name of test group.
     * @return List with a list of 3 strings (name of test case, type of
     * application, description of test case).
     */
    @Override
    public List<TCase> findTestCaseByTest(String test) {
        List<TCase> list = null;
        final String query = "SELECT * FROM testcase tec WHERE test = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public AnswerList readByTestByCriteria(String system, String test, int start, int amount, String sortInformation, String searchTerm, Map<String, List<String>> individualSearch) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TCase> testCaseList = new ArrayList<TCase>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM testcase tec ");
        query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
        query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");
        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        }

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" AND app.`system` = ? ");
        }
        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`project` like ?");
            searchSQL.append(" or tec.`creator` like ?");
            searchSQL.append(" or tec.`lastmodifier` like ?");
            searchSQL.append(" or tec.`tcactive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`group` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`tcdatecrea` like ?");
            searchSQL.append(" or tec.`description` like ?");
            searchSQL.append(" or lab.`label` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);

        query.append(" group by tec.test, tec.testcase ");
        
        if (!StringUtil.isNullOrEmpty(sortInformation)) {
            query.append(" order by ").append(sortInformation);
        }
        
        if (amount != 0) {
            query.append(" limit ").append(start).append(" , ").append(amount);
        } else {
            query.append(" limit ").append(start).append(" , ").append(MAX_ROW_SELECTED);
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
                if (!StringUtil.isNullOrEmpty(system)) {
                    preStat.setString(i++, system);
                }
                if (!StringUtil.isNullOrEmpty(test)) {
                    preStat.setString(i++, test);
                }
                if (!Strings.isNullOrEmpty(searchTerm)) {
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                    preStat.setString(i++, "%" + searchTerm + "%");
                }
                for (String individualColumnSearchValue : individalColumnSearchValues) {
                    preStat.setString(i++, individualColumnSearchValue);
                }

                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testCaseList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (testCaseList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        answer = new AnswerList(testCaseList, nrTotalRows);
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList(testCaseList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList(testCaseList, nrTotalRows);
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseList);
        return answer;
    }

    /**
     * Get test case information.
     *
     * @param test Name of test group.
     * @param testCase Name of test case.
     * @return TestCase object or null.
     * @throws org.cerberus.exception.CerberusException
     * @see org.cerberus.crud.entity.TestCase
     */
    @Override
    public TCase findTestCaseByKey(String test, String testCase) throws CerberusException {
        boolean throwExcep = false;
        TCase result = null;
        final String query = "SELECT * FROM testcase tec WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = this.loadFromResultSet(resultSet);
                    } else {
                        result = null;
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwExcep) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return result;
    }

    @Override
    public boolean updateTestCaseInformation(TestCase testCase) {
        boolean res = false;
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getDescription());
                preStat.setString(4, testCase.isRunQA() ? "Y" : "N");
                preStat.setString(5, testCase.isRunUAT() ? "Y" : "N");
                preStat.setString(6, testCase.isRunPROD() ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.isActive() ? "Y" : "N");
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean updateTestCaseInformationCountries(TestCase tc) {
        boolean res = false;
        final String sql_count = "SELECT Country FROM testcasecountry WHERE Test = ? AND TestCase = ?";
        ArrayList<String> countriesDB = new ArrayList<String>();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql_count);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql_count);
            try {
                preStat.setString(1, tc.getTest());
                preStat.setString(2, tc.getTestCase());
                ResultSet rsCount = preStat.executeQuery();
                try {
                    while (rsCount.next()) {
                        countriesDB.add(rsCount.getString("Country"));
                        if (!tc.getCountryList().contains(rsCount.getString("Country"))) {
                            final String sql_delete = "DELETE FROM testcasecountry WHERE Test = ? AND TestCase = ? AND Country = ?";

                            PreparedStatement preStat2 = connection.prepareStatement(sql_delete);
                            try {
                                preStat2.setString(1, tc.getTest());
                                preStat2.setString(2, tc.getTestCase());
                                preStat2.setString(3, rsCount.getString("Country"));

                                preStat2.executeUpdate();
                            } catch (SQLException exception) {
                                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                            } finally {
                                preStat2.close();
                            }
                        }
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    rsCount.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }

            res = true;
            for (int i = 0; i < tc.getCountryList().size() && res; i++) {
                if (!countriesDB.contains(tc.getCountryList().get(i))) {
                    final String sql_insert = "INSERT INTO testcasecountry (test, testcase, country) VALUES (?, ?, ?)";

                    PreparedStatement preStat2 = connection.prepareStatement(sql_insert);
                    try {
                        preStat2.setString(1, tc.getTest());
                        preStat2.setString(2, tc.getTestCase());
                        preStat2.setString(3, tc.getCountryList().get(i));

                        res = preStat2.executeUpdate() > 0;
                    } catch (SQLException exception) {
                        MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    } finally {
                        preStat2.close();
                    }
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;
    }

    @Override
    public boolean createTestCase(TCase testCase) {
        boolean res = false;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`ChainNumberNeeded`, `Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `Creator`, ")
                .append("`Implementer`, `LastModifier`, `function`, `activeQA`, `activeUAT`, `activePROD`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(2, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(3, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(4, ParameterParserUtil.parseStringParam(testCase.getProject(), ""));
                preStat.setString(5, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(6, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(7, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(8, null);
                preStat.setString(9, Integer.toString(testCase.getPriority()));
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(11, testCase.getActive() != null && !testCase.getActive().equals("Y") ? "N" : "Y");
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getOrigin(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getRefOrigin(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getCreator(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(27, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(28, testCase.getRunQA() != null && !testCase.getRunQA().equals("Y") ? "N" : "Y");
                preStat.setString(29, testCase.getRunUAT() != null && !testCase.getRunUAT().equals("Y") ? "N" : "Y");
                preStat.setString(30, testCase.getRunPROD() != null && !testCase.getRunPROD().equals("N") ? "Y" : "N");

                res = preStat.executeUpdate() > 0;
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return res;

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TCase> findTestCaseByCriteria(String test, String application, String country, String active) {
        List<TCase> list = null;
        final String query = "SELECT tec.* FROM testcase tec JOIN testcasecountry tcc "
                + "WHERE tec.test=tcc.test AND tec.testcase=tcc.testcase "
                + "AND tec.test = ? AND tec.application = ? AND tcc.country = ? AND tec.tcactive = ? ";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, application);
                preStat.setString(3, country);
                preStat.setString(4, active);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByCampaignName(String campaign) {
        List<TCase> list = null;
        final String query = new StringBuilder("select tec.* ")
                .append("from testcase tec ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ")
                .toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, campaign);

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    /**
     * @since 0.9.1
     */
    @Override
    public List<TCase> findTestCaseByCriteria(TCase testCase, String text, String system) {
        List<TCase> list = null;
        String query = new StringBuilder()
                .append("SELECT tec.* FROM testcase tec LEFT OUTER JOIN application a ON a.application=tec.application ")
                .append(" WHERE (tec.test LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.test", testCase.getTest()))
                .append(") AND (tec.project LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.project", testCase.getProject()))
                .append(") AND (tec.ticket LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.ticket", testCase.getTicket()))
                .append(") AND (tec.bugid LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.bugid", testCase.getBugID()))
                .append(") AND (tec.origine LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.origine", testCase.getOrigin()))
                .append(") AND (a.system LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("a.system", system))
                .append(") AND (tec.application LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.application", testCase.getApplication()))
                .append(") AND (tec.priority LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfMinusOne("tec.priority", testCase.getPriority()))
                .append(") AND (tec.status LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.status", testCase.getStatus()))
                .append(") AND (tec.group LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.group", testCase.getGroup()))
                .append(") AND (tec.activePROD LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activePROD", testCase.getRunPROD()))
                .append(") AND (tec.activeUAT LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activeUAT", testCase.getRunUAT()))
                .append(") AND (tec.activeQA LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.activeQA", testCase.getRunQA()))
                .append(") AND (tec.description LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.description", text))
                .append(" OR tec.howto LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.howto", text))
                .append(" OR tec.behaviororvalueexpected LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.behaviororvalueexpected", text))
                .append(" OR tec.comment LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.comment", text))
                .append(") AND (tec.TcActive LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.TcActive", testCase.getActive()))
                .append(") AND (tec.frombuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.frombuild", testCase.getFromSprint()))
                .append(") AND (tec.fromrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.fromrev", testCase.getFromRevision()))
                .append(") AND (tec.tobuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.tobuild", testCase.getToSprint()))
                .append(") AND (tec.torev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.torev", testCase.getToRevision()))
                .append(") AND (tec.targetbuild LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.targetbuild", testCase.getTargetSprint()))
                .append(") AND (tec.targetrev LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.targetrev", testCase.getTargetRevision()))
                .append(") AND (tec.testcase LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.testcase", testCase.getTestCase()))
                .append(") AND (tec.function LIKE ")
                .append(ParameterParserUtil.wildcardOrIsNullIfEmpty("tec.function", testCase.getFunction()))
                .append(")").toString();

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    private String createInClauseFromList(String[] list, String column) {
        StringBuilder query = new StringBuilder();

        if (list != null) {
            query.append("AND ");
            query.append(column);
            query.append(" IN (");
            int i = 0;
            while (i < list.length - 1) {
                query.append("'");
                query.append(list[i]);
                query.append("',");
                i++;
            }
            query.append("'");
            query.append(list[i]);
            query.append("')");
        }
        return query.toString();
    }

    @Override
    public AnswerList readByVariousCriteria(String[] test, String[] idProject, String[] app, String[] creator, String[] implementer, String[] system,
            String[] testBattery, String[] campaign, String[] priority, String[] group, String[] status) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<TCase> testCaseList = new ArrayList<TCase>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM testcase tec ");
        query.append("LEFT JOIN application app ON tec.application = app.application ");
        query.append("LEFT JOIN testbatterycontent tb ON tec.test = tb.test AND tec.testcase = tb.testcase ");
        query.append("LEFT JOIN campaigncontent cc ON tb.testbattery = cc.testbattery ");
        query.append("WHERE 1=1 AND tec.tcactive = 'Y' ");
        query.append(createInClauseFromList(test, "tec.test"));
        query.append(createInClauseFromList(idProject, "tec.project"));
        query.append(createInClauseFromList(app, "tec.application"));
        query.append(createInClauseFromList(creator, "tec.creator"));
        query.append(createInClauseFromList(implementer, "tec.implementer"));
        query.append(createInClauseFromList(system, "app.system"));
        query.append(createInClauseFromList(testBattery, "tb.testbattery"));
        query.append(createInClauseFromList(campaign, "cc.campaign"));
        query.append(createInClauseFromList(priority, "tec.priority"));
        query.append(createInClauseFromList(group, "tec.group"));
        query.append(createInClauseFromList(status, "tec.status"));
        query.append("GROUP BY tec.test, tec.testcase ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        testCaseList.add(this.loadFromResultSet(resultSet));
                    }

                    if (testCaseList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        answer = new AnswerList(testCaseList, testCaseList.size());
                    } else if (testCaseList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        answer = new AnswerList(testCaseList, testCaseList.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        answer = new AnswerList(testCaseList, testCaseList.size());
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

        answer.setResultMessage(msg);
        answer.setDataList(testCaseList);
        return answer;
    }

    /**
     * @since 0.9.1
     */
    private TCase loadFromResultSet(ResultSet resultSet) throws SQLException {
        String test = resultSet.getString("tec.Test");
        String testCase = resultSet.getString("tec.TestCase");
        String tcapplication = resultSet.getString("tec.Application");
        String project = resultSet.getString("tec.Project");
        String ticket = resultSet.getString("tec.Ticket");
        String description = resultSet.getString("tec.Description");
        String behavior = resultSet.getString("tec.BehaviorOrValueExpected");
        int priority = resultSet.getInt("tec.Priority");
        String status = resultSet.getString("tec.Status");
        String tcactive = resultSet.getString("tec.TcActive");
        String group = resultSet.getString("tec.Group");
        String origin = resultSet.getString("tec.Origine");
        String refOrigin = resultSet.getString("tec.RefOrigine");
        String howTo = resultSet.getString("tec.HowTo");
        String comment = resultSet.getString("tec.Comment");
        String fromSprint = resultSet.getString("tec.FromBuild");
        String fromRevision = resultSet.getString("tec.FromRev");
        String toSprint = resultSet.getString("tec.ToBuild");
        String toRevision = resultSet.getString("tec.ToRev");
        String bugID = resultSet.getString("tec.BugID");
        String targetSprint = resultSet.getString("tec.TargetBuild");
        String targetRevision = resultSet.getString("tec.TargetRev");
        String creator = resultSet.getString("tec.Creator");
        String implementer = resultSet.getString("tec.Implementer");
        String lastModifier = resultSet.getString("tec.LastModifier");
        String runQA = resultSet.getString("tec.activeQA");
        String runUAT = resultSet.getString("tec.activeUAT");
        String runPROD = resultSet.getString("tec.activePROD");
        String function = resultSet.getString("tec.function");
        String dateCrea = resultSet.getString("tec.tcdatecrea");

        return factoryTestCase.create(test, testCase, origin, refOrigin, creator, implementer,
                lastModifier, project, ticket, function, tcapplication, runQA, runUAT, runPROD, priority, group,
                status, description, behavior, howTo, tcactive, fromSprint, fromRevision, toSprint,
                toRevision, status, bugID, targetSprint, targetRevision, comment, dateCrea);
    }

    @Override
    public List<String> findUniqueDataOfColumn(String column) {
        List<String> list = null;
        final String query = "SELECT DISTINCT tec." + column + " FROM testcase tec ORDER BY tec." + column + " ASC";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<String>();
                try {
                    while (resultSet.next()) {
                        list.add(resultSet.getString(1));

                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class
                            .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();

                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public boolean deleteTestCase(TCase testCase) {
        boolean bool = false;
        final String query = "DELETE FROM testcase WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                bool = preStat.executeUpdate() > 0;

            } catch (SQLException exception) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(UserDAO.class
                    .getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(UserDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }
        return bool;
    }

    @Override
    public void updateTestCaseField(TCase tc, String columnName, String value) {
        boolean throwExcep = false;
        StringBuilder query = new StringBuilder();
        query.append("update testcase set `");
        query.append(columnName);
        query.append("`=? where `test`=? and `testcase`=? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, value);
                preStat.setString(2, tc.getTest());
                preStat.setString(3, tc.getTestCase());

                preStat.executeUpdate();
                throwExcep = false;

            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();

            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class
                    .getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();

                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class
                        .getName(), Level.WARN, e.toString());
            }
        }

    }

    /**
     * @param testCase
     * @param system
     * @return
     * @since 1.0.2
     */
    @Override
    public List<TCase> findTestCaseByGroupInCriteria(TCase testCase, String system) {
        List<TCase> list = null;
        StringBuilder query = new StringBuilder();
        query.append("SELECT tec.* FROM testcase tec LEFT OUTER JOIN application a ON a.application=tec.application WHERE 1=1");
        if (!StringUtil.isNull(testCase.getTest())) {
            query.append(" AND tec.test IN (");
            query.append(testCase.getTest());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getProject())) {
            query.append(" AND tec.project IN (");
            query.append(testCase.getProject());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTicket())) {
            query.append(" AND tec.ticket IN (");
            query.append(testCase.getTicket());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTicket())) {
            query.append(" AND tec.ticket IN (");
            query.append(testCase.getTicket());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getBugID())) {
            query.append(" AND tec.bugid IN (");
            query.append(testCase.getBugID());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getOrigin())) {
            query.append(" AND tec.origine IN (");
            query.append(testCase.getOrigin());
            query.append(") ");
        }
        if (!StringUtil.isNull(system)) {
            query.append(" AND a.system IN (");
            query.append(system);
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getApplication())) {
            query.append(" AND tec.application IN (");
            query.append(testCase.getApplication());
            query.append(") ");
        }
        if (testCase.getPriority() != -1) {
            query.append(" AND tec.priority IN (");
            query.append(testCase.getPriority());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getStatus())) {
            query.append(" AND tec.status IN (");
            query.append(testCase.getStatus());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getGroup())) {
            query.append(" AND tec.group IN (");
            query.append(testCase.getGroup());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunPROD())) {
            query.append(" AND tec.activePROD IN (");
            query.append(testCase.getRunPROD());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunUAT())) {
            query.append(" AND tec.activeUAT IN (");
            query.append(testCase.getRunUAT());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getRunQA())) {
            query.append(" AND tec.activeQA IN (");
            query.append(testCase.getRunQA());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getShortDescription())) {
            query.append(" AND tec.description LIKE '%");
            query.append(testCase.getShortDescription());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getHowTo())) {
            query.append(" AND tec.howto LIKE '%");
            query.append(testCase.getHowTo());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getDescription())) {
            query.append(" AND tec.behaviororvalueexpected LIKE '%");
            query.append(testCase.getDescription());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getComment())) {
            query.append(" AND tec.comment LIKE '%");
            query.append(testCase.getComment());
            query.append("%'");
        }
        if (!StringUtil.isNull(testCase.getActive())) {
            query.append(" AND tec.TcActive IN (");
            query.append(testCase.getActive());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromSprint())) {
            query.append(" AND tec.frombuild IN (");
            query.append(testCase.getFromSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFromRevision())) {
            query.append(" AND tec.fromrev IN (");
            query.append(testCase.getFromRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToSprint())) {
            query.append(" AND tec.tobuild IN (");
            query.append(testCase.getToSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getToRevision())) {
            query.append(" AND tec.torev IN (");
            query.append(testCase.getToRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetSprint())) {
            query.append(" AND tec.targetbuild IN (");
            query.append(testCase.getTargetSprint());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTargetRevision())) {
            query.append(" AND tec.targetrev IN (");
            query.append(testCase.getTargetRevision());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getTestCase())) {
            query.append(" AND tec.testcase IN (");
            query.append(testCase.getTestCase());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getFunction())) {
            query.append(" AND tec.function IN (");
            query.append(testCase.getFunction());
            query.append(") ");
        }
        if (!StringUtil.isNull(testCase.getCreator())) {
            query.append(" AND tec.Creator IN (");
            query.append(testCase.getCreator());
            query.append(") ");
        }
        query.append(" ORDER BY tec.test, tec.testcase");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }

    @Override
    public void updateTestCase(TCase testCase) throws CerberusException {
        final String sql = "UPDATE testcase tc SET tc.Application = ?, tc.Project = ?, tc.BehaviorOrValueExpected = ?, tc.activeQA = ?, tc.activeUAT = ?, tc.activePROD = ?, "
                + "tc.Priority = ?, tc.Status = ?, tc.TcActive = ?, tc.Description = ?, tc.Group = ?, tc.HowTo = ?, tc.Comment = ?, tc.Ticket = ?, tc.FromBuild = ?, "
                + "tc.FromRev = ?, tc.ToBuild = ?, tc.ToRev = ?, tc.BugID = ?, tc.TargetBuild = ?, tc.Implementer = ?, tc.LastModifier = ?, tc.TargetRev = ?, tc.`function` = ? "
                + "WHERE tc.Test = ? AND tc.Testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, testCase.getApplication());
                preStat.setString(2, testCase.getProject());
                preStat.setString(3, testCase.getDescription());
                preStat.setString(4, testCase.getRunQA().equals("Y") ? "Y" : "N");
                preStat.setString(5, testCase.getRunUAT().equals("Y") ? "Y" : "N");
                preStat.setString(6, testCase.getRunPROD().equals("Y") ? "Y" : "N");
                preStat.setString(7, Integer.toString(testCase.getPriority()));
                preStat.setString(8, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(9, testCase.getActive().equals("Y") ? "Y" : "N");
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(11, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));

                preStat.executeUpdate();
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
    }

    @Override
    public List<TCase> findTestCaseByTestSystems(String test, List<String> systems) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMaxNumberTestCase(String test) {
        String max = "";
        final String sql = "SELECT  convert ( Max( Testcase ) + 0, UNSIGNED) as MAXTC FROM testcase where test = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        max = resultSet.getString("MAXTC");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return max;
    }

    @Override
    public List<TCase> findTestCaseByCampaignNameAndCountries(String campaign, String[] countries) {
        List<TCase> list = null;
        final StringBuilder query = new StringBuilder("select tec.* ")
                .append("from testcase tec ")
                .append("inner join testcasecountry tcc ")
                .append("on tcc.Test = tec.Test ")
                .append("and tcc.TestCase = tec.TestCase ")
                .append("inner join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("inner join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ")
                .append("where cc.campaign = ? ");

        query.append(" and tcc.Country in (");
        for (int i = 0; i < countries.length; i++) {
            query.append("?");
            if (i < countries.length - 1) {
                query.append(", ");
            }
        }
        query.append(")");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                int index = 1;
                preStat.setString(index, campaign);
                index++;

                for (String c : countries) {
                    preStat.setString(index, c);
                    index++;
                }

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TCase>();
                try {
                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByTestSystem(String test, String system) {
        List<TCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tec join application app on tec.application=app.application ");
        sb.append(" WHERE tec.test = ? and app.system = ? ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sb.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                preStat.setString(1, test);
                preStat.setString(2, system);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public List<TCase> findTestCaseByCriteria(String testClause, String projectClause, String appClause, String activeClause, String priorityClause, String statusClause, String groupClause, String targetBuildClause, String targetRevClause, String creatorClause, String implementerClause, String functionClause, String campaignClause, String batteryClause) {
        List<TCase> list = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM testcase tec join application app on tec.application=app.application ")
                .append("left join testbatterycontent tbc ")
                .append("on tbc.Test = tec.Test ")
                .append("and tbc.TestCase = tec.TestCase ")
                .append("left join campaigncontent cc ")
                .append("on cc.testbattery = tbc.testbattery ");
        sb.append(" WHERE 1=1 ");
        sb.append(testClause);
        sb.append(projectClause);
        sb.append(appClause);
        sb.append(activeClause);
        sb.append(priorityClause);
        sb.append(statusClause);
        sb.append(groupClause);
        sb.append(targetBuildClause);
        sb.append(targetRevClause);
        sb.append(creatorClause);
        sb.append(implementerClause);
        sb.append(functionClause);
        sb.append(campaignClause);
        sb.append(batteryClause);
        sb.append(" GROUP BY tec.test, tec.testcase ");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sb.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sb.toString());
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }

        return list;
    }

    @Override
    public String findSystemOfTestCase(String test, String testcase) throws CerberusException {
        String result = "";
        final String sql = "SELECT system from application a join testcase tec on tec.application=a.Application where tec.test= ? and tec.testcase= ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testcase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.next()) {
                        result = resultSet.getString("system");
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return result;
    }

    @Override
    public AnswerList readTestCaseByStepsInLibrary(String test) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
        List<TCase> list = new ArrayList<TCase>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM testcase tec  ");
        query.append("inner join testcasestep  tcs on tec.test = tcs.test and tec.testcase = tcs.testcase ");
        query.append("WHERE tec.test= ? and (tcs.inlibrary = 'Y' or tcs.inlibrary = 'y') ");
        query.append("group by tec.testcase order by tec.testcase ");
        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, test);

                ResultSet resultSet = preStat.executeQuery();
                try {
                    list = new ArrayList<TCase>();

                    while (resultSet.next()) {
                        list.add(loadFromResultSet(resultSet));
                    }

                    if (list.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(list, list.size());
                    } else if (list.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(list, list.size());
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(list, list.size());
                    }

                } catch (SQLException exception) {
                    MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                if (preStat != null) {
                    preStat.close();
                }
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        response.setDataList(list);
        response.setResultMessage(msg);
        return response;
    }

    @Override
    public AnswerItem readByKey(String test, String testCase) {
        AnswerItem ans = new AnswerItem();
        TCase result = null;
        final String query = "SELECT * FROM `testcase` tec WHERE tec.`test` = ? AND tec.`testcase` = ?";
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, test);
                preStat.setString(2, testCase);
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        result = loadFromResultSet(resultSet);
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        ans.setItem(result);
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
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }

        //sets the message
        ans.setResultMessage(msg);
        return ans;
    }

    @Override
    public AnswerList<List<String>> readDistinctValuesByCriteria(String system, String test, String searchTerm, Map<String, List<String>> individualSearch, String columnName) {
        AnswerList answer = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<String> distinctValues = new ArrayList<>();
        StringBuilder searchSQL = new StringBuilder();
        List<String> individalColumnSearchValues = new ArrayList<String>();

        StringBuilder query = new StringBuilder();

        query.append("SELECT distinct ");
        query.append(columnName);
        query.append(" as distinctValues FROM testcase tec ");
        query.append(" LEFT OUTER JOIN testcaselabel tel on tec.test = tel.test AND tec.testcase = tel.testcase ");
        query.append(" LEFT OUTER JOIN label lab on tel.labelId = lab.id ");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" LEFT OUTER JOIN application app on app.application = tec.application ");
        }

        searchSQL.append("WHERE 1=1");

        if (!StringUtil.isNullOrEmpty(system)) {
            searchSQL.append(" AND app.`system` = ? ");
        }
        if (!StringUtil.isNullOrEmpty(test)) {
            searchSQL.append(" AND tec.`test` = ?");
        }

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (tec.`testcase` like ?");
            searchSQL.append(" or tec.`test` like ?");
            searchSQL.append(" or tec.`application` like ?");
            searchSQL.append(" or tec.`project` like ?");
            searchSQL.append(" or tec.`creator` like ?");
            searchSQL.append(" or tec.`lastmodifier` like ?");
            searchSQL.append(" or tec.`tcactive` like ?");
            searchSQL.append(" or tec.`status` like ?");
            searchSQL.append(" or tec.`group` like ?");
            searchSQL.append(" or tec.`priority` like ?");
            searchSQL.append(" or tec.`tcdatecrea` like ?");
            searchSQL.append(" or lab.`label` like ?");
            searchSQL.append(" or tec.`description` like ?)");
        }
        if (individualSearch != null && !individualSearch.isEmpty()) {
            searchSQL.append(" and ( 1=1 ");
            for (Map.Entry<String, List<String>> entry : individualSearch.entrySet()) {
                searchSQL.append(" and ");
                searchSQL.append(SqlUtil.getInSQLClauseForPreparedStatement(entry.getKey(), entry.getValue()));
                individalColumnSearchValues.addAll(entry.getValue());
            }
            searchSQL.append(" )");
        }
        query.append(searchSQL);
        query.append(" order by ").append(columnName).append(" asc");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        try (Connection connection = databaseSpring.connect();
                PreparedStatement preStat = connection.prepareStatement(query.toString())) {

            int i = 1;
            if (!StringUtil.isNullOrEmpty(system)) {
                preStat.setString(i++, system);
            }
            if (!StringUtil.isNullOrEmpty(test)) {
                preStat.setString(i++, test);
            }
            if (!Strings.isNullOrEmpty(searchTerm)) {
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
                preStat.setString(i++, "%" + searchTerm + "%");
            }
            for (String individualColumnSearchValue : individalColumnSearchValues) {
                preStat.setString(i++, individualColumnSearchValue);
            }

            ResultSet resultSet = preStat.executeQuery();

            //gets the data
            while (resultSet.next()) {
                distinctValues.add(resultSet.getString("distinctValues") == null ? "" : resultSet.getString("distinctValues"));
            }

            //get the total number of rows
            resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
            int nrTotalRows = 0;

            if (resultSet != null && resultSet.next()) {
                nrTotalRows = resultSet.getInt(1);
            }

            if (distinctValues.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                LOG.error("Partial Result in the query.");
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else if (distinctValues.size() <= 0) {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                answer = new AnswerList(distinctValues, nrTotalRows);
            } else {
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                answer = new AnswerList(distinctValues, nrTotalRows);
            }
        } catch (Exception e) {
            LOG.warn("Unable to execute query : " + e.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED).resolveDescription("DESCRIPTION",
                    e.toString());
        } finally {
            // We always set the result message
            answer.setResultMessage(msg);
        }

        answer.setResultMessage(msg);
        answer.setDataList(distinctValues);
        return answer;
    }

    @Override
    public Answer update(TCase tc) {
        MessageEvent msg = null;
        StringBuilder query = new StringBuilder("UPDATE testcase SET");

        query.append(" implementer = ?,");
        query.append(" lastmodifier = ?,");
        query.append(" project = ?,");
        query.append(" ticket = ?,");
        query.append(" application = ?,");
        query.append(" activeQA = ?,");
        query.append(" activeUAT = ?,");
        query.append(" activeProd = ?,");
        query.append(" status = ?,");
        query.append(" description = ?,");
        query.append(" behaviorOrValueExpected = ?,");
        query.append(" howTo = ?,");
        query.append(" tcactive = ?,");
        query.append(" fromBuild = ?,");
        query.append(" fromRev = ?,");
        query.append(" toBuild = ?,");
        query.append(" toRev = ?,");
        query.append(" bugId = ?,");
        query.append(" targetBuild = ?,");
        query.append(" targetRev = ?,");
        query.append(" comment = ?,");
        query.append(" function = ?,");
        query.append(" priority = ?,");
        query.append(" `group` = ?,");
        query.append(" `origine` = ?");
        query.append(" WHERE test = ? AND testcase = ?;");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            try {
                preStat.setString(1, tc.getImplementer());
                preStat.setString(2, tc.getLastModifier());
                preStat.setString(3, tc.getProject());
                preStat.setString(4, tc.getTicket());
                preStat.setString(5, tc.getApplication());
                preStat.setString(6, tc.getRunQA());
                preStat.setString(7, tc.getRunUAT());
                preStat.setString(8, tc.getRunPROD());
                preStat.setString(9, tc.getStatus());
                preStat.setString(10, tc.getShortDescription());
                preStat.setString(11, tc.getDescription());
                preStat.setString(12, tc.getHowTo());
                preStat.setString(13, tc.getActive());
                preStat.setString(14, tc.getFromSprint());
                preStat.setString(15, tc.getFromRevision());
                preStat.setString(16, tc.getToSprint());
                preStat.setString(17, tc.getToRevision());
                preStat.setString(18, tc.getBugID());
                preStat.setString(19, tc.getTargetSprint());
                preStat.setString(20, tc.getTargetRevision());
                preStat.setString(21, tc.getComment());
                preStat.setString(22, tc.getFunction());
                preStat.setString(23, Integer.toString(tc.getPriority()));
                preStat.setString(24, tc.getGroup());
                preStat.setString(25, tc.getOrigin());
                preStat.setString(26, tc.getTest());
                preStat.setString(27, tc.getTestCase());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "UPDATE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer create(TCase testCase) {
        MessageEvent msg = null;

        final StringBuffer sql = new StringBuffer("INSERT INTO `testcase` ")
                .append(" ( `Test`, `TestCase`, `Application`, `Project`, `Ticket`, ")
                .append("`Description`, `BehaviorOrValueExpected`, ")
                .append("`ChainNumberNeeded`, `Priority`, `Status`, `TcActive`, ")
                .append("`Group`, `Origine`, `RefOrigine`, `HowTo`, `Comment`, ")
                .append("`FromBuild`, `FromRev`, `ToBuild`, `ToRev`, ")
                .append("`BugID`, `TargetBuild`, `TargetRev`, `Creator`, ")
                .append("`Implementer`, `LastModifier`, `function`, `activeQA`, `activeUAT`, `activePROD`) ")
                .append("VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ")
                .append("?, ?, ?, ?, ?, ?, ?, ?, ?, ? ); ");

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + sql.toString());
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(sql.toString());
            try {
                preStat.setString(1, ParameterParserUtil.parseStringParam(testCase.getTest(), ""));
                preStat.setString(2, ParameterParserUtil.parseStringParam(testCase.getTestCase(), ""));
                preStat.setString(3, ParameterParserUtil.parseStringParam(testCase.getApplication(), ""));
                preStat.setString(4, testCase.getProject());
                preStat.setString(5, ParameterParserUtil.parseStringParam(testCase.getTicket(), ""));
                preStat.setString(6, ParameterParserUtil.parseStringParam(testCase.getShortDescription(), ""));
                preStat.setString(7, ParameterParserUtil.parseStringParam(testCase.getDescription(), ""));
                preStat.setString(8, null);
                preStat.setString(9, Integer.toString(testCase.getPriority()));
                preStat.setString(10, ParameterParserUtil.parseStringParam(testCase.getStatus(), ""));
                preStat.setString(11, testCase.getActive() != null && !testCase.getActive().equals("Y") ? "N" : "Y");
                preStat.setString(12, ParameterParserUtil.parseStringParam(testCase.getGroup(), ""));
                preStat.setString(13, ParameterParserUtil.parseStringParam(testCase.getOrigin(), ""));
                preStat.setString(14, ParameterParserUtil.parseStringParam(testCase.getRefOrigin(), ""));
                preStat.setString(15, ParameterParserUtil.parseStringParam(testCase.getHowTo(), ""));
                preStat.setString(16, ParameterParserUtil.parseStringParam(testCase.getComment(), ""));
                preStat.setString(17, ParameterParserUtil.parseStringParam(testCase.getFromSprint(), ""));
                preStat.setString(18, ParameterParserUtil.parseStringParam(testCase.getFromRevision(), ""));
                preStat.setString(19, ParameterParserUtil.parseStringParam(testCase.getToSprint(), ""));
                preStat.setString(20, ParameterParserUtil.parseStringParam(testCase.getToRevision(), ""));
                preStat.setString(21, ParameterParserUtil.parseStringParam(testCase.getBugID(), ""));
                preStat.setString(22, ParameterParserUtil.parseStringParam(testCase.getTargetSprint(), ""));
                preStat.setString(23, ParameterParserUtil.parseStringParam(testCase.getTargetRevision(), ""));
                preStat.setString(24, ParameterParserUtil.parseStringParam(testCase.getCreator(), ""));
                preStat.setString(25, ParameterParserUtil.parseStringParam(testCase.getImplementer(), ""));
                preStat.setString(26, ParameterParserUtil.parseStringParam(testCase.getLastModifier(), ""));
                preStat.setString(27, ParameterParserUtil.parseStringParam(testCase.getFunction(), ""));
                preStat.setString(28, testCase.getRunQA() != null && !testCase.getRunQA().equals("Y") ? "N" : "Y");
                preStat.setString(29, testCase.getRunUAT() != null && !testCase.getRunUAT().equals("Y") ? "N" : "Y");
                preStat.setString(30, testCase.getRunPROD() != null && !testCase.getRunPROD().equals("N") ? "Y" : "N");

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT"));

            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());

                if (exception.getSQLState().equals(SQL_DUPLICATED_CODE)) { //23000 is the sql state for duplicate entries
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_DUPLICATE);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "INSERT").replace("%REASON%", exception.toString()));
                } else {
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                    msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
                }
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }

    @Override
    public Answer delete(TCase testCase) {
        MessageEvent msg = null;
        final String query = "DELETE FROM testcase WHERE test = ? AND testcase = ?";

        // Debug message on SQL.
        if (LOG.isDebugEnabled()) {
            LOG.debug("SQL : " + query);
        }
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, testCase.getTest());
                preStat.setString(2, testCase.getTestCase());

                preStat.executeUpdate();
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "DELETE"));
            } catch (SQLException exception) {
                LOG.error("Unable to execute query : " + exception.toString());
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
                msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            LOG.error("Unable to execute query : " + exception.toString());
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
            msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", exception.toString()));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException exception) {
                LOG.warn("Unable to close connection : " + exception.toString());
            }
        }
        return new Answer(msg);
    }
}
