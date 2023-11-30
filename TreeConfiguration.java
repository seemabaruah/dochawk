
package dynamicpdfvalidator.wipro.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.json.JSONException;

import dynamicpdfvalidator.wipro.utils.Constants;


public class TreeConfiguration extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	
	public JTree jsonMapperTree;
	
	JPopupMenu parentpopupMenu;
	
	JPopupMenu childpopupMenu;
	
	JPopupMenu leafpopupMenu;
	
	JMenu pasteMenuItem;
	
	static DefaultMutableTreeNode copyNode = null;
	
	public TreeConfiguration (String configureMethod, String filePath) throws JSONException, IOException  {
	
	initComponents(configureMethod, filePath);
	}
	
	private void initComponents(String configureMethod, String filepath) throws JSONException, IOException {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setBounds(100, 100, 450, 300);
		
		contentPane = new JPanel();
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		contentPane.setLayout(new BorderLayout(0, 0));
		
		setContentPane(contentPane);
		
		if(configureMethod.equalsIgnoreCase(Constants.CREATEJSON)) {
			
			jsonMapperTree = new JTree();
			jsonMapperTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("document") {
			
			private static final long serialVersionULD = 1L;
			{
				DefaultMutableTreeNode node_1;
				node_1 = new DefaultMutableTreeNode ("pages");
				node_1.add(new DefaultMutableTreeNode("pagevalidations"));
				add(node_1);
			}
		}));
		
	} else if (configureMethod.equalsIgnoreCase(Constants.OPENJSON)) {
		
		jsonMapperTree = new ReadJSONTree(filepath).readJSONTree;
	}
	
	jsonMapperTree.setEditable(true);
	
	contentPane.add(jsonMapperTree, BorderLayout.CENTER);
	
	jsonMapperTree.addMouseListener(addTreeMouseListener());
	
	parentpopupMenu = new JPopupMenu();
	
	childpopupMenu = new JPopupMenu();
	
	leafpopupMenu = new JPopupMenu();
	
	leafpopupMenu.add(createMenuItem(Constants.UPDATE));
	
	leafpopupMenu.add(createMenuItem(Constants.DELETE));
	
	childpopupMenu.add(createMenuItem(Constants.UPDATE));
	
	childpopupMenu.add(createMenuItem(Constants.DELETE));
	
	childpopupMenu.add(createMenuItem(Constants.ADDSIBLING));
	
	childpopupMenu.add(createMenuItem(Constants.TYPETEXTNODE));
	
	childpopupMenu.add(createMenuItem(Constants.TYPEDYNAMICNODE));
	
//	childpopupMenu.add(createMenuItem(Constants.TYPETABLENODE));
	
//	childpopupMenu.add(createMenuItem(Constants.TYPEMULTILINETABLE));
	
//	childpopupMenu.add(createMenuItem(Constants.TYPECUSTOMREGULAREXPRESSION));
	
	childpopupMenu.add(createMenuItem(Constants.TYPEEXCLUDE));
	
