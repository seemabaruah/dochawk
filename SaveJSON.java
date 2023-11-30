package dynamicpdfvalidator.wipro.common;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import dynamicpdfvalidator.wipro.utils.Constants;

public class SaveJSON {

	public SaveJSON(JTree tree, String filepath){
		  
		   saveJSONTree(tree,filepath);
		  
		  }

		  private void saveJSONTree(JTree savejsonTree, String filepath){
		  
		  JSONObject jsonContent=new JSONObject();
		  
		  JSONObject pageValidationsJSONObject=new JSONObject();
		  
		  TreeNode treeValidations=((TreeNode) savejsonTree.getModel().getRoot()).getChildAt(0).getChildAt(0);
		  
		  int i=0;
		  
		  while(i<treeValidations.getChildCount()){
		  
		  TreeNode panelNode=treeValidations.getChildAt(i);
		  
		  JSONArray panel=new JSONArray();
		  
		  int j=0;
		  
		  while(j<panelNode.getChildCount()){
		  
		  TreeNode ruleNode=panelNode.getChildAt(j);
		  
		  JSONObject rule=new JSONObject();
		  
		  JSONObject ruleMap=new JSONObject();
		  
		  int k=0;
		  
		  while(k<ruleNode.getChildCount()){
		  
		  try{
		  
		  String ruleValue=ruleNode.getChildAt(k).toString();
		  
		  String key=ruleValue.substring(0,ruleValue.indexOf(':')).trim();
		  
		  String value=ruleValue.substring(ruleValue.indexOf(':')+1,ruleValue.length()).trim();
		  
		  if(key.equalsIgnoreCase("headers")){
		  
		  String[] headerData=value.split(",");
		  
		  JSONArray headers = new JSONArray();
		  
		  for(int h=0;h<headerData.length;h++){
		  
		    headers.put(headerData[h]);
			
		  }
		  
		  ruleMap.put(key,headers);
		  
		  }else{
		  
		  ruleMap.put(key,value);
		  
		  }
		  
		  }catch (StringIndexOutOfBoundsException | JSONException e) {
		  
		  JOptionPane.showMessageDialog(null,
		   "Issue in Saving JSON : Path - " + panelNode.toString() + " -> " + ruleNode.toString()
		         + " -> " + ruleNode.getChildAt(k).toString(),
				 "Error", JOptionPane.ERROR_MESSAGE,
				 new ImageIcon(Constants.IMAGESDIRECTORY + "error.png"));
		  
		      }
		  
		     k++;
		  }
		  
		  rule.put(panelNode.getChildAt(j).toString(),ruleMap);
		  
		  panel.put(rule);
		  
		  j++;
		  
		  } 
		  
		  pageValidationsJSONObject.put(panelNode.toString(),panel);
		  
		  i++;
		  
		  } 
		  
		  jsonContent.put("document",new JSONObject().put("pages", new JSONArray()
		     .put(new JSONObject().put("pageValidations", new JSONArray().put(pageValidationsJSONObject)))));
		  
		  try(OutputStreamWriter writer= new OutputStreamWriter(new FileOutputStream(new File(filepath)),
		  StandardCharsets.UTF_8)){
		  
		  writer.write(jsonContent.toString());
		  
		  JOptionPane.showMessageDialog(null, "JSON File Successfully Saved","Success",
		       JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY+ "success.png"));
		  
		  } catch(IOException e){
		  
		  JOptionPane.showMessageDialog(null, "Issue in Saving JSON : Path - " + e.getMessage(),"Error",
		       JOptionPane.ERROR_MESSAGE, new ImageIcon(Constants.IMAGESDIRECTORY+ "error.png"));
		  
		  }
		  
		  }
		  
		  public static void main(String args[]){
		  
		  new SaveJSON(new JTree(), "");
		  
		  }
		  
	
}
