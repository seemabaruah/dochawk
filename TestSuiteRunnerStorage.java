
package dynamicpdfvalidator.wipro.common;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONException;
import org.json.JSONObject;

import dynamicpdfvalidator.wipro.utils.Constants;
import dynamicpdfvalidator.wipro.utils.TestSuiteConfig;
import dynamicpdfvalidator.wipro.views.PDFValidatorHome;
import dynamicpdfvalidator.wipro.views.TestSuiteRunner;
import dynamicpdfvalidator.wipro.views.TestSuiteRunnerDialog;

class TreeTransferHandler extends TransferHandler {
	
	private static final long serialsVersionUID = 1L;
	
	DataFlavor nodesFlavor;
	
	DataFlavor[] flavors = new DataFlavor[1];
	
	/**
	 * 
	 */
	
	public TreeTransferHandler() {
		
		String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\""
		+ javax.swing.tree.DefaultMutableTreeNode[].class.getName()+"\"";
		
		try {
			
			nodesFlavor = new DataFlavor(mimeType);
			
		} catch(ClassNotFoundException e) {
			
			e.printStackTrace();
		}
		
		flavors[0] = nodesFlavor;
		
	}
	
	public boolean canImport(TransferHandler.TransferSupport transferSupport) {
		
		if (!transferSupport.isDrop()) {
			
			return false;
			
		}
		
		return true;
	}
	
	
	public boolean importData(TransferHandler.TransferSupport transferSupport) {
		
		if(!canImport(transferSupport)) {
			
			return false;
			
		}
		
		JTree.DropLocation dropLocation = (JTree.DropLocation) transferSupport.getDropLocation();
		
		TreePath dropPath = dropLocation.getPath();
		
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
		
		if(parent.isRoot()) {
			
			return false;
	
		}
		
		if (parent.getAllowsChildren()) {
			
			DefaultTreeModel treeModel = (DefaultTreeModel) ((JTree) transferSupport.getComponent()).getModel();
			
			try {
				
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
						((DefaultMutableTreeNode) transferSupport.getTransferable().getTransferData(nodesFlavor))
						.toString());
				
				for(int c=0;c<parent.getChildCount();c++) {
					
					if(parent.getChildAt(c).toString().equalsIgnoreCase(childNode.toString()))
					{
						return false;
						
					}
					
				}
				
				TreeNode[] treeNodes = ((DefaultMutableTreeNode)transferSupport.getTransferable()
						.getTransferData(nodesFlavor)).getPath();
				
				String testfilePath ="";
				
				String executionfilePath="";
				
				for(int i=1;i<treeNodes.length;i++) {
					
					if(i<treeNodes.length-1) {
					
					executionfilePath +=treeNodes[i] +  Constants.FILESEPARATOR;
					
				}
				
				testfilePath +=treeNodes[i] +  Constants.FILESEPARATOR;
			
			}
			
			JSONObject testObject = new JSONObject();
			
			testObject.put("executionpath",executionfilePath + "executionsummary.json");
			
			testObject.put("path", testfilePath.substring(0, testfilePath.length() -1));
			
			new TestSuiteConfig(parent.toString()).addTest(parent.toString(),childNode.toString(), testObject);
			
			childNode.setAllowsChildren(false);
			
			parent.add(childNode);
			
			treeModel.reload(parent);
				
		} catch (UnsupportedFlavorException | IOException e) {
			
			JOptionPane.showMessageDialog(null, e.getMessage());
			
		}
			
		}
		
	return true;
	
	}
	
}

public class TestSuiteRunnerStorage {
	
	public JTree createtestRunnerStorageTree;
	
	JPopupMenu parentpopupMenu;
	
	JPopupMenu leafpopupMenu;
	
	JPopupMenu rootpopupMenu;
	
	public TestSuiteRunnerStorage() {
		
		 getRunnerStorageTree();
		 
	}
	
