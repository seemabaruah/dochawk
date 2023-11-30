package dynamicpdfvalidator.wipro.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
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
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dynamicpdfvalidator.wipro.pdf.exceptions.ColumnNotFoundException;
import dynamicpdfvalidator.wipro.pdf.exceptions.TagCoordinatesNotFoundException;
import dynamicpdfvalidator.wipro.pdf.utils.PDFUtils;
import dynamicpdfvalidator.wipro.pdf.utils.ServiceFactory;
import dynamicpdfvalidator.wipro.pdf.validation.ValidateRules;
import dynamicpdfvalidator.wipro.utils.Constants;
import dynamicpdfvalidator.wipro.utils.ReportDetails;
import dynamicpdfvalidator.wipro.utils.UIDesign;

public class ValidationReportView implements Runnable {

	private String testPath = "";

	private ReadTestConfig testConfiguration;

	private static ThreadLocal<String> testName = ThreadLocal.withInitial(() -> new String(""));

	private static ThreadLocal<ReadTestConfig> testConfig = new ThreadLocal<ReadTestConfig>();

	private static ThreadLocal<JTree> validationTree = new ThreadLocal<JTree>();

	private static ThreadLocal<JProgressBar> progressBar = new ThreadLocal<JProgressBar>();

	public static ThreadLocal<JPanel> subconfigPanel = new ThreadLocal<JPanel>();

	public static ThreadLocal<String> excelFile = new ThreadLocal<String>();

	public static ThreadLocal<JSONArray> excludeRulesArray = new ThreadLocal<JSONArray>();

	public static ThreadLocal<String> executionID = new ThreadLocal<String>();

	public static ThreadLocal<Integer> excludedRules = new ThreadLocal<Integer>();

	public static ThreadLocal<Integer> failedRules = new ThreadLocal<Integer>();

	public static ThreadLocal<ReportDetails> reportDetails = new ThreadLocal<ReportDetails>();

	public static ThreadLocal<JSONObject> rulesObject = new ThreadLocal<JSONObject>();

	public ValidationReportView() {

	}

	public ValidationReportView(String testPath) {
		
		this.testPath = testPath;

		try {

			this.testConfiguration = new ReadTestConfig(testPath);

		} catch (IOException e) {

			JOptionPane.showMessageDialog(null, "Issue in Reading JSON : Path - " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

		}

	}

	@Override
	public void run() {

		try {

			testName.set(testPath);

			testConfig.set(testConfiguration);

			excelFile.set((String) testConfig.get().getProperty("excelPath"));

			excludeRulesArray.set((JSONArray) testConfig.get().getProperty("excludeRules"));

			/******
			 * 
			 * /****** Execution Summary Details
			 */

			excludedRules.set(0);

			failedRules.set(0);

			rulesObject.set(new JSONObject());

			reportDetails.set(new ReportDetails(
					new File(testName.get()).getParent() + Constants.FILESEPARATOR + "executionsummary.json"));

			executionID.set(String.valueOf(reportDetails.get().getExecutionsummaryJSONObject().keySet().size() + 1));

			reportDetails.get().addObject(reportDetails.get().getExecutionsummaryJSONObject(), executionID.get(),
					Constants.EMPTYJSONOBJECT, "");

			executionID.set(String.valueOf(reportDetails.get().getExecutionsummaryJSONObject().keySet().size() + 1));

			reportDetails.get().addObject(reportDetails.get().getExecutionsummaryJSONObject(), executionID.get(),
					Constants.EMPTYJSONOBJECT, "");

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "jsonfile",
					Constants.STRING, testConfig.get().getProperty("jsonPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "excelfile",
					Constants.STRING, testConfig.get().getProperty("excelPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "pdffile",
					Constants.STRING, testConfig.get().getProperty("documentPath"));

			reportDetails.get().addObject(
					reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()),
					"validationtype", Constants.STRING, testConfig.get().getProperty("validationType"));

			/******
			 * Execution Summary Details
			 */

			JFrame validationFrame = new JFrame();
			validationFrame.setIconImage(Toolkit.getDefaultToolkit()
					.getImage(Constants.IMAGESDIRECTORY + Constants.FILESEPARATOR + "pdf100.png"));

			GraphicsDevice graphicDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

			validationFrame.setSize(graphicDevice.getDisplayMode().getWidth(),
					graphicDevice.getDisplayMode().getHeight() - 100);

			validationFrame.setTitle(testName.get());

			JPanel validationPanel = new JPanel();

			validationPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

			validationPanel.setLayout(new BorderLayout(0, 0));

			initComponents(validationPanel);

			validationFrame.setContentPane(validationPanel);

			validationFrame.setVisible(true);

			validationFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			validatePDF(validationTree.get());

			ServiceFactory.pdDocument.get().close();

			addLog("Closed PDF document : " + testConfig.get().getProperty("pdfPath"), "info.png");

		} catch (IOException | JSONException e) {

			JOptionPane.showMessageDialog(null, e.getMessage(), "Errorwdd", JOptionPane.ERROR_MESSAGE,
					new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

		}

	}

	/*****
	 * 
	 * @param validationPanel
	 * @throws JSONException
	 * @throws IOException
	 */
	private void initComponents(JPanel validationPanel) throws JSONException, IOException {

		JPanel toolPanel = new JPanel();
		toolPanel.setBackground(new Color(102, 205, 170));
		validationPanel.add(toolPanel, BorderLayout.NORTH);

		JLabel lblReportView = UIDesign.getLabel("Report View");
		lblReportView.setHorizontalAlignment(SwingConstants.CENTER);

		GroupLayout gl_toolPanel = new GroupLayout(toolPanel);
		gl_toolPanel.setHorizontalGroup(gl_toolPanel.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
				gl_toolPanel.createSequentialGroup()
						.addComponent(lblReportView, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(335, Short.MAX_VALUE)));
		gl_toolPanel.setVerticalGroup(gl_toolPanel.createParallelGroup(Alignment.LEADING).addComponent(lblReportView,
				GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE));
		toolPanel.setLayout(gl_toolPanel);

		JPanel configPanel = UIDesign.getPanel();
		validationPanel.add(configPanel, BorderLayout.WEST);

		JLabel lblConfiguration = UIDesign.getLabel("Configuration :");

		JLabel validationType = new JLabel("ValidationType : " + testConfig.get().getProperty("validationType"));
		validationType.setFont(new Font("Verdana", Font.PLAIN, 11));
		validationType.setHorizontalAlignment(SwingConstants.CENTER);

		JButton viewReport = UIDesign.getButton("View Report");

		String filePath = new File(reportDetails.get().getExecutionsummaryPath()).toURI().getPath();
		String executionDay = reportDetails.get().getDate().replaceAll(" ", "%20");
		String executionid = String.valueOf(executionID.get());

		viewReport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					URI url = new URI(Constants.DOCHAWKLOCALHOST + "/GenerateReport?filename=" + filePath
							+ "&executionday=" + executionDay + "&executionid=" + executionid);

					java.awt.Desktop.getDesktop().browse(url);

				} catch (java.io.IOException | URISyntaxException e1) {

					JOptionPane.showMessageDialog(null, "Issue in Opening Report Summary : " + e1.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

				}
			}
		});

