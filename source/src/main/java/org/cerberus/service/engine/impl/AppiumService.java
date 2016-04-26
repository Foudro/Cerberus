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
package org.cerberus.service.engine.impl;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cerberus.crud.entity.Identifier;
import org.cerberus.crud.entity.MessageEvent;
import org.cerberus.crud.entity.Parameter;
import org.cerberus.crud.entity.Session;
import org.cerberus.crud.entity.SwipeAction;
import org.cerberus.crud.service.impl.ParameterService;
import org.cerberus.enums.MessageEventEnum;
import org.cerberus.log.MyLogger;
import org.cerberus.service.engine.IAppiumService;
import org.cerberus.util.ParameterParserUtil;
import org.cerberus.util.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.geom.Line2D;
import java.util.Set;

/**
 * @author bcivel
 */
public abstract class AppiumService implements IAppiumService {

    /**
     * The associated {@link Logger} to this class
     */
    private static final Logger LOGGER = Logger.getLogger(AppiumService.class);

    /**
     * The Appium swipe duration parameter which is got thanks to the {@link ParameterService}
     */
    private static final String APPIUM_SWIPE_DURATION_PARAMETER = "appium_swipeDuration";

    /**
     * The default Appium swipe duration if no {@link AppiumService#APPIUM_SWIPE_DURATION_PARAMETER} has been defined
     */
    private static final int DEFAULT_APPIUM_SWIPE_DURATION = 2000;

    @Autowired
    private ParameterService parameters;

