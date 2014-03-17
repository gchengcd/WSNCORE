package org.apache.servicemix.wsn.router.detection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.wsn.router.mgr.RtMgr;
import org.apache.servicemix.wsn.router.mgr.base.SysInfo;
import org.apache.servicemix.wsn.router.msg.udp.MsgHello;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/***
 * �ڵ��̽�� hello��Ϣ���ͺʹ��� ���ü�ʱ����ÿ�յ�һ��hello����Ը��ھ����¼�ʱ����ʱ�򱨸�RtMgr
 * 
 * @author Sylvia
 * 
 */

public class DtMgr implements IDt {
	private static Log log = LogFactory.getLog(DtMgr.class);

	private long threshold;// ʧЧ��ֵ��ȱʡֵ
	private long sendPeriod;// ����Ƶ�ʵ�ȱʡֵ

	private RtMgr rtMgr;// ����ģ��

	private Timer timer;// ��ʱ��
	private SendTask sendTask; // ����hello��Ϣ�ļ�ʱ��

	private List<String> neighbors;// Hello��Ϣ������¼�ھӼ�Ⱥ
	private Timer timerForHellos; // Ϊ�ھ��Ǽ�¼hello��Ϣ�ļ�ʱ��
	private LostTask lostTask[]; // ���ھӶ�ʧʱ��Ҫ�����Ķ���
	private ConcurrentHashMap<String, Integer> tblOfNbr; // ��¼�ھӼ�Ⱥ���Ƶ���������LostTask��Ķ�Ӧ
	private List<Integer> avlbNum; // ��¼���õ�losttask����

	public DtMgr(RtMgr rtMgr) {
		this.rtMgr = rtMgr;

		neighbors = new ArrayList<String>();
		// added by shmily
		TestLog("initialize-tbl: " + neighbors.toString());
		timer = new Timer();
		timerForHellos = new Timer();
		lostTask = new LostTask[20];
		tblOfNbr = new ConcurrentHashMap<String, Integer>();
		avlbNum = new ArrayList<Integer>();
		for (int i = 0; i < 20; i++)
			avlbNum.add(i);
	}

	public void setSendPeriod(long value) {
		if (sendTask != null)
			sendTask.cancel();
		TestLog("initialize-Period: " + value);

		sendPeriod = value;
		sendTask = new SendTask();
		timerForHellos.schedule(sendTask, sendPeriod, sendPeriod);
	}

	public void setThreshold(long value) {// Զ������
		threshold = value;
		TestLog("initialize-Threshold: " + value);
	}

	@SuppressWarnings("static-access")
	private void sendAction() {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		MsgHello hello = new MsgHello();
		byte[] outHello = null;

		try {

			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			if (rtMgr.getRepAddr().equals(rtMgr.getLocalAddr())) {
				hello.indicator = rtMgr.getGroupName();
			} else {
				hello.indicator = rtMgr.getLocalAddr();
			}
			hello.helloInterval = sendPeriod;
			hello.deadInterval = threshold;
			oos.writeObject(hello);
			outHello = baos.toByteArray();

		} catch (IOException e) {
			e.printStackTrace();
			log.warn(e);
		}

		try {
			DatagramSocket s = new DatagramSocket();
			// sent to other groups
		
				for (String n : neighbors) {
					if (rtMgr.getGroupMap().containsKey(n)) {
						DatagramPacket p = new DatagramPacket(outHello,
								outHello.length, InetAddress.getByName(rtMgr
										.getGroupMap().get(n).addr), rtMgr
										.getGroupMap().get(n).uPort);
						s.send(p);
						System.out.println("hello to "
								+ p.getAddress().getHostAddress() + " "
								+ p.getPort());
						log.info("hello to " + p.getAddress().getHostAddress()
								+ " " + p.getPort());
					}
				}

				if (rtMgr.getRepAddr().equals(rtMgr.getLocalAddr()) && !SysInfo.getFellows().isEmpty()) {
					MulticastSocket mss = new MulticastSocket(rtMgr.getUPort());
					mss.joinGroup(InetAddress.getByName(rtMgr.getMultiAddr()));
				DatagramPacket p = new DatagramPacket(outHello,
						outHello.length, InetAddress.getByName(rtMgr
								.getMultiAddr()), rtMgr.getUPort());
				mss.send(p);
			} 
		} catch (IOException e) {
			e.printStackTrace();
			log.warn(e);
		}
	}

	public void addTarget(String indicator) {
		if (!neighbors.contains(indicator)) {
			System.out.println("add new neighbor :" + indicator);
			neighbors.add(indicator);
		}

		Integer index;
		if (tblOfNbr.containsKey(indicator)) {
			lostTask[tblOfNbr.get(indicator)].cancel();
			index = tblOfNbr.get(indicator);
		} else {
			index = avlbNum.get(0);
			tblOfNbr.put(indicator, index);
			avlbNum.remove(index);
		}
		lostTask[index] = new LostTask(indicator);
		timer.schedule(lostTask[index], threshold);
	}

	public void onMsg(Object msg) {
		// on receiving hello message, set a timer for the neighbor
		MsgHello hello = (MsgHello) msg;
		if (!neighbors.contains(hello.indicator)) {
			log.info("Hello dropped because it's not a neighbor"
					+ hello.indicator);
			System.out.println("Hello dropped because it's not a neighbor"
					+ hello.indicator);
			return;
		}
		if (hello.helloInterval != sendPeriod) {
			System.out
					.println("Hello dropped because its sendPeriod doesn't match");
			log.info("Hello dropped because it's sendPeriod doesn't match");
			return;
		}
		if (hello.deadInterval != threshold) {
			System.out
					.println("Hello dropped because its deadLine doesn't match");
			log.info("Hello dropped because its deadLine doesn't match");
			return;
		}

		this.addTarget(hello.indicator);
	}

	public void removeTarget(String indicator) {

		if (neighbors.contains(indicator)) {
			neighbors.remove(indicator);
		}

		if (tblOfNbr.containsKey(indicator)) {
			Integer index = tblOfNbr.get(indicator);
			if (lostTask[index] != null)
				lostTask[index].cancel();
			tblOfNbr.remove(indicator);
			avlbNum.add(index);
		}

	}

	public static void TestLog(String LogContent) {
		/*
		 * try { testLog = new RandomAccessFile("C:\\testLog.log", "rw"); long
		 * fileLength = testLog.length(); testLog.seek(fileLength);
		 * testLog.writeBytes(LogContent + "\r\n"); testLog.close(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */}

	// define the lost action that should be taken when a neighbor is timeout
	class LostTask extends TimerTask {
		String groupName;

		public LostTask(String groupName) {
			this.groupName = groupName;
		}

		@Override
		public void run() {
			removeTarget(groupName);
			rtMgr.lost(groupName);
		}
	}

	// define the action that should be taken when a representative has
	// neighbors and want to send hellos to them
	class SendTask extends TimerTask {

		@Override
		public void run() {
			// send hellos to active neighbors
			sendAction();
		}
	}
}