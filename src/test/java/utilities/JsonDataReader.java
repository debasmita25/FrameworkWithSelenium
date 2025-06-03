package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Set;

import org.testng.annotations.DataProvider;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonDataReader {

	// To read test data from json file
	@DataProvider
	public Object[][] getData(Method m) {
		Object[][] data = null;
		String testName = m.getName();
		String dataFileName = m.getDeclaringClass().getName();

		if (dataFileName.toLowerCase().contains("testclass")) {

			String filePath = Paths
					.get(System.getProperty("user.dir"), "src", "test", "resources", "testdata", "testclassdata.json")
					.toString();
			data = readJsonData(filePath, testName);
		}

		return data;
	}

	private Object[][] readJsonData(String filePath, String testName) {
		Object[][] data = null;
		try {
			JsonElement jElement = JsonParser.parseReader(new FileReader(filePath));
			if (jElement != null) {
				JsonObject jObject = jElement.getAsJsonObject();
				JsonArray jArray = jObject.getAsJsonArray(testName);
				int totalRecords = jArray.size();
				HashMap<String, String> dataMap = null;
				data = new Object[totalRecords][1];
				int i = 0;
				for (; i < totalRecords; i++) {
					dataMap = new HashMap<String, String>();
					JsonObject jobj = jArray.get(i).getAsJsonObject();
					for (String key : jobj.keySet()) {
						dataMap.put(key, jobj.get(key).getAsString());
					}
					data[i][0] = dataMap;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

}
