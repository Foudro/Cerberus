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
package org.cerberus.service.engine.impl;

import org.apache.log4j.Level;
import org.cerberus.crud.entity.ExecutionSOAPResponse;
import org.cerberus.crud.entity.ExecutionUUID;
import org.cerberus.crud.entity.TestCaseExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IExecutionRunService;
import org.cerberus.service.engine.IExecutionStartService;
import org.cerberus.service.engine.IRunTestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {Insert class description here}
 *
 * @author Tiago Bernardes
 * @version 1.0, 23/01/2013
 * @since 2.0.0
 */
@Service
public class RunTestCaseService implements IRunTestCaseService {

    @Autowired
    private IExecutionStartService executionStartService;
    @Autowired
    private IExecutionRunService executionRunService;
    @Autowired
    private ExecutionUUID executionUUID;
    @Autowired
    private ExecutionSOAPResponse eSResponse;

    @Override
    public TestCaseExecution runTestCase(TestCaseExecution tCExecution) {

        /**
         * Start Execution (Checks and Creation of ID)
         *
         */
        try {
            //TODO:FN debug messages to be removed
            org.apache.log4j.Logger.getLogger(ExecutionStartService.class.getName()).log(org.apache.log4j.Level.DEBUG, "[DEBUG] START " + "__ID=" + tCExecution.getId());
            tCExecution = executionStartService.startExecution(tCExecution);
            MyLogger.log(ExecutionStartService.class.getName(), Level.INFO, "Execution Started : UUID=" + tCExecution.getExecutionUUID() + "__ID=" + tCExecution.getId());

        } catch (CerberusException ex) {
            tCExecution.setResultMessage(ex.getMessageError());
            MyLogger.log(RunTestCaseService.class.getName(), Level.INFO, "Execution not Launched : UUID=" + tCExecution.getExecutionUUID() + "__causedBy=" + ex.getMessageError().getDescription());
            return tCExecution;
        }

        /**
         * Execute TestCase in new thread if asynchroneous execution
         */
        if (tCExecution.getId() != 0) {
            try {
                // MyLogger.log(ExecutionStartService.class.getName(), Level.INFO, "to remove");
//                if (!tCExecution.getManualExecution().equals("Y")) {
                    if (!tCExecution.isSynchroneous()) {
                        executionRunService.executeAsynchroneouslyTestCase(tCExecution);
                    } else {
                        tCExecution = executionRunService.executeTestCase(tCExecution);
                    }
//                } else {
//                
//                }
            } catch (CerberusException ex) {
                tCExecution.setResultMessage(ex.getMessageError());
            }
        }
        /**
         * Return tcexecution object
         */
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Exit RunTestCaseService : " + tCExecution.getId());
        return tCExecution;
    }
}
