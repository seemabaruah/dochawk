package dynamicpdfvalidator.wipro.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.json.JSONTokener;

public class ReadTestConfig {

	JSONObject configObject = null;

	public ReadTestConfig(String filePath) throws IOException {

		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(filePath)), StandardCharsets.UTF_8))) {
			JSONObject jsonContent = new JSONObject(new JSONTokener(in));

			configObject = jsonContent.getJSONObject("testConfig");
		} catch (IOException e) {
			throw new IOException("Issue in Reading Test Configuration : " + e.getMessage());
		}

	}

	public Object getProperty(String propertyName) {
		if (configObject.has(propertyName)) {
			return configObject.get(propertyName);
		}
		return null;
	}

}
