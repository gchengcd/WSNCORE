package org.Mina.shorenMinaTest.handlers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBException;
import javax.xml.ws.Endpoint;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.apache.servicemix.application.WsnProcessImpl;
import org.apache.servicemix.wsn.push.SendNotification;
import org.apache.servicemix.wsn.router.wsnPolicy.ShorenUtils;
import org.apache.servicemix.wsn.router.wsnPolicy.msgs.TargetGroup;
import org.apache.servicemix.wsn.router.wsnPolicy.msgs.WsnPolicyMsg;

import org.Mina.shorenMinaTest.MinaUtil;
import org.Mina.shorenMinaTest.mgr.RtMgr;
import org.Mina.shorenMinaTest.msg.WsnMsg;
import org.Mina.shorenMinaTest.msg.tcp.MsgNotis;
import org.Mina.shorenMinaTest.msg.tcp.highPriority;
import org.Mina.shorenMinaTest.msg.tcp.lowPriority;
import org.Mina.shorenMinaTest.queues.ForwardMsg;
import org.Mina.shorenMinaTest.queues.MsgQueueMgr;
import org.Mina.shorenMinaTest.queues.TCPForwardMsg;
import org.Mina.shorenMinaTest.router.MsgSubsForm;
import org.Mina.shorenMinaTest.router.generateNode;

public class Start {
	public static RtMgr mgr = RtMgr.getInstance();
    public static ArrayList<String> forwardIP = new ArrayList<String>();//�洢ת��Ŀ��ip�ı���
	public static ArrayList<MsgSubsForm> nodeList = new ArrayList<MsgSubsForm>();
	public static MsgSubsForm groupTableRoot = null;
	public static generateNode gN = new generateNode();
	public static ConcurrentHashMap<String, MsgSubsForm> testMap = new ConcurrentHashMap<String, MsgSubsForm>();
	
	private static int UDPtotalCount = 0;
	private static int totalCount = 0;
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		
		WsnProcessImpl wsnprocess = new WsnProcessImpl();
		try {
			wsnprocess.init();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Endpoint.publish(args[0], wsnprocess);
		
		RtMgr.getInstance();
		initNode();
		MsgQueueMgr.getInstance();
	}

	public static void initNode(){
		generateNode.generateNodeList(nodeList);
		groupTableRoot = nodeList.get(0);
		
        for(int i=0;i<10000;i++){			
			if(i==500){
				testMap.put("500", groupTableRoot);
			}else{
			    MsgSubsForm tempNode = generateNode.generateTestNode(Integer.toString(i));
			    testMap.put(tempNode.topicComponent, tempNode);
			}
		}
	}
	
    public static void removeAimAddr(String ip){
		for(int i = 0; i<forwardIP.size(); i++){
			if(forwardIP.get(i).equals(ip)){
				//System.out.println("Ҫ�Ƴ���ip�ǣ�"+ip);
				forwardIP.remove(i);
				//System.out.println("�Ƴ����forwardipΪ��"+forwardIP.toString());
			}
		}
	}
    
	//��ȡ�յ���TCP��Ϣ����
	public static int GetTCPRecievedCount(){
		return totalCount;
	}
	//��ȡ�յ���UDP��Ϣ����
	public static int GetUDPRecievedCount(){
		return UDPtotalCount;
	}
	//����������Ϊ��
	public static void SetTCPRecievedCountToZero(){
		totalCount = 0;
	}
	
	public static void SetUDPRecievedToZero(){
		UDPtotalCount = 0;
	}
	
