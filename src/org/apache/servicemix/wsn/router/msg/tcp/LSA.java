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
	 * ��·״̬��Ϣ
	 */
	private static final long serialVersionUID = 1L;
	public int seqNum; // ���к�
	public int syn; // 0Ϊ��ͨLSA��1Ϊͬ��LSA
	public String originator; // ����Դ����
	public ArrayList<String> lostGroup; // ��ʧ��Ⱥ�����޶�ʧ��Ϊ��
	public ArrayList<String> subsTopics; // ����Դ�Ķ���
	public ArrayList<String> cancelTopics; //����Դȡ���Ķ���
	public  ConcurrentHashMap<String, DistBtnNebr> distBtnNebrs; // ����Դ���ھӵľ���
	public long sendTime; //����ʱ��
	
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

		// �ڱ���Ⱥ���鲥
		state.spreadInLocalGroup(lsa);

		// ת����������Ⱥ
		state.sendObjectToNeighbors(lsa);

	}
}
 