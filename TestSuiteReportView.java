package dynamicpdfvalidator.wipro.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dynamicpdfvalidator.wipro.pdf.exceptions.ColumnNotFoundException;
import dynamicpdfvalidator.wipro.pdf.exceptions.TagCoordinatesNotFoundException;
import dynamicpdfvalidator.wipro.pdf.utils.PDFUtils;
import dynamicpdfvalidator.wipro.pdf.utils.ServiceFactory;
import dynamicpdfvalidator.wipro.pdf.validation.ValidateRules;
import dynamicpdfvalidator.wipro.restassured.utilities.JIRA;
import dynamicpdfvalidator.wipro.utils.Constants;
import dynamicpdfvalidator.wipro.utils.ReportDetails;
import dynamicpdfvalidator.wipro.utils.TestSuiteConfig;
import dynamicpdfvalidator.wipro.utils.UIDesign;

public class TestSuiteReportView implements Runnable {

	public static HashMap<String, JSONObject> testsuiteSummary = new HashMap<String, JSONObject>();

	private static ThreadLocal<JSONObject> testsuiteObject = new ThreadLocal<JSONObject>();

	private static ReadTestConfig testConfiguration;

	private static ThreadLocal<String> testName = ThreadLocal.withInitial(() -> new String(""));

	private static ThreadLocal<ReadTestConfig> testConfig = new ThreadLocal<ReadTestConfig>();

	private static ThreadLocal<JTree> validationTree = new ThreadLocal<JTree>();

	public static ThreadLocal<String> excelFile = new ThreadLocal<String>();

	public static ThreadLocal<String> executionID = new ThreadLocal<String>();

	public static ThreadLocal<Integer> excludedRules = new ThreadLocal<Integer>();

	public static ThreadLocal<Integer> failedRules = new ThreadLocal<Integer>();

	public static ThreadLocal<ReportDetails> reportDetails = new ThreadLocal<ReportDetails>();

	public static ThreadLocal<JSONObject> rulesObject = new ThreadLocal<JSONObject>();

	private static ThreadLocal<JSONObject> testsuiterunnerObject = new ThreadLocal<JSONObject>();

	private static ThreadLocal<String> testsuiteName = new ThreadLocal<String>();

	private static JPanel subconfigPanel;

	private static JProgressBar progressBar;

	private String suiteName;

	private JSONObject suiteObject;

	private static JSONObject jiraObject;

	private static JIRA jira;

	private static int passCount = 0, failCount = 0;

	public TestSuiteReportView() {

	}

	public TestSuiteReportView(String suiteName, JSONObject testsuiteObject) {

		try {

			this.suiteName = suiteName;

			this.suiteObject = testsuiteObject.getJSONObject("testsuite");

			TestSuiteReportView.jiraObject = testsuiteObject.getJSONObject("jiradetails"); 
			
			jira = new JIRA(TestSuiteReportView.jiraObject);
			

		} catch (IOException | RuntimeException e) {
			
			JOptionPane.showMessageDialog(null, "Issue in Executing Test Suite : " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

		}
	}

	/*****
	 * 
	 * @param validationPanel
	 * @throws JSONException
	 * @throws IOException
	 */

	private void initComponents(JPanel validationPanel) {

		JPanel toolPanel = new JPanel();
		toolPanel.setBackground(new Color(102, 205, 170));
		validationPanel.add(toolPanel, BorderLayout.NORTH);

		JLabel lblReportView = UIDesign.getLabel("Testsuite Report View");
		lblReportView.setHorizontalAlignment(SwingConstants.CENTER);

		GroupLayout gl_toolPanel = new GroupLayout(toolPanel);
		gl_toolPanel.setHorizontalGroup(gl_toolPanel.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
				gl_toolPanel.createSequentialGroup()
						.addComponent(lblReportView, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(335, Short.MAX_VALUE)));
		gl_toolPanel.setVerticalGroup(gl_toolPanel.createParallelGroup(Alignment.LEADING).addComponent(lblReportView,
				GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE));
		toolPanel.setLayout(gl_toolPanel);

		JPanel configPanel = UIDesign.getPanel();
		validationPanel.add(configPanel, BorderLayout.WEST);

		JLabel lblConfiguration = UIDesign.getLabel("Test Suite Summary :");

		JLabel suiteName = new JLabel("Test Suite Name : " + testsuiteName.get());
		suiteName.setFont(new Font("Verdana", Font.PLAIN, 11));
		suiteName.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel totalTests = new JLabel("Total Tests : " + testsuiterunnerObject.get().keySet().size());
		totalTests.setFont(new Font("Verdana", Font.PLAIN, 11));
		totalTests.setHorizontalAlignment(SwingConstants.CENTER);

		JButton viewReport = UIDesign.getButton("View Report");

		String temptestsuitename = testsuiteName.get();

		viewReport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				try {

					String testsuitePath = new File(Constants.TESTSUITERUNNERDIRECTORY + Constants.FILESEPARATOR
							+ temptestsuitename + Constants.FILESEPARATOR + "testsuite.json").toURI().getPath();

					String dochawkHome = new File(Constants.TESTSUITEDIRECTORY).toURI().getPath();

					URI url = new URI(Constants.DOCHAWKLOCALHOST + "/GenerateTestSuiteReport?filename=" + testsuitePath
							+ "&dochawkhome=" + dochawkHome);

					java.awt.Desktop.getDesktop().browse(url);

				} catch (java.io.IOException | URISyntaxException e1) {

					JOptionPane.showMessageDialog(null, "Issue in Opening Report Summary : " + e1.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

				}
			}
		});

