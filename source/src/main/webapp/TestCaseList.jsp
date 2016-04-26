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
    Document   : Test2
    Created on : 23 sept. 2015, 16:07:19
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/tinymce/tinymce.min.js"></script>
        <script type="text/javascript" src="js/pages/TestCaseList.js"></script>
        <title id="pageTitle">Test Case List</title>
    </head>
    <body>
        <%@ include file="include/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>
            <%@ include file="include/testcaselist/editTestCase.html"%>
            <%@ include file="include/testcaselist/addTestCase.html"%>

            <h1 class="page-title-line" id="title">Test Case List</h1>

            <div class="row">
                <div class="col-lg-6" id="FiltersPanel">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <span class="fa fa-tag fa-fw"></span>
                            <label id="filters">Filters</label>
                        </div>
                        <div class="panel-body">
                            <label for="selectTest" name="testField">Test :</label>
                            <div class="form-inline">
                                <select class="form-control" id="selectTest" style="width: 90%"></select>
                                <button type="button" class="btn btn-default" name="btnLoad" id="btnLoad" onclick="loadTable()">Load</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="panel panel-default">
                <div class="panel-heading" id="testCaseListLabel">
                    Test Case List
                </div>
                <div class="panel-body" id="testCaseList">
                    <table id="testCaseTable" class="table table-hover display" name="testCaseTable"></table>
                    <div class="marginBottom20"></div>
                </div>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