  	public static void generateMsgNoticeMsg(MsgNotis msg){
		//ArrayList<String> forwardIp = getForwardIp(topicStr, origin);
		ArrayList<String> ret = org.apache.servicemix.wsn.router.mgr.RtMgr.calForwardGroups(msg.topicName,
				msg.originatorGroup);
		ArrayList<String> forwardIp = new ArrayList<String>();
		Iterator<String> it = ret.iterator();
		while (it.hasNext()) {
			String itNext = it.next();
			String addr = org.apache.servicemix.wsn.router.mgr.base.SysInfo.groupMap.get(itNext).addr;
			forwardIp.add(addr);
		}
		
		System.out.println("�ڶ�����Ϣ++++++"+forwardIp);
		
		String splited[] = msg.topicName.split(":");
		String ex = "";
		boolean filtered = false;
		for (int i = 0; i < splited.length; i++) {
			if (i > 0)
				ex += ":";
			ex += splited[i];
			WsnPolicyMsg wpm = ShorenUtils.decodePolicyMsg(ex);
			if (wpm != null) {
				for (TargetGroup tg : wpm.getAllGroups()) {
					if (tg.getName().equals(org.Mina.shorenMinaTest.mgr.base.SysInfo.groupName) && tg.isAllMsg()) {
						filtered = true;
						break;
					}
				}
			}
		}
		
		if (!filtered) {
			boolean send = false;
			for(String topic :  
				org.apache.servicemix.wsn.router.mgr.base.SysInfo.clientTable) {
				if(isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send && !msg.originatorAddr.equals(org.apache.servicemix.wsn.router.mgr.base.SysInfo.localAddr))// �����ж��Ĳ��Ҳ�����Ϣ�����ߣ����Ͻ�wsn��������ط����ʺ������̣߳�ȫ�ֱ�����ֻ����Ҳ���������
			{
				final org.Mina.shorenMinaTest.msg.tcp.MsgNotis mns = 
					(org.Mina.shorenMinaTest.msg.tcp.MsgNotis) msg;
			    SendNotification SN = new SendNotification();// �����ϲ�wsn�Ľӿ�
				try {
					SN.send(mns.doc);
					org.apache.servicemix.wsn.router.mgr.RtMgr.subtract();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			send = false;
			for (String topic : org.apache.servicemix.wsn.router.mgr.base.SysInfo.brokerTable.keySet()) {
				if (isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send) {
				spreadInLocalGroup(msg);
			}
			
			}
		
		//���Կ��λ�ã��ɲ��Կ�������ip
		ForwardMsg forwardMsg = new TCPForwardMsg(forwardIp, 30008, (WsnMsg)msg);
		MsgQueueMgr.addTCPMsgInQueue(forwardMsg);
		
/*		for(int i=0;i<forwardIp.size();i++){
			System.out.println(forwardIp.size());
			NioSocketConnector connector = MinaUtil.createSocketConnector();
			//ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), SysInfo.gettPort()));//��������   
			ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), 30008));//��������   
			cf.awaitUninterruptibly();//�ȴ����Ӵ������   
				
		    try {
			    session = cf.getSession();
				session.write(forwardMsg.getMsg());
		    }catch (Exception e) {
			    //System.out.println("��������ʧ�ܣ�����"+ip+"�ڵ㣡");
			    return;
			    // TODO: handle exception
		    }
		    session.close(true);
		}*/
	}
	
	
	public static void generateHighPrioriyMsg(highPriority msg){
		//ArrayList<String> forwardIp = getForwardIp(topicStr, origin);
		ArrayList<String> ret = org.apache.servicemix.wsn.router.mgr.RtMgr.calForwardGroups(msg.topicName,
				msg.originatorGroup);
		
		ArrayList<String> forwardIp = new ArrayList<String>();
		Iterator<String> it = ret.iterator();
		while (it.hasNext()) {
			String itNext = it.next();
			//System.out.println("@@@@@@@@@@@@@@@@@@@@@:"+org.apache.servicemix.wsn.router.mgr.base.SysInfo.groupMap.get(itNext).addr);
			String addr = org.apache.servicemix.wsn.router.mgr.base.SysInfo.groupMap.get(itNext).addr;
			forwardIp.add(addr);
		}
		
		String splited[] = msg.topicName.split(":");
		String ex = "";
		boolean filtered = false;
		for (int i = 0; i < splited.length; i++) {
			if (i > 0)
				ex += ":";
			ex += splited[i];
			WsnPolicyMsg wpm = ShorenUtils.decodePolicyMsg(ex);
			if (wpm != null) {
				for (TargetGroup tg : wpm.getAllGroups()) {
					if (tg.getName().equals(org.Mina.shorenMinaTest.mgr.base.SysInfo.groupName) && tg.isAllMsg()) {
						filtered = true;
						break;
					}
				}
			}
		}
		
		if (!filtered) {
			boolean send = false;
			for(String topic :  
				org.apache.servicemix.wsn.router.mgr.base.SysInfo.clientTable) {
				if(isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send && !msg.originatorAddr.equals(org.apache.servicemix.wsn.router.mgr.base.SysInfo.localAddr))// �����ж��Ĳ��Ҳ�����Ϣ�����ߣ����Ͻ�wsn��������ط����ʺ������̣߳�ȫ�ֱ�����ֻ����Ҳ���������
			{
			}
			
			send = false;
			for (String topic : org.apache.servicemix.wsn.router.mgr.base.SysInfo.brokerTable.keySet()) {
				if (isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send) {
				spreadInLocalGroup(msg);
			}
			
			}
		
		System.out.println(forwardIp);
		//���Կ��λ�ã��ɲ��Կ�������ip
		ForwardMsg forwardMsg = new TCPForwardMsg(forwardIp, 30008, (WsnMsg)msg);
		MsgQueueMgr.addTCPMsgInQueue(forwardMsg);
		
/*		for(int i=0;i<forwardIp.size();i++){
			System.out.println(forwardIp.size());
			NioSocketConnector connector = MinaUtil.createSocketConnector();
			//ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), SysInfo.gettPort()));//��������   
			ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), 30008));//��������   
			cf.awaitUninterruptibly();//�ȴ����Ӵ������   
				
		    try {
			    session = cf.getSession();
				session.write(forwardMsg.getMsg());
			    final org.Mina.shorenMinaTest.msg.tcp.highPriority mns = 
					(org.Mina.shorenMinaTest.msg.tcp.highPriority) msg;
			    SendNotification SN = new SendNotification();// �����ϲ�wsn�Ľӿ�
				try {
					SN.send(mns.doc);
					org.apache.servicemix.wsn.router.mgr.RtMgr.subtract();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
		    }catch (Exception e) {
			    //System.out.println("��������ʧ�ܣ�����"+ip+"�ڵ㣡");
			    return;
			    // TODO: handle exception
		    }
		    session.close(true);
		}*/
	}
	
	
	public static void generateLowPrioriyMsg(lowPriority msg){
		//ArrayList<String> forwardIp = getForwardIp(topicStr, origin);
		ArrayList<String> ret = org.apache.servicemix.wsn.router.mgr.RtMgr.calForwardGroups(msg.topicName,
				msg.originatorGroup);
		
		ArrayList<String> forwardIp = new ArrayList<String>();
		Iterator<String> it = ret.iterator();
		while (it.hasNext()) {
			String itNext = it.next();
			//System.out.println("@@@@@@@@@@@@@@@@@@@@@:"+org.apache.servicemix.wsn.router.mgr.base.SysInfo.groupMap.get(itNext).addr);
			String addr = org.apache.servicemix.wsn.router.mgr.base.SysInfo.groupMap.get(itNext).addr;
			forwardIp.add(addr);
		}
		
		String splited[] = msg.topicName.split(":");
		String ex = "";
		boolean filtered = false;
		for (int i = 0; i < splited.length; i++) {
			if (i > 0)
				ex += ":";
			ex += splited[i];
			WsnPolicyMsg wpm = ShorenUtils.decodePolicyMsg(ex);
			if (wpm != null) {
				for (TargetGroup tg : wpm.getAllGroups()) {
					if (tg.getName().equals(org.Mina.shorenMinaTest.mgr.base.SysInfo.groupName) && tg.isAllMsg()) {
						filtered = true;
						break;
					}
				}
			}
		}
		
		if (!filtered) {
			boolean send = false;
			for(String topic :  
				org.apache.servicemix.wsn.router.mgr.base.SysInfo.clientTable) {
				if(isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send && !msg.originatorAddr.equals(org.apache.servicemix.wsn.router.mgr.base.SysInfo.localAddr))// �����ж��Ĳ��Ҳ�����Ϣ�����ߣ����Ͻ�wsn��������ط����ʺ������̣߳�ȫ�ֱ�����ֻ����Ҳ���������
			{
			}
			
			send = false;
			for (String topic : org.apache.servicemix.wsn.router.mgr.base.SysInfo.brokerTable.keySet()) {
				if (isIncluded(topic, msg.topicName)) {
					send = true;
					break;
				}
			}
			if (send) {
				spreadInLocalGroup(msg);
			}
			
			}
		
		System.out.println(forwardIp);
		//���Կ��λ�ã��ɲ��Կ�������ip
		ForwardMsg forwardMsg = new TCPForwardMsg(forwardIp, 30008, (WsnMsg)msg);
		MsgQueueMgr.addTCPMsgInQueue(forwardMsg);
		
/*		for(int i=0;i<forwardIp.size();i++){
			System.out.println(forwardIp.size());
			NioSocketConnector connector = MinaUtil.createSocketConnector();
			//ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), SysInfo.gettPort()));//��������   
			ConnectFuture cf = connector.connect(new InetSocketAddress(forwardIp.get(i), 30008));//��������   
			cf.awaitUninterruptibly();//�ȴ����Ӵ������   
				
		    try {
			    session = cf.getSession();
				session.write(forwardMsg.getMsg());
			    final org.Mina.shorenMinaTest.msg.tcp.lowPriority mns = 
					(org.Mina.shorenMinaTest.msg.tcp.lowPriority) msg;
			    SendNotification SN = new SendNotification();// �����ϲ�wsn�Ľӿ�
				try {
					SN.send(mns.doc);
					org.apache.servicemix.wsn.router.mgr.RtMgr.subtract();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
		    }catch (Exception e) {
			    //System.out.println("��������ʧ�ܣ�����"+ip+"�ڵ㣡");
			    return;
			    // TODO: handle exception
		    }
		    session.close(true);
		}*/
	}
	
	public static boolean isIncluded(String mother, String child) {
		String splited[] = mother.split(":");
		String spliCh[] = child.split(":");
		if (spliCh.length < splited.length)
			return false;
		String temp = spliCh[0];
		for (int i = 1; i < splited.length; i++)
			temp += ":" + spliCh[i];
		if (temp.equals(mother))
			return true;
		else
			return false;
	}
	
	
	public static void spreadInLocalGroup(Object obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream doos = null;
		DatagramSocket s = null;
		DatagramPacket p = null;
		byte[] buf = null;

		try {
			doos = new ObjectOutputStream(baos);
			s = new DatagramSocket();
			doos.writeObject(obj);
			buf = baos.toByteArray();

			// multicast it in this group
			p = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(org.apache.servicemix.wsn.router.mgr.base.SysInfo.multiAddr), 
					org.apache.servicemix.wsn.router.mgr.base.SysInfo.uPort);
			s.send(p);
			
			//System.out.println("�鲥��ַ��:"+org.apache.servicemix.wsn.router.mgr.base.SysInfo.multiAddr);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