		GroupLayout gl_configPanel = new GroupLayout(configPanel);
		gl_configPanel.setHorizontalGroup(gl_configPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_configPanel
				.createSequentialGroup().addGap(5)
				.addGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(validationType, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
						.addComponent(viewReport).addComponent(lblConfiguration))
				.addGap(39)));
		gl_configPanel.setVerticalGroup(gl_configPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_configPanel.createSequentialGroup().addGap(15).addComponent(lblConfiguration)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(validationType)
						.addPreferredGap(ComponentPlacement.RELATED).addComponent(viewReport)));
		configPanel.setLayout(gl_configPanel);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(null);
		splitPane.setDividerSize(4);
		splitPane.setResizeWeight(0.5);
		validationPanel.add(splitPane, BorderLayout.CENTER);

		JPanel consolePanel = UIDesign.getPanel();
		consolePanel.setBackground(Color.WHITE);
		consolePanel.setLayout(new BorderLayout(0, 0));

		JPanel tempsubconfigPanel = new JPanel();
		tempsubconfigPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		tempsubconfigPanel.setBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0)));
		tempsubconfigPanel.setBackground(Color.WHITE);
		consolePanel.add(new JScrollPane(tempsubconfigPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		tempsubconfigPanel.setLayout(new BoxLayout(tempsubconfigPanel, BoxLayout.Y_AXIS));

		subconfigPanel.set(tempsubconfigPanel);

		splitPane.setLeftComponent(consolePanel);

		JPanel treeviewPanel = UIDesign.getPanel();
		treeviewPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JTree tree = new ReadJSONTree((String) testConfig.get().getProperty("jsonPath")).readJSONTree;
		System.out.println();
		tree.setFont(new Font("Verdana", Font.PLAIN, 12));
		tree.setCellRenderer(new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"),
				new ImageIcon(Constants.IMAGESDIRECTORY + "leaf.png")));
		validationTree.set(tree);
		treeviewPanel.add(validationTree.get());

		splitPane.setRightComponent(new JScrollPane(treeviewPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		JPanel progressPanel = new JPanel();
		progressPanel.setBackground(new Color(102, 205, 170));
		validationPanel.add(progressPanel, BorderLayout.SOUTH);
		progressPanel.setLayout(new BorderLayout(0, 0));

		JProgressBar tempprogressBar = new JProgressBar();
		tempprogressBar.setMinimum(0);

		tempprogressBar.setMaximum(gettotalRules(excludeRulesArray.get(), validationTree.get(),
				(String) testConfig.get().getProperty("validationType"), testConfig.get()));

		tempprogressBar.setStringPainted(true);
		tempprogressBar.setBackground(Color.WHITE);
		tempprogressBar.setForeground(new Color(51, 204, 51));
		progressBar.set(tempprogressBar);
		progressPanel.add(progressBar.get(), BorderLayout.CENTER);

		/**
		 * Execution Summary Details
		 */

		reportDetails.get().addObject(
				reportDetails.get().getExecutionsummaryJSONObject().getJSONObject(executionID.get()), "totalrules",
				Constants.STRING, String.valueOf(gettotalRules(excludeRulesArray.get(), validationTree.get(),
						(String) testConfig.get().getProperty("validationType"), testConfig.get())));

	}

	/*****
	 * * Validate PDF
	 * 
	 * @throws IOException
	 */

	private void validatePDF(JTree ruleTree) throws IOException {

		Instant startTime = Instant.now();

		int totalRules = 0;

		ServiceFactory.loadPDFDocument((String) testConfig.get().getProperty("documentPath"));

		addLog("Loaded PDF File : " + (String) testConfig.get().getProperty("documentPath"), "info.png");

		ServiceFactory.pdfEndContent.set(PDFUtils.getlastlineinPage(PDFUtils.getPDFContent()));

		PDFUtils.getPDFcontentIntoFile();

		addLog("PDF content loaded into file", "info.png");

		TreeNode treeValidations = ((TreeNode) ruleTree.getModel().getRoot()).getChildAt(0).getChildAt(0);

		int i = 0;
		while (i < treeValidations.getChildCount()) {

			TreeNode panelNode = treeValidations.getChildAt(i);

			addLog("Panel : " + panelNode.toString(), "package.png");

			int j = 0;

			while (j < panelNode.getChildCount()) {

				TreeNode ruleNode = panelNode.getChildAt(j);

				JSONObject ruleMap = new JSONObject();

				JSONObject rulefailureDetails = new JSONObject();

				if (checkruleExcluded(excludeRulesArray.get(), ruleNode.toString())) {

					int k = 0;

					addLog("Rule : " + ruleNode.toString(), "info.png");

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

							JOptionPane.showMessageDialog(null,
									"Issue in Reading Tree : Path - " + panelNode.toString() + " -> "
											+ ruleNode.toString() + " -> " + ruleNode.getChildAt(k).toString(),
									"Error", JOptionPane.ERROR_MESSAGE,
									new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

						}

						k++;

					}

					if (((String) testConfig.get().getProperty("validationType")).equalsIgnoreCase("All")) {

						ValidationReportView.parseandExecute(ruleMap, rulefailureDetails, ruleNode);

						totalRules += 1;

					} else {

						if (ruleMap.getString("type")
								.equalsIgnoreCase((String) testConfig.get().getProperty("validationType"))) {

							ValidationReportView.parseandExecute(ruleMap, rulefailureDetails, ruleNode);

							totalRules += 1;

						}

					}

				}

				progressBar.get().setValue(totalRules);

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

			ArrayList<Object> validationOutput = ValidateRules.parseJSON(ValidationReportView.excelFile.get(), ruleMap);

			if (ruleMap.has("display")) {
				if (ruleMap.getString("display").equalsIgnoreCase("false")) {
					if (!(boolean) validationOutput.get(0)) {
						addLog((String) validationOutput.get(1), "pass.png");
					} else {
						/***
						 * Execution Summary
						 */

						rulefailureDetails.put("failurereason", "Text should not present in the PDF, but found");

						rulefailureDetails.put("failuretype", "PDFIssue");

						rulefailureDetails.put("ruletype", ruleMap.getString("type"));

						rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

						failedRules.set(failedRules.get() + 1);

						/***
						 * Execution Summary
						 */
						addLog("Text should not present in the PDF, but found", "closebtn.png");

						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("result:fail");

						newNode.setAllowsChildren(false);

						((DefaultMutableTreeNode) ruleNode).add(newNode);

						((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

						newNode = new DefaultMutableTreeNode(
								"reason:" + "Text should not present in the PDF, but found");

						newNode.setAllowsChildren(false);

						((DefaultMutableTreeNode) ruleNode).add(newNode);

						((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

						progressBar.get().setForeground(new Color(255, 133, 102));
					}
				}
			} else {

				if ((boolean) validationOutput.get(0)) {

					addLog((String) validationOutput.get(1), "pass.png");

				} else {

					/***
					 * Execution Summary
					 */

					rulefailureDetails.put("failurereason", validationOutput.get(1).toString());

					rulefailureDetails.put("failuretype", "PDFIssue");

					rulefailureDetails.put("ruletype", ruleMap.getString("type"));

					rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

					failedRules.set(failedRules.get() + 1);

					/***
					 * Execution Summary
					 */

					addLog((String) validationOutput.get(1), "closebtn.png");

					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("result:fail");

					newNode.setAllowsChildren(false);

					((DefaultMutableTreeNode) ruleNode).add(newNode);

					((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

					newNode = new DefaultMutableTreeNode("reason:" + validationOutput.get(1).toString());

					newNode.setAllowsChildren(false);

					((DefaultMutableTreeNode) ruleNode).add(newNode);

					((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

					progressBar.get().setForeground(new Color(255, 133, 102));

				}

			}

		} catch (PatternSyntaxException | IOException | ColumnNotFoundException | TagCoordinatesNotFoundException e) {

			/***
			 * Execution Summary
			 */
			rulefailureDetails.put("failurereason", e.getMessage());

			rulefailureDetails.put("failuretype", "InputIssue");

			rulefailureDetails.put("ruletype", ruleMap.getString("type"));

			failedRules.set(failedRules.get() + 1);

			rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

			/***
			 * Execution Summary
			 */

			addLog(e.getMessage(), "exception.png");

			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("result:fail");

			newNode.setAllowsChildren(false);

			((DefaultMutableTreeNode) ruleNode).add(newNode);

			((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

			newNode = new DefaultMutableTreeNode("reason:" + e.getMessage());

			newNode.setAllowsChildren(false);

			((DefaultMutableTreeNode) ruleNode).add(newNode);

			((DefaultTreeModel) validationTree.get().getModel()).reload(ruleNode);

			progressBar.get().setForeground(new Color(255, 133, 102));

		} catch (IndexOutOfBoundsException e) {

			if (!ruleMap.getString("type").equalsIgnoreCase("exclude")) {

				rulefailureDetails.put("failurereason", e.getMessage());

				rulefailureDetails.put("failuretype", "InputIssue");

				rulefailureDetails.put("ruletype", ruleMap.getString("type"));

				failedRules.set(failedRules.get() + 1);

				rulesObject.get().put(ruleNode.toString(), rulefailureDetails);

				addLog(e.getMessage(), "exception.png");

			}

		}

	}

	/******
	 * Add Validation Log
	 */

	public static void addLog(String validationMessage, String validationIcon) {

		JLabel validationRule = new JLabel(validationMessage);

		validationRule.setBorder(new EmptyBorder(1, 1, 4, 4));

		validationRule.setIcon(new ImageIcon(Constants.IMAGESDIRECTORY + validationIcon));

		validationRule.setHorizontalAlignment(SwingConstants.LEFT);

		validationRule.setFont(new Font("Verdana", Font.PLAIN, 11));

		subconfigPanel.get().add(validationRule);

		subconfigPanel.get().revalidate();

		subconfigPanel.get().repaint();

	}

	/******
	 * Total Rule Count
	 */

	public int gettotalRules(JSONArray excludeRules, JTree validationTree, String ruleType,
			ReadTestConfig configObject) {

		int totalRules = 0;

		TreeNode treeValidations = ((TreeNode) validationTree.getModel().getRoot()).getChildAt(0).getChildAt(0);

		int i = 0;

		while (i < treeValidations.getChildCount()) {

			TreeNode panelNode = treeValidations.getChildAt(i);

			int j = 0;

			while (j < panelNode.getChildCount()) {

				TreeNode ruleNode = panelNode.getChildAt(j);

				JSONObject ruleMap = new JSONObject();

				int k = 0;

				if (checkruleExcluded(excludeRules, ruleNode.toString())) {

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

							JOptionPane.showMessageDialog(null,
									"Issue in Reading Tree : Path - " + panelNode.toString() + " -> "
											+ ruleNode.toString() + " -> " + ruleNode.getChildAt(k).toString(),
									"Error", JOptionPane.ERROR_MESSAGE,
									new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

						}

						k++;

					}

					if (((String) configObject.getProperty("validationType")).equalsIgnoreCase("All")) {

						totalRules += 1;

					} else {

						if (ruleMap.getString("type")
								.equalsIgnoreCase((String) configObject.getProperty("validationType"))) {

							totalRules += 1;

						}

					}

				}

				j++;

			}

			i++;
		}

		return totalRules;

	}

	/**
	 * Check Rule Excluded
	 */

	public static boolean checkruleExcluded(JSONArray rulesArray, String ruleName) {
		boolean result = true;
		for (int i = 0; i < rulesArray.length(); i++) {
			if (rulesArray.getString(i).equalsIgnoreCase(ruleName)) {
				result = false;
				break;
			}
		}
		return result;
	}

}
