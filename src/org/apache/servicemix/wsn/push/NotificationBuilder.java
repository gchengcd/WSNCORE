package org.apache.servicemix.wsn.push;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class NotificationBuilder {
	private String hashCode;
	private String tempMessage;
	
	private String before;
	private String after;
	private String fragment;
	private String[] splitFragment;
	private String packageString;
	
	private int total;
	private Map<Integer, String> notification = new HashMap<Integer, String>(3000);
	
	
	public void setHashCode(String _hashCode){
		hashCode = _hashCode;
	}
	public String getHashCode(){
		return hashCode;
	}
	public void setTempMessage(String _tempMessage){
		tempMessage = _tempMessage;
	}
	//拆掉分包消息，获取消息的包裹层，以便在组包时使用
	public void breakMessage(){
		if(tempMessage != null){
			before = tempMessage.split("<wsnt:Package>")[0] + "<wsnt:Message>";
			after = "</wsnt:Message>" + tempMessage.split("</wsnt:Message>")[1];
		}
	} 
	//解析分包
	public void parse(){
		packageString = tempMessage.split("<wsnt:Message>")[1].split("</wsnt:Message>")[0];
		fragment = tempMessage.split("<Fragment>")[1].split("</Fragment>")[0];
		splitFragment = fragment.split("-");
		if(Integer.parseInt(splitFragment[1]) == 0)
			total = Integer.parseInt(splitFragment[3]) + 1;
		notification.put(Integer.parseInt(splitFragment[3]), packageString);
	}
	//检查是否接收到足够的分包
	public boolean isReadyToBuild(){
		System.out.println("[Map:] " + notification.toString());
		System.out.println("isReadyToBuild: sizeof: " + notification.size() + " total: " + total);
		if(notification.size() == total)
			return true;
		else
			return false;
	}
	//组包
	public String build(){
		System.out.println("enter build!!!!!");
		TreeMap tm = new TreeMap(notification);
		StringBuilder sb = new StringBuilder(before);
		for(int j=0;j<tm.size();j++){
			sb.append(tm.get(Integer.valueOf(j)));
		}
		sb.append(after);
		return sb.toString();
	}
}