    @Override
    public MessageEvent switchToContext(Session session, Identifier identifier) {
        MessageEvent message;
        AppiumDriver driver = session.getAppiumDriver();
        String newContext = "";
        Set<String> contextNames = driver.getContextHandles();
        for (String contextName : contextNames) {
            System.out.println("context" + contextName);
            if (contextName.contains("WEBVIEW")) {
                driver.context(contextName);
                newContext = contextName;
                break;
            }
        }
        //driver.context("WEBVIEW_1");
        message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWITCHTOWINDOW);
        message.setDescription(message.getDescription().replaceAll("%WINDOW%", newContext));
        return message;
    }

    @Override
    public MessageEvent type(Session session, Identifier identifier, String property, String propertyName) {
        MessageEvent message;
        try {
            if (!StringUtil.isNull(property)) {
                TouchAction action = new TouchAction(session.getAppiumDriver());
                action.press(this.getElement(session, identifier, false, false)).release().perform();
                session.getAppiumDriver().getKeyboard().pressKey(property);
            }
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_TYPE);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            if (!StringUtil.isNull(property)) {
                message.setDescription(message.getDescription().replaceAll("%DATA%", ParameterParserUtil.securePassword(property, propertyName)));
            } else {
                message.setDescription(message.getDescription().replaceAll("%DATA%", "No property"));
            }
            return message;
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_TYPE_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
            return message;
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }
    }

    @Override
    public MessageEvent click(Session session, Identifier identifier) {
        MessageEvent message;
        try {
            TouchAction action = new TouchAction(session.getAppiumDriver());
            action.press(this.getElement(session, identifier, false, false))
                    .release()
                    .perform();
            message = new MessageEvent(MessageEventEnum.ACTION_SUCCESS_CLICK);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            return message;
        } catch (NoSuchElementException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_CLICK_NO_SUCH_ELEMENT);
            message.setDescription(message.getDescription().replaceAll("%ELEMENT%", identifier.getIdentifier() + "=" + identifier.getLocator()));
            MyLogger.log(WebDriverService.class.getName(), Level.DEBUG, exception.toString());
            return message;
        } catch (WebDriverException exception) {
            message = new MessageEvent(MessageEventEnum.ACTION_FAILED_SELENIUM_CONNECTIVITY);
            MyLogger.log(WebDriverService.class.getName(), Level.FATAL, exception.toString());
            return message;
        }

    }

    private By getBy(Identifier identifier) {

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding selenium Element : " + identifier.getLocator() + " by : " + identifier.getIdentifier());

        if (identifier.getIdentifier().equalsIgnoreCase("id")) {
            return By.id(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("name")) {
            return By.name(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("class")) {
            return By.className(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("css")) {
            return By.cssSelector(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("xpath")) {
            return By.xpath(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("link")) {
            return By.linkText(identifier.getLocator());

        } else if (identifier.getIdentifier().equalsIgnoreCase("data-cerberus")) {
            return By.xpath("//*[@data-cerberus='" + identifier.getLocator() + "']");

        } else {
            throw new NoSuchElementException(identifier.getIdentifier());
        }
    }

    private WebElement getElement(Session session, Identifier identifier, boolean visible, boolean clickable) {
        AppiumDriver driver = session.getAppiumDriver();
        By locator = this.getBy(identifier);

        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Waiting for Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        try {
            WebDriverWait wait = new WebDriverWait(driver, session.getDefaultWait());
            if (visible) {
                if (clickable) {
                    wait.until(ExpectedConditions.elementToBeClickable(locator));
                } else {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                }
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            }
        } catch (TimeoutException exception) {
            MyLogger.log(RunTestCaseService.class.getName(), Level.FATAL, "Exception waiting for element :" + exception);
            throw new NoSuchElementException(identifier.getIdentifier() + "=" + identifier.getLocator());
        }
        MyLogger.log(RunTestCaseService.class.getName(), Level.DEBUG, "Finding Element : " + identifier.getIdentifier() + "=" + identifier.getLocator());
        return driver.findElement(locator);
    }

    @Override
    public MessageEvent swipe(Session session, SwipeAction action) {
        try {
            // Compute the swipe direction according to the given swipe action
            Dimension window = session.getAppiumDriver().manage().window().getSize();
            SwipeAction.Direction direction;
            switch (action.getActionType()) {
                case UP:
                    direction = SwipeAction.Direction.fromLine(
                            new Line2D.Double(
                                    window.getWidth() / 2,
                                    2 * window.getHeight() / 3,
                                    window.getWidth() / 2,
                                    window.getHeight() / 3
                            )
                    );
                    break;
                case DOWN:
                    direction = SwipeAction.Direction.fromLine(
                            new Line2D.Double(
                                    window.getWidth() / 2,
                                    window.getHeight() / 3,
                                    window.getWidth() / 2,
                                    2 * window.getHeight() / 3
                            )
                    );
                    break;
                case LEFT:
                    direction = SwipeAction.Direction.fromLine(
                            new Line2D.Double(
                                    2 * window.getWidth() / 3,
                                    window.getHeight() / 2,
                                    window.getWidth() / 3,
                                    window.getHeight() / 2
                            )
                    );
                    break;
                case RIGHT:
                    direction = SwipeAction.Direction.fromLine(
                            new Line2D.Double(
                                    window.getWidth() / 3,
                                    window.getHeight() / 2,
                                    2 * window.getWidth() / 3,
                                    window.getHeight() / 2
                            )
                    );
                    break;
                case CUSTOM:
                    direction = action.getCustomDirection();
                    break;
                default:
                    return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                            .resolveDescription("DIRECTION", action.getActionType().name())
                            .resolveDescription("REASON", "Unknown direction");
            }

            // Get the parametrized swipe duration
            Parameter duration = parameters.findParameterByKey(APPIUM_SWIPE_DURATION_PARAMETER, "");

            // Do the swipe thanks to the Appium driver
            session.getAppiumDriver().swipe(
                    direction.getX1(),
                    direction.getY1(),
                    direction.getX2(),
                    direction.getY2(),
                    duration == null ? DEFAULT_APPIUM_SWIPE_DURATION : Integer.parseInt(duration.getValue())
            );
            return new MessageEvent(MessageEventEnum.ACTION_SUCCESS_SWIPE).resolveDescription("DIRECTION", action.getActionType().name());
        } catch (Exception e) {
            LOGGER.warn("Unable to swipe screen due to " + e.getMessage(), e);
            return new MessageEvent(MessageEventEnum.ACTION_FAILED_SWIPE)
                    .resolveDescription("DIRECTION", action.getActionType().name())
                    .resolveDescription("REASON", e.getMessage());
        }
    }
}
