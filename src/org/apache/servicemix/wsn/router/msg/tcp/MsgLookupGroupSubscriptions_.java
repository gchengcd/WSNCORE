package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.Serializable;
import java.util.TreeSet;

public class MsgLookupGroupSubscriptions_ implements Serializable {

	public TreeSet<String> topics;
	
	public MsgLookupGroupSubscriptions_() {
		topics = new TreeSet<String>();
	}
	
}
