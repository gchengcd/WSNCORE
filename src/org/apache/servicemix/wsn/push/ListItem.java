package org.apache.servicemix.wsn.push;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;

public class ListItem{
    	private String topicName;
    	private String subscriberAddress;
    	public HttpAsyncClient getAsyClient() {
			return asyClient;
		}
		public void setAsyClient(HttpAsyncClient asyClient) {
			this.asyClient = asyClient;
		}
		public HttpPost getHttpPost() {
			return httpPost;
		}
		public void setHttpPost(HttpPost httpPost) {
			this.httpPost = httpPost;
		}
		private HttpAsyncClient asyClient;
    	private HttpPost httpPost;
    	
    	public String getTopicName(){
    		return topicName;
    	}
    	public String getSubscriberAddress(){
    		return subscriberAddress;
    	}
    	
    	public void setTopicName(String value){
    		topicName = value;
    	}
    	public void setSubscriberAddress(String value){
    		subscriberAddress = value;
    		try {
    			asyClient = new DefaultHttpAsyncClient();
    		} catch (IOReactorException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		asyClient.start();
    		httpPost = new HttpPost(subscriberAddress);
    	}
    }
