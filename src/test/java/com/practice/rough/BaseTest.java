package com.practice.rough;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;

import utilities.DriverManager;
import utilities.ExtentManager;
import utilities.ThreadLocalLogger;

public class BaseTest {

	//private WebDriver driver;
	protected SoftAssert sf = new SoftAssert();
	private ExtentReports ext;

	@BeforeSuite(alwaysRun = true)
	public void initializeExtentReport() {

		ext = ExtentManager.getInstance(); // Initializes only once
		// System.out.println(">>> Extent Report initialized");
		//info(">>> Extent Report initialized");
	}

	@AfterSuite(alwaysRun = true)
	public void flushExtentReport() {

		ExtentManager.flushReports();
		// System.out.println(">>> Extent Report flushed");
		/*
		 * try { Desktop.getDesktop().browse(ExtentManager.reportFile.toURI()); } catch
		 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
         //info(">>> Extent Report flushed");
	}

	@BeforeMethod
	public void setupLogger() {

		System.out.println(this.getClass().getName());
		ThreadLocalLogger.setLogger(this.getClass());
		// System.out.println(">>> Logger initialized");
	}

	@AfterMethod
	public void removeLogger() {
		ThreadLocalLogger.removeLogger();
		// System.out.println(">>> Logger removed");
	}


	public void info(String message) {
		Logger logger = ThreadLocalLogger.getLogger();
		logger.info(message);
	}

	public void openBrowser(String browser) {
		WebDriver driver=null;
		if (browser.equals("chrome")) {
			System.out.println("Launching  : " + browser);
			ChromeOptions options = new ChromeOptions();
			// Disable Chrome's password manager and warnings
			options.addArguments("--disable-save-password-bubble");
			options.setExperimentalOption("prefs",
					Map.of("credentials_enable_service", false, "profile.password_manager_enabled", false));
			options.addArguments("--headless");
			options.addArguments("--disable-gpu"); // Applicable to Windows
			options.addArguments("--window-size=1920,1080"); // Recommended for visual tests
			options.addArguments("--no-sandbox"); // Useful in CI/CD pipelines
			options.addArguments("--disable-dev-shm-usage"); // Helps with memory issues in Docker

			driver = new ChromeDriver(options);
			info("Launching  : " + browser);
		} else if (browser.equals("firefox")) {
			System.out.println("Launching : " + browser);
			// Create Firefox Profile
			FirefoxProfile profile = new FirefoxProfile();

			// Disable Firefox password manager and warnings
			profile.setPreference("signon.rememberSignons", false); // Disable save password prompt
			profile.setPreference("signon.autofillForms", false); // Disable autofill
			profile.setPreference("signon.autologin.proxy", false); // Disable proxy login autofill
			profile.setPreference("signon.passwordEditCapture.enabled", false); // Disable password change capture
			profile.setPreference("security.insecure_password.ui.enabled", false); // Disable insecure password warning

			// Set profile in FirefoxOptions
			FirefoxOptions options = new FirefoxOptions();
			options.setProfile(profile);
			options.addArguments("-headless"); // Enable headless mode
			driver = new FirefoxDriver(options);
			info("Launching  : " + browser);
		} else if (browser.equals("edge")) {
			System.out.println("Launching : " + browser);
			// Create Edge Options
			EdgeOptions options = new EdgeOptions();
			options.addArguments("--headless=new"); // Use --headless=new for Chromium-based headless
			options.addArguments("--disable-gpu");
			options.addArguments("--window-size=1920,1080");
			// Disable password manager and warnings
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("credentials_enable_service", false);
			prefs.put("profile.password_manager_enabled", false);

			options.setExperimentalOption("prefs", prefs);
			options.addArguments("--disable-save-password-bubble");

			// Optional: reduce automation detection (optional)
			options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
			options.setExperimentalOption("useAutomationExtension", false);

			driver = new EdgeDriver(options);
			info("Launching  : " + browser);
		}
		DriverManager.setDriver(driver);
		DriverManager.getDriver().manage().window().maximize();
		DriverManager.getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	}
 
	@AfterMethod
	public void quitBrowser() {
		
		if (DriverManager.getDriver() != null)
		{
			DriverManager.getDriver().quit();
		}
		
	}

}
