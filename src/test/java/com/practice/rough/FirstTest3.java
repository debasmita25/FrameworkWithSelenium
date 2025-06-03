package com.practice.rough;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.practice.rough.BaseTest;
import com.saucelab.pageObjects.LoginPage;
import com.saucelab.pageObjects.ProductPage;

public class FirstTest3 extends BaseTest {

	@Test(dataProvider="getData")
	public void doLogin(String username,String password,String browser) {
		
		openBrowser(browser);
		getDriver().get("https://www.saucedemo.com/");
		info("URL launched");
		LoginPage lp=new LoginPage(getDriver());
		ProductPage pp=lp.doLogin(username, password);
		info("username and password entered and submitted");
		//pp.displayTotalProductsNames();
		info("product name displayed");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//WebElement appLogo=getDriver().findElement(By.className("app_logo"));
		//Assert.assertTrue(appLogo.isDisplayed());
		
		quitBrowser();

	}

	@DataProvider()
	public Object[][] getData() {
		Object[][] data = new Object[2][3];
		data[0][0] = "standard_user";
		data[0][1] = "secret_sauce";
		data[0][2] = "chrome";

		data[1][0] = "visual_user";
		data[1][1] = "secret_sauce";
		data[1][2] = "edge";

		return data;

	}

}
