<%--

    Cerberus  Copyright (C) 2013 - 2016  vertigo17
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<% Date DatePageStart = new Date();%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css">
        <link rel="stylesheet" type="text/css" href="css/jquery-ui.css">
        <link rel="stylesheet" type="text/css" href="css/dataTables_jui.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.mini.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
        <script type="text/javascript" src="js/jquery.dataTables.editable.js"></script>
        <script type="text/javascript" src="js/jquery.validate.min.js"></script>
        <title>Parameters</title>

    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <%
            String MySystem = ParameterParserUtil.parseStringParam(request.getAttribute("MySystem").toString(), "");
        %>
        <script type="text/javascript">      
            $(document).ready(function(){
                $('#parametersTable').dataTable({
                    "aLengthMenu": [
                        [20, 50, 100, 200, -1],
                        [20, 50, 100, 200, "All"]
                    ], 
                    "iDisplayLength" : 20,
                    "bServerSide": false,
                    "sAjaxSource": "GetParameterSystem?system=<%=MySystem%>",
                    "bJQueryUI": true,
                    "bProcessing": true,
                    "sPaginationType": "full_numbers",
                    "bSearchable": false, "aTargets": [ 0 ],
                    "aoColumns": [
                        {"sName": "Parameter"},
                        {"sName": "ValueCerberus"},
                        {"sName": "ValueSystem"},
                        {"sName": "Description"}
                    ]
                }
            ).makeEditable({
                    sUpdateURL: "UpdateParameter?system=<%=MySystem%>",
                    fnOnEdited: function(status){
                        $(".dataTables_processing").css('visibility', 'hidden');
                    },
                    "aoColumns": [
                        null,
                        {
                            tooltip: 'Click to edit Default Global Cerberus parameter.',
                            type: 'textarea',
                            submit:'Save changes'},
                        {
                            tooltip: 'Click to edit the Specific parameter for System <%=MySystem%>.',
                            type: 'textarea',
                            submit:'Save changes'},
                        null
                    ]
                });
            });
        </script>
        <p class="dttTitle">Parameter</p>
        <div style="width: 100%;  font: 90% sans-serif">
            <table id="parametersTable" class="display">
                <thead>
                    <tr>
                        <th>Parameter</th>
                        <th>Cerberus Value</th>
                        <th>System <%=MySystem%> Value</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <br><%
            out.print(display_footer(DatePageStart));
        %>
    </body>
</html>
