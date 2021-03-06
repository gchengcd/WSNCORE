package org.Mina.shorenMinaTest.mgr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.Mina.shorenMinaTest.MinaUtil;
import org.Mina.shorenMinaTest.mgr.base.AConfiguration;
import org.Mina.shorenMinaTest.msg.tcp.BrokerUnit;
import org.Mina.shorenMinaTest.msg.tcp.GroupUnit;
import org.Mina.shorenMinaTest.msg.tcp.MsgConf;
import org.Mina.shorenMinaTest.msg.tcp.MsgConf_;


public class Configuration extends AConfiguration {
	private static Log log = LogFactory.getLog(Configuration.class);
	private RtMgr mgr;
	private int mgr2;
	
	public Configuration(RtMgr mgr) {
		this.mgr = mgr;
	}
	
	public Configuration() {
		// TODO Auto-generated constructor stub
		this.mgr2 = EnQueueTime;
	}
	
	public boolean configure2(){
		
		File file = new File("configure.txt");//读取本地配置文件

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));

			String l;
			String[] s;

			while ((l = reader.readLine()) != null) {

				s = l.split(":");

				s[0] = s[0].trim();

				String adminAddr3;
				String adminAddr4;
				if (s[0].equals("administrator's address1"))
					adminAddr3 = s[1].trim();
				else if (s[0].equals("administrator's address2"))
					adminAddr4 = s[1].trim();
				else if (s[0].equals("administrator's port"))
					adminPort = Integer.parseInt(s[1].trim());
				else if (s[0].equals("queueSize"))
					queueSize = Integer.parseInt(s[1].trim());
				else if (s[0].equals("poolCount"))
					poolCount = Integer.parseInt(s[1].trim());
				else if (s[0].equals("connectCount"))
					connectCount = Integer.parseInt(s[1].trim());
				else if (s[0].equals("local group name"))
					groupName = s[1].trim();
				else if (s[0].equals("local address"))
					localAddr = s[1].trim();
				else if (s[0].equals("local port")){
					tPort = Integer.parseInt(s[1].trim());
				}
				else if (s[0].equals("EnqueueTime"))
					EnQueueTime = Integer.parseInt(s[1].trim());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
		}
		
		
		return true;
		
	}
	

	public boolean configure() {
		// TODO Auto-generated method stub
		String adminAddr1 = "";//存放配置的主从管理员地址
		String adminAddr2 = "";//存放配置的主从管理员地址

		File file = new File("configure.txt");//读取本地配置文件

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));

			String l;
			String[] s;

			while ((l = reader.readLine()) != null) {

				s = l.split(":");

				s[0] = s[0].trim();

				if (s[0].equals("administrator's address1"))
					adminAddr1 = s[1].trim();
				else if (s[0].equals("administrator's address2"))
					adminAddr2 = s[1].trim();
				else if (s[0].equals("administrator's port"))
					adminPort = Integer.parseInt(s[1].trim());
				else if (s[0].equals("queueSize"))
					queueSize = Integer.parseInt(s[1].trim());
				else if (s[0].equals("poolCount"))
					poolCount = Integer.parseInt(s[1].trim());
				else if (s[0].equals("connectCount"))
					connectCount = Integer.parseInt(s[1].trim());
				else if (s[0].equals("local group name"))
					groupName = s[1].trim();
				else if (s[0].equals("local address"))
					localAddr = s[1].trim();
				else if (s[0].equals("local port")){
					tPort = Integer.parseInt(s[1].trim());
			        System.out.println("当前的TCP端口号为："+tPort);
				}
				else if (s[0].equals("EnqueueTime"))
					EnQueueTime = Integer.parseInt(s[1].trim());
//				else if (s[0].equals("thresholdInitialize"))
//					thresholdInitialize = Long.parseLong(s[1].trim());
//				else if (s[0].equals("sendPeriodInitialize"))
//					sendPeriodInitialize = Long.parseLong(s[1].trim());
//				else if (s[0].equals("scanPeriodInitialize"))
//					scanPeriodInitialize = Long.parseLong(s[1].trim());
//				else if (s[0].equals("synPeriodInitialize"))
//					synPeriodInitialize = Long.parseLong(s[1].trim());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.warn(e);
		}

		Socket s = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;

		boolean FindAdmin = false;
		adminAddr = adminAddr1;
		String AnotherAddr = adminAddr2;
/*		while (!FindAdmin) {
			int flag = 1;//管理员是否在启动状态
			try {
				//get configuration
				s = new Socket();
				s.setSoTimeout(10000);
				s.connect(new InetSocketAddress(adminAddr, adminPort));
				//	s = new Socket(adminAddr, adminPort);
			} catch (Exception e) {
				log.warn(e);
				try {
					s.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				flag = 0;
			}
			if (flag == 1) {//启动状态,向管理员发送配置，看能否返回正确配置信息，以确定是否是主管理员
				try {
					oos = new ObjectOutputStream(s.getOutputStream());
					ois = new ObjectInputStream(s.getInputStream());

					//ask admin for configuration 
					MsgConf conf = new MsgConf();
					conf.name = groupName;
					conf.addr = localAddr;
					oos.writeObject(conf);

					//configurations returned and set them

					Object msg = ois.readObject();

					oos.close();
					ois.close();
					s.close();

					if (msg instanceof MsgConf_) {
						FindAdmin = true;
						MsgConf_ conf_ = (MsgConf_) msg;
						rep = new BrokerUnit();
						rep.addr = conf_.repAddr;
						rep.tPort = conf_.tPort;

						multiAddr = conf_.multiAddr;
						childrenSize = conf_.childrenSize;
						nextInsertChild = 0;
						uPort = conf_.uPort;
						joinTimes = conf_.joinTimes;

						threshold = conf_.lostThreshold;
						sendPeriod = conf_.sendPeriod;
						scanPeriod = conf_.scanPeriod;
						synPeriod = conf_.synPeriod;
					}//if	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.warn(e);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log.warn(e);
				}
			}//if(flag==1)
			else {
				String str = adminAddr;
				adminAddr = AnotherAddr;//不是主管理员，设置管理员地址为adminAddr2
				AnotherAddr = str;
			}
		}//while
*/
		//测试添加
		rep = new BrokerUnit();
		rep.addr = "";
		rep.tPort = tPort;
		uPort = 30002;
		//测试添加
		
		id = 0;

		joinOK = false;

		String repAddr = rep.addr;

		mgr.setState(rep.addr.equals("") ? mgr.getRepState() : mgr.getRegState());

		groupMap = new ConcurrentHashMap<String, GroupUnit>();

		clientTable = new ArrayList<String>();

		brokerTable = new ConcurrentHashMap<String, TreeSet<String>>();

		groupTable = new ConcurrentHashMap<String, TreeSet<String>>();

		neighbors = new ConcurrentHashMap<String, BrokerUnit>();

		children = new ArrayList<String>();

		wait4Hrt = new ArrayList<String>();

		udpMsgThreadSwitch = true;
		tcpMsgThreadSwitch = true;

		System.out.println("configuration finished! It's " + (repAddr.equals("") ? "representative" : "regular"));
		log.info("configuration finished! It's " + (repAddr.equals("") ? "representative" : "regular"));

		return FindAdmin;
	}
}
