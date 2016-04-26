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
package org.cerberus.servlet.crud.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TCase;
import org.cerberus.crud.entity.TestCaseCountry;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestCaseStep;
import org.cerberus.crud.entity.TestCaseStepAction;
import org.cerberus.crud.entity.TestCaseStepActionControl;
import org.cerberus.crud.factory.IFactoryTCase;
import org.cerberus.crud.factory.IFactoryTestCaseCountry;
import org.cerberus.crud.factory.IFactoryTestCaseCountryProperties;
import org.cerberus.crud.factory.IFactoryTestCaseStep;
import org.cerberus.crud.factory.IFactoryTestCaseStepAction;
import org.cerberus.crud.factory.IFactoryTestCaseStepActionControl;
import org.cerberus.crud.service.IGroupService;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestCaseCountryService;
import org.cerberus.crud.service.ITestCaseService;
import org.cerberus.crud.service.ITestCaseStepActionControlService;
import org.cerberus.crud.service.ITestCaseStepActionService;
import org.cerberus.crud.service.ITestCaseStepService;
import org.cerberus.crud.service.ITestService;
import org.cerberus.crud.service.IUserService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.exception.CerberusException;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.servlet.ServletUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.HtmlUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "UpdateTestCaseWithDependencies1", urlPatterns = {"/UpdateTestCaseWithDependencies1"})
public class UpdateTestCaseWithDependencies1 extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.cerberus.exception.CerberusException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, CerberusException, JSONException {

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        response.setContentType("application/json");

        // Calling Servlet Transversal Util.
        ServletUtil.servletStart(request);

        /**
         * Parsing and securing all required parameters.
         */
        String initialTest = request.getParameter("informationInitialTest");
        String initialTestCase = request.getParameter("informationInitialTestCase");
        String test = request.getParameter("informationTest");
        String testCase = request.getParameter("informationTestCase");
        boolean duplicate = false;

