package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.servicemix.wsn.router.mgr.BrokerUnit;

public class MsgLookupGroupMember_ implements Serializable {

	public ArrayList<BrokerUnit> members;
	
	public MsgLookupGroupMember_() {
		members = new ArrayList<BrokerUnit>();
	}
	
}
