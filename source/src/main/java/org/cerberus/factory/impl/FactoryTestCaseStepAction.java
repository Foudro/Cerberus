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
package org.cerberus.factory.impl;

import org.cerberus.entity.TestCaseStepAction;
import org.cerberus.factory.IFactoryTestCaseStepAction;
import org.springframework.stereotype.Service;

/**
 * @author bcivel
 */
@Service
public class FactoryTestCaseStepAction implements IFactoryTestCaseStepAction {

    @Override
    public TestCaseStepAction create(String test, String testCase, int step, int sequence, String action, String object, String property, String description) {
        TestCaseStepAction testCaseStepAction = new TestCaseStepAction();
        testCaseStepAction.setAction(action);
        testCaseStepAction.setObject(object);
        testCaseStepAction.setProperty(property);
        testCaseStepAction.setSequence(sequence);
        testCaseStepAction.setStep(step);
        testCaseStepAction.setTest(test);
        testCaseStepAction.setTestCase(testCase);
        testCaseStepAction.setDescription(description);
        return testCaseStepAction;
    }

}