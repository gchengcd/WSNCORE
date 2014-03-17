package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.apache.servicemix.wsn.router.mgr.RtMgr;
import org.apache.servicemix.wsn.router.mgr.base.AState;

public class MsgNewRep implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String sender;
	
	public String name;//¼¯ÈºÃû×Ö
	
	public String netmask;
	
	public int uPort;
	
	//below is representative;'s information
	public long id;
	
	public String addr;
	
	public int tPort;
	
	@SuppressWarnings("static-access")
	public void processRepMsg(ObjectInputStream ois,
			ObjectOutputStream oos, Socket s, MsgNewRep mnr) {
		AState state = RtMgr.getInstance().getState();

		if (state.getGroupMap().containsKey(mnr.name)
				&& state.getGroupMap().get(mnr.name).addr.equals(mnr.addr)) {
			return;
		}
		System.out.println("group: " + mnr.name + " new rep" + mnr.addr);
		
		// find the group and update its information
		state.getGroupMap().get(mnr.name).uPort = mnr.uPort;
		state.getGroupMap().get(mnr.name).addr = mnr.addr;
		state.getGroupMap().get(mnr.name).tPort = mnr.tPort;
		state.getGroupMap().get(mnr.name).id = mnr.id;
		state.getGroupMap().get(mnr.name).netmask = mnr.netmask;
		
		if (state.getWaitHello().contains(mnr.name)) {
			state.getWaitHello().remove(mnr.name);
			RtMgr.getInstance().addTarget(mnr.name);
		}

		state.spreadInLocalGroup(mnr);

		mnr.sender = state.getGroupName();
		state.sendObjectToNeighbors(mnr);
	}
	
}
