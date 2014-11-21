/*******************************************************************************
 * Copyright (c) 2010-2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.selenium.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.skalli.selenium.pageobjects.concrete.LoginFailedPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.LoginPage;
import org.eclipse.skalli.selenium.pageobjects.concrete.MainPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.PageFactory;

import com.thoughtworks.selenium.SeleniumException;

/**
 * The DriverProvider can be used to get a {@link org.openqa.selenium.WebDriver} instance which
 * will be instantiated, started, stopped when the program terminates and which will {@link org.openqa.selenium.WebDriver#get(String)}
 * the base url ({@link #getHTTPBaseUrl()}).
 * <br/>
 * The DriverProvider provides a method to {@link #login(WebDriver)} on the page and a method to be able to navigate to the
 * base url ({@link #navigateToBaseUrl(WebDriver)}).
 */
public class DriverProvider {
    /**
     * The host system property "tomcat.host" or "localhost" by default
     */
    private static final String TOMCAT_HOST = System.getProperty("tomcat.host", "localhost");

    /**
    * The port system property "tomcat.http.port" or "8080" by default
    */
    private static final int TOMCAT_HTTP_PORT = Integer.parseInt(System.getProperty("tomcat.http.port", "8080"));

    /**
     * The folder containing the browser plugins
     */
    private static final File BROWSER_PLUGINS_FOLDER = new File("browser_plugins");

    /**
     * Directory where test content can be copied from
     */
    private static final File TEST_CONTENT_DIR = new File("testdata");

    /**
     * Storage directory for test content
     */
    private static final File STORAGE_DIR = new File("workdir/storage");

    /**
     * The driver
     */
    private static WebDriver driver;

    /**
     * The shutdown hook
     */
    private static Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            stopDriver();
        }
    };

    /**
     * Returns the {@link org.openqa.selenium.WebDriver} instance used for the tests.
     * The driver navigates to the URL returned by  {@link #getHTTPBaseUrl()}
     * @return The {@link org.openqa.selenium.WebDriver} instance used for the tests.
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            startDriver();
        }
        return driver;
    }

    /**
    * Restarts the driver (means it stops the driver and then starts it again).
    * @return The driver to be used now
    */
    public static WebDriver restartDriver() {
        stopDriver();
        startDriver();

        return driver;
    }

    /**
     * Deletes all cookies so the login page can be displayed again
     */
    public static void goToLoginPage() {
        driver.manage().deleteAllCookies();
        navigateToBaseUrl(driver);

        //waits for the login page to be displayed
        LoginPage login = PageFactory.initElements(driver, LoginPage.class);
        login.isDisplayedWithExplicitWait();
    }

    /**
     * Starts the driver, adds a shutdown hook to stop the driver and navigates to {@link #getHTTPBaseUrl()}
     */
    private static void startDriver() {
        FirefoxProfile profile = new FirefoxProfile();

        //load the browser plugins
        if (BROWSER_PLUGINS_FOLDER.exists()) {
            File[] plugins = BROWSER_PLUGINS_FOLDER.listFiles();

            for (int i = 0; i < plugins.length; i++) {
                File plugin = plugins[i];
                String name = plugin.getName();

                if (!name.toLowerCase().contains(".xpi")) {
                    System.err.println("\"" + name + "\" is not a plugin -> remove from " + BROWSER_PLUGINS_FOLDER);
                    continue;
                }

                //pluginName = "firebug-" + FIREBUG_VERSION + ".xpi"
                //pluginName = "firepath-" + FIREPATH_VERSION + "-fx.xpi"

                //set up the plugin if needed
                if (name.toLowerCase().contains("firebug")) {
                    //pluginName = "firebug-" + FIREBUG_VERSION + ".xpi"
                    String version = name.substring("firebug-".length());
                    version = version.substring(0, version.length() - ".xpi".length());

                    //avoids that the first run page is shown
                    profile.setPreference("extensions.firebug.currentVersion", version);
                }

                //add the plugin
                try {
                    profile.addExtension(plugin);
                } catch (IOException e) {
                    System.err.println("could not add extension \"" + name + "\" to the profile\n" + e);
                    e.printStackTrace();
                }
            }
        }

        driver = new FirefoxDriver(profile);
        registerShutdownHook();
        driver.get(getHTTPBaseUrl());

        copyTestContent();
    }

    private static void copyTestContent() {
        try {
            FileUtils.copyDirectory(TEST_CONTENT_DIR, STORAGE_DIR);
        } catch (IOException e) {
            throw new SeleniumException("failed to copy test content", e);
        }
    }

    private static void removeTestContent() {
        try {
            FileUtils.deleteDirectory(STORAGE_DIR);
        } catch (IOException e) {
            throw new SeleniumException("failed to delete test content", e);
        }
    }

    /**
     * Stops the driver
     */
    private static void stopDriver() {
        driver.quit();
        driver = null;
        removeTestContent();
    }

    /**
     * Registers a shutdown hook via {@link Runtime#addShutdownHook(Thread)}
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * Returns the http base URL
     * @return the http base URL
     */
    private static String getHTTPBaseUrl() {
        return "http://" + TOMCAT_HOST + ":" + TOMCAT_HTTP_PORT;
    }

    /**
     * Navigates to the base url ({@link #getHTTPBaseUrl()})
     * @param driver The driver
     */
    public static void navigateToBaseUrl(WebDriver driver) {
        driver.navigate().to(getHTTPBaseUrl());
    }

    /**
     * Navigates to "{@link #getHTTPBaseUrl()} + {@code subUrl}"
     * @param driver The driver
     * @param subUrl The sub url
     */
    public static void navigateToSubUrl(WebDriver driver, String subUrl) {
        driver.navigate().to(getHTTPBaseUrl() + subUrl);
    }

    /**
     * Try to login with the given user name and password
     * @param driver The {@link org.openqa.selenium.WebDriver}
     * @param username The user name
     * @param password The password
     * @return {@code true} if the login was possible, {@code false} otherwise
     */
    public static boolean login(WebDriver driver, String username, String password) {
        try {
            LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
            LoginFailedPage loginFailedPage = PageFactory.initElements(driver, LoginFailedPage.class);
            MainPage mainPage = PageFactory.initElements(driver, MainPage.class);

            driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            while (loginPage.isDisplayed()) {
                loginPage.sendKeysToUserNameAndPasswordFieldAndSubmit(username, password);

                driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);

                //avoiding that the driver is positioned at getHTTPBaseUrl() + "/favicon.ico"
                //maybe a bug!?
                try {
                    if (!loginFailedPage.isDisplayed() && !mainPage.isDisplayed()) {
                        navigateToBaseUrl(driver);
                    }
                } catch (Exception e) {
                }

                //if the login page is displayed the login was successful
                try {
                    if (mainPage.isDisplayed()) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        } catch (NoSuchElementException e) {
            return false;
        }

        //should never be reached
        return true;
    }

    /**
     * Try to login with user name = "admin" and password = "admin"
     * @param driver The {@link org.openqa.selenium.WebDriver}
     * @return {@code true} if the login was possible, {@code false} otherwise
     */
    public static boolean login(WebDriver driver) {
        return login(driver, "admin", "admin");
    }

    /**
     * Returns whether the driver is located on the login page or not
     * @param driver The driver
     * @return {@code true} if the driver is on the login page {@code false} otherwise
     */
    public static boolean isOnLoginPage(WebDriver driver) {
        try {
            LoginPage login = PageFactory.initElements(driver, LoginPage.class);
            driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
            login.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }

        return true;
    }
}
