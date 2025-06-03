package utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadLocalLogger {
	private static ThreadLocal<Logger> threadLocalLogger = new ThreadLocal<>();

	public static void setLogger(Class<?> clazz) {
		threadLocalLogger.set(LogManager.getLogger(clazz));
	}

	public static Logger getLogger() {
		return threadLocalLogger.get();
	}

	public static void removeLogger() {
		threadLocalLogger.remove();
	}

}
