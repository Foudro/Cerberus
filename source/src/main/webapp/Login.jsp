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
<%@page import="org.cerberus.crud.factory.impl.FactoryLogEvent"%>
<%@page import="org.cerberus.crud.factory.IFactoryLogEvent"%>
<%@page import="org.cerberus.crud.service.impl.LogEventService"%>
<%@page import="org.cerberus.crud.service.ILogEventService"%>
<%@page import="org.cerberus.version.Infos"%>
<%@page import="org.cerberus.log.MyLogger"%>
<%@page import="org.cerberus.crud.service.IParameterService"%>
<%@page import="org.apache.log4j.Level"%>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">
        <title>Login</title>
    </head>
    <body>

<%
	ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
    IParameterService myParameterService = appContext.getBean(IParameterService.class);
    try {
        String CerberusSupportEmail = myParameterService.findParameterByKey("cerberus_support_email", "").getValue();
        String errorMessage = "";
        if (request.getParameter("error") != null && request.getParameter("error").equalsIgnoreCase("1")) {
            errorMessage = "User or password invalid !!!";
            /**
             * Adding Log entry.
             */
            ILogEventService logEventService = appContext.getBean(LogEventService.class);
            IFactoryLogEvent factoryLogEvent = appContext.getBean(FactoryLogEvent.class);
            logEventService.create(factoryLogEvent.create(0, 0, request.getParameter("j_username"), null, "/Login.jsp", "LOGINERROR", "Invalid Password for : " + request.getParameter("j_username"), request.getRemoteAddr(), request.getLocalAddr()));
        }
%>
        <script type='text/javascript' src='js/Form.js'></script>
        <script type="text/javascript">
            EnvTuning("<%=System.getProperty("org.cerberus.environment")%>");
        </script>

        <div style="padding-top: 7%; padding-left: 30%">
            <div id="login-box">
                <H2>Cerberus Login</H2><br>V<%=Infos.getInstance().getProjectVersion()%><br><br>
                Please login in order to change TestCases and run Tests.<br>
                If you don't have login, please contact <%= CerberusSupportEmail%>
                <br>
                <br>
                <form method="post" action="j_security_check">
                    <div class="login-box-name" style="margin-top:20px;">
                        Username:
                    </div>
                    <div class="login-box-field" style="margin-top:20px;">
                        <input name="j_username" class="form-login" title="Username" value="" size="30" maxlength="10">
                    </div>
                    <div class="login-box-name">
                        Password:
                    </div>
                    <div class="login-box-field">
                        <input name="j_password" type="password" class="form-login" title="Password" value="" size="30" maxlength="20">
                    </div>
                    <div class="login-box-error">
                        <%= errorMessage%>
                    </div>
                    <input id="Login" name="Login" type="image" src="images/login-btn.png" value="Submit" alt="Submit" style="margin-left:90px;" onclick=sessionStorage.clear();>
                </form>
            </div>
        </div>
    </body>
</html>
<%
    } catch (Exception ex) {
        // This exception should only happen when the database is empty. In 
        // that case we redirect to the page that will automatically create the database.        
        request.getRequestDispatcher("/DatabaseMaintenance.jsp").forward(request, response);
    }
%>