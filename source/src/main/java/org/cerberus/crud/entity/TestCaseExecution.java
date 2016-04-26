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

import org.cerberus.service.engine.testdata.TestDataLibResult;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cerberus.util.answer.AnswerList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author bcivel
 */
public class TestCaseExecution {

    private long id;
    private String test;
    private String testCase;
    private String build;
    private String revision;
    private String environment;
    private String country;
    private String browser;
    private String version;
    private String platform;
    private String browserFullVersion;
    private long start;
    private long end;
    private String controlStatus;
    private String controlMessage;
    private Application application;
    private String ip; // Host the Selenium IP
    private String url;
    private String port; // host the Selenium Port
    private String tag;
    private String finished;
    private int verbose;
    private String status;
    private String crbVersion;
    private String executor;
    private String screenSize;

    /**
     * From here are data outside database model.
     */
    private String environmentData;
    private Invariant environmentDataObj;
    private Invariant CountryObj;
    private int screenshot;
    private String outputFormat;
    private Test testObj;
    private TCase tCase;
    private List<TCase> PreTCase;
    private CountryEnvParam countryEnvParam;
    private CountryEnvironmentParameters countryEnvironmentParameters;
    private boolean manualURL;
    private String myHost;
    private String myContextRoot;
    private String myLoginRelativeURL;
    private String seleniumIP;
    private String seleniumPort;
    private List<TestCaseStepExecution> testCaseStepExecutionList; // Host the list of Steps that will be executed (both pre tests and main test)
    private List<TestCaseExecutionData> testCaseExecutionDataList; // Host the full list of data calculated during the execution.
    private HashMap<String, TestDataLibResult> dataLibraryExecutionDataList;
    private MessageGeneral resultMessage;
    private Selenium selenium;
    private String executionUUID;
    private Integer pageSource;
    private Integer seleniumLog;
    private Session session;
    private String manualExecution;
    private List<TestCaseCountryProperties> testCaseCountryPropertyList;
    private long idFromQueue;
    private Integer numberOfRetries;
    private String userAgent;
    private boolean synchroneous;
    private String timeout;
    private AnswerList testCaseStepExecutionAnswerList;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(Integer numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public void decreaseNumberOfRetries() {
        this.numberOfRetries--;
    }

    public long getIdFromQueue() {
        return idFromQueue;
    }

    public void setIdFromQueue(long idFromQueue) {
        this.idFromQueue = idFromQueue;
    }

    public List<TestCaseCountryProperties> getTestCaseCountryPropertyList() {
        return testCaseCountryPropertyList;
    }

    public void setTestCaseCountryPropertyList(List<TestCaseCountryProperties> testCaseCountryPropertyList) {
        this.testCaseCountryPropertyList = testCaseCountryPropertyList;
    }

    public String getManualExecution() {
        return manualExecution;
    }

    public void setManualExecution(String manualExecution) {
        this.manualExecution = manualExecution;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Integer getPageSource() {
        return pageSource;
    }

    public void setPageSource(Integer pageSource) {
        this.pageSource = pageSource;
    }

    public Integer getSeleniumLog() {
        return seleniumLog;
    }

    public void setSeleniumLog(Integer seleniumLog) {
        this.seleniumLog = seleniumLog;
    }

    public boolean isSynchroneous() {
        return synchroneous;
    }

    public void setSynchroneous(boolean synchroneous) {
        this.synchroneous = synchroneous;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getExecutionUUID() {
        return executionUUID;
    }

    public void setExecutionUUID(String executionUUID) {
        this.executionUUID = executionUUID;
    }

    public Selenium getSelenium() {
        return selenium;
    }

    public void setSelenium(Selenium selenium) {
        this.selenium = selenium;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Invariant getCountryObj() {
        return CountryObj;
    }

    public void setCountryObj(Invariant CountryObj) {
        this.CountryObj = CountryObj;
    }

    public Invariant getEnvironmentDataObj() {
        return environmentDataObj;
    }

    public void setEnvironmentDataObj(Invariant environmentDataObj) {
        this.environmentDataObj = environmentDataObj;
    }

    public String getEnvironmentData() {
        return environmentData;
    }

    public void setEnvironmentData(String environmentData) {
        this.environmentData = environmentData;
    }

    public boolean isManualURL() {
        return manualURL;
    }

    public void setManualURL(boolean manualURL) {
        this.manualURL = manualURL;
    }

    public String getMyHost() {
        return myHost;
    }

    public void setMyHost(String myHost) {
        this.myHost = myHost;
    }

    public String getMyContextRoot() {
        return myContextRoot;
    }

    public void setMyContextRoot(String myContextRoot) {
        this.myContextRoot = myContextRoot;
    }

    public String getMyLoginRelativeURL() {
        return myLoginRelativeURL;
    }

    public void setMyLoginRelativeURL(String myLoginRelativeURL) {
        this.myLoginRelativeURL = myLoginRelativeURL;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public int getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(int screenshot) {
        this.screenshot = screenshot;
    }

    public MessageGeneral getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(MessageGeneral resultMessage) {
        this.resultMessage = resultMessage;
        if (resultMessage != null) {
            this.setControlMessage(resultMessage.getDescription());
            this.setControlStatus(resultMessage.getCodeString());
        }
    }

    public List<TestCaseStepExecution> getTestCaseStepExecutionList() {
        return testCaseStepExecutionList;
    }

    public void setTestCaseStepExecutionList(List<TestCaseStepExecution> testCaseStepExecutionList) {
        this.testCaseStepExecutionList = testCaseStepExecutionList;
    }

    public String getSeleniumIP() {
        return seleniumIP;
    }

    public void setSeleniumIP(String seleniumIP) {
        this.seleniumIP = seleniumIP;
    }

    public String getSeleniumPort() {
        return seleniumPort;
    }

    public void setSeleniumPort(String seleniumPort) {
        this.seleniumPort = seleniumPort;
    }

    public CountryEnvParam getCountryEnvParam() {
        return countryEnvParam;
    }

    public void setCountryEnvParam(CountryEnvParam countryEnvParam) {
        this.countryEnvParam = countryEnvParam;
    }

    public CountryEnvironmentParameters getCountryEnvironmentParameters() {
        return countryEnvironmentParameters;
    }

    public void setCountryEnvironmentParameters(CountryEnvironmentParameters countryEnvironmentParameters) {
        this.countryEnvironmentParameters = countryEnvironmentParameters;
    }

    public Test getTestObj() {
        return testObj;
    }

    public void setTestObj(Test testObj) {
        this.testObj = testObj;
    }

    public TCase gettCase() {
        return tCase;
    }

    public void settCase(TCase tCase) {
        this.tCase = tCase;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getBrowserFullVersion() {
        return browserFullVersion;
    }

    public void setBrowserFullVersion(String browserFullVersion) {
        this.browserFullVersion = browserFullVersion;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getControlMessage() {
        return controlMessage;
    }

    public void setControlMessage(String controlMessage) {
        this.controlMessage = controlMessage;
    }

    public String getControlStatus() {
        return controlStatus;
    }

    public void setControlStatus(String controlStatus) {
        this.controlStatus = controlStatus;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCrbVersion() {
        return crbVersion;
    }

    public void setCrbVersion(String crbVersion) {
        this.crbVersion = crbVersion;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getFinished() {
        return finished;
    }

    public void setFinished(String finished) {
        this.finished = finished;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public List<TCase> getPreTCase() {
        return PreTCase;
    }

    public void setPreTCase(List<TCase> PreTCase) {
        this.PreTCase = PreTCase;
    }

    public List<TestCaseExecutionData> getTestCaseExecutionDataList() {
        return testCaseExecutionDataList;
    }

    public void setTestCaseExecutionDataList(List<TestCaseExecutionData> testCaseExecutionDataList) {
        this.testCaseExecutionDataList = testCaseExecutionDataList;
    }

    public HashMap<String, TestDataLibResult> getDataLibraryExecutionDataList() {
        return dataLibraryExecutionDataList;
    }

    public void setDataLibraryExecutionDataList(HashMap<String, TestDataLibResult> dataLibraryExecutionDataList) {
        this.dataLibraryExecutionDataList = dataLibraryExecutionDataList;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }
    
    public void setTestCaseStepExecutionList(AnswerList testCaseStepExecutionAnswerList) {
        this.testCaseStepExecutionAnswerList = testCaseStepExecutionAnswerList;
    }
    
    public AnswerList getTestCaseStepExecutionAnswerList(){
        return testCaseStepExecutionAnswerList;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        try {
            result.append("id", this.getId());
            result.append("test", this.getTest());
            result.append("testcase", this.getTestCase());
            result.append("build", this.getBuild());
            result.append("revision", this.getRevision());
            result.append("environment", this.getEnvironment());
            result.append("country", this.getCountry());
            result.append("browser", this.getBrowser());
            result.append("version", this.getVersion());
            result.append("platform", this.getPlatform());
            result.append("browserFullVersion", this.getBrowserFullVersion());
            result.append("start", this.getStart());
            result.append("end", this.getEnd());
            result.append("controlStatus", this.getControlStatus());
            result.append("controlMessage", this.getControlMessage());
            result.append("application", this.getApplication());
            result.append("ip", this.getIp());
            result.append("url", this.getUrl());
            result.append("port", this.getPort());
            result.append("tag", this.getTag());
            result.append("finished", this.getFinished());
            result.append("verbose", this.getVerbose());
            result.append("status", this.getStatus());
            result.append("crbVersion", this.getCrbVersion());
            result.append("executor", this.getExecutor());
            result.append("screenSize", this.getScreenSize());
            JSONArray array = new JSONArray();
            for (Object testCaseStepExecution : this.getTestCaseStepExecutionAnswerList().getDataList()) {
                array.put( ((TestCaseStepExecution)testCaseStepExecution).toJson());
            }
            result.append("testCaseStepExecutionList", array);
        } catch (JSONException ex) {
            Logger.getLogger(TestCaseExecution.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
