/*
 * Cerberus  Copyright (C) 2013  vertigo17
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
package org.cerberus.servlet.crud.testdata;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringEscapeUtils;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.TestCaseCountryProperties;
import org.cerberus.crud.entity.TestDataLib;
import org.cerberus.crud.service.ILogEventService;
import org.cerberus.crud.service.ITestCaseCountryPropertiesService;
import org.cerberus.crud.service.ITestDataLibService;
import org.cerberus.crud.service.impl.LogEventService;
import org.cerberus.dto.TestListDTO;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.util.answer.Answer;
import org.cerberus.util.answer.AnswerItem;
import org.cerberus.util.answer.AnswerList;
import org.cerberus.util.answer.AnswerUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet responsible for handling the deletion of a test data lib
 *
 * @author FNogueira
 */
@WebServlet(name = "DeleteTestDataLib", urlPatterns = {"/DeleteTestDataLib"})
public class DeleteTestDataLib extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JSONObject jsonResponse = new JSONObject();
        Answer ans = new Answer();
        MessageEvent msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_UNEXPECTED);
        msg.setDescription(msg.getDescription().replace("%DESCRIPTION%", ""));
        ans.setResultMessage(msg);

        response.setContentType("application/json");

        /**
         * Parsing and securing all required parameters.
         */
        Integer key = 0;
        boolean testdatalibid_error = true;
        try {
            if (request.getParameter("testdatalibid") != null && !request.getParameter("testdatalibid").isEmpty()) {
                key = Integer.valueOf(request.getParameter("testdatalibid"));
                testdatalibid_error = false;
            }
        } catch (NumberFormatException ex) {
            testdatalibid_error = true;
            org.apache.log4j.Logger.getLogger(DeleteTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
        }

        /**
         * Checking all constrains before calling the services.
         */
        if (testdatalibid_error) {
            msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
            msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                    .replace("%OPERATION%", "Delete")
                    .replace("%REASON%", "Test data library (testdatalibid) is missing."));
            ans.setResultMessage(msg);
        } else {
            /**
             * All data seems cleans so we can call the services.
             */
            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            ITestDataLibService libService = appContext.getBean(ITestDataLibService.class);

            AnswerItem resp = libService.readByKey(key);
            
            if (!(resp.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode()))) {
                /**
                 * Object could not be found. We stop here and report the error.
                 */
                msg = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                msg.setDescription(msg.getDescription().replace("%ITEM%", "Test Data Library")
                        .replace("%OPERATION%", "Delete")
                        .replace("%REASON%", "Test Data Library does not exist."));
                ans.setResultMessage(msg);
            } else {
                /**
                 * The service was able to perform the query and confirm the
                 * object exist, then we can delete it.
                 */

                TestDataLib lib = (TestDataLib) resp.getItem();
                
                //check if the lib can be deleted by search the lib name in the property                
                ITestCaseCountryPropertiesService  propService = appContext.getBean(ITestCaseCountryPropertiesService.class);
                 
                AnswerList testCasesAnswer = propService.findTestCaseCountryPropertiesByValue1(lib.getTestDataLibID(), lib.getName(), lib.getCountry(), "getFromDataLib");
                
                List<TestListDTO> list = (List<TestListDTO>)testCasesAnswer.getDataList();
                
                if(list.isEmpty()){ //if list is empty then the library entry can be deleted
                    ans = libService.delete(lib);                
                    /**
                     * Delete was perform with success. Adding Log entry.
                     */
                    if (ans.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        ILogEventService logEventService = appContext.getBean(LogEventService.class);
                        logEventService.createPrivateCalls("/DeleteTestDataLib", "DELETE", "Delete TestDataLib : " + key, request);
                    }
                }else{
                    if (testCasesAnswer.isCodeEquals(MessageEventEnum.DATA_OPERATION_OK.getCode())) {
                        //ans.setResultMessage(testCasesAnswer.getResultMessage());
                        MessageEvent deleteNotAllowed = new MessageEvent(MessageEventEnum.DATA_OPERATION_ERROR_EXPECTED);
                        //DATA_OPERATION_ERROR_EXPECTED(550, MessageCodeEnum.DATA_OPERATION_CODE_ERROR.getCode(), 
                        //"%ITEM% - operation %OPERATION% failed to complete. %REASON%", false, false, false, MessageGeneralEnum.DATA_OPERATION_ERROR),
                        int totalTestCases = 0;
                        for(TestListDTO test : list){
                            totalTestCases += test.getTestCaseList().size();
                        }
                        
                        deleteNotAllowed.setDescription(deleteNotAllowed.getDescription()
                                                            .replace("%ITEM%", "Test Data Library")
                                                            .replace("%OPERATION%", "Delete")
                                                            .replace("%REASON%", "There are " + totalTestCases + " test cases using this library [# different tests involved:" + list.size() + " ]"));
                        ans.setResultMessage(deleteNotAllowed);
                    }else{
                        //if another error was obtained than revert it to the user
                        ans.setResultMessage(testCasesAnswer.getResultMessage());
                    }
                }

            }

        }

        try {
            /**
             * Formating and returning the json result.
             */
            jsonResponse.put("messageType", ans.getResultMessage().getMessage().getCodeString());
            jsonResponse.put("message", ans.getResultMessage().getDescription());

            response.getWriter().print(jsonResponse.toString());
            response.getWriter().flush();

        } catch (JSONException ex) {
            org.apache.log4j.Logger.getLogger(DeleteTestDataLib.class.getName()).log(org.apache.log4j.Level.ERROR, null, ex);
            response.setContentType("application/json");
            response.getWriter().print(AnswerUtil.createGenericErrorAnswer());
            response.getWriter().flush();
        }
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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