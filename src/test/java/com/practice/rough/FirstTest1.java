package com.practice.rough;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import utilities.DriverManager;

public class FirstTest1 extends BaseTest {

	@Test(dataProvider="getData")
	public void doLogin(String username,String password,String browser) {
		
		openBrowser(browser);
		DriverManager.getDriver().get("https://www.saucedemo.com/");
		DriverManager.getDriver().findElement(By.id("user-name")).sendKeys(username);
		DriverManager.getDriver().findElement(By.id("password")).sendKeys(password);
		DriverManager.getDriver().findElement(By.id("login-button")).submit();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WebElement appLogo=DriverManager.getDriver().findElement(By.className("app_logo"));
		Assert.assertTrue(appLogo.isDisplayed());
		
		quitBrowser();

	}

	@DataProvider(parallel = true)
	public Object[][] getData() {
		Object[][] data = new Object[2][3];
		data[0][0] = "standard_user";
		data[0][1] = "secret_sauce";
		data[0][2] = "chrome";

		data[1][0] = "";
		data[1][1] = "secret_savisual_useruce";
		data[1][2] = "firefox";

		return data;

	}

}
