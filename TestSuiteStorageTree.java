package dynamicpdfvalidator.wipro.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.poi.hssf.util.HSSFColor.WHITE;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.json.JSONArray;
import org.json.JSONObject;

import NewEdits.Validator.HtmlValidationReportView;
import dynamicpdfvalidator.wipro.utils.Constants;
import dynamicpdfvalidator.wipro.utils.ReadConfigFile;
import dynamicpdfvalidator.wipro.utils.UIDesign;
import dynamicpdfvalidator.wipro.views.PDFDialog;
import dynamicpdfvalidator.wipro.views.PDFExecutor;
import dynamicpdfvalidator.wipro.views.PDFValidatorHome;

public class TestSuiteStorageTree {

	public JTree createtestStorageTree;

	JPopupMenu parentpopupMenu;

	JPopupMenu childpopupMenu;

	JPopupMenu leafpopupMenu;

	JPopupMenu rootpopupMenu;

	public TestSuiteStorageTree() {

		getTestStorageTree();

	}

	public void getTestStorageTree() {

		File testSuiteStorage = new File(Constants.TESTSUITEDIRECTORY);

		if (!testSuiteStorage.exists()) {

			testSuiteStorage.mkdir();

		}

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(testSuiteStorage.getName());

		createtestStorageTree = new JTree(getTreeView(rootNode, new File(Constants.TESTSUITEDIRECTORY)));

		createtestStorageTree.setBorder(null);
		createtestStorageTree.setVisibleRowCount(50);
		createtestStorageTree.setFont(new Font("Verdana", Font.PLAIN, 11));

		createtestStorageTree.addMouseListener(addTreeMouseListener());

		createtestStorageTree.setFont(new Font("Verdana", Font.PLAIN, 12));

		createtestStorageTree
				.setCellRenderer(new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"),
						new ImageIcon(Constants.IMAGESDIRECTORY + "test.png")));

		parentpopupMenu = new JPopupMenu();

		childpopupMenu = new JPopupMenu();

		leafpopupMenu = new JPopupMenu();

		rootpopupMenu = new JPopupMenu();

		parentpopupMenu.add(createMenuItem(Constants.PACKAGE));

		parentpopupMenu.add(createMenuItem(Constants.REFRESH));

		childpopupMenu.add(createMenuItem(Constants.PACKAGE));

		childpopupMenu.add(createMenuItem(Constants.RENAME));

		childpopupMenu.add(createMenuItem(Constants.ADDTEST));

		childpopupMenu.add(createMenuItem(Constants.DELETE));

		leafpopupMenu.add(createMenuItem(Constants.DELETE));

		leafpopupMenu.add(createMenuItem(Constants.RENAME));

