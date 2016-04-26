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
package org.cerberus.servlet.crud.transversaltables;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cerberus.crud.entity.Invariant;
import org.cerberus.crud.service.IInvariantService;
import org.cerberus.util.ParameterParserUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author bcivel
 */
@WebServlet(name = "FindAllInvariantPublic", urlPatterns = {"/FindAllInvariantPublic"})
public class FindAllInvariantPublic extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String echo = request.getParameter("sEcho");
            String sStart = request.getParameter("iDisplayStart");
            String sAmount = request.getParameter("iDisplayLength");
            String sCol = request.getParameter("iSortCol_0");
            String sdir = request.getParameter("sSortDir_0");
            String dir = "asc";
            String[] cols = {"key", "idname", "value", "sort", "description", "VeryShortDesc", "gp1", "gp2", "gp3"};

            int amount = 10;
            int start = 0;
            int col = 0;

            String sIdname = "";
            String sValue = "";

            sIdname = ParameterParserUtil.parseStringParam(request.getParameter("sSearch_1"), "");
            sValue = ParameterParserUtil.parseStringParam(request.getParameter("sSearch_2"), "");

            List<String> sArray = new ArrayList<String>();
            if (!sIdname.equals("")) {
                sArray.add(" `idname` like '%" + sIdname + "%'");
            }
            if (!sValue.equals("")) {
                sArray.add(" value like '%" + sValue + "%'");
            }

            StringBuilder individualSearch = new StringBuilder();
            if (sArray.size() == 1) {
                individualSearch.append(sArray.get(0));
            } else if (sArray.size() > 1) {
                for (int i = 0; i < sArray.size() - 1; i++) {
                    individualSearch.append(sArray.get(i));
                    individualSearch.append(" and ");
                }
                individualSearch.append(sArray.get(sArray.size() - 1));
            }

            if (sStart != null) {
                start = Integer.parseInt(sStart);
                if (start < 0) {
                    start = 0;
                }
            }
            if (sAmount != null) {
                amount = Integer.parseInt(sAmount);
            } else {
                amount = 10;
            }

            if (sCol != null) {
                col = Integer.parseInt(sCol);
                if (col < 0 || col > 10) {
                    col = 0;
                }
            }
            if (sdir != null) {
                if (!sdir.equals("asc")) {
                    dir = "desc";
                }
            }
            String colName = cols[col];

            String searchTerm = "";
            searchTerm = ParameterParserUtil.parseStringParam(request.getParameter("sSearch"), "");

            String inds = String.valueOf(individualSearch);

            JSONArray data = new JSONArray(); //data that will be shown in the table

            ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
            IInvariantService invariantService = appContext.getBean(IInvariantService.class);

            List<Invariant> invariantList = invariantService.readByPublicByCriteria(start, amount, colName, dir, searchTerm, inds);

            JSONObject jsonResponse = new JSONObject();

            for (Invariant InvariantData : invariantList) {
                JSONArray row = new JSONArray();
                row.put(InvariantData.getIdName() + "$#" + InvariantData.getValue())
                        .put(InvariantData.getIdName())
                        .put(InvariantData.getValue())
                        .put(InvariantData.getSort())
                        .put(InvariantData.getDescription())
                        .put(InvariantData.getVeryShortDesc())
                        .put(InvariantData.getGp1())
                        .put(InvariantData.getGp2())
                        .put(InvariantData.getGp3());

                data.put(row);
            }
            Integer iTotalRecords = invariantService.getNumberOfPublicInvariant("");
            Integer iTotalDisplayRecords = invariantService.getNumberOfPublicInvariant(searchTerm);

            jsonResponse.put("aaData", data);
            jsonResponse.put("sEcho", echo);
            jsonResponse.put("iTotalRecords", iTotalRecords);
            jsonResponse.put("iTotalDisplayRecords", iTotalDisplayRecords);

            response.setContentType("application/json");
            response.getWriter().print(jsonResponse.toString());
        } catch (JSONException ex) {
            Logger.getLogger(FindAllInvariantPublic.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