	public void getRunnerStorageTree() {
		
		try {
			
			File testSuiteRunnerStorage = new File(Constants.TESTSUITERUNNERDIRECTORY);
			
			if(!testSuiteRunnerStorage.exists()) {
				
				testSuiteRunnerStorage.mkdir();
				
			}
			
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(testSuiteRunnerStorage.getName());
			
			createtestRunnerStorageTree = new JTree
					(getTreeView(rootNode, new File(Constants.TESTSUITERUNNERDIRECTORY)));
			
			createtestRunnerStorageTree.setBorder(null);
			
			createtestRunnerStorageTree.setVisibleRowCount(50);
			
			createtestRunnerStorageTree.setFont(new Font("Verdana", Font.PLAIN, 11));
			
			createtestRunnerStorageTree.setDropMode(DropMode.ON_OR_INSERT);
			
			createtestRunnerStorageTree.setTransferHandler(new TreeTransferHandler());
			
			createtestRunnerStorageTree.addMouseListener(addTreeMouseListener());
			
			createtestRunnerStorageTree.setFont(new Font("Verdana", Font.PLAIN, 12));
			
			createtestRunnerStorageTree
				.setCellRenderer(new TreeCellRenderer(new ImageIcon(Constants.IMAGESDIRECTORY + "package.png"), 
						new ImageIcon(Constants.IMAGESDIRECTORY + "test.png")));
			
			parentpopupMenu = new JPopupMenu();
			
			leafpopupMenu = new JPopupMenu();
			
			rootpopupMenu = new JPopupMenu();
			
			rootpopupMenu.add(createMenuItem(Constants.NEWTESTSUITE));
			
			parentpopupMenu.add(createMenuItem(Constants.DELETE));
			
			parentpopupMenu.add(createMenuItem(Constants.EDIT));
			
			parentpopupMenu.add(createMenuItem(Constants.TESTSUITERUNNER));
			
			leafpopupMenu.add(createMenuItem(Constants.REMOVETEST));
			
			leafpopupMenu.add(createMenuItem(Constants.EXCLUDETEST));
			
		} catch (JSONException | IOException e) {
			
			JOptionPane.showMessageDialog(null, "Issue in Reading TestSuites :"+ e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY+"error.png"));
			
		}
		
	}
			