		leafpopupMenu.add(createMenuItem(Constants.OPENJSON));

	}

	/**********
	 * Tree View
	 */

	public static DefaultMutableTreeNode getTreeView(DefaultMutableTreeNode rootNode, File fileName) {

		File[] files = fileName.listFiles();

		for (File file : files) {

			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file.getName());

			if (file.isDirectory()) {

				rootNode.add(newNode);

				getTreeView(newNode, file);

			} else {

				if (file.getName().substring(file.getName().lastIndexOf(".")).equalsIgnoreCase(".json")
						&& !file.getName().equalsIgnoreCase("executionsummary.json")
						&& !file.getName().equalsIgnoreCase("comments.json")) {

					newNode.setAllowsChildren(false);

					rootNode.add(newNode);

				}

			}

		}
		return rootNode;

	}

	/***********
	 * Tree Mouse Listeners
	 */

	private MouseListener addTreeMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getButton() == MouseEvent.BUTTON3) {

					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createtestStorageTree
							.getLastSelectedPathComponent();

					try {
						if (currentNode.isRoot()) {

							parentpopupMenu.show(createtestStorageTree, e.getX(), e.getY());

						} else {

							if (currentNode.getAllowsChildren()) {

								childpopupMenu.show(createtestStorageTree, e.getX(), e.getY());

							} else {

								leafpopupMenu.show(createtestStorageTree, e.getX(), e.getY());

							}

						}
					} catch (NullPointerException e1) {

						JOptionPane.showMessageDialog(null, "Please Select Node and Do Right Click");

					}

				}
			}
		};
	}

	/***********
	 * ActionListeners
	 */

	private ActionListener getRefreshActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				PDFExecutor.createtestStorage.remove(PDFExecutor.createtestStorageTree);

				PDFExecutor.createtestStorageTree = new TestSuiteStorageTree().createtestStorageTree;

				PDFExecutor.createtestStorage.add(PDFExecutor.createtestStorageTree);

				PDFExecutor.createtestStorage.revalidate();

				PDFExecutor.createtestStorage.repaint();

			}

		};

	}

	private ActionListener getRenameActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				PDFDialog.getaddtestDialog(Constants.RENAME, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getPackageActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				PDFDialog.getaddtestDialog(Constants.PACKAGE, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getAddTestActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				PDFDialog.getaddtestDialog(Constants.ADDTEST, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getOpenJSONActionListener() {

		return new ActionListener() {

			@SuppressWarnings("unused")
			@Override
			public void actionPerformed(ActionEvent e) {

				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createtestStorageTree
						.getLastSelectedPathComponent();

				TreeNode[] treeNodes = currentNode.getPath();

				String filePath = Constants.USERDIRECTORY + Constants.FILESEPARATOR;

				int i = 0;

				while (i < treeNodes.length - 1) {

					filePath += treeNodes[i].toString() + Constants.FILESEPARATOR;

					i++;

				}

				filePath += treeNodes[treeNodes.length - 1].toString();

				int tabCount = PDFExecutor.tabbedPane.getTabCount();

				boolean result = false;

				i = 0;

				while (i < tabCount) {

					if (PDFExecutor.tabbedPane.getToolTipTextAt(i).toString().equalsIgnoreCase(filePath)) {

						result = true;

						break;

					}

					i++;

				}

				if (!result) {

					try {

						ReadTestConfig configObject = new ReadTestConfig(filePath);

						final File testFile = new File(filePath);

						JPanel configPanel = UIDesign.getPanel();
						PDFExecutor.tabbedPane.add(configPanel);
						PDFExecutor.tabbedPane.setTabComponentAt(PDFExecutor.tabbedPane.indexOfComponent(configPanel),
								UIDesign.getTitlePanel(PDFExecutor.tabbedPane, configPanel, testFile.getName(),
										"test.png"));
						PDFExecutor.tabbedPane.setSelectedIndex(PDFExecutor.tabbedPane.getTabCount() - 1);
						PDFExecutor.tabbedPane.setToolTipTextAt(PDFExecutor.tabbedPane.getTabCount() - 1, filePath);
						configPanel.setLayout(new GridLayout(1, 0, 0, 0));

						JPanel subconfigPanel = UIDesign.getPanel();
						configPanel.add(subconfigPanel);
						
						JPanel documentValidationTypePanel = new JPanel();
						documentValidationTypePanel.setBackground(Color.WHITE);
						documentValidationTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
						
						JLabel documentValidationTypeLabel = UIDesign.getLabel("Document Validation Type : ");
						documentValidationTypeLabel.setPreferredSize(new Dimension(176, 20));
						
						String documentValidationTypeList[] = { "HTML", "PDF" };
						@SuppressWarnings("unchecked")
						JComboBox<Object> documentValidationType = UIDesign.getcomboBox(documentValidationTypeList);
						documentValidationType.setPreferredSize(new Dimension(83, 20));

						
						documentValidationTypePanel.add(documentValidationTypeLabel);
						documentValidationTypePanel.add(documentValidationType);
						
						JPanel validationTypePanel = new JPanel();
						validationTypePanel.setBackground(Color.WHITE);
						validationTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));
						
						JLabel validationTypelbl = UIDesign.getLabel("Validation Type : ");
						validationTypelbl.setPreferredSize(new Dimension(109, 20));
						
						String items[] = { "All", "text", "dynamic" };
						@SuppressWarnings("unchecked")

						final JComboBox<Object> validationType = UIDesign.getcomboBox(items);
						validationType.setPreferredSize(new Dimension(150, 20));
						
						validationTypePanel.add(validationTypelbl);
						validationTypePanel.add(validationType);

						JLabel lblSelectRule = new JLabel("Select Rules To Exclude:");
						lblSelectRule.setFont(new Font("Verdana", Font.PLAIN, 11));

						DefaultListModel<String> model = new DefaultListModel<>();
						JList<String> ruleList = new JList<String>(model);
						ruleList.setBackground(Color.WHITE);
						ruleList.setFont(new Font("Verdana", Font.PLAIN, 11));
						ruleList.setCursor(new Cursor(Cursor.HAND_CURSOR));
						JScrollPane listScroller = new JScrollPane(ruleList);

						JSONArray excludeRules = null;

						if (configObject.getProperty("excludeRules") != null) {
							excludeRules = (JSONArray) configObject.getProperty("excludeRules");
						}

						PDFDialog.getModelContent((String) configObject.getProperty("jsonPath"), model, listScroller,
								excludeRules, ruleList);

						validationType.setSelectedItem(configObject.getProperty("validationType"));
						
						documentValidationType.setSelectedItem(configObject.getProperty("documentValidationType"));

						JLabel jsonpathLabel = UIDesign.getLabel("JSON File Path *");

						final JTextField jsonFilePath = UIDesign.getTextField("JSON File Path");

						jsonFilePath.setText((String) configObject.getProperty("jsonPath"));

						JButton jsonbrowseBtn = UIDesign.getButton("Browse");

						jsonbrowseBtn.addActionListener(PDFDialog.getBrowseActionListener("json", jsonFilePath, model,
								listScroller, null, ruleList));

						final JTextField pdfFilePath = UIDesign.getTextField("Document File Path");

						pdfFilePath.setText((String) configObject.getProperty("documentPath"));

						JLabel pdfpathLabel = UIDesign.getLabel("Document File Path *");

						JButton pdfbrowseBtn = UIDesign.getButton("Browse");
						
						pdfbrowseBtn.addActionListener(new ActionListener() {
							
							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								
								if(documentValidationType.getSelectedItem().toString().equals("HTML")) {
									getFileChooser(pdfFilePath, "html");
								}else if(documentValidationType.getSelectedItem().toString().equals("PDF")) {
									getFileChooser(pdfFilePath, "pdf");
								}
							}
						});

//						pdfbrowseBtn.addActionListener(PDFDialog.getBrowseActionListener("html", pdfFilePath, model,
//								listScroller, null, ruleList));

						JLabel excelpathLabel = UIDesign.getLabel("Excel File Path");

						final JTextField excelFilePath = UIDesign.getTextField("Excel File Path");

						excelFilePath.setText((String) configObject.getProperty("excelPath"));

						JButton excelbrowseBtn = UIDesign.getButton("Browse");

						excelbrowseBtn.addActionListener(PDFDialog.getBrowseActionListener("xlsx", excelFilePath, model,
								listScroller, null, ruleList));

						JButton saveBtn = UIDesign.getButton("Save");
						saveBtn.setBackground(new Color(88, 212, 117));
						saveBtn.setIcon(
								new ImageIcon(Constants.IMAGESDIRECTORY + Constants.FILESEPARATOR + "save.png"));

						final JButton validateBtn = UIDesign.getButton("Validate");
						validateBtn.setBackground(new Color(255, 153, 0));
						validateBtn.setIcon(
								new ImageIcon(Constants.IMAGESDIRECTORY + Constants.FILESEPARATOR + "save.png"));
						validateBtn.setEnabled(false);
						validateBtn.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {

								if(documentValidationType.getSelectedItem().toString().equals("HTML")) {
		 							 
		 							 Runnable runnable = new HtmlValidationReportView(testFile.getAbsolutePath());
		 							 
		 							 new Thread(runnable).start();
		 							 
		 						 }else if(documentValidationType.getSelectedItem().toString().equals("PDF")) {
		 							 
		 							Runnable runnable = new ValidationReportView(testFile.getAbsolutePath());

									new Thread(runnable).start();
		 							 
		 						 }

							}

						});

						saveBtn.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {

								if (jsonFilePath.getText().trim().length() > 0
										&& pdfFilePath.getText().trim().length() > 0) {

									if (!new File(jsonFilePath.getText()).exists()
											|| !new File(pdfFilePath.getText()).exists()) {

										JOptionPane.showMessageDialog(null, "Please check File Path Specified");

									} else {

										try (OutputStreamWriter writer = new OutputStreamWriter(
												new FileOutputStream(testFile), StandardCharsets.UTF_8)) {

											testFile.createNewFile();

											JSONObject jsonObject = new JSONObject();

											JSONObject configObject = new JSONObject();

											JSONArray excluderulesArray = new JSONArray();

											for (String rule : ruleList.getSelectedValuesList()) {
												excluderulesArray.put(rule);
											}
		 									 
		 									configObject.put("documentValidationType", 
		 											 documentValidationType.getSelectedItem().toString());

											configObject.put("validationType",
													validationType.getSelectedItem().toString());

											configObject.put("jsonPath", jsonFilePath.getText());

											configObject.put("documentPath", pdfFilePath.getText());

											configObject.put("excelPath", excelFilePath.getText());

											configObject.put("excludeRules", excluderulesArray);

											jsonObject.put("testConfig", configObject);

											writer.write(jsonObject.toString(4));

											JOptionPane.showMessageDialog(null, "Test Configuration Successfully Saved",
													"Success", JOptionPane.INFORMATION_MESSAGE,
													new ImageIcon(Constants.IMAGESDIRECTORY + "success.png"));

											validateBtn.setEnabled(true);

										} catch (IOException e1) {

											JOptionPane.showMessageDialog(null,
													"Error in Creating New Test File : " + e1.getMessage());

										}
									}

								} else {

									JOptionPane.showMessageDialog(null, "Please fill all the mandatory fields");

								}

							}

						});

						GroupLayout gl_subconfigPanel = new GroupLayout(subconfigPanel);
						gl_subconfigPanel.setHorizontalGroup(gl_subconfigPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_subconfigPanel.createSequentialGroup().addContainerGap()
										.addGroup(gl_subconfigPanel.createParallelGroup(Alignment.LEADING)
												.addComponent(jsonFilePath, GroupLayout.PREFERRED_SIZE, 259,
														GroupLayout.PREFERRED_SIZE)
												.addGroup(gl_subconfigPanel
														.createParallelGroup(Alignment.TRAILING, false)
														.addComponent(jsonpathLabel, Alignment.LEADING,
																GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
																Short.MAX_VALUE)
														.addComponent(documentValidationTypePanel, Alignment.LEADING, 0, 259, 
																Short.MAX_VALUE)
														.addComponent(validationTypePanel,GroupLayout.PREFERRED_SIZE, 259,
																GroupLayout.PREFERRED_SIZE))
												.addComponent(jsonbrowseBtn)
												.addComponent(lblSelectRule, GroupLayout.PREFERRED_SIZE, 180,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(listScroller, GroupLayout.PREFERRED_SIZE, 259,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(pdfpathLabel, GroupLayout.PREFERRED_SIZE, 180,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(pdfFilePath, GroupLayout.PREFERRED_SIZE, 259,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(pdfbrowseBtn)
												.addComponent(excelpathLabel, GroupLayout.PREFERRED_SIZE, 112,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(excelFilePath, GroupLayout.PREFERRED_SIZE, 259,
														GroupLayout.PREFERRED_SIZE)
												.addComponent(excelbrowseBtn).addComponent(saveBtn)
												.addComponent(validateBtn))
										.addContainerGap(971, Short.MAX_VALUE)));

						gl_subconfigPanel.setVerticalGroup(gl_subconfigPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_subconfigPanel.createSequentialGroup().addContainerGap()
										.addComponent(documentValidationTypePanel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(validationTypePanel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(jsonpathLabel)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(jsonFilePath, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(jsonbrowseBtn, GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(lblSelectRule, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(listScroller, GroupLayout.PREFERRED_SIZE,
												GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(pdfpathLabel, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pdfFilePath, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pdfbrowseBtn, GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(excelpathLabel, GroupLayout.DEFAULT_SIZE, 16, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(excelFilePath, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(excelbrowseBtn, GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(saveBtn)
										.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(validateBtn)
										.addGap(616)));
						subconfigPanel.setLayout(gl_subconfigPanel);

						PDFExecutor.tabbedPane.setSelectedIndex(PDFExecutor.tabbedPane.getTabCount() - 1);

					} catch (IOException e2) {

						JOptionPane.showMessageDialog(null, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE,
								new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

					}

				} else {

					PDFExecutor.tabbedPane.setSelectedIndex(i);

				}
			}

		};

	}

	/***********
	 * Menu
	 */

	private JMenuItem createMenuItem(String menuName) {

		JMenuItem menuItem = new JMenuItem(menuName);

		menuItem.setFont(new Font("Verdana", Font.PLAIN, 12));

		switch (menuName) {

		case Constants.PACKAGE:

			menuItem.addActionListener(getPackageActionListener());

			break;

		case Constants.ADDTEST:

			menuItem.addActionListener(getAddTestActionListener());

			break;

		case Constants.OPENJSON:

			menuItem.addActionListener(getOpenJSONActionListener());

			break;

		case Constants.DELETE:

			menuItem.addActionListener(getDeleteActionListener());
			
			break;

		case Constants.RENAME:

			menuItem.addActionListener(getRenameActionListener());

			break;

		case Constants.REFRESH:

			menuItem.addActionListener(getRefreshActionListener());

		default:

			break;

		}
		return menuItem;
	}

	private ActionListener getDeleteActionListener() {
		// TODO Auto-generated method stub
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				DefaultTreeModel model = (DefaultTreeModel) createtestStorageTree.getModel();
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createtestStorageTree.getLastSelectedPathComponent();
				
				String msg = "Do you really want to delete: " + currentNode.toString();
				if(!currentNode.toString().endsWith(".json")) {
					msg+=" and its contents ?";
				}
				
				int input = JOptionPane.showConfirmDialog(null,
    					msg, "Delete", JOptionPane.YES_OPTION,
    					JOptionPane.QUESTION_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "delete.png"));
				
				if(input == 0) {
					File jsonDirectory = new File(System.getProperty("user.dir") + Constants.FILESEPARATOR 
							+ currentNode.getPath()[0].toString());
					
					String currentPath = jsonDirectory.getAbsolutePath();
					
					for (int i = 1; i < currentNode.getPath().length; i++) {
						
						File currentFile = new File(
								currentPath + Constants.FILESEPARATOR + currentNode.getPath()[i]);
						currentPath = currentFile.getAbsolutePath();
					}
					
					TreePath[] paths = createtestStorageTree.getSelectionPaths();
	                if (paths != null) {
	                    for (TreePath path : paths) {
	                    	
	                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
	                            path.getLastPathComponent();
	                       
	                        if (node.getParent() != null) {
	                            model.removeNodeFromParent(node);
	                           File file = new File(currentPath);
	                           deleteFile(file);
	                        }
	                    }
	                }	
				}
			}
		};
	}
	
	public void deleteFile(File file) {
		
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			if(files != null) {
				for(File newFile : files) {
					deleteFile(newFile);
				}
			}
			file.delete();
		}else {
			file.delete();
		}
		
	}
	
	private static void getFileChooser(JTextField textField, String fileType) {
		
		String filePath = "";
		 
		 try {
			 
			 String lastvisitedPath = new ReadConfigFile(Constants.CONFIGFILEPATH)
					 .getProperty("lastvisitedpath");
			 
			 if (!(lastvisitedPath.equalsIgnoreCase("null"))) {
				 
				 filePath = UIDesign.getfileChooser(lastvisitedPath, fileType);
				 
			 } else {
				 
				 filePath = UIDesign.getfileChooser(Constants.USERDIRECTORY, fileType);
				 
			 }
			 
			 if (!(filePath ==null)) {
				 
				 textField.setText(filePath);
				 
				 new ReadConfigFile(Constants.CONFIGFILEPATH).storeProperty("lastvisitedpath", filePath);
				 
			 }
			 
		 } catch (IOException e) {
			 
			 JOptionPane.showMessageDialog(null,  e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE,
					 new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));
		 
		 }
		
	}
	
}
