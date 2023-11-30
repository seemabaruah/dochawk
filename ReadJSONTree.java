package dynamicpdfvalidator.wipro.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ReadJSONTree {

	public JTree readJSONTree;

	public ReadJSONTree(String filePath) throws IOException, JSONException {

		readJSONTree(filePath);

	}

	private void readJSONTree(String filePath) throws IOException, JSONException {
		
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(new FileInputStream(new File(filePath)), StandardCharsets.UTF_8))) {

			DefaultMutableTreeNode rootNode = null;

			JSONObject jsonContent = new JSONObject(new JSONTokener(in));

			for (String contentObject : jsonContent.keySet()) {

				rootNode = new DefaultMutableTreeNode(contentObject);

				JSONObject pdfContent = jsonContent.getJSONObject(contentObject);

				for (String pdfObject : pdfContent.keySet()) {

					DefaultMutableTreeNode pdfdocuemntNode = new DefaultMutableTreeNode(pdfObject);

					DefaultMutableTreeNode pageValidations = new DefaultMutableTreeNode("pageValidations");

					JSONArray validationArray = pdfContent.getJSONArray("pages").getJSONObject(0)
							.getJSONArray("pageValidations");

					for (int i = 0; i < validationArray.length(); i++) {

						JSONObject validationRuleObject = validationArray.getJSONObject(i);

						for (String validationRule : validationRuleObject.keySet()) {

							DefaultMutableTreeNode sectionRules = new DefaultMutableTreeNode(validationRule);

							JSONArray validationObjectArray = validationRuleObject.getJSONArray(validationRule);

							for (int j = 0; j < validationObjectArray.length(); j++) {

								JSONObject sectionvalidationRules = validationObjectArray.getJSONObject(j);

								for (String sectionvalidationRule : sectionvalidationRules.keySet()) {

									DefaultMutableTreeNode sectionsubRules = new DefaultMutableTreeNode(
											sectionvalidationRule);

									JSONObject sectionchildvalidationRules = sectionvalidationRules
											.getJSONObject(sectionvalidationRule);

									DefaultMutableTreeNode ruleNode = null;

									for (Object sectionchildvalidationRule : sectionchildvalidationRules.keySet()) {

										if (sectionchildvalidationRules
												.get(sectionchildvalidationRule.toString()) instanceof String) {

											ruleNode = new DefaultMutableTreeNode(sectionchildvalidationRule.toString()
													+ ":" + sectionchildvalidationRules
															.getString(sectionchildvalidationRule.toString()));

										} else if (sectionchildvalidationRules
												.get(sectionchildvalidationRule.toString()) instanceof JSONArray) {

											JSONArray headersList = sectionchildvalidationRules
													.getJSONArray(sectionchildvalidationRule.toString());

											String headers = "";

											for (int k = 0; k < headersList.length(); k++) {

												headers += headersList.getString(k) + ",";

											}

											ruleNode = new DefaultMutableTreeNode(sectionchildvalidationRule.toString()
													+ ":" + headers.substring(0, headers.lastIndexOf(",")));

										}

										ruleNode.setAllowsChildren(false);

										sectionsubRules.add(ruleNode);

									}

									sectionRules.add(sectionsubRules);

								}

							}

							pageValidations.add(sectionRules);

						}

					}

					pdfdocuemntNode.add(pageValidations);

					rootNode.add(pdfdocuemntNode);

				}

			}

			readJSONTree = new JTree(rootNode);

		} catch (IOException e) {

			throw new IOException("Issue in Reading JSON : Path - " + e.getMessage());

		} catch (JSONException e) {

			throw new JSONException("Issue in Reading JSON : Path - " + e.getMessage());

		}

	}

}