package dynamicpdfvalidator.wipro.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONException;

import dynamicpdfvalidator.wipro.utils.Constants;
import dynamicpdfvalidator.wipro.utils.UIDesign;
import dynamicpdfvalidator.wipro.views.JSONDialog;
import dynamicpdfvalidator.wipro.views.PDFValidatorHome;
import dynamicpdfvalidator.wipro.views.JSONBuilder;

public class JSONStorageTree {

	public JTree createjsonStorageTree;

	JPopupMenu parentpopupMenu;

	JPopupMenu childpopupMenu;

	JPopupMenu leafpopupMenu;

	JPopupMenu rootpopupMenu;

	public JSONStorageTree() {

		getJSONStorageTree();

	}

	public void getJSONStorageTree() {

		File jsonFileStorage = new File(Constants.JSONFILESDIRECTORY);

		if (!jsonFileStorage.exists()) {

			jsonFileStorage.mkdir();

		}

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(jsonFileStorage.getName());

		createjsonStorageTree = new JTree(getTreeView(rootNode, new File(Constants.JSONFILESDIRECTORY)));

		createjsonStorageTree.setBorder(null);

		createjsonStorageTree.setVisibleRowCount(50);

		createjsonStorageTree.addMouseListener(addTreeMouseListener());

		createjsonStorageTree.setFont(new Font("Verdana", Font.PLAIN, 12));

		createjsonStorageTree
				.setCellRenderer(new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"),
						new ImageIcon(Constants.IMAGESDIRECTORY + "jsontree.png")));

		parentpopupMenu = new JPopupMenu();

		childpopupMenu = new JPopupMenu();

		leafpopupMenu = new JPopupMenu();

		rootpopupMenu = new JPopupMenu();

		parentpopupMenu.add(createMenuItem(Constants.PACKAGE));

		parentpopupMenu.add(createMenuItem(Constants.REFRESH));

		childpopupMenu.add(createMenuItem(Constants.PACKAGE));

		childpopupMenu.add(createMenuItem(Constants.RENAME));

		childpopupMenu.add(createMenuItem(Constants.CREATEJSON));

		childpopupMenu.add(createMenuItem(Constants.DELETE));

		leafpopupMenu.add(createMenuItem(Constants.DELETE));

		leafpopupMenu.add(createMenuItem(Constants.RENAME));

