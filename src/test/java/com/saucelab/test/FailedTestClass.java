package com.saucelab.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.practice.rough.BaseTest;

import utilities.DriverManager;

public class FailedTestClass extends BaseTest {

	@Test
	public void testFail() {
		String browser = "chrome";
		openBrowser(browser);
		DriverManager.getDriver().get("https://www.saucedemo.com/");
		info("URL Launched");
		System.out.println("About to fail");
		if(DriverManager.getDriver()!=null)
			System.out.println("Not null");
		Assert.fail("Deliberate fail");
	}

}
