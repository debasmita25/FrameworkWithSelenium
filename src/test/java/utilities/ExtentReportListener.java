package utilities;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.practice.rough.BaseTest;

public class ExtentReportListener extends BaseTest implements ITestListener {

	private static ThreadLocal<ExtentTest> testReport = new ThreadLocal<ExtentTest>();
	private static LocalDateTime ldt = LocalDateTime.now();
	private static DateTimeFormatter dft = DateTimeFormatter.ofPattern("dd_MM_yyyy_hh_mm");
	private static String filename = dft.format(ldt);
	private static String screenshotFolderPath = Paths
			.get(System.getProperty("user.dir"), "target", "screenshots", filename).toString();
	public static int count = 1;

	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String moduleName = result.getMethod().getTestClass().getName();
		String browserInfo = result.getTestContext().getCurrentXmlTest().getParameter("browser");
		ExtentTest test = ExtentManager.getInstance().createTest(testName + "_" + moduleName + "_" + browserInfo);
		testReport.set(test);
		System.out.println(">> onTestStart triggered");
		info(">> onTestStart triggered");
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		testReport.get().pass("Test Passed");
		testReport.remove();
		info("onTestSuccess");
		System.out.println("onTest success");
	}

	@Override
	public void onTestFailure(ITestResult result) {
		info("onTest failure");
		testReport.get().fail(result.getThrowable());
		String errorScreenshotName = result.getMethod().getMethodName();
		// Create folder if not exists
		File folder = new File(screenshotFolderPath);
		if (!folder.exists()) {
			folder.mkdirs(); // creates all directories if needed
		}
		File destFile = new File(folder + File.separator + errorScreenshotName + "_" + (count++) + ".png");
		if (DriverManager.getDriver() != null)
			CaptureScreenshot.clickScreenshot(destFile, DriverManager.getDriver());
		info("Screenshot is taken");
		testReport.remove();
		info("onTest failure");
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		testReport.get().skip("Test is Skipped");
		testReport.remove();
		info("Test is Skipped");
	}

	@Override
	public void onStart(ITestContext context) {

	}

	@Override
	public void onFinish(ITestContext context) {

	}

}
