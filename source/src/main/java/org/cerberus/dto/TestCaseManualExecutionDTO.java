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
package org.cerberus.dto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.TCase;
import org.cerberus.log.MyLogger;
import org.cerberus.util.DateUtil;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 22/11/2013
 * @since 0.9.1
 */
@Repository
public class TestCaseManualExecutionDTO implements ITestCaseManualExecutionDTO {

    /**
     * Class used to manage connection.
     *
     * @see org.cerberus.database.DatabaseSpring
     */
    @Autowired
    private DatabaseSpring databaseSpring;

    @Override
    public List<TestCaseManualExecution> findTestCaseManualExecution(TCase testCase, String text, String system, String country, String env, String campaign, String battery) {
        List<TestCaseManualExecution> list = null;
        final String query = "SELECT tc.test, tc.testcase, tc.behaviororvalueexpected, tc.howto, tcc.country, a.application, a.system, a.type, CONCAT( CONCAT( cep.ip , cep.url ), cep.urllogin ) AS url, cenvp.build, cenvp.revision, tce.controlstatus, tce.end, tce.id, tce.build as lastbuild, tce.revision as lastrevision " +
                ", tce.start FROM testcase tc " +
                "  JOIN testcasecountry tcc " +
                "    ON (tc.test = tcc.test AND tc.testcase=tcc.testcase) " +
                "  JOIN testbatterycontent tbc " +
                "    ON (tbc.test = tc.test AND tbc.testcase = tc.testcase) " +
                "  JOIN campaigncontent cc " +
                "    ON (cc.testbattery = tbc.testbattery) " +
                "  JOIN application a " +
                "    ON (a.application=tc.application) " +
                "  JOIN countryenvironmentparameters cep " +
                "    ON (cep.system=a.system AND cep.country=tcc.country AND cep.application=a.application) " +
                "  JOIN countryenvparam cenvp " +
                "    ON (cenvp.system=a.system AND cenvp.country=tcc.country AND cenvp.environment=cep.environment) " +
                "  LEFT JOIN testcaseexecution tce " +
                "    ON tce.id = (SELECT max(id) FROM testcaseexecution ttce WHERE ttce.test=tc.test AND ttce.testcase=tc.testcase AND ttce.environment=cep.environment AND ttce.country=tcc.country) " +
                "WHERE tc.tcactive='Y' AND tcc.country=? AND cep.environment=? AND " +
                "tc.test LIKE ? AND tc.project LIKE ? AND tc.ticket LIKE ? AND tc.bugid LIKE ? AND tc.origine LIKE ? " +
                "AND tc.creator LIKE ? AND a.system LIKE ? AND tc.application LIKE ? AND tc.priority LIKE ? " +
                "AND tc.status LIKE ? AND tc.activePROD LIKE ? AND tc.activeUAT LIKE ? AND tc.activeQA LIKE ? AND " +
                "( tc.description LIKE ? OR tc.howto LIKE ? OR tc.behaviororvalueexpected LIKE ? OR tc.comment LIKE ?) " +
                "AND tc.frombuild LIKE ? AND tc.fromrev LIKE ? AND tc.tobuild LIKE ? " +
                "AND tc.torev LIKE ? AND tc.targetbuild LIKE ? AND tc.targetrev LIKE ? AND tc.testcase LIKE ? AND tc.group LIKE ? AND cc.campaign like ? AND tbc.testbattery like ? ";

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                preStat.setString(1, country);
                preStat.setString(2, env);
                preStat.setString(3, ParameterParserUtil.wildcardIfEmpty(testCase.getTest()));
                preStat.setString(4, ParameterParserUtil.wildcardIfEmpty(testCase.getProject()));
                preStat.setString(5, ParameterParserUtil.wildcardIfEmpty(testCase.getTicket()));
                preStat.setString(6, ParameterParserUtil.wildcardIfEmpty(testCase.getBugID()));
                preStat.setString(7, ParameterParserUtil.wildcardIfEmpty(testCase.getOrigin()));
                preStat.setString(8, ParameterParserUtil.wildcardIfEmpty(testCase.getCreator()));
                preStat.setString(9, ParameterParserUtil.wildcardIfEmpty(system));
                preStat.setString(10, ParameterParserUtil.wildcardIfEmpty(testCase.getApplication()));
                if (testCase.getPriority() != -1) {
                    preStat.setInt(11, testCase.getPriority());
                } else {
                    preStat.setString(11, "%");
                }
                preStat.setString(12, ParameterParserUtil.wildcardIfEmpty(testCase.getStatus()));
                preStat.setString(13, ParameterParserUtil.wildcardIfEmpty(testCase.getRunPROD()));
                preStat.setString(14, ParameterParserUtil.wildcardIfEmpty(testCase.getRunUAT()));
                preStat.setString(15, ParameterParserUtil.wildcardIfEmpty(testCase.getRunQA()));
                if (text != null && !text.equalsIgnoreCase("")) {
                    preStat.setString(16, text);
                    preStat.setString(17, text);
                    preStat.setString(18, text);
                    preStat.setString(19, text);
                } else {
                    preStat.setString(16, "%");
                    preStat.setString(17, "%");
                    preStat.setString(18, "%");
                    preStat.setString(19, "%");
                }
                preStat.setString(20, ParameterParserUtil.wildcardIfEmpty(testCase.getFromSprint()));
                preStat.setString(21, ParameterParserUtil.wildcardIfEmpty(testCase.getFromRevision()));
                preStat.setString(22, ParameterParserUtil.wildcardIfEmpty(testCase.getToSprint()));
                preStat.setString(23, ParameterParserUtil.wildcardIfEmpty(testCase.getToRevision()));
                preStat.setString(24, ParameterParserUtil.wildcardIfEmpty(testCase.getTargetSprint()));
                preStat.setString(25, ParameterParserUtil.wildcardIfEmpty(testCase.getTargetRevision()));
                preStat.setString(26, ParameterParserUtil.wildcardIfEmpty(testCase.getTestCase()));
                preStat.setString(27, ParameterParserUtil.wildcardIfEmpty(testCase.getGroup()));
                preStat.setString(28, ParameterParserUtil.wildcardIfEmpty(campaign));
                preStat.setString(29, ParameterParserUtil.wildcardIfEmpty(battery));

                ResultSet resultSet = preStat.executeQuery();
                list = new ArrayList<TestCaseManualExecution>();
                try {
                    while (resultSet.next()) {
                        TestCaseManualExecution tcme = new TestCaseManualExecution();
                        tcme.setTest(resultSet.getString("test"));
                        tcme.setTestCase(resultSet.getString("testcase"));
                        tcme.setValueExpected(resultSet.getString("behaviororvalueexpected"));
                        tcme.setHowTo(resultSet.getString("howto"));
                        tcme.setApplication(resultSet.getString("application"));
                        tcme.setAppType(resultSet.getString("type"));
                        if (resultSet.getString("url") != null) {
                            tcme.setUrl(resultSet.getString("url").replace("//", "/"));
                        }
                        tcme.setSystem(resultSet.getString("system"));
                        tcme.setBuild(resultSet.getString("build"));
                        tcme.setRevision(resultSet.getString("revision"));
                        tcme.setLastStatus(resultSet.getString("controlstatus"));
                        if (resultSet.getTimestamp("end") != null) {
                            DateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT_DISPLAY);
                            tcme.setLastStatusDate(df.format(resultSet.getTimestamp("end")));
                        }
                        tcme.setLastStatusID(resultSet.getLong("ID"));
                        tcme.setLastStatusBuild(resultSet.getString("lastbuild"));
                        tcme.setLastStatusRevision(resultSet.getString("lastrevision"));
                        list.add(tcme);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(TestCaseManualExecutionDTO.class.getName(), Level.ERROR, exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(TestCaseManualExecutionDTO.class.getName(), Level.ERROR, exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(TestCaseManualExecutionDTO.class.getName(), Level.ERROR, exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(TestCaseManualExecutionDTO.class.getName(), Level.WARN, e.toString());
            }
        }
        return list;
    }
}
