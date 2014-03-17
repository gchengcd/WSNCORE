/**
 * @author shoren
 * @date 2013-4-24
 */
package org.Mina.shorenMinaTest.queues;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.Mina.shorenMinaTest.MinaUtil;
import org.Mina.shorenMinaTest.msg.WsnMsg;
import org.Mina.shorenMinaTest.msg.tcp.EmergencyMsg;
import org.Mina.shorenMinaTest.msg.tcp.MsgNotis;
import org.Mina.shorenMinaTest.msg.tcp.highPriority;

/**
 *���й�������
 *���й�����ֻ��һ����ʹ�õ���ģʽ.
 *
 *ʹ���������У���һ���洢��Ϣ���ڶ����Ƿ��Ͷ��У���������MINA client�˵�ͨ�����ͻ�����С�
 *
 *���Ͷ�����Ϊ�ַ��̳߳ص�������С�
 *���Ͷ��м�һ��������������Ϣ���.��һ���������߳������ɡ�ͬʱ����������Ϣ������Ϣ�����ȷ��͡�
 *���淢�Ͷ����еĶ�����Ϣ���Լ�������Ϣ��ӵı�����
 *
 *�������������ö�������������
 */
public class MsgQueueMgr {
	
	private static Log log = LogFactory.getLog(MsgQueueMgr.class);
	
	/******************************  ��Ϣ����     **********************/
	
	//�н���������,��Ϊ��һ������
	//������Ϣ,����һ��ͨ������
	private static LinkedBlockingQueue<ForwardMsg> emergencyMsgQueue;   //���ȼ�1
	//TCP֪ͨ��Ϣ
	private static LinkedBlockingQueue<ForwardMsg> TCPNoticeMsgQueue;   //���ȼ�2
	//TCP������Ϣ
	private static LinkedBlockingQueue<ForwardMsg> otherMsgQueue;       //���ȼ�3
	//udp��Ϣ
	private static LinkedBlockingQueue<ForwardMsg> UDPMsgQueue;          //���ȼ�4
	
	//����һ�����еĴ�С,�ܹ��������е���Ϣ
	private static final int QUEUESIZE = 10000;  
	
	//���Ͷ��У���Ϊ��������
	private static LinkedList<ForwardMsg> forwardQueue;    

	private static double inQueueRatio = 0.8;  //ʮ��֮��
	//һ����ӵ�����
	private static int inQueueNum = 800;
	
	
	//���ȼ�����еĶ�Ӧ
//	private HashMap<String, ArrayBlockingQueue<ForwardMsg>> priority_queue;
	//�̰߳�ȫ��map
	private static ConcurrentHashMap<String, IoSession> dest_session;	

	private static MsgQueueMgr INSTANCE = null;
	
	protected static int PRIMSG = 0;
	public static final int FIRSTPRIMSG = 1;   //0x0001,������Ϣ����һ��ͨ��,ֱ���͵�����������
	public static final int SECONDPRIMSG = 2;  //0x0010
	public static final int THIRDPRIMSG = 4;   //0x0100
	public static final int FOURTHPRIMSG = 8;  //0x1000	
	public static final int LOWESTPRI = 2;  //������ȼ�
//	private static int PRIORITYMSG = 14;        //Ĭ�ϰ�����������Ϣ֮���������Ϣ��0x1110

	
	//������Ϣ��ӵı���,�ɵ���   
	private static final double INISECPRO = 0.5;  //�ڶ����ȼ���Ϣ��ӳ�ʼ������ʮ��֮��
	private static final double INITHIRDPRO = 0.3;  //�������ȼ���Ϣ��ӳ�ʼ������ʮ��֮��
	private static final double INIFOURPRO = 0.2;
 	private static final double proin = 0.05;  //���ӱ���
 	private static final double prode = 0.08;  //���ٱ���
 	private static final double MINRATIO = 0.1;  //��ͱ�����0.1
// 	private static final double MAXRATIO = 0.6;  //��߱�����0.6
 
