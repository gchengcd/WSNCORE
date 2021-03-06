package org.apache.servicemix.wsn.router.msg.tcp;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.wsn.router.mgr.RtMgr;
import org.apache.servicemix.wsn.router.mgr.base.AState;

public class LSA implements Serializable{
	/**
	 * 链路状态消息
	 */
	private static final long serialVersionUID = 1L;
	public int seqNum; // 序列号
	public int syn; // 0为普通LSA，1为同步LSA
	public String originator; // 发送源名称
	public ArrayList<String> lostGroup; // 丢失集群，若无丢失则为空
	public ArrayList<String> subsTopics; // 发送源的订阅
	public ArrayList<String> cancelTopics; //发送源取消的订阅
	public  ConcurrentHashMap<String, DistBtnNebr> distBtnNebrs; // 发送源与邻居的距离
	public long sendTime; //发送时间
	
	public LSA() {
		lostGroup = new ArrayList<String>();
		subsTopics = new ArrayList<String>();
		cancelTopics = new ArrayList<String>();
		distBtnNebrs = new ConcurrentHashMap<String, DistBtnNebr>();
	}
	
	public void processRepMsg(ObjectInputStream ois,
			ObjectOutputStream oos, Socket s, LSA lsa) {
		AState state = RtMgr.getInstance().getState();
		// receive lsa from other groups
		System.out.println("lsa from " + lsa.originator + " lsa seqNum: "
				+ lsa.seqNum);
		if (!state.addLSAToLSDB(lsa))
			return;

		// 在本集群中组播
		state.spreadInLocalGroup(lsa);

		// 转发到其他集群
		state.sendObjectToNeighbors(lsa);

	}
}
 