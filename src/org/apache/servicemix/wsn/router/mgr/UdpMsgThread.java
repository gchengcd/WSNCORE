package org.apache.servicemix.wsn.router.mgr;

import java.io.ByteArrayInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.wsn.push.NotifyObserverMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Date;

import org.apache.servicemix.wsn.router.mgr.base.SysInfo;

public class UdpMsgThread extends SysInfo implements Runnable {
	private static Log log = LogFactory.getLog(UdpMsgThread.class);
	private RtMgr rt;
	private MulticastSocket s;
	private byte[] buf = new byte[4096];
	private DatagramPacket p;
	private ObjectInputStream ois;
	private ByteArrayInputStream bais;

	public UdpMsgThread(RtMgr rt) {
		this.rt = rt;

		try {

			s = new MulticastSocket(new InetSocketAddress(localAddr, uPort));
//			System.out.println("local address: " + localAddr + "	port: " + uPort);
			log.info("local address: " + localAddr + "	port: " + uPort);
			s.joinGroup(InetAddress.getByName(multiAddr));
//			System.out.println("multicast address: " + multiAddr);
			log.info("multicast address: " + multiAddr);
			s.setLoopbackMode(true);

			s.setReceiveBufferSize(1024 * 1024);

			p = new DatagramPacket(buf, buf.length);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
		}

	}

	public void run() {
		// TODO Auto-generated method stub
		while (udpMsgThreadSwitch) {
			try {

				bais = new ByteArrayInputStream(buf);
				s.receive(p);
				ois = new ObjectInputStream(bais);

				Object msg = ois.readObject();

				if (msg instanceof MsgNotis) {
					MsgNotis mns = (MsgNotis) msg;
					Date time = new Date();
					log.info("receive msg :Delay" + (time.getTime() - mns.sendDate.getTime()));
					rt.addMqLast(mns);
					log.info("add a msg,and the Queue size is" + rt.getMqSize());

//					System.out.println("Receive:topic" + mns.topicName + ":content" + mns.doc + ":startTime" + mns.sendDate);
					log.info("Receive:topic" + mns.topicName + ":content" + mns.doc + ":startTime" + mns.sendDate);

				} else {
					rt.getState().processUdpMsg(msg);

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.warn(e);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.warn(e);
			}
		}
	}

}