 	protected static double secPro = INISECPRO;     
	protected static double thirdPro = INITHIRDPRO;  
	protected static double fourPro = INIFOURPRO;
 	/**
 	 * ���������Ϣ����ز���
 	 * */
 	private static long last_update = 0;
 	private static long now = 0;
 	//�̳߳�
	private ThreadPoolExecutor deliverWorker;
		
	private static final int iniCoreSize = 4;
	private static final int iniMaxSize = 8;
//	private static final int minSize = 2;
	private static final int stepde = 2;
	private static final int stepin = 1;
	private static final int taskSize = 5;  //������еĳ���
	
	/***********************   ������     *******************/
	public static MsgQueueMgr getInstance(){
		if (INSTANCE == null)
			INSTANCE = new MsgQueueMgr();
		return INSTANCE;
	}

	
	
	private MsgQueueMgr(){
		//ָ��һ���㹻��С������
		emergencyMsgQueue = new LinkedBlockingQueue<ForwardMsg>(QUEUESIZE);
		TCPNoticeMsgQueue = new LinkedBlockingQueue<ForwardMsg>(QUEUESIZE); 		 
		otherMsgQueue = new LinkedBlockingQueue<ForwardMsg>(QUEUESIZE);
		UDPMsgQueue = new LinkedBlockingQueue<ForwardMsg>(QUEUESIZE);
		forwardQueue = new LinkedList<ForwardMsg>();
	
		dest_session = new ConcurrentHashMap<String, IoSession>();
		//���Ͷ��е����
		new ForwardQueueExecutor().start();
		
		//���Ͷ��еĳ���
		initThreadPool();
	}

