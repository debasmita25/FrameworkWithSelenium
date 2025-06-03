package com.saucelab.pageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {
	
	public LoginPage(WebDriver driver)
	{
		super(driver);	
	}
	
	@FindBy(id="user-name")
	private WebElement username;
	
	@FindBy(id="password")
	private WebElement password;
	
	@FindBy(id="login-button")
	private WebElement submitButton;
	
	public ProductPage doLogin(String usrname,String pass)
	{
		username.sendKeys(usrname);
		password.sendKeys(pass);
		submitButton.click();
		return new ProductPage(driver);
	}

}
