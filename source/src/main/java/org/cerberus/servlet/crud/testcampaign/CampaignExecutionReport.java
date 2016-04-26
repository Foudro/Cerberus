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
package org.cerberus.servlet.crud.testcampaign;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.dto.TestCaseWithExecution;
import org.cerberus.exception.CerberusException;
import org.cerberus.crud.service.ICampaignService;
import org.cerberus.crud.service.ITestCaseExecutionInQueueService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.JavaScriptUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class CampaignExecutionReport.
 *
 * @author memiks
 */
public class CampaignExecutionReport extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            ApplicationContext appContext = WebApplicationContextUtils
                    .getWebApplicationContext(this.getServletContext());

            ICampaignService campaignService = appContext
                    .getBean(ICampaignService.class);

            ITestCaseExecutionInQueueService testCaseExecutionInQueueService = appContext
                    .getBean(ITestCaseExecutionInQueueService.class);

            PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

            String tag = request.getParameter("Tag");

            JSONObject result = new JSONObject();
            JSONArray executionList = new JSONArray();

            /**
             * Get list of execution by tag, env, country, browser
             */
            List<TestCaseWithExecution> testCaseWithExecutions = campaignService.getCampaignTestCaseExecutionForEnvCountriesBrowserTag(tag);

            /**
             * Get list of Execution in Queue by Tag
             */
            List<TestCaseWithExecution> testCaseWithExecutionsInQueue = testCaseExecutionInQueueService.findTestCaseWithExecutionInQueuebyTag(tag);

            /**
             * Feed hash map with execution from the two list (to get only one
             * by test,testcase,country,env,browser)
             */
            Map<String, TestCaseWithExecution> testCaseWithExecutionsList = new HashMap();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
                String key = testCaseWithExecution.getBrowser() + "_"
                        + testCaseWithExecution.getCountry() + "_"
                        + testCaseWithExecution.getEnvironment() + "_"
                        + testCaseWithExecution.getTest() + "_"
                        + testCaseWithExecution.getTestCase();
                testCaseWithExecutionsList.put(key, testCaseWithExecution);
            }
            for (TestCaseWithExecution testCaseWithExecutionInQueue : testCaseWithExecutionsInQueue) {
                String key = testCaseWithExecutionInQueue.getBrowser() + "_"
                        + testCaseWithExecutionInQueue.getCountry() + "_"
                        + testCaseWithExecutionInQueue.getEnvironment() + "_"
                        + testCaseWithExecutionInQueue.getTest() + "_"
                        + testCaseWithExecutionInQueue.getTestCase();
                if ((testCaseWithExecutionsList.containsKey(key)
                        && formater.parse(testCaseWithExecutionsList.get(key).getStart()).before(formater.parse(testCaseWithExecutionInQueue.getStart())))
                        || !testCaseWithExecutionsList.containsKey(key)) {
                    testCaseWithExecutionsList.put(key, testCaseWithExecutionInQueue);
                }
            }
            testCaseWithExecutions = new ArrayList<TestCaseWithExecution>(testCaseWithExecutionsList.values());

            Map<String, JSONObject> ttc = new HashMap<String, JSONObject>();
            Map<String, JSONObject> ceb = new HashMap<String, JSONObject>();
            for (TestCaseWithExecution testCaseWithExecution : testCaseWithExecutions) {
                try {
                    executionList.put(testCaseExecutionToJSONObject(testCaseWithExecution));
                    JSONObject ttcObject = new JSONObject();
                    ttcObject.put("test", testCaseWithExecution.getTest());
                    ttcObject.put("testCase", testCaseWithExecution.getTestCase());
                    ttcObject.put("function", testCaseWithExecution.getFunction());
                    ttcObject.put("shortDesc", testCaseWithExecution.getShortDescription());
                    ttcObject.put("status", testCaseWithExecution.getStatus());
                    ttcObject.put("application", testCaseWithExecution.getApplication());
                    ttcObject.put("bugId", testCaseWithExecution.getBugID());
                    ttcObject.put("comment", testCaseWithExecution.getComment());
                    ttcObject.put("application", testCaseWithExecution.getApplication());
                    JSONObject cebObject = new JSONObject();
                    cebObject.put("country", testCaseWithExecution.getCountry());
                    cebObject.put("environment", testCaseWithExecution.getEnvironment());
                    cebObject.put("browser", testCaseWithExecution.getBrowser());
                    ttc.put(testCaseWithExecution.getTest() + "_" + testCaseWithExecution.getTestCase(), ttcObject);
                    ceb.put(testCaseWithExecution.getBrowser() + "_" + testCaseWithExecution.getCountry() + "_" + testCaseWithExecution.getEnvironment(), cebObject);
                } catch (JSONException ex) {
                    Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            JSONArray columns = new JSONArray(ceb.values());
            JSONArray lines = new JSONArray(ttc.values());
            result.put("Columns", columns);
            result.put("Lines", lines);
            result.put("Values", executionList);

            response.setContentType("application/json");
            response.getWriter().print(result);
        } catch (CerberusException ex) {
            Logger.getLogger(CampaignExecutionReport.class.getName()).log(
                    Level.WARNING, null, ex);

        } catch (ParseException ex) {
            Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CampaignExecutionReport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed"
    // desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
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
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    /**
     * Test case execution to json object.
     *
     * @param testCaseExecutions the test case executions
     * @return the JSON object
     * @throws JSONException the JSON exception
     */
    private JSONObject testCaseExecutionToJSONObject(
            TestCaseWithExecution testCaseWithExecution) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("ID", String.valueOf(testCaseWithExecution.getStatusExecutionID()));
        result.put("Test", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getTest()));
        result.put("TestCase", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getTestCase()));
        result.put("Environment", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getEnvironment()));
        result.put("Start", testCaseWithExecution.getStart());
        result.put("End", testCaseWithExecution.getEnd());
        result.put("Country", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getCountry()));
        result.put("Browser", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getBrowser()));
        result.put("ControlStatus", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getControlStatus()));
        result.put("ControlMessage", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getControlMessage()));
        result.put("Status", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getStatus()));

        String bugId;
        if (testCaseWithExecution.getApplicationObject() != null && testCaseWithExecution.getApplicationObject().getBugTrackerUrl() != null
                && !"".equals(testCaseWithExecution.getApplicationObject().getBugTrackerUrl()) && testCaseWithExecution.getBugID() != null) {
            bugId = testCaseWithExecution.getApplicationObject().getBugTrackerUrl().replaceAll("%BUGID%", testCaseWithExecution.getBugID());
            bugId = new StringBuffer("<a href='")
                    .append(bugId)
                    .append("' target='reportBugID'>")
                    .append(testCaseWithExecution.getBugID())
                    .append("</a>")
                    .toString();
        } else {
            bugId = testCaseWithExecution.getBugID();
        }
        result.put("BugID", bugId);

        result.put("Comment", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getComment()));
        result.put("Function", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getFunction()));
        result.put("Application", JavaScriptUtils.javaScriptEscape(testCaseWithExecution.getApplication()));
        result.put("ShortDescription", testCaseWithExecution.getShortDescription());

        return result;
    }
}