	//��ʼ���̳߳�
	public void initThreadPool(){
		//core=4; max=8; time=3s; queuelen=3;
		deliverWorker = new ThreadPoolExecutor(iniCoreSize, iniMaxSize, 3, TimeUnit.SECONDS,  
				new ArrayBlockingQueue<Runnable>(taskSize),new ThreadPoolExecutor.CallerRunsPolicy());
		while(true){
			if(forwardQueue.isEmpty()){
				try {
					Thread.sleep(MinaUtil.freeze_time );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				deliverWorker.execute(new SingleChannelQueueThread());
				
				
			}				
		}

	}

	
	//���ݲ��ԣ���ȡҪ���͵���Ϣ�����뷢�Ͷ����С�
	protected static void addSubmitMsgToQueue(){
		ForwardMsg msg = null; //how to get msg ????????
		forwardQueue.offer(msg);
	}
	
	//�ӷ��Ͷ����л�ȡ������Ϣ��������Ϊ�գ�����null.
	public static void getForwardMsg(LinkedList<ForwardMsg> temp, int count){		
		synchronized(forwardQueue){
			for(int i = 0; i< count; i++){
				ForwardMsg msg = forwardQueue.poll();
				if(msg == null)
					break;
				temp.offer(msg);
			}			 
		}		
	}

	protected int getForwardQueueSize(){
		synchronized(forwardQueue){
			return forwardQueue.size();
		}		
	}
	
	public static void addDestMsg(String dest, IoSession session){
		dest_session.put(dest, session);
	}
	
	/**
	 * �������ȼ������У�ͬʱΪÿ����Ϣָ�����ȼ�
	 * */
	public static void addUDPMsgInQueue(ForwardMsg msg){
		try {
			UDPMsgQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		msg.setPriority(FOURTHPRIMSG);
	//	System.out.println("udpMsgQueue:" + msg.getMsg().msgToString());
	}
	
	/**
	 * �������ȼ������У�ͬʱΪÿ����Ϣָ�����ȼ�
	 * */
	public static void addTCPMsgInQueue(ForwardMsg forwardMsg){
		WsnMsg msg = forwardMsg.getMsg();
		
		if(msg instanceof EmergencyMsg|| msg instanceof highPriority){
			try {
				emergencyMsgQueue.put(forwardMsg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			forwardMsg.setPriority(FIRSTPRIMSG);
	//		System.out.println("emergencyMsgQueue:" + msg.msgToString());
		}			
		else if(msg instanceof MsgNotis){
			try {
				TCPNoticeMsgQueue.put(forwardMsg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			forwardMsg.setPriority(SECONDPRIMSG);
	//		System.out.println("TCPNoticeMsgQueue:" + msg.msgToString());
		}			
		else{
			try {
				otherMsgQueue.put(forwardMsg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			forwardMsg.setPriority(THIRDPRIMSG);
	//		System.out.println("otherTCPMsgQueue:" + msg.msgToString());
		}
			
	}
	
	public static ConcurrentHashMap<String, IoSession> getDest_session() {
		return dest_session;
	}
	
	public static void deleteDest_session(){
		dest_session.clear();
	}

	public static void setDest_session(ConcurrentHashMap<String, IoSession> dest_session) {
		MsgQueueMgr.dest_session = dest_session;
	}

	
	
	public static void main(String[] args) {
		try {
			emergencyMsgQueue.put(new TCPForwardMsg("ip", 1234,null));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ForwardMsg msg = emergencyMsgQueue.peek();
		System.out.println(msg.getClass().getName());
		
	}
	
	
	//���Ͷ��еļ����̣߳�������Ϣ���
	class ForwardQueueExecutor extends Thread{
		private final Log log = LogFactory.getLog(ForwardQueueExecutor.class);
		
		//��ӵ�ʱ������һ����ʱ���У�Ȼ��һ����뵽���Ͷ�����
		//��Ϊ���Ͷ������̰߳�ȫ�ģ����ϵػ�ȡ����������Ϣ����ռ�úܶ���Դ��ʱ��
		private LinkedList<ForwardMsg> tempQueue;
		
		public ForwardQueueExecutor(){
			super();
			tempQueue = new LinkedList<ForwardMsg>();			
		}
		
		public void run(){
			last_update = System.currentTimeMillis();
			while(true){
				try {
						Thread.sleep(MinaUtil.freeze_time);
						adjustRatio();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
				enQueueWithRatio();
			}						
		}
		
		/**
		 * ����ȫ�ֶ��������������ӱ���.
		 * ����Ҫ������state����״̬�ı�󣬼��һ��ʱ���ж�һ�Σ���Ҫһֱ������Դ.
		 * ��ȥ��̫�����ˣ��ɶ���Ҳ̫���ˡ�����
		 * */
		protected void adjustRatio(){
			
			now = System.currentTimeMillis();
			//���һ��ʱ����һ��

			if(now - last_update < MinaUtil.checkRatio)
				return;

			int tcp_blocked = MinaUtil.getTCPBlockCount();
			int tcp_total = (MinaUtil.getTCPTotalCount() != 0) ? MinaUtil.getTCPTotalCount() : 1;
			int udp_blocked = MinaUtil.getUDPBlockedCount();
			int udp_total = (MinaUtil.getUDPTotalCount() != 0) ? MinaUtil.getUDPTotalCount() : 1;
			double tcpRatio = (double)tcp_blocked/tcp_total;
			double udpRatio = (double)udp_blocked/udp_total;
			int state = MinaUtil.getState();

			//��ǰ����״̬
			if((tcpRatio <= MinaUtil.TcpRatio_unwell) && (udpRatio <= MinaUtil.UdpRatio_unwell)){
				//�����в�����Ϊ����״̬
				MinaUtil.setState(MinaUtil.HEALTHY);
				deliverWorker.setCorePoolSize(iniCoreSize);
				deliverWorker.setMaximumPoolSize(iniMaxSize);
				secPro = INISECPRO;
				thirdPro = INITHIRDPRO;
				fourPro = INIFOURPRO;	
				MinaUtil.deFreezeTime();
				
			}
			
			//��ǰill״̬����ʱtcp��udpͨ����Ϊsick,����
			else if((tcpRatio >= MinaUtil.TcpRatio_sick) && (udpRatio >= MinaUtil.UdpRatio_sick)){
				
				state = (MinaUtil.ILL_TCP | MinaUtil.ILL_UDP);
				MinaUtil.setState(state);
				deliverWorker.setCorePoolSize(iniCoreSize);
				deliverWorker.setMaximumPoolSize(iniCoreSize);
				//System.out.println("�̳߳�������С---0");
				
				MinaUtil.inFreezeTime();
			}else{
				//�Ƚ��֮ǰ��״̬���鿴�Ƿ��л��⣬ȷ��״̬state���ٲ�ȡ��ʩ
				if((tcpRatio >= MinaUtil.TcpRatio_sick)||(udpRatio >= MinaUtil.UdpRatio_sick)){
					//��ǰΪsick״̬
					if(tcpRatio >= MinaUtil.TcpRatio_sick){
						//��֮ǰҲ��sick״̬��������ڱ��㣬��Ϊill״̬
						if(isState(state, MinaUtil.SICK_TCP)){
							state &= (~MinaUtil.SICK_TCP);
							state |= MinaUtil.ILL_TCP;
							
							int curSize = deliverWorker.getMaximumPoolSize();
							curSize -= stepde;
							if(curSize < iniCoreSize){
								curSize = iniCoreSize;						
							}
							deliverWorker.setMaximumPoolSize(curSize);
							//System.out.println("�̳߳�������С---1");
							
							MinaUtil.inFreezeTime();
							
						}else{
							//��Ȼ��sick״̬
							state &= 0x11110000;
							state |= MinaUtil.SICK_TCP;
						}
					}
					//so as udp
					if(udpRatio >= MinaUtil.UdpRatio_sick){
						//��֮ǰҲ��sick״̬��������ڱ��㣬��Ϊill״̬
						if(isState(state, MinaUtil.SICK_UDP)){
							state &= (~MinaUtil.SICK_UDP);
							state |= MinaUtil.ILL_UDP;
							
							int curSize = deliverWorker.getMaximumPoolSize();
							curSize -= stepde;
							if(curSize < iniCoreSize){
								curSize = iniCoreSize;						
							}
							deliverWorker.setMaximumPoolSize(curSize);
							//System.out.println("�̳߳�������С----2");
						}else{
							//��Ȼ��sick״̬
							state &= 0x1111;
							state |= MinaUtil.SICK_UDP;
						}
					}										
				}else{
					//��ǰΪunwell�����ڱ���				
					if((tcpRatio > MinaUtil.TcpRatio_unwell) && (tcpRatio < MinaUtil.TcpRatio_sick)){
						//��֮ǰ����unwell״̬��˵������ڶ񻯣���Ϊsick״̬
						if(isState(state, MinaUtil.UNWELL_TCP)){
							state &= (~MinaUtil.UNWELL_TCP);
							state |= MinaUtil.SICK_TCP;
						}else{
							//�������������unwell״̬
							state &= 0x11110000;
							state |= MinaUtil.UNWELL_TCP;
						}
						
						MinaUtil.inFreezeTime();
						
					}
					
					if((udpRatio > MinaUtil.UdpRatio_unwell) && (udpRatio < MinaUtil.UdpRatio_sick)){
						//��֮ǰ����unwell״̬��˵������ڶ񻯣���Ϊsick״̬
						if(isState(state, MinaUtil.UNWELL_UDP)){
							state &= (~MinaUtil.UNWELL_UDP);
							state |= MinaUtil.SICK_UDP;
						}else{
							//�������������unwell״̬
							state &= 0x1111;
							state |= MinaUtil.UNWELL_UDP;
						}
						
						MinaUtil.inFreezeTime();
						
					}
					
				}
				
				//����״̬����ȡ��ʩ	
				//����sick״̬���򽵵ͷַ��ٶ�
				if(isState(state, MinaUtil.SICK_TCP) || isState(state, MinaUtil.SICK_UDP)){
					int curSize = deliverWorker.getMaximumPoolSize();
					curSize -= stepde;
					if(curSize < iniCoreSize){
						curSize = iniCoreSize;						
					}
					deliverWorker.setMaximumPoolSize(curSize); //???????????
					//System.out.println("�̳߳�������С----3");
				}else{
					//����unwell״̬�����ڱ���
					if(isState(state, MinaUtil.UNWELL_TCP)) {
						thirdPro -= prode;	
						if(thirdPro < MINRATIO)
							thirdPro = MINRATIO;
					} 
					if(isState(state, MinaUtil.UNWELL_UDP)){
						fourPro -= prode;
						if(fourPro < MINRATIO)
							fourPro = MINRATIO;
					}
					
					if(tcpRatio < MinaUtil.TcpRatio_unwell){
						if((state & 0x1111) > MinaUtil.HEALTHY_TCP){														
							//���ӱ���
							if(secPro < INISECPRO){
								secPro += proin;
								if(secPro > INISECPRO)
									secPro = INISECPRO;
							}else if(thirdPro < INITHIRDPRO){
								thirdPro += proin;
								if(thirdPro > INITHIRDPRO)
									thirdPro = INITHIRDPRO;
							}
						}
					}
					if(udpRatio < MinaUtil.UdpRatio_unwell){
						if((state & 0x11110000) > MinaUtil.HEALTHY_UDP){
							//���ӱ���
							if(fourPro < INIFOURPRO){
								fourPro += proin;
								if(fourPro > INIFOURPRO)
									fourPro = INIFOURPRO;
							}
						}
					}
					
					//���ӷַ��ٶ�
					int curSize = deliverWorker.getMaximumPoolSize();
					curSize += stepin;
					if(curSize > iniMaxSize)
						curSize = iniMaxSize;
					deliverWorker.setMaximumPoolSize(curSize);
					//System.out.println("�̳߳���������");
					
					MinaUtil.deFreezeTime();
					
				}								

				MinaUtil.setState(state);
				//over					
			}
			last_update = System.currentTimeMillis();
			
			ConcurrentHashMap destmap = MsgQueueMgr.getDest_session();
			
			
		}
		
		protected int updateState(double tcpRatio, double udpRatio){
			int state = 0;
			if(tcpRatio < MinaUtil.TcpRatio_unwell)
				state |= MinaUtil.HEALTHY_TCP;
			else if(tcpRatio > MinaUtil.TcpRatio_sick)
				state |= MinaUtil.SICK_TCP;
			else
				state |= MinaUtil.UNWELL_TCP;
			
			if(udpRatio < MinaUtil.UdpRatio_unwell)
				state |= MinaUtil.HEALTHY_UDP;
			else if(udpRatio > MinaUtil.UdpRatio_sick)
				state |= MinaUtil.SICK_TCP;
			else
				state |= MinaUtil.UNWELL_UDP;
			
			return state;
		}
		
		public boolean isState(int state, int targetState){
			return ((state & targetState) != 0);
		}
		
		//һ��ֻ��һ����־λ�б�ʶ�����������
		protected int worstTcpState(int state){			
			if(isState(state, MinaUtil.ILL_TCP))
				return MinaUtil.ILL_TCP;
			if(isState(state, MinaUtil.SICK_TCP))
				return MinaUtil.SICK_TCP;
			if(isState(state, MinaUtil.UNWELL_TCP))
				return MinaUtil.UNWELL_TCP;
			
			return MinaUtil.HEALTHY_TCP;			
		}
		
		protected int WorstUdpState(int state){			
			if(isState(state, MinaUtil.ILL_UDP))
				return MinaUtil.ILL_UDP;
			if(isState(state, MinaUtil.SICK_UDP))
				return MinaUtil.SICK_UDP;
			if(isState(state, MinaUtil.UNWELL_UDP))
				return MinaUtil.UNWELL_UDP;
			
			return MinaUtil.HEALTHY_UDP;			
		}
		
		/**
		 * ������Ϣ�����������
		 * */
		protected void enQueueWithRatio(){
			//��ӳ��ȷ�ֵ
			int minth = (int)(inQueueNum * inQueueRatio);
			int msgCount = 0;  //������ӵ���Ϣ��
		//	log.info("���Ͷ��г��ȣ�" + forwardQueue.size());
			//�����Կգ���Ϣ���
			if(getForwardQueueSize() < minth){
			//	if(forwardQueue.size() > 0){
			//		log.info("���Ͷ��м����������г��ȣ�" + forwardQueue.size());
			//	}

				msgCount = inQueueNum;
				//�����ֽ�����Ϣ���������
				if(!emergencyMsgQueue.isEmpty() && msgCount > 0){
					PRIMSG |= FIRSTPRIMSG;
					msgCount = calculateMsg(emergencyMsgQueue, tempQueue, msgCount);
//					log.info("####��һ���ȼ��������" + (inQueueNum - msgCount) + "  ʣ�ࣺ" + msgCount);
				}else{
					//�޳���Ϣ��־λ
					PRIMSG &= (~FIRSTPRIMSG);
				}
				
				//������δ������ʣ�µ���Ϣ�������������
				if(!TCPNoticeMsgQueue.isEmpty() && msgCount > 0 && secPro != 0){
					PRIMSG |= SECONDPRIMSG;
					int num = calculateMsg(TCPNoticeMsgQueue, tempQueue, 
							(int)(msgCount*secPro));
					msgCount = msgCount - (int)(msgCount*secPro - num);
					//log.info("####�ڶ����ȼ��������" + test1 + "  ʣ�ࣺ" + msgCount);
					
				}else{
					//�޳���Ϣ��־λ
					PRIMSG &= (~SECONDPRIMSG);
				}
				
				//���ϼ�����û����������ʣ�µİ��������롣��������0.0001����Ϊ�˷�ֹ����Ϊ0.
				if(!otherMsgQueue.isEmpty() && msgCount > 0 && thirdPro != 0){
					PRIMSG |= THIRDPRIMSG;
					int temp = Math.round((float)(msgCount*thirdPro/(fourPro + thirdPro + 0.0001)));
					int num = calculateMsg(otherMsgQueue, tempQueue, 
							temp);
					msgCount = msgCount - (temp - num);
					//log.info("####�������ȼ��������" + (temp - num) + "  ʣ�ࣺ" + msgCount);
				}else{
					//�޳���Ϣ��־λ
					PRIMSG &= (~THIRDPRIMSG);
				}
				
				//ʣ�µ�ȫ����ͼ���Ϣ
				if(!UDPMsgQueue.isEmpty() && msgCount > 0 && fourPro != 0){
					PRIMSG |= FOURTHPRIMSG;
					calculateMsg(UDPMsgQueue, tempQueue, msgCount);
				}else{
					//�޳���Ϣ��־λ
					PRIMSG &= (~FOURTHPRIMSG);
				}
				
				//����ʱ�����е���Ϣȫ���뷢�Ͷ���
				synchronized(forwardQueue){
					while(true){
						ForwardMsg msg = tempQueue.poll();   //��ȡ���Ƴ����б��ͷ����һ��Ԫ��)
						if(msg == null)
							break;					
						forwardQueue.offer(msg);
						
					}
				}
			}				
			
		}

		
		//��count����Ϣ��һ�����и��Ƶ���һ��������,����δ������Ϣ�ĸ���
		protected int calculateMsg(LinkedBlockingQueue<ForwardMsg> source, 
				LinkedList<ForwardMsg> dest , int count){
		synchronized(source){
			while(count > 0){
				ForwardMsg msg = source.poll();
				if(msg == null)
					break;
				
				dest.offer(msg);  //add to tail
				count--;				
			}
			}
			return count;
		}
	}
	
	
	public class listen extends Thread{
		ArrayList<String> aimAddr = null;
		public listen(){
			ArrayList<String> aimAddr = MinaUtil.putIP();
		}
		public void run(){
			for(int i=0;i<aimAddr.size();i++){
				
			}
		}
	}

}
