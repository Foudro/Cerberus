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
package org.cerberus.crud.factory;

import org.cerberus.crud.entity.TestCase;

/**
 *
 * @author bcivel
 */
public interface IFactoryTestCase {
    
    TestCase create( String test,String testCase,String origin,String refOrigin,String creator,
            String implementer,String lastModifier,String project,String ticket,String application,
            String runQA,String runUAT,String runPROD,int priority,String group,String status,
            String shortDescription,String description,String howTo,String active,String fromSprint,
            String fromRevision,String toSprint,String toRevision,String lastExecutionStatus,String bugID,
            String targetSprint,String targetRevision,String comment);
}
