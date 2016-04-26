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
<%-- 
    Document   : ExecutionThreadMonitoring
    Created on : 3 mars 2015, 12:42:00
    Author     : bcivel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="css/crb_style.css">
        <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.10.2.js"></script>
        <title>ExecutionThreadMonitoring</title>
        <script>
            $(document).ready(function() {
                $.get('ExecutionThreadMonitoring', function(data) {
                    $("#sizeOfQueue").html(data.size_queue);
                    $("#QueueInExecution").html(data.queue_in_execution);
                    $("#NumberOfThread").html(data.number_of_thread);
                    $("#SimultaneousExecution").html(data.simultaneous_execution);
                    $("#SimultaneousSession").html(data.simultaneous_session);
                    $.each(data.active_users, function (a, v){
                        $("#ActiveUsers").append("<li>"+ v + "</li>");
                    });
                    $.each(data.simultaneous_execution_list, function (a, v){
                        $("#ExecutionList").append("<li>[<a href='./ExecutionDetail.jsp?id_tc="+ v.id + "'>"+ v.id + "</a>] : " + v.test + " " +v.testcase + "</li>");
                    });
                    
                });

            });
        </script>
        <script>
            function resetThreadPool(){
                $.get('ExecutionThreadReset', function(data) {
                    alert('Thread Pool Cleaned');
                });
            }
        </script>
    </head>
    <body>
        <%@ include file="include/function.jsp" %>
        <%@ include file="include/header.jsp" %>
        <h3>Execution Monitoring</h3>
        <h4>Thread Execution</h4>
        <p>Size Of Pending Execution In Queue : </p><p id="sizeOfQueue"></p>
        <br>
        <p>Number of Workers In Execution : </p><div style="float:left" id="QueueInExecution"></div><div style="float:left">/</div><div style="float:left" id="NumberOfThread"></div>
        <br>
        <input type="button" value="Reset Queue" onclick="resetThreadPool()">
        <br>
        <br>

        <h4>Execution</h4>
        <p>Number of Actual Simultaneous Execution : </p><p id="SimultaneousExecution"></p>
        <br>
        <p>Execution List : </p>
        <br>
        <ul id="ExecutionList"></ul>
        <br><br>
        <h3>Session Monitoring</h3>
        <p>Number of HTTP Session opened : </p><p id="SimultaneousSession"></p>
        <br>
        <p>List of Active Users : </p>
        <br>
        <ul id="ActiveUsers"></ul>
        <br>
    </body>
</html>
