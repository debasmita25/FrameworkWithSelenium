package com.practice.rough;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import utilities.DriverManager;

public class FirstTest2 extends BaseTest {

	@Test(dataProvider = "getData")
	public void doLogin(String username, String password, String browser) {

		openBrowser(browser);
		DriverManager.getDriver().get("https://www.saucedemo.com/");
		info("URL Launched");
		DriverManager.getDriver().findElement(By.id("user-name")).sendKeys(username);
		DriverManager.getDriver().findElement(By.id("password")).sendKeys(password);
		DriverManager.getDriver().findElement(By.id("login-button")).submit();
		info("Username and Password entered and submitted");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WebElement appLogo = DriverManager.getDriver().findElement(By.className("app_logo"));
		Assert.assertTrue(appLogo.isDisplayed());
		info("app Logo is asserted true");
		// quitBrowser();

	}

	@DataProvider
	public Object[][] getData() {
		Object[][] data = new Object[2][3];
		data[0][0] = "standard_user";
		data[0][1] = "secret_sauce";
		data[0][2] = "chrome";

		data[1][0] = "standard_user";
		data[1][1] = "secret_sauce1";
		data[1][2] = "firefox";

		return data;

	}

	@Parameters({ "browser" })
	@Test
	public void testFail(String browser) {
		openBrowser(browser);
		DriverManager.getDriver().get("https://www.saucedemo.com/");
		info("URL Launched");
		System.out.println("About to fail");
		Assert.fail("Deliberate fail");
	}

}
