package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.Serializable;
import java.util.TreeSet;

public class MsgLookupMemberSubscriptions_ implements Serializable {

	public TreeSet<String> topics;
	
	public MsgLookupMemberSubscriptions_() {
		topics = new TreeSet<String>();
	}
	
}
