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
package org.cerberus.service.executor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Level;
import org.cerberus.crud.entity.ExecutionThreadPool;
import org.cerberus.crud.entity.TestCaseExecutionInQueue;
import org.cerberus.crud.service.IParameterService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.cerberus.exception.CerberusException;
import org.cerberus.log.MyLogger;
import org.cerberus.servlet.zzpublic.RunTestCase;
import org.cerberus.util.ParamRequestMaker;
import org.cerberus.util.ParameterParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author bcivel
 */
@Service
public class ExecutionThreadPoolService {

    @Autowired
    ITestCaseExecutionInQueueService tceiqService;
    @Autowired
    ExecutionThreadPool threadPool;
    @Autowired
    IParameterService parameterService;

    @Async
    public void putExecutionInQueue(String url, String tag) throws CerberusException, InterruptedException {
        ExecutionWorkerThread task = new ExecutionWorkerThread();
        task.setExecutionUrl(url);
        task.setExecThreadPool(threadPool);
        task.setTag(tag);
        try {
            Future<?> future = threadPool.getExecutor().submit(task);
            threadPool.increment(tag, future);
            task.setFuture(future);
        } catch (RejectedExecutionException e) {
            System.out.println("RejectedExecutionException :"+e);
        }
    }


    public void searchExecutionInQueueTableAndTriggerExecution() throws CerberusException, UnsupportedEncodingException, InterruptedException {

        try {
            /**
             * Verify if numberOfPool to use has been initialized. If not
             * initialize.
             */
            if (!threadPool.isNumberOfPoolInitialized()) {
                threadPool.setNumberOfPool(Integer.valueOf(parameterService.findParameterByKey("cerberus_execution_threadpool_size", "").getValue()));
                threadPool.setNumberOfPoolInitialized(true);
            }
            /**
             * Find all testCase in Queue not Procedeed
             */
            List<TestCaseExecutionInQueue> tceiqList = tceiqService.findAllNotProcedeed();
            if (null != tceiqList && !tceiqList.isEmpty()) {
                /**
                 * Generate the URL for the execution
                 */
                String host = parameterService.findParameterByKey("cerberus_url", "").getValue();
                host += "/RunTestCase?";
                for (TestCaseExecutionInQueue tceiq : tceiqList) {
                    ParamRequestMaker paramRequestMaker = makeParamRequestfromLastInQueue(tceiq);
                    String uri = paramRequestMaker.mkString().replace(" ", "+");
                    String query = host + uri;
                    this.putExecutionInQueue(query, tceiq.getTag());
                }
            }

        } catch (CerberusException ex) {
            MyLogger.log(ExecutionThreadPoolService.class.getName(), Level.INFO, ex.toString());
        }

    }

    public static ParamRequestMaker makeParamRequestfromLastInQueue(TestCaseExecutionInQueue lastInQueue) {
        ParamRequestMaker paramRequestMaker = new ParamRequestMaker();
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST, lastInQueue.getTest());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TEST_CASE, lastInQueue.getTestCase());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_COUNTRY, lastInQueue.getCountry());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ENVIRONMENT, lastInQueue.getEnvironment());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT, lastInQueue.getRobot());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_IP, lastInQueue.getRobotIP());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_ROBOT_PORT, lastInQueue.getRobotPort());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER, lastInQueue.getBrowser());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_BROWSER_VERSION, lastInQueue.getBrowserVersion());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_PLATFORM, lastInQueue.getPlatform());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_URL, lastInQueue.isManualURL() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE : null);
        if (lastInQueue.isManualURL()) {
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_HOST, lastInQueue.getManualHost());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_CONTEXT_ROOT, lastInQueue.getManualContextRoot());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_LOGIN_RELATIVE_URL, lastInQueue.getManualLoginRelativeURL());
            paramRequestMaker.addParam(RunTestCase.PARAMETER_MANUAL_ENV_DATA, lastInQueue.getManualEnvData());
        }
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TAG, lastInQueue.getTag());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_OUTPUT_FORMAT, lastInQueue.getOutputFormat());
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SCREENSHOT, Integer.toString(lastInQueue.getScreenshot()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_VERBOSE, Integer.toString(lastInQueue.getVerbose()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_TIMEOUT, Long.toString(lastInQueue.getTimeout()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SYNCHRONEOUS, lastInQueue.isSynchroneous() ? ParameterParserUtil.DEFAULT_BOOLEAN_TRUE_VALUE
                : ParameterParserUtil.DEFAULT_BOOLEAN_FALSE_VALUE);
        paramRequestMaker.addParam(RunTestCase.PARAMETER_PAGE_SOURCE, Integer.toString(lastInQueue.getPageSource()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_SELENIUM_LOG, Integer.toString(lastInQueue.getSeleniumLog()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_EXECUTION_QUEUE_ID, Long.toString(lastInQueue.getId()));
        paramRequestMaker.addParam(RunTestCase.PARAMETER_NUMBER_OF_RETRIES, Long.toString(lastInQueue.getRetries()));
        return paramRequestMaker;
    }

}