		GroupLayout gl_configPanel = new GroupLayout(configPanel);
		gl_configPanel.setHorizontalGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_configPanel.createSequentialGroup().addGap(5)
						.addGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(suiteName, GroupLayout.PREFERRED_SIZE, 356, GroupLayout.PREFERRED_SIZE)
								.addComponent(totalTests, GroupLayout.PREFERRED_SIZE, 356, GroupLayout.PREFERRED_SIZE)
								.addComponent(viewReport, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblConfiguration))
						.addGap(39)));
		gl_configPanel.setVerticalGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_configPanel.createSequentialGroup().addGap(15).addComponent(lblConfiguration)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(suiteName)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(totalTests)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(viewReport)));

		configPanel.setLayout(gl_configPanel);

		JPanel consolePanel = UIDesign.getPanel();
		consolePanel.setBackground(Color.WHITE);
		consolePanel.setLayout(new BorderLayout(0, 0));

		subconfigPanel = new JPanel();
		subconfigPanel.setLayout(new BoxLayout(subconfigPanel, BoxLayout.Y_AXIS));
		subconfigPanel.setBackground(Color.WHITE);
		consolePanel.add(new JScrollPane(subconfigPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		validationPanel.add(consolePanel, BorderLayout.CENTER);
		JPanel progressPanel = new JPanel();
		progressPanel.setBackground(new Color(102, 205, 170));
		validationPanel.add(progressPanel, BorderLayout.SOUTH);
		progressPanel.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(testsuiterunnerObject.get().keySet().size());
		progressBar.setStringPainted(true);
		progressBar.setBackground(Color.WHITE);
		progressBar.setForeground(new Color(51, 204, 51));

		progressPanel.add(progressBar, BorderLayout.CENTER);

	}

	@Override
	public void run() {

		passCount = 0;

		failCount = 0;

		Instant startTime = Instant.now();

		testsuiteName.set(this.suiteName.toUpperCase());

		testsuiterunnerObject.set(this.suiteObject);

		JFrame validationFrame = new JFrame();

		validationFrame.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(Constants.IMAGESDIRECTORY + Constants.FILESEPARATOR + "pdf100.png"));

		GraphicsDevice graphicDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		validationFrame.setSize(graphicDevice.getDisplayMode().getWidth(),
				graphicDevice.getDisplayMode().getHeight() - 100);
		validationFrame.setTitle("DocHawk - TestSuite Summary");

		JPanel validationPanel = new JPanel();

		validationPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		validationPanel.setLayout(new BorderLayout(0, 0));

		initComponents(validationPanel);

		validationFrame.setContentPane(validationPanel);

		validationFrame.setVisible(true);

		validationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		addLog("Loaded Test Suite configuration Successfully for : " + testsuiteName.get(), "info.png", subconfigPanel);

		int count = 1;

		for (String tName : testsuiterunnerObject.get().keySet()) {

			testName.set(Constants.TESTSUITEDIRECTORY + Constants.FILESEPARATOR
					+ testsuiterunnerObject.get().getJSONObject(tName).getString("path"));

			try {

				testConfiguration = new ReadTestConfig(testName.get());

				testConfig.set(testConfiguration);

				runTest(tName);

				progressBar.setValue(count);

			} catch (IOException e) {

				addLog("Issue in running test : " + tName + ": " + e.getMessage(), "closebtn.png", subconfigPanel);

			}
			if (((JSONObject) TestSuiteReportView.testsuiteSummary.get(tName)).getString("status")
					.equalsIgnoreCase("fail")) {

				failCount += 1;

				addLog("Test " + tName + " Successfully Executed", "closebtn.png", subconfigPanel);

				updateJIRA(tName.substring(0, tName.lastIndexOf(".")), "FAIL");

			} else {

				passCount += 1;

				addLog("Test " + tName + " Successfully Executed", "pass.png", subconfigPanel);

				updateJIRA(tName.substring(0, tName.lastIndexOf(".")), "PASS");

			}

			count++;

		}

		try {

			JSONObject testsuiteObject = new TestSuiteConfig(testsuiteName.get()).testsuiteObject;

			for (String testName : TestSuiteReportView.testsuiteSummary.keySet()) {

				testsuiteObject.getJSONObject("testsuite").getJSONObject(testName).put("executionid",
						((JSONObject) TestSuiteReportView.testsuiteSummary.get(testName)).getString("executionid"));

				testsuiteObject.getJSONObject("testsuite").getJSONObject(testName).put("executionday",
						((JSONObject) TestSuiteReportView.testsuiteSummary.get(testName)).getString("executionday"));

				testsuiteObject.getJSONObject("testsuite").getJSONObject(testName).put("totalrules",
						((JSONObject) TestSuiteReportView.testsuiteSummary.get(testName)).getString("totalrules"));

				testsuiteObject.getJSONObject("testsuite").getJSONObject(testName).put("status",
						((JSONObject) TestSuiteReportView.testsuiteSummary.get(testName)).getString("status"));

			}

			new TestSuiteConfig(testsuiteObject, new File(Constants.TESTSUITERUNNERDIRECTORY + Constants.FILESEPARATOR
					+ testsuiteName.get() + Constants.FILESEPARATOR + "testsuite.json"));

		} catch (IOException e) {

			e.printStackTrace();

		}

		addLog("Test Suite Execution Completed Successfully", "info.png", subconfigPanel);

		addLog("-------------------------------------------------------------------------------------", "info.png",
				subconfigPanel);

		addLog("Total Tests : " + testsuiterunnerObject.get().keySet().size(), "info.png", subconfigPanel);

		addLog("Tests Passed : " + passCount, "info.png", subconfigPanel);

		addLog("Tests Failed : " + failCount, "info.png", subconfigPanel);

		addLog("Total Time : " + new ReportDetails().gettotalTime(startTime), "info.png", subconfigPanel);

		addLog("-------------------------------------------------------------------------------------", "info.png",
				subconfigPanel);

	}

	private static void updateJIRA(String testName, String testStatus) {

		if ((boolean) jiraObject.get("jiraUpdate")) {

			try {

				jira.updateTestStatus(testName, testStatus);

				addLog("Successfully updated JIRA status for Test :" + testName, "info.png", subconfigPanel);

			} catch (IOException | RuntimeException e) {

				addLog("Issue in updating JIRA status for Test :" + testName + " , Error Message : " + e.getMessage(),
						"info.png", subconfigPanel);

			}

		}

	}

	public static void runTest(String testcaseName) throws IOException, JSONException {

		try {

			excelFile.set((String) testConfig.get().getProperty("excelPath"));

			/******
			 * Execution Summary Details
			 */

			excludedRules.set(0);

			failedRules.set(0);

			rulesObject.set(new JSONObject());

			testsuiteObject.set(new JSONObject());

			reportDetails.set(new ReportDetails(
					new File(testName.get()).getParent() + Constants.FILESEPARATOR + "executionsummary.json"));
			executionID.set(String.valueOf(reportDetails.get().getExecutionsummaryJSONObject().keySet().size() + 1));

			reportDetails.get().addObject(reportDetails.get().getExecutionsummaryJSONObject(), executionID.get(),
					Constants.EMPTYJSONOBJECT, "");

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "jsonfile",
					Constants.STRING, (String) testConfig.get().getProperty("jsonPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "excelfile",
					Constants.STRING, (String) testConfig.get().getProperty("excelPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "pdffile",
					Constants.STRING, testConfig.get().getProperty("pdfPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()),
					"validationtype", Constants.STRING, testConfig.get().getProperty("validationType"));

			/******
			 * Execution Summary Details
			 */

			JTree tree = new ReadJSONTree((String) testConfig.get().getProperty("jsonPath")).readJSONTree;

			tree.setFont(new Font("Verdana", Font.PLAIN, 12));

			tree.setCellRenderer(new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"),
					new ImageIcon(Constants.IMAGESDIRECTORY + "leaf.png")));

			validationTree.set(tree);

			/**
			 * Execution Summary Details
			 */
			String totalRules = String.valueOf(new ValidationReportView().gettotalRules(
					(JSONArray) testConfig.get().getProperty("excludeRules"), validationTree.get(),
					(String) testConfig.get().getProperty("validationType"), testConfig.get()));

			testsuiteObject.get().put("executionid", String.valueOf(executionID.get()));

			testsuiteObject.get().put("executionday", reportDetails.get().getDate());

			testsuiteObject.get().put("totalrules", totalRules);

			testsuiteObject.get().put("status", "pass");

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "totalrules",
					Constants.STRING, totalRules);

			validatePDF(validationTree.get());

			ServiceFactory.pdDocument.get().close();

			for (String testObject : TestSuiteReportView.testsuiteSummary.keySet()) {

				TestSuiteReportView.testsuiteSummary.remove(testObject);

			}

			TestSuiteReportView.testsuiteSummary.put(testcaseName, testsuiteObject.get());

		} catch (IOException e) {

			throw new IOException("Issue in running test case : " + testcaseName + "," + e.getMessage());

		} catch (JSONException e) {

			throw new JSONException("Issue in running test case : " + testcaseName + "," + e.getMessage());

		}

	}

	/*****
	 * * Validate PDF
	 * 
	 * @throws IOException
	 */

	private static void validatePDF(JTree ruleTree) throws IOException {

		Instant startTime = Instant.now();

		JSONArray excludeRuleArray = (JSONArray) testConfig.get().getProperty("excludeRules");

		ServiceFactory.loadPDFDocument((String) testConfig.get().getProperty("pdfPath"));

		ServiceFactory.pdfEndContent.set(PDFUtils.getlastlineinPage(PDFUtils.getPDFContent()));

		PDFUtils.getPDFcontentIntoFile();

		TreeNode treeValidations = ((TreeNode) ruleTree.getModel().getRoot()).getChildAt(0).getChildAt(0);

		int i = 0;

		while (i < treeValidations.getChildCount()) {

			TreeNode panelNode = treeValidations.getChildAt(i);

			int j = 0;

			while (j < panelNode.getChildCount()) {

				TreeNode ruleNode = panelNode.getChildAt(j);

				JSONObject ruleMap = new JSONObject();

				JSONObject rulefailureDetails = new JSONObject();

				if (ValidationReportView.checkruleExcluded(excludeRuleArray, ruleNode.toString())) {

					int k = 0;

					while (k < ruleNode.getChildCount()) {

						try {

							String ruleValue = ruleNode.getChildAt(k).toString();
							String key = ruleValue.substring(0, ruleValue.indexOf(":")).trim();

							String value = ruleValue.substring(ruleValue.indexOf(":") + 1, ruleValue.length()).trim();

							if (key.equalsIgnoreCase("headers")) {

								String[] headerData = value.split(",");

								JSONArray headers = new JSONArray();

								for (int h = 0; h < headerData.length; h++) {

									headers.put(headerData[h]);

								}

								ruleMap.put(key, headers);

							} else {

								ruleMap.put(key, value);

							}

						} catch (StringIndexOutOfBoundsException | JSONException e) {

						}

						k++;

					}

					if (((String) testConfig.get().getProperty("validationType")).equalsIgnoreCase("All")) {

						TestSuiteReportView.parseandExecute(ruleMap, rulefailureDetails, ruleNode);

					} else {

						if (ruleMap.getString("type")
								.equalsIgnoreCase((String) testConfig.get().getProperty("validationType"))) {

							TestSuiteReportView.parseandExecute(ruleMap, rulefailureDetails, ruleNode);

						}

					}

				}

				j++;

			}

			i++;

		}
		/**
		 * Execution Summary Details
		 */

		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()),
				"excludedrulescount", Constants.STRING, String.valueOf(excludedRules.get()));

		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()),
				"failedrulescount", Constants.STRING, String.valueOf(failedRules.get()));

		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "executiontime",
				Constants.STRING, reportDetails.get().gettotalTime(startTime));
		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "validationtype",
				Constants.STRING, testConfig.get().getProperty("validationType"));

		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "rules",
				Constants.JSONOBJECTWITHVALUE, rulesObject.get());

		reportDetails.get().saveexecutionSummaryDetails();
	}

	/****
	 * Parse And Execute
	 */

	public static void parseandExecute(JSONObject ruleMap, JSONObject rulefailureDetails, TreeNode ruleNode) {

		try {

			if (ruleMap.getString("type").equalsIgnoreCase("exclude")) {

				excludedRules.set(excludedRules.get() + 1);

			}

			ArrayList<Object> validationOutput = ValidateRules.parseJSON(TestSuiteReportView.excelFile.get(), ruleMap);

			if (ruleMap.has("display")) {
				if (ruleMap.getString("display").equalsIgnoreCase("false")) {
					if ((boolean) validationOutput.get(0)) {

						/***
						 * Execution Summary
						 */

						rulefailureDetails.put("failurereason", validationOutput.get(1).toString());

						rulefailureDetails.put("failuretype", "PDFIssue");

						rulefailureDetails.put("ruletype", ruleMap.getString("type"));

						rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

						failedRules.set(failedRules.get() + 1);

						testsuiteObject.get().put("status", "fail");

						/***
						 * Execution Summary
						 */

					}
				}
			} else {

				if (!(boolean) validationOutput.get(0)) {

					/***
					 * Execution Summary
					 */

					rulefailureDetails.put("failurereason", validationOutput.get(1).toString());

					rulefailureDetails.put("failuretype", "PDFIssue");

					rulefailureDetails.put("ruletype", ruleMap.getString("type"));

					rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

					failedRules.set(failedRules.get() + 1);

					testsuiteObject.get().put("status", "fail");

					/***
					 * Execution Summary
					 */

				}
			}
		} catch (PatternSyntaxException | IOException | ColumnNotFoundException | TagCoordinatesNotFoundException
				| NullPointerException e) {

			/***
			 * Execution Summary
			 */

			rulefailureDetails.put("failurereason", e.getMessage());

			rulefailureDetails.put("failuretype", "InputIssue");

			rulefailureDetails.put("ruletype", ruleMap.getString("type"));

			failedRules.set(failedRules.get() + 1);

			rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

			testsuiteObject.get().put("status", "fail");

			/***
			 * Execution Summary
			 */

		} catch (IndexOutOfBoundsException e) {

			if (!ruleMap.getString("type").equalsIgnoreCase("exclude")) {

				rulefailureDetails.put("failurereason", e.getMessage());

				rulefailureDetails.put("failuretype", "InputIssue");
				rulefailureDetails.put("ruletype", ruleMap.getString("type"));

				failedRules.set(failedRules.get() + 1);

				rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

				testsuiteObject.get().put("status", "fail");

			}

		}

	}

	/******
	 * Add Validation Log
	 */

	public static void addLog(String validationMessage, String validationIcon, JPanel subconfigPanel) {

		JLabel validationRule = new JLabel(validationMessage);

		validationRule.setBorder(new EmptyBorder(1, 1, 4, 4));

		validationRule.setIcon(new ImageIcon(Constants.IMAGESDIRECTORY + validationIcon));

		validationRule.setHorizontalAlignment(SwingConstants.LEFT);

		validationRule.setFont(new Font("Verdana", Font.PLAIN, 11));

		subconfigPanel.add(validationRule);

		subconfigPanel.revalidate();

		subconfigPanel.repaint();

	}

}