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
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.dao.ICampaignParameterDAO;
import org.cerberus.database.DatabaseSpring;
import org.cerberus.crud.entity.CampaignParameter;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.MessageGeneral;
import org.cerberus.enums.MessageGeneralEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.factory.IFactoryCampaignParameter;
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
public class CampaignParameterDAO implements ICampaignParameterDAO {

    @Autowired
    private DatabaseSpring databaseSpring;
    @Autowired
    private IFactoryCampaignParameter factoryCampaignParameter;
    
    private static final Logger LOG = Logger.getLogger(CampaignParameterDAO.class);

    private final String OBJECT_NAME = "CampaignParameter";
    private final String SQL_DUPLICATED_CODE = "23000";
    private final int MAX_ROW_SELECTED = 100000;

    @Override
    public List<CampaignParameter> findAll() throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c";

        List<CampaignParameter> campaignParameterList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParameterList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParameterList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParameterList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterList;
    }

    @Override
    public CampaignParameter findCampaignParameterByKey(Integer campaignparameterID) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c WHERE c.campaignparameterID = ?";

        CampaignParameter campaignParameterResult = null;
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setInt(1, campaignparameterID);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    if (resultSet.first()) {
                        campaignParameterResult = this.loadFromResultSet(resultSet);
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterResult;
    }

    @Override
    public List<CampaignParameter> findCampaignParametersByCampaign(String campaign) throws CerberusException {
        boolean throwEx = false;
        final String query = "SELECT * FROM campaignparameter c WHERE c.campaign = ?";

        List<CampaignParameter> campaignParameterList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query);
            preStat.setString(1, campaign);
            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParameterList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParameterList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParameterList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParameterList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParameterList;
    }

    @Override
    public boolean updateCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("UPDATE `campaignparameter` SET campaign=?, `Parameter`=?, `Value`=? WHERE campaignparameterID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());
            preStat.setInt(4, campaignParameter.getCampaignparameterID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public boolean createCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("INSERT INTO `campaignparameter` (`campaign`, `Parameter`, `Value`) VALUES (?, ?, ?);");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setString(1, campaignParameter.getCampaign());
            preStat.setString(2, campaignParameter.getParameter());
            preStat.setString(3, campaignParameter.getValue());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public List<CampaignParameter> findCampaignParameterByCriteria(Integer campaignparameterID, String campaign, String parameter, String value) throws CerberusException {
        boolean throwEx = false;
        final StringBuilder query = new StringBuilder("SELECT * FROM campaignparameter c WHERE 1=1 ");

        if (campaignparameterID != null) {
            query.append(" AND c.campaignparameterID = ?");
        }
        if (campaign != null && !"".equals(campaign.trim())) {
            query.append(" AND c.campaign LIKE ?");
        }
        if (parameter != null && !"".equals(parameter.trim())) {
            query.append(" AND c.parameter LIKE ?");
        }

        if (value != null && !"".equals(value.trim())) {
            query.append(" AND c.value LIKE ?");
        }

        // " c.campaignID = ? AND c.campaign LIKE ? AND c.description LIKE ?";
        List<CampaignParameter> campaignParametersList = new ArrayList<CampaignParameter>();
        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            int index = 1;
            if (campaignparameterID != null) {
                preStat.setInt(index, campaignparameterID);
                index++;
            }
            if (campaign != null && !"".equals(campaign.trim())) {
                preStat.setString(index, "%" + campaign.trim() + "%");
                index++;
            }
            if (parameter != null && !"".equals(parameter.trim())) {
                preStat.setString(index, "%" + parameter.trim() + "%");
                index++;
            }
            if (value != null && !"".equals(value.trim())) {
                preStat.setString(index, "%" + value.trim() + "%");
                index++;
            }

            try {
                ResultSet resultSet = preStat.executeQuery();
                try {
                    while (resultSet.next()) {
                        campaignParametersList.add(this.loadFromResultSet(resultSet));
                    }
                } catch (SQLException exception) {
                    MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                    campaignParametersList = null;
                } finally {
                    resultSet.close();
                }
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
                campaignParametersList = null;
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            campaignParametersList = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        if (throwEx) {
            throw new CerberusException(new MessageGeneral(MessageGeneralEnum.NO_DATA_FOUND));
        }
        return campaignParametersList;
    }

    @Override
    public boolean deleteCampaignParameter(CampaignParameter campaignParameter) {
        final StringBuilder query = new StringBuilder("DELETE FROM `campaignparameter` WHERE campaignparameterID=?");

        Connection connection = this.databaseSpring.connect();
        try {
            PreparedStatement preStat = connection.prepareStatement(query.toString());
            preStat.setInt(1, campaignParameter.getCampaignparameterID());

            try {
                return (preStat.executeUpdate() == 1);
            } catch (SQLException exception) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
            } finally {
                preStat.close();
            }
        } catch (SQLException exception) {
            MyLogger.log(CampaignParameterDAO.class.getName(), Level.ERROR, "Unable to execute query : " + exception.toString());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                MyLogger.log(CampaignParameterDAO.class.getName(), Level.WARN, e.toString());
            }
        }
        return false;
    }

    @Override
    public AnswerList readByCampaignByCriteria(String campaign, int start, int amount, String column, String dir, String searchTerm, String individualSearch) {
        AnswerList response = new AnswerList();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        List<CampaignParameter> campaignParameterList = new ArrayList<CampaignParameter>();
        StringBuilder searchSQL = new StringBuilder();

        StringBuilder query = new StringBuilder();
        //SQL_CALC_FOUND_ROWS allows to retrieve the total number of columns by disrearding the limit clauses that 
        //were applied -- used for pagination p
        query.append("SELECT SQL_CALC_FOUND_ROWS * FROM campaignparameter ");

        searchSQL.append(" where 1=1 ");

        if (!StringUtil.isNullOrEmpty(searchTerm)) {
            searchSQL.append(" and (`campaignparameterid` like ?");
            searchSQL.append(" or `campaign` like ?");
            searchSQL.append(" or `parameter` like ?");
            searchSQL.append(" or `value` like ?)");
        }
        if (!StringUtil.isNullOrEmpty(individualSearch)) {
            searchSQL.append(" and (`?`)");
        }
        if (!StringUtil.isNullOrEmpty(campaign)) {
            searchSQL.append(" and (`campaign` = ? )");
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
                if (!StringUtil.isNullOrEmpty(campaign)) {
                    preStat.setString(i++, campaign);
                }
                ResultSet resultSet = preStat.executeQuery();
                try {
                    //gets the data
                    while (resultSet.next()) {
                        campaignParameterList.add(this.loadFromResultSet(resultSet));
                    }

                    //get the total number of rows
                    resultSet = preStat.executeQuery("SELECT FOUND_ROWS()");
                    int nrTotalRows = 0;

                    if (resultSet != null && resultSet.next()) {
                        nrTotalRows = resultSet.getInt(1);
                    }

                    if (campaignParameterList.size() >= MAX_ROW_SELECTED) { // Result of SQl was limited by MAX_ROW_SELECTED constrain. That means that we may miss some lines in the resultList.
                        LOG.error("Partial Result in the query.");
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_WARNING_PARTIAL_RESULT);
                        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", "Maximum row reached : " + MAX_ROW_SELECTED));
                        response = new AnswerList(campaignParameterList, nrTotalRows);
                    } else if (campaignParameterList.size() <= 0) {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_NO_DATA_FOUND);
                        response = new AnswerList(campaignParameterList, nrTotalRows);
                    } else {
                        msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_OK);
                        msg.setDescription(msg.getDescription().replace("%ITEM%", OBJECT_NAME).replace("%OPERATION%", "SELECT"));
                        response = new AnswerList(campaignParameterList, nrTotalRows);
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
        response.setDataList(campaignParameterList);
        return response;
    }
    
    private CampaignParameter loadFromResultSet(ResultSet rs) throws SQLException {
        Integer campaignparameterID = ParameterParserUtil.parseIntegerParam(rs.getString("campaignparameterID"), -1);
        String campaign = ParameterParserUtil.parseStringParam(rs.getString("campaign"), "");
        String parameter = ParameterParserUtil.parseStringParam(rs.getString("Parameter"), "");
        String value = ParameterParserUtil.parseStringParam(rs.getString("Value"), "");

        return factoryCampaignParameter.create(campaignparameterID, campaign, parameter, value);
    }
}
