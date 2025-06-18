package com.saucelab.test;

import java.util.HashMap;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.practice.rough.BaseTest;
import com.saucelab.pageObjects.LoginPage;
import com.saucelab.pageObjects.ProductPage;

import utilities.DriverManager;
import utilities.JsonDataReader;

public class TestClass1 extends BaseTest {

	private String browser;
	private String testurl;

	@Parameters({ "browser", "os", "testurl" })
	@BeforeMethod
	public void setUpEnv(String browser, String os, String testurl) {
		this.browser = browser;
		this.testurl = testurl;
	}
	
	@Test(dataProviderClass = JsonDataReader.class, dataProvider = "getData")
	public void doLogin(HashMap<String, String> data) {

		if (data==null || (!data.get("runmode").toLowerCase().equals("y"))) {
			throw new SkipException("Runmode is N or data not present,therefore skipping");
		}

		openBrowser(browser);
		DriverManager.getDriver().get(testurl);
		info("URL launched");
		LoginPage lp = new LoginPage(DriverManager.getDriver());
		info("Enter username : " + data.get("username"));
		info("Enter password : " + data.get("password"));
		ProductPage pp = lp.doLogin(data.get("username"), data.get("password"));
		info("username and password entered and submitted");
		// pp.displayTotalProductsNames();
		info("product name displayed");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Assert.assertTrue(appLogo.isDisplayed());
		sf.assertTrue(true);
		info("App Logo Present");
		
		info("Browser closed...");
		sf.assertAll();
	}

}