/********
 * Tree view			
 *
*@throws IOException
*@throws JSONEXception
*/
			
	private static DefaultMutableTreeNode getTreeView(DefaultMutableTreeNode rootNode, File fileName)
			throws JSONException, IOException{
		
		File[] files = fileName.listFiles();
		
		for (File file : files) {
			
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file.getName());
			
			if(file.isDirectory())
			{
				
				JSONObject testsuiteObject = new TestSuiteConfig(file.getName()).testsuiteObject
						.getJSONObject("testsuite");
			
				for (String testName : testsuiteObject.keySet()) {
					
					DefaultMutableTreeNode testNode = new DefaultMutableTreeNode(testName);
					
					testNode.setAllowsChildren(false);
					
					newNode.add(testNode);
					
				}
				
				rootNode.add(newNode);
			}
			
		}
		
		return rootNode;
		
	}
	
	/*********
	 * Tree Mouse Listeners
	 */
	
	private MouseListener addTreeMouseListener() {
		
		return new MouseAdapter() {
			
		public void mouseClicked(MouseEvent e) {
			
			if(e.getButton()==MouseEvent.BUTTON3) {
				
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createtestRunnerStorageTree
						.getLastSelectedPathComponent();
				
				try {
					
					if(currentNode.isRoot()) {
						
						rootpopupMenu.show(createtestRunnerStorageTree, e.getX(), e.getY());
						
					} else if(!currentNode.getAllowsChildren()) {
						
						leafpopupMenu.show(createtestRunnerStorageTree, e.getX(), e.getY());
						
					}else if (currentNode.getAllowsChildren()) {
						
						parentpopupMenu.show(createtestRunnerStorageTree, e.getX(), e.getY());
						
					}
					
				} catch (NullPointerException e1) {
					
					JOptionPane.showMessageDialog(null, "Please Select Node and Right Click");
					
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
			
			TestSuiteRunner.createrunnerStorage.remove(TestSuiteRunner.createtestRunnerStorageTree);
			
			TestSuiteRunner.createtestRunnerStorageTree= createtestRunnerStorageTree;
			
			TestSuiteRunner.createrunnerStorage.add(TestSuiteRunner.createtestRunnerStorageTree);
			
			TestSuiteRunner.createrunnerStorage.revalidate();
			
			TestSuiteRunner.createrunnerStorage.repaint();
			
			}
		
		};
		
	}

	private ActionListener  getEditActionListener() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				TestSuiteRunnerDialog.gettestsuiteRunnerDialog(Constants.EDIT, PDFValidatorHome.parentFrame);
				
			}
				
			};
			
		}
	
	private ActionListener  getPackageActionListener() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				TestSuiteRunnerDialog.gettestsuiteRunnerDialog(Constants.NEWTESTSUITE, PDFValidatorHome.parentFrame);
				
			}
				
			};
			
		}
	
	private ActionListener getRemoveTestActionListener() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) createtestRunnerStorageTree
							.getLastSelectedPathComponent();
					
					TestSuiteConfig testsuiteConfig = new TestSuiteConfig(selectedNode.getParent().toString());
					
					JSONObject testsuiteObject = testsuiteConfig.testsuiteObject;
					
					testsuiteObject.getJSONObject("testsuite").remove(selectedNode.toString());
					
					TestSuiteConfig.savejsontoFile(testsuiteObject, 
							new File(Constants.TESTSUITERUNNERDIRECTORY + Constants.FILESEPARATOR
									+ selectedNode.getParent().toString() + Constants.FILESEPARATOR
									+ "testsuite.json"));
					
					((DefaultTreeModel) createtestRunnerStorageTree.getModel()).removeNodeFromParent(selectedNode);
					
					((DefaultTreeModel) createtestRunnerStorageTree.getModel()).reload(selectedNode.getParent());
					
				} catch (IOException e1) {
					
					JOptionPane.showMessageDialog(null, "Issue in Removing Test From Test Suite :" +e1.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY +"error.png"));
					
				}
						
				}
			
			}; 
			
		}
	
	private ActionListener getRunTestSuiteActionListener() {
		
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					
					JSONObject testsuiteObject = new TestSuiteConfig (
							createtestRunnerStorageTree.getLastSelectedPathComponent().toString()).testsuiteObject;
					
					Runnable runnable = new TestSuiteReportView(
							createtestRunnerStorageTree.getLastSelectedPathComponent().toString(), testsuiteObject);
					
					new Thread(runnable).start();
					
				}catch (IOException e1) {
				
				JOptionPane.showMessageDialog(null, "Issue is Reading Test Suite Config Details" + e1.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "error. png"));
				
				}
					
				}
			};
		}
			
	
	/*****
	 * Menu
	 */
		
		private JMenuItem createMenuItem(String menuName) {
			
			JMenuItem menuItem = new JMenuItem(menuName);
			
			menuItem.setFont(new Font ("Verdana", Font.PLAIN, 12));
			
			switch (menuName) {
			
			case Constants.NEWTESTSUITE :
				
				menuItem.addActionListener(getPackageActionListener());
				
				break;
				
			case Constants.TESTSUITERUNNER :
				
				menuItem.addActionListener(getRunTestSuiteActionListener());
				
				break;
				
			case Constants.REMOVETEST :
				
				menuItem.addActionListener(getRemoveTestActionListener());
				
				break;	
			
			case Constants.EXCLUDETEST :
				
				menuItem.addActionListener(getRemoveTestActionListener());
				
				break;	
			
			case Constants.DELETE :
				
				menuItem.addActionListener(getDeleteActionListener());
				
				break;
			
			case Constants.EDIT :
				
				menuItem.addActionListener(getEditActionListener());
				
				break;
				
			case Constants.REFRESH :
				
				menuItem.addActionListener(getRefreshActionListener());
				
			default :
				
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
				DefaultTreeModel model = (DefaultTreeModel) createtestRunnerStorageTree.getModel();
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) createtestRunnerStorageTree.getLastSelectedPathComponent();
				
				int input = JOptionPane.showConfirmDialog(null,
    					"Do you really want to delete " + currentNode.toString() + " and its contents ?", "Delete", JOptionPane.YES_OPTION,
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
					
					TreePath[] paths = createtestRunnerStorageTree.getSelectionPaths();
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