		leafpopupMenu.add(createMenuItem(Constants.OPENJSON));

	}

	/********
	 * Tree View
	 */

	private static DefaultMutableTreeNode getTreeView(DefaultMutableTreeNode rootNode, File fileName) {

		File[] files = fileName.listFiles();

		for (File file : files) {

			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file.getName());

			if (file.isDirectory()) {

				rootNode.add(newNode);

				getTreeView(newNode, file);

			} else {

				if (file.getName().substring(file.getName().lastIndexOf(".")).equalsIgnoreCase(".json")) {

					newNode.setAllowsChildren(false);

					rootNode.add(newNode);

				}

			}

		}
		return rootNode;

	}

	/*************
	 * Tree Mouse Listeners
	 */

	private MouseListener addTreeMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getButton() == MouseEvent.BUTTON3) {

					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createjsonStorageTree
							.getLastSelectedPathComponent();

					try {
						if (currentNode.isRoot()) {

							parentpopupMenu.show(createjsonStorageTree, e.getX(), e.getY());

						} else {

							if (currentNode.getAllowsChildren()) {

								childpopupMenu.show(createjsonStorageTree, e.getX(), e.getY());

							} else {

								leafpopupMenu.show(createjsonStorageTree, e.getX(), e.getY());

							}

						}
					} catch (NullPointerException e1) {

						JOptionPane.showMessageDialog(null, "Please Select Node and Do Right Click");

					}

				}
			}
		};
	}

	/**********
	 * ActionListeners
	 */

	private ActionListener getRefreshActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JSONBuilder.createjsonStorage.remove(JSONBuilder.createjsonStorageTree);

				JSONBuilder.createjsonStorageTree = new JSONStorageTree().createjsonStorageTree;

				JSONBuilder.createjsonStorage.add(JSONBuilder.createjsonStorageTree);

				JSONBuilder.createjsonStorage.revalidate();

				JSONBuilder.createjsonStorage.repaint();

			}

		};

	}

	private ActionListener getRenameActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JSONDialog.getcreateJSONFileDialog(Constants.RENAME, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getPackageActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JSONDialog.getcreateJSONFileDialog(Constants.PACKAGE, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getCreateJSONActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				JSONDialog.getcreateJSONFileDialog(Constants.CREATEJSON, PDFValidatorHome.parentFrame);

			}

		};

	}

	private ActionListener getOpenJSONActionListener() {

		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				final String jsonfilePath;

				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createjsonStorageTree
						.getLastSelectedPathComponent();

				TreeNode[] treeNodes = currentNode.getPath();

				String filePath = Constants.USERDIRECTORY + Constants.FILESEPARATOR;

				int i = 0;

				while (i < treeNodes.length - 1) {

					filePath += treeNodes[i].toString() + Constants.FILESEPARATOR;

					i++;

				}

				filePath += treeNodes[treeNodes.length - 1].toString();

				jsonfilePath = filePath;

				int tabCount = JSONBuilder.tabbedPane.getTabCount();

				boolean result = false;

				i = 0;

				while (i < tabCount) {

					if (JSONBuilder.tabbedPane.getToolTipTextAt(i).toString().equalsIgnoreCase(filePath)) {

						result = true;

						break;

					}

					i++;

				}

				if (!result) {

					try {

						JPanel tabpanel = new JPanel();
						tabpanel.setBackground(Color.WHITE);
						tabpanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
						JSONBuilder.tabbedPane.add(tabpanel);
						JSONBuilder.tabbedPane.setTabComponentAt(JSONBuilder.tabbedPane.indexOfComponent(tabpanel),
								UIDesign.getTitlePanel(JSONBuilder.tabbedPane, tabpanel, currentNode.toString(),
										"jsontree.png"));
						JSONBuilder.tabbedPane.setSelectedIndex(JSONBuilder.tabbedPane.getTabCount() - 1);
						JSONBuilder.tabbedPane.setToolTipTextAt(JSONBuilder.tabbedPane.getTabCount() - 1, filePath);
						tabpanel.setLayout(new BorderLayout(0, 0));

						final JTree tree = new TreeConfiguration(Constants.OPENJSON, filePath).jsonMapperTree;
						tree.setFont(new Font("Verdana", Font.PLAIN, 12));
						tree.setEditable(true);

						tree.setCellRenderer(
								new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"),
										new ImageIcon(Constants.IMAGESDIRECTORY + "leaf.png")));

						tabpanel.add(tree, BorderLayout.CENTER);

						JPanel panel = new JPanel();
						panel.setBorder(null);
						panel.setFont(new Font("Vetdana", Font.PLAIN, 11));
						panel.setBackground(Color.WHITE);
						tabpanel.add(panel, BorderLayout.NORTH);
						panel.setLayout(new FlowLayout(FlowLayout.LEFT));

						JButton saveBtn = new JButton("Save");
						saveBtn.setBackground(new Color(0, 204, 204));
						saveBtn.setIcon(new ImageIcon(Constants.IMAGESDIRECTORY + Constants.FILESEPARATOR + "save.png"));
						saveBtn.setFont(new Font("Verdana", Font.PLAIN, 11));
						saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
						saveBtn.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {

								new SaveJSON(tree, jsonfilePath);

							}

						});
						panel.add(saveBtn, BorderLayout.EAST);

						JSONBuilder.tabbedPane.setSelectedIndex(JSONBuilder.tabbedPane.getTabCount() - 1);

					} catch (IOException | JSONException e1) {

						JOptionPane.showMessageDialog(null, "Issue in Reading JSON : Path - " + e1.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE,
								new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));

					}

				} else {

					JSONBuilder.tabbedPane.setSelectedIndex(i);

				}
			}

		};

	}

	/********
	 * Menu
	 */

	private JMenuItem createMenuItem(String menuName) {

		JMenuItem menuItem = new JMenuItem(menuName);

		menuItem.setFont(new Font("Vetdana", Font.PLAIN, 12));

		switch (menuName) {

		case Constants.PACKAGE:

			menuItem.addActionListener(getPackageActionListener());

			break;

		case Constants.CREATEJSON:

			menuItem.addActionListener(getCreateJSONActionListener());

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
				
				DefaultTreeModel model = (DefaultTreeModel) createjsonStorageTree.getModel();
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createjsonStorageTree.getLastSelectedPathComponent();
				
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
					
					TreePath[] paths = createjsonStorageTree.getSelectionPaths();
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

}
