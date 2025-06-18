package utilities;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;

public class ExtentManager {

    private static ExtentReports extent;
    public static  File reportFile;

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String path = generateReportPath();
            reportFile = new File(path);
          //Create HTML file and add Configuration
            ExtentSparkReporter htmlReporter=new ExtentSparkReporter(reportFile);
    	    htmlReporter.viewConfigurer().viewOrder().as(new ViewName[]{ViewName.DASHBOARD,ViewName.TEST,ViewName.LOG});
    		htmlReporter.config().setEncoding("utf-8");
    		htmlReporter.config().setDocumentTitle("Test Summary Report");
    		htmlReporter.config().setReportName("Test Summary Report");
    		htmlReporter.config().setTheme(Theme.DARK);
    		
    		//creating object of ExtentReports
    		extent=new ExtentReports();
    		extent.attachReporter(htmlReporter);
    		extent.setSystemInfo("OS", System.getProperty("os.name"));
        }
        return extent;
    }

    private static String generateReportPath() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH_mm_ss"));
        return Paths.get(System.getProperty("user.dir"),"target","reports", "TestReport_" + timestamp + ".html").toString();
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
