package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.apache.servicemix.wsn.router.mgr.RtMgr;
import org.apache.servicemix.wsn.router.mgr.base.AState;

public class MsgLookupGroupSubscriptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	
	@SuppressWarnings("static-access")
	public void processRepMsg(ObjectInputStream ois,
			ObjectOutputStream oos, Socket s, MsgLookupGroupSubscriptions mlgs) {
		AState state = RtMgr.getInstance().getState();
		System.out.println("look up group Subscriptions");
		if (mlgs.name.equals(state.getGroupName())) {
			MsgLookupGroupSubscriptions_ mlgs_ = new MsgLookupGroupSubscriptions_();

			for (String t : state.getBrokerTable().keySet()) {
				mlgs_.topics.add(t);
			}
			// System.out.println(clientTable.get(key));
			for (String t : state.getClientTable()) {
				mlgs_.topics.add(t);
			}// for

			try {
				oos.writeObject(mlgs_);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}// if

	}
	
}