//	childpopupMenu.add(createMenuItem(Constants.NEGATIVETEST));
	
	childpopupMenu.add(createMenuItem(Constants.COPY));
	
	pasteMenuItem = new JMenu(Constants.PASTE);
	
	pasteMenuItem.add(createMenuItem(Constants.INSERTBEFORE));
	
	pasteMenuItem.add(createMenuItem(Constants.INSERTAFTER));
	
	pasteMenuItem.add(createMenuItem(Constants.INSERTHERE));
	
	pasteMenuItem.setEnabled(false);
	
	childpopupMenu.add(pasteMenuItem);
	
	parentpopupMenu.add(createMenuItem(Constants.ADDSECTION));
	
	}
	
	private MouseListener addTreeMouseListener() {
		
		return new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {
				
				
				
				if (e.getButton() == MouseEvent.BUTTON3) {
					
					if(copyNode != null) {
						
						pasteMenuItem.setEnabled(true);
					}
					
					DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) jsonMapperTree
							.getLastSelectedPathComponent();
					
					try {
						
						if(!currentNode.isRoot()){
						
							if(currentNode.getParent().toString().equalsIgnoreCase("pages")) {
							
							parentpopupMenu.show((Component) e.getSource(), e.getX(), e.getY());
					
					} else if(!currentNode.getParent().toString().equalsIgnoreCase("document")) {
						
						if(currentNode.getAllowsChildren()) {
						
						childpopupMenu.show((Component) e.getSource(), e.getX(), e.getY());
					
					} else if(!currentNode.getAllowsChildren()) {
						
						leafpopupMenu.show((Component) e.getSource(), e.getX(), e.getY());
					
					}
				}
			}
		
		} catch (NullPointerException el) {
			
			JOptionPane.showMessageDialog(null, "Please select the node and do the right click");
		}
	   }
	  }	
	};
   }
   
   private ActionListener getUpdateActionListener() {
	   
	   return new ActionListener() {
	   
	   @Override
	   public void actionPerformed(ActionEvent e) {
		   
		   jsonMapperTree.startEditingAtPath(jsonMapperTree.getSelectionPath());
	   }
      };
    }
    
    private ActionListener getAddSectionActionListener() {
    	
    	return new ActionListener() {
    		
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			currentNode.add(new DefaultMutableTreeNode("New Node - Right Click to Edit"));
    			
    			((DefaultTreeModel) jsonMapperTree.getModel()).reload(currentNode);
    		}
    	};
    }
    
    private ActionListener getCopyActionListener() {
    	
    	return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			copyNode = new DefaultMutableTreeNode(currentNode.toString());
    			
    			copyNode = (DefaultMutableTreeNode) copySubTree(copyNode,currentNode);
    			
    		}
    	};
    }
    
    private ActionListener getPasteActionListener() {
    	
    	return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			DefaultTreeModel model = ((DefaultTreeModel) jsonMapperTree.getModel());
    			
    			MutableTreeNode parentNode = (MutableTreeNode) currentNode.getParent();
    			
    			if(e.getActionCommand().equalsIgnoreCase(Constants.INSERTHERE)) {
    			
    				currentNode.add(copyNode);
    				
    				model.reload(currentNode);
    			
    			} else if (e.getActionCommand().equalsIgnoreCase(Constants.INSERTBEFORE)) {
    			
    				if(parentNode.getIndex(currentNode) == 0) {
    				
    					model.insertNodeInto(copyNode, parentNode, 0);
    				
    				} else {
    				
    					model.insertNodeInto(copyNode, parentNode, parentNode.getIndex(currentNode) - 1);
    					
    				}
    				
    				model.reload(parentNode);
    			
    			} else {
    			
    				model.insertNodeInto(copyNode, parentNode, parentNode.getIndex(currentNode) + 1);
    				
    				model.reload(parentNode);
					
				}
    		
    		pasteMenuItem.setEnabled(false);
    		
    		copyNode = null;
    			
    		}
    	};
    }
    
    	private ActionListener getTypeActionListener() {
    	
    	return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			DefaultTreeModel model = ((DefaultTreeModel) jsonMapperTree.getModel());
    			
    			MutableTreeNode parentNode = (MutableTreeNode) currentNode.getParent();
    	
    			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New Node - Right Click to Edit");
    			
    		switch(e.getActionCommand()) {
    		
    		case Constants.TYPETEXTNODE:
    		
    			 newNode.add(gettypeTreeNode("type:text"));
    			
    			 newNode.add(gettypeTreeNode("starttag:starttag"));
    		
    			 newNode.add(gettypeTreeNode("endtag:none"));
    			
    			 newNode.add(gettypeTreeNode("data:data"));
    			
    		break;
    			
    	case Constants.TYPEDYNAMICNODE:
    		
			 newNode.add(gettypeTreeNode("type:dynamic"));
			
			 newNode.add(gettypeTreeNode("starttag:starttag"));
		
			 newNode.add(gettypeTreeNode("endtag:none"));
			
			 newNode.add(gettypeTreeNode("dynamicdata:data"));
			 
			 newNode.add(gettypeTreeNode("sheetname:sheetname"));
			
		break;
		
    case Constants.TYPETABLENODE:
		
		 newNode.add(gettypeTreeNode("type:table"));
		
		 newNode.add(gettypeTreeNode("starttag:starttag"));
	
		 newNode.add(gettypeTreeNode("endtag:none"));
		
		 newNode.add(gettypeTreeNode("headers:header1,header2.."));
		 
		 newNode.add(gettypeTreeNode("sheetname:sheetname"));
		 
		 newNode.add(gettypeTreeNode("tablesheetname:tablesheetname"));
		
	break;
		
    case Constants.TYPEMULTILINETABLE:
	
	     newNode.add(gettypeTreeNode("type:multilinetable"));
	
	     newNode.add(gettypeTreeNode("starttag:starttag"));

	     newNode.add(gettypeTreeNode("endtag:none"));
	
	     newNode.add(gettypeTreeNode("headers:header1,header2.."));
	 
	     newNode.add(gettypeTreeNode("sheetname:sheetname"));
	 
	     newNode.add(gettypeTreeNode("tablesheetname:tablesheetname"));
	
    break;
    
    case Constants.TYPECUSTOMREGULAREXPRESSION:
	
        newNode.add(gettypeTreeNode("type:customerregularexpression"));

        newNode.add(gettypeTreeNode("pattern:pattern"));

        newNode.add(gettypeTreeNode("index:0"));

        newNode.add(gettypeTreeNode("dynamicdata:data"));
 
        newNode.add(gettypeTreeNode("sheetname:sheetname"));

    break;
    
    case Constants.TYPEEXCLUDE:
	
        newNode.add(gettypeTreeNode("type:exclude"));

        newNode.add(gettypeTreeNode("starttag:starttag"));

        newNode.add(gettypeTreeNode("endtag:none"));

        newNode.add(gettypeTreeNode("data:data"));

    break;
    
     default:
    
     break;
    }
    		
    if(parentNode.toString().equalsIgnoreCase("pagevalidations")) {
    
    	currentNode.add(newNode);
    	
    	((DefaultTreeModel) jsonMapperTree.getModel()).reload(currentNode);
    	
     } else {
     
    	 ((DefaultTreeModel) jsonMapperTree.getModel()).insertNodeInto(newNode, parentNode,
    			 parentNode.getIndex(currentNode) + 1);
     
            }
    	  }
        };
    }
    	
    	private DefaultMutableTreeNode gettypeTreeNode(String treenodeText) {
    	
    		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode (treenodeText);
    		
    		treeNode.setAllowsChildren(false);
    		
    		return treeNode;
    	}
    	
    	private ActionListener getDeleteActionListener() {
        	
    		return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    		
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			int input = JOptionPane.showConfirmDialog(null,
    					"Do you really want to delete: " + currentNode.toString(), "Delete", JOptionPane.YES_OPTION,
    					JOptionPane.QUESTION_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY + "delete.png"));
    			
    			if(input == 0) {
    			
    				((DefaultTreeModel) jsonMapperTree.getModel()).removeNodeFromParent(currentNode);
    				
    				jsonMapperTree.revalidate();
    				
    				jsonMapperTree.repaint();
    			}
      		  }
    		};
    	  }
    	
    	private ActionListener getNegativeTestActionListener() {
        	
    		return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    			boolean result = true;
    		
    			DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    			
    			for (int i=0; i < currentNode.getChildCount(); i++) {
    			if(currentNode.getChildAt(i).toString().equalsIgnoreCase("display:false")
    					|| currentNode.getChildAt(i).toString().equalsIgnoreCase("display:true")) {
    				
    				result = false;
    				break;
    				}
      		     }
    			
    			if(result) {
    			
    				DefaultTreeModel model = ((DefaultTreeModel) jsonMapperTree.getModel());
    				
    				if(currentNode.getChildCount() > 0) {
    				
    					DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("display:false");
    					
    					newNode.setAllowsChildren(false);
    					
    					currentNode.insert(newNode, currentNode.getChildCount() - 1);
    				}
    				
    				model.reload(currentNode);
    			}
    		}
    	};
    }
    	
    	private ActionListener getSiblingActionListener() {
        	
    		return new ActionListener() {
    	
    		@Override
    		public void actionPerformed(ActionEvent e) {
    			
    		DefaultMutableTreeNode currentNode = ((DefaultMutableTreeNode) jsonMapperTree.getLastSelectedPathComponent());
    		
    		DefaultTreeModel model = ((DefaultTreeModel) jsonMapperTree.getModel());
    		
    		MutableTreeNode parentNode = (MutableTreeNode) currentNode.getParent();
    		
    		model.insertNodeInto(new DefaultMutableTreeNode("New Node - Right Click to Edit"), parentNode,
    				parentNode.getIndex(currentNode) + 1);
    		
    		model.reload(parentNode);
    		}
    	};
    }
 
    	private JMenuItem createMenuItem(String menuName) {
    	
    		JMenuItem menuItem = new JMenuItem(menuName);
    		
    		menuItem.setFont(new Font("Verdana", Font.PLAIN, 12));
    		
    		switch(menuName) {
    		
    		case Constants.UPDATE:
    		
    			menuItem.addActionListener(getUpdateActionListener());
    		
    		break;
    		
    		case Constants.DELETE:
        		
        		menuItem.addActionListener(getDeleteActionListener());
        		
        	break;
        	
    		case Constants.TYPETEXTNODE:
        		
        		menuItem.addActionListener(getTypeActionListener());
        		
        	break;
        	
    		case Constants.TYPEDYNAMICNODE:
        		
        		menuItem.addActionListener(getTypeActionListener());
        		
        	break;
        	
//    		case Constants.TYPECUSTOMREGULAREXPRESSION:
//        		
//        		menuItem.addActionListener(getTypeActionListener());
//        		
//        	break;
        	
//    		case Constants.TYPETABLENODE:
//        		
//        		menuItem.addActionListener(getTypeActionListener());
//        		
//        	break;
        	
//    		case Constants.TYPEMULTILINETABLE:
//        		
//        		menuItem.addActionListener(getTypeActionListener());
//        		
//        	break;
        	
    		case Constants.ADDSECTION:
        		
        		menuItem.addActionListener(getAddSectionActionListener());
        		
        	break;
        	
    		case Constants.ADDSIBLING:
        		
        		menuItem.addActionListener(getSiblingActionListener());
        		
        	break;
        	
    		case Constants.TYPEEXCLUDE:
        		
        		menuItem.addActionListener(getTypeActionListener());
        		
        	break;
        	
//    		case Constants.NEGATIVETEST:
//        		
//        		menuItem.addActionListener(getNegativeTestActionListener());
//        		
//        	break;
        	
    		case Constants.COPY:
        		
        		menuItem.addActionListener(getCopyActionListener());
        		
        	break;
        	
    		case Constants.PASTE:
        		
        		menuItem.addActionListener(getPasteActionListener());
        		
        	break;
        	
    		case Constants.INSERTAFTER:
        		
        		menuItem.addActionListener(getPasteActionListener());
        		
        	break;
        	
    		case Constants.INSERTBEFORE:
        		
        		menuItem.addActionListener(getPasteActionListener());
        		
        	break;
        	
    		case Constants.INSERTHERE:
        		
        		menuItem.addActionListener(getPasteActionListener());
        		
        	break;
        	
        default:
        	
        	break;
        		
    		}
    	
    		return menuItem;
    	
    	}
    	
    	
    	private Object copySubTree(DefaultMutableTreeNode subRoot, DefaultMutableTreeNode sourceTree) {
    	
    		for(int i = 0; i < sourceTree.getChildCount();i++) {
    		
    			DefaultMutableTreeNode child = (DefaultMutableTreeNode) sourceTree.getChildAt(i);
    			
    			DefaultMutableTreeNode clone = new DefaultMutableTreeNode(child.getUserObject());
    			
    			if(!child.getAllowsChildren()) {
    			
    				clone.setAllowsChildren(false);
    			}
    			
    			subRoot.add(clone);
    			
    			copySubTree(clone, child);
    		}
    		
    		return subRoot;
    	}
}
