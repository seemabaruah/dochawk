package dynamicpdfvalidator.wipro.common;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import dynamicpdfvalidator.wipro.utils.Constants;

public class TreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Icon haschildIcon, hasnochildIcon;

	public TreeCellRenderer(Icon haschildIcon, Icon hasnochildIcon) {

		this.haschildIcon = haschildIcon;

		this.hasnochildIcon = hasnochildIcon;

	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,

			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;

		if (treeNode.getAllowsChildren()) {

			setIcon(haschildIcon);

		} else {

			setIcon(hasnochildIcon);

		}

		setBackgroundNonSelectionColor(Color.WHITE);

		setBackgroundSelectionColor(Color.GREEN);

		int childCount = treeNode.getChildCount();

		int i = 0;

		while (i < childCount) {

			if (treeNode.getChildAt(i).toString().equalsIgnoreCase("type:exclude")) {

				setBackgroundNonSelectionColor(new Color(255, 204, 153));

				setBackgroundSelectionColor(new Color(255, 204, 153));

			} else if (treeNode.getChildAt(i).toString().equalsIgnoreCase("display:false")) {

				setBackgroundNonSelectionColor(new Color(127, 179, 213));

				setBackgroundSelectionColor(new Color(127, 179, 213));

			} else if (treeNode.getChildAt(i).toString().equalsIgnoreCase("result:fail")) {

				setBackgroundNonSelectionColor(new Color(255, 128, 128));

				setBackgroundSelectionColor(new Color(255, 128, 128));

			}

			i++;
		}

		setClosedIcon(new ImageIcon(Constants.IMAGESDIRECTORY + "treeclosed.png"));

		setOpenIcon(new ImageIcon(Constants.IMAGESDIRECTORY + "treeexpand.png"));

		return this;

	}

}