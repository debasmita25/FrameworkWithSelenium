package utilities;

import org.openqa.selenium.WebDriver;

public class DriverManager {

	private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();

	public static void setDriver(WebDriver driverInstance) {
		driver.set(driverInstance);
	}

	public static WebDriver getDriver() {
		return driver.get();
	}

	public static void removeDriver() {
		driver.remove();
	}
}