        /**
         * Checking all constrains before calling the services.
         */
        if (StringUtil.isNullOrEmpty(test) || StringUtil.isNullOrEmpty(testCase)) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Case")
                    .replace("%OPERATION%", "Update")
                    .replace("%REASON%", "mendatory fields are missing."));
            ans.setResultMessage(msg);
        } else {
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestCaseService testCaseService = appContext.getBean(ITestCaseService.class);
            ITestCaseCountryPropertiesService tccpService = appContext.getBean(ITestCaseCountryPropertiesService.class);
            ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
            ITestCaseStepActionService tcsaService = appContext.getBean(ITestCaseStepActionService.class);
            ITestCaseStepActionControlService tcsacService = appContext.getBean(ITestCaseStepActionControlService.class);

            AnswerItem resp = testCaseService.readByKey(test, testCase);
            TCase tc = (TCase) resp.getItem();
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()) && resp.getItem()!=null)) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                        .replace("%OPERATION%", "Update")
                        .replace("%REASON%", "TestCase does not exist."));
                ans.setResultMessage(msg);

            } else /**
             * The service was able to perform the query and confirm the object
             * exist, then we can update it.
             */
             if (!request.isUserInRole("Test")) { // We cannot update the testcase if the user is not at least in Test role.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. You mut belong to Test Privilege."));
                    ans.setResultMessage(msg);

                } else if ((tc.getStatus().equalsIgnoreCase("WORKING")) && !(request.isUserInRole("TestAdmin"))) { // If Test Case is WORKING we need TestAdmin priviliges.
                    msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                    msg.setDescription(msg.getDescription().replace("%ITEM%", "TestCase")
                            .replace("%OPERATION%", "Update")
                            .replace("%REASON%", "Not enought privilege to update the testcase. The test case is in WORKING status and needs TestAdmin privilige to be updated"));
                    ans.setResultMessage(msg);

                } else {

                    // Test Case exist and we can update it so Global update start here //
                    /**
                     * For the list of testcase country verify it exists. If it
                     * does not exists > create it If it exist, verify if it's
                     * the
                     */
                    List<TestCaseCountryProperties> tccpFromPage = getTestCaseCountryPropertiesFromParameter(request, appContext, test, testCase);
                    List<TestCaseCountryProperties> tccpFromDtb = tccpService.findListOfPropertyPerTestTestCase(initialTest, initialTestCase);

                    /**
                     * Iterate on (TestCaseCountryProperties From Page -
                     * TestCaseCountryProperties From Database) If
                     * TestCaseCountryProperties in Database has same key :
                     * Update and remove from the list. If
                     * TestCaseCountryProperties in database does ot exist :
                     * Insert it.
                     */
                    List<TestCaseCountryProperties> tccpToUpdateOrInsert = new ArrayList(tccpFromPage);
                    tccpToUpdateOrInsert.removeAll(tccpFromDtb);
                    List<TestCaseCountryProperties> tccpToUpdateOrInsertToIterate = new ArrayList(tccpToUpdateOrInsert);

                    for (TestCaseCountryProperties tccpDifference : tccpToUpdateOrInsertToIterate) {
                        for (TestCaseCountryProperties tccpInDatabase : tccpFromDtb) {
                            if (tccpDifference.hasSameKey(tccpInDatabase)) {
                                tccpService.updateTestCaseCountryProperties(tccpDifference);
                                tccpToUpdateOrInsert.remove(tccpDifference);
                            }
                        }
                    }
                    tccpService.insertListTestCaseCountryProperties(tccpToUpdateOrInsert);

                    /**
                     * Iterate on (TestCaseCountryProperties From Database -
                     * TestCaseCountryProperties From Page). If
                     * TestCaseCountryProperties in Page has same key : remove
                     * from the list. Then delete the list of
                     * TestCaseCountryProperties
                     */
                    if (!duplicate) {
                        List<TestCaseCountryProperties> tccpToDelete = new ArrayList(tccpFromDtb);
                        tccpToDelete.removeAll(tccpFromPage);
                        List<TestCaseCountryProperties> tccpToDeleteToIterate = new ArrayList(tccpToDelete);

                        for (TestCaseCountryProperties tccpDifference : tccpToDeleteToIterate) {
                            for (TestCaseCountryProperties tccpInPage : tccpFromPage) {
                                if (tccpDifference.hasSameKey(tccpInPage)) {
                                    tccpToDelete.remove(tccpDifference);
                                }
                            }
                        }
                        tccpService.deleteListTestCaseCountryProperties(tccpToDelete);
                    }
                    /**
                     * For the list of testcasestep verify it exists. If it does
                     * not exists > create it If it exist, verify if it's the
                     */
                    List<TestCaseStep> tcsFromPage = getTestCaseStepFromParameter(request, appContext, test, testCase, duplicate);
                    List<TestCaseStepAction> tcsaFromPage = new ArrayList();
                    List<TestCaseStepActionControl> tcsacFromPage = new ArrayList();

                    for (TestCaseStep tcsL : tcsFromPage) {
                        if (tcsL.getTestCaseStepAction() != null) {
                            tcsaFromPage.addAll(tcsL.getTestCaseStepAction());
                            for (TestCaseStepAction tcsaL : tcsL.getTestCaseStepAction()) {
                                tcsacFromPage.addAll(tcsaL.getTestCaseStepActionControl());
                            }
                        }
                    }

                    List<TestCaseStep> tcsFromDtb = new ArrayList(tcsService.getListOfSteps(initialTest, initialTestCase));
                    tcsService.compareListAndUpdateInsertDeleteElements(tcsFromPage, tcsFromDtb, duplicate);

                    List<TestCaseStepAction> tcsaFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(initialTest, initialTestCase));
                    tcsaService.compareListAndUpdateInsertDeleteElements(tcsaFromPage, tcsaFromDtb, duplicate);

                    List<TestCaseStepActionControl> tcsacFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(initialTest, initialTestCase));
                    tcsacService.compareListAndUpdateInsertDeleteElements(tcsacFromPage, tcsacFromDtb, duplicate);

                    List<TestCaseStep> tcsNewFromPage = new ArrayList();
                    List<TestCaseStepAction> tcsaNewFromPage = new ArrayList();
                    List<TestCaseStepActionControl> tcsacNewFromPage = new ArrayList();
                    List<TestCaseStep> tcsNewFromDtb = new ArrayList();
                    List<TestCaseStepAction> tcsaNewFromDtb = new ArrayList();
                    List<TestCaseStepActionControl> tcsacNewFromDtb = new ArrayList();

                    tcsNewFromDtb = tcsService.getListOfSteps(test, testCase);
                    int incrementStep = 0;
                    for (TestCaseStep tcsNew : tcsNewFromDtb) {
                        if (tcsService.getTestCaseStepUsingStepInParamter(test, testCase, tcsNew.getStep()).isEmpty()) {
                            tcsNew.setIsStepInUseByOtherTestCase(false);
                        } else {
                            tcsNew.setIsStepInUseByOtherTestCase(true);
                        }
                        incrementStep++;
                        tcsaNewFromDtb = tcsaService.getListOfAction(test, testCase, tcsNew.getStep());
                        int incrementAction = 0;
                        for (TestCaseStepAction tcsaNew : tcsaNewFromDtb) {
                            incrementAction++;
                            tcsacNewFromDtb = tcsacService.findControlByTestTestCaseStepSequence(test, testCase, tcsaNew.getStep(), tcsaNew.getSequence());
                            int incrementControl = 0;
                            for (TestCaseStepActionControl tcsacNew : tcsacNewFromDtb) {
                                incrementControl++;
                                tcsacNew.setControl(incrementControl);
                                tcsacNew.setSequence(incrementAction);
                                tcsacNew.setStep(incrementStep);
                                tcsacNewFromPage.add(tcsacNew);
                            }
                            tcsaNew.setSequence(incrementAction);
                            tcsaNew.setStep(incrementStep);
                            tcsaNewFromPage.add(tcsaNew);
                        }
                        tcsNew.setInitialStep(tcsNew.getStep());
                        tcsNew.setStep(incrementStep);
                        tcsNewFromPage.add(tcsNew);
                    }

                    List<TestCaseStep> tcsNewNewFromDtb = new ArrayList(tcsService.getListOfSteps(test, testCase));
                    tcsService.compareListAndUpdateInsertDeleteElements(tcsNewFromPage, tcsNewNewFromDtb, duplicate);

                    List<TestCaseStepAction> tcsaNewNewFromDtb = new ArrayList(tcsaService.findTestCaseStepActionbyTestTestCase(test, testCase));
                    tcsaService.compareListAndUpdateInsertDeleteElements(tcsaNewFromPage, tcsaNewNewFromDtb, duplicate);

                    List<TestCaseStepActionControl> tcsacNewNewFromDtb = new ArrayList(tcsacService.findControlByTestTestCase(test, testCase));
                    tcsacService.compareListAndUpdateInsertDeleteElements(tcsacNewFromPage, tcsacNewNewFromDtb, duplicate);

                    /**
                     * Adding Log entry.
                     */
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        /**
                         * Update was succesfull. Adding Log entry.
                         */
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/UpdateTestCaseWithDependencies1", "UPDATE", "Update testcase : ['" + tc.getTest() + "'|'" + tc.getTestCase() + "']", request);
                    }

                }
        }

        /**
         * Formating and returning the json result.
         */
        jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
        jsonResponse.put("message", ans.getResultMessage().getDescription());

        response.getWriter().print(jsonResponse);
        response.getWriter().flush();

    }

    /**
     * Create new TestCase object from the information of request form
     *
     * @param request information from the request page
     * @return TestCase object
     * @see org.cerberus.crud.entity.TestCase
     */
    private TCase getTestCaseFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {

        IFactoryTCase testCaseFactory = appContext.getBean(IFactoryTCase.class);
        String origin = request.getParameter("editOrigin");
        String refOrigin = request.getParameter("editRefOrigin");
        String creator = request.getParameter("editCreator");
        String implementer = request.getParameter("editImplementer");
        String lastModifier = request.getUserPrincipal().getName();
        String project = request.getParameter("editProject");
        String ticket = request.getParameter("editTicket");
        String application = request.getParameter("editApplication");
        String runQA = request.getParameter("editRunQA");
        String runUAT = request.getParameter("editRunUAT");
        String runPROD = request.getParameter("editRunPROD");
        Integer priority = Integer.parseInt(request.getParameter("editPriority"));
        String group = request.getParameter("editGroup");
        String status = request.getParameter("editStatus");
        String shortDescription = HtmlUtils.htmlEscape(request.getParameter("editDescription"));
        String description = request.getParameter("valueDetail");
        String howTo = request.getParameter("howtoDetail");
        String active = request.getParameter("editTcActive");
        String fromSprint = request.getParameter("editFromBuild");
        String fromRevision = request.getParameter("editFromRev");
        String toSprint = request.getParameter("editToBuild");
        String toRevision = request.getParameter("editToRev");
        String bugID = request.getParameter("editBugID");
        String targetSprint = request.getParameter("editTargetBuild");
        String targetRevision = request.getParameter("editTargetRev");
        String comment = HtmlUtils.htmlEscape(request.getParameter("editComment"));
        String function = request.getParameter("editFunction");
        return testCaseFactory.create(test, testCase, origin, refOrigin, creator, implementer, lastModifier, project, ticket, function, application,
                runQA, runUAT, runPROD, priority, group, status, shortDescription, description, howTo, active, fromSprint, fromRevision, toSprint,
                toRevision, null, bugID, targetSprint, targetRevision, comment, null, null, null, null);
    }

    private List<TestCaseCountry> getTestCaseCountryFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) {
        IFactoryTestCaseCountry testCaseCountryFactory = appContext.getBean(IFactoryTestCaseCountry.class);
        List<TestCaseCountry> countries = new ArrayList<TestCaseCountry>();
        if (request.getParameterValues("editTestCaseCountry") != null) {
            for (String country : request.getParameterValues("editTestCaseCountry")) {
                countries.add(testCaseCountryFactory.create(test, testCase, country));
            }
        }
        return countries;
    }

    /**
     * FIXME this method do nothing more than return
     * request.getParameter(parameter)!
     */
    private String getParameterIfExists(HttpServletRequest request, String parameter) {
        String result = null;
        if (request.getParameter(parameter) != null) {
            result = request.getParameter(parameter);
        }
        return result;
    }

    /**
     * FIXME this method do nothing more than return
     * request.getParameterValues(parameter)!
     */
    private String[] getParameterValuesIfExists(HttpServletRequest request, String parameter) {
        String[] result = null;
        if (request.getParameterValues(parameter) != null) {
            result = request.getParameterValues(parameter);
        }
        return result;
    }

    private List<TestCaseCountryProperties> getTestCaseCountryPropertiesFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase) throws JSONException {
        List<TestCaseCountryProperties> testCaseCountryProp = new ArrayList();
//        String[] testcase_properties_increment = getParameterValuesIfExists(request, "property_increment");
        IFactoryTestCaseCountryProperties testCaseCountryPropertiesFactory = appContext.getBean(IFactoryTestCaseCountryProperties.class);
        JSONArray properties = new JSONArray(request.getParameter("propArr"));

        for (int i = 0; i < properties.length(); i++) {
            JSONObject propJson = properties.getJSONObject(i);

            boolean delete = propJson.getBoolean("toDelete");
            String property = propJson.getString("property");
            String type = propJson.getString("type");
            String value = propJson.getString("value1");
            String value2 = propJson.getString("value2");
            int length = propJson.getInt("length");
            int rowLimit = propJson.getInt("rowLimit");
            String nature = propJson.getString("nature");
            String database = propJson.getString("database");
            JSONArray countries = propJson.getJSONArray("country");
            if (!delete && !property.equals("")) {
                for (int j = 0; j < countries.length(); j++) {
                    String country = countries.getString(j);

                    testCaseCountryProp.add(testCaseCountryPropertiesFactory.create(test, testCase, country, property, type, database, value, value2, length, rowLimit, nature));
                }
            }
        }
        return testCaseCountryProp;
    }

    private List<TestCaseStep> getTestCaseStepFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, boolean duplicate) throws JSONException {
        List<TestCaseStep> testCaseStep = new ArrayList();
        ITestCaseStepService tcsService = appContext.getBean(ITestCaseStepService.class);
        IFactoryTestCaseStep testCaseStepFactory = appContext.getBean(IFactoryTestCaseStep.class);
        JSONArray stepArray = new JSONArray(request.getParameter("stepArray"));

        for (int i = 0; i < stepArray.length(); i++) {
            JSONObject step = stepArray.getJSONObject(i);

            boolean delete = step.getBoolean("toDelete");
            int stepNumber = step.getInt("step");
            String description = step.getString("description");
            String useStep = step.getString("useStep");
            String useStepTest = step.getString("useStepTest");
            String useStepTestCase = step.getString("useStepTestCase");
            int useStepStep = step.getInt("useStepStep");
            String inLibrary = step.getString("inLibrary");
            JSONArray stepActions = step.getJSONArray("actionArr");

            if (!delete) {
                TestCaseStep tcStep = testCaseStepFactory.create(test, testCase, stepNumber, description, useStep, useStepTest, useStepTestCase, useStepStep, inLibrary);

                if (useStep.equals("N")) {
                    tcStep.setTestCaseStepAction(getTestCaseStepActionFromParameter(request, appContext, test, testCase, stepActions));
                } else {
                    TestCaseStep tcs = null;
                    if (useStepStep != -1 && !useStepTest.equals("") && !useStepTestCase.equals("")) {
                        tcs = tcsService.findTestCaseStep(useStepTest, useStepTestCase, useStepStep);
                        if (tcs != null) {
                            tcStep.setUseStepTest(tcs.getTest());
                            tcStep.setUseStepTestCase(tcs.getTestCase());
                            tcStep.setUseStepStep(tcs.getStep());
                        }
                    }
                }
                testCaseStep.add(tcStep);
            }
        }
        return testCaseStep;
    }

    private List<TestCaseStepAction> getTestCaseStepActionFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray testCaseStepActionJson) throws JSONException {
        List<TestCaseStepAction> testCaseStepAction = new ArrayList();
        IFactoryTestCaseStepAction testCaseStepActionFactory = appContext.getBean(IFactoryTestCaseStepAction.class);

        for (int i = 0; i < testCaseStepActionJson.length(); i++) {
            JSONObject tcsaJson = testCaseStepActionJson.getJSONObject(i);

            boolean delete = tcsaJson.getBoolean("toDelete");
            int step = tcsaJson.getInt("step");
            int sequence = tcsaJson.getInt("sequence");
            String action = tcsaJson.getString("action");
            String object = tcsaJson.getString("object");
            String property = tcsaJson.getString("property");
            String description = tcsaJson.getString("description");
            String screenshot = tcsaJson.getString("screenshotFileName");
            JSONArray controlArray = tcsaJson.getJSONArray("controlArr");

            if (!delete) {
                TestCaseStepAction tcsa = testCaseStepActionFactory.create(test, testCase, step, sequence, action, object, property, description, screenshot);
                tcsa.setTestCaseStepActionControl(getTestCaseStepActionControlFromParameter(request, appContext, test, testCase, controlArray));
                testCaseStepAction.add(tcsa);
            }
        }
        return testCaseStepAction;
    }

    private List<TestCaseStepActionControl> getTestCaseStepActionControlFromParameter(HttpServletRequest request, ApplicationContext appContext, String test, String testCase, JSONArray controlArray) throws JSONException {
        List<TestCaseStepActionControl> testCaseStepActionControl = new ArrayList();
        IFactoryTestCaseStepActionControl testCaseStepActionControlFactory = appContext.getBean(IFactoryTestCaseStepActionControl.class);

        for (int i = 0; i < controlArray.length(); i++) {
            JSONObject controlJson = controlArray.getJSONObject(i);

            boolean delete = controlJson.getBoolean("toDelete");
            int step = controlJson.getInt("step");
            int sequence = controlJson.getInt("sequence");
            int control = controlJson.getInt("control");
            String type = controlJson.getString("type");
            String controlValue = controlJson.getString("controlValue");
            String controlProperty = controlJson.getString("controlProperty");
            String fatal = controlJson.getString("fatal");
            String description = controlJson.getString("description");
            String screenshot = controlJson.getString("screenshotFileName");
            if (!delete) {
                testCaseStepActionControl.add(testCaseStepActionControlFactory.create(test, testCase, step, sequence, control, type, controlValue, controlProperty, fatal, description, screenshot));
            }
        }
        return testCaseStepActionControl;
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (CerberusException ex) {
            java.util.logging.Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(UpdateTestCaseWithDependencies1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
