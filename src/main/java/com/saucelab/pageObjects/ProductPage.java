package com.saucelab.pageObjects;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProductPage extends BasePage {
	
	public ProductPage(WebDriver driver)
	{
		super(driver);
	}
	
	@FindBy(className="inventory_item_name")
	private List<WebElement> productNameList;
	
	@FindBy(className="app_logo")
	private WebElement appLogo;
	
	public void displayTotalProductsNames()
	{
		System.out.println( "Total products present : "+ productNameList.size());
		System.out.println("Product Names");
		for(WebElement ele:productNameList)
		{
			System.out.println(ele.getText());
		}
	}
	
	public boolean isAppLogoPresent()
	{
		return appLogo.isDisplayed();
	}

}
