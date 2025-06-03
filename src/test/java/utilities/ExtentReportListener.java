package utilities;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentTest;
import com.practice.rough.BaseTest;

public class ExtentReportListener extends BaseTest implements ITestListener {

	private static ThreadLocal<ExtentTest> testReport = new ThreadLocal<ExtentTest>();
	
	@Override
	public void onTestStart(ITestResult result) {
		String testName = result.getMethod().getMethodName();
		String moduleName = result.getMethod().getTestClass().getName();
		String browserInfo = result.getTestContext().getCurrentXmlTest().getParameter("browser");
		ExtentTest test = ExtentManager.getInstance().createTest(testName + "_" + moduleName + "_" + browserInfo);
		testReport.set(test);
		System.out.println(">> onTestStart triggered");
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		testReport.get().pass("Test Passed");
		testReport.remove();
		System.out.println("onTest success");
	}

	@Override
	public void onTestFailure(ITestResult result) {
		testReport.get().fail(result.getThrowable());
		testReport.remove();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		testReport.get().skip("Test is Skipped");
		testReport.remove();
	}

	@Override
	public void onStart(ITestContext context) {

		 
	}
	
	
	@Override
	public void onFinish(ITestContext context) {

		
		
	}

}
