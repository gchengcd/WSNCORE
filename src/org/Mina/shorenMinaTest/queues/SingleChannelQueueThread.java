/**
 * @author shoren
 * @date 2013-4-28
 */
package org.Mina.shorenMinaTest.queues;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import org.Mina.shorenMinaTest.MinaUtil;
import org.Mina.shorenMinaTest.handlers.Start;

/**
 *
 *������Ϣ�ַ�����������Ϣ�����ض���ͨ������û�����½�ͨ����
 *�����̳߳ص��߳���������forwardQueue���̳߳ص�������С�
 *��Ϣ�ַ����ֲ��������������Ϊ�߳�ֻ�ǽ���Ϣ����ͨ����ͨ���ڵĶ�����ʱ����ͨ�����Լ�����
 *�Ӷ���֤��Ϣ�ַ�����������
 *
 *ȫ������ʱ�����ƶ����������ڷַ�ǰ�����ж��Ƿ�����Ϣ��
 *ȫ���������жϣ����ڵ�ͨ���У����Լ�������������������������ͬʱ�鿴�Ƿ�ȫ������������Ӧ��
 *�޸�ȫ��������ʶ��
 *
 *�˴��̵߳Ĳ����Ƚϼ򵥣���Ϊ�˱���˴��ַ���Ϊ���Ͳ��ֵ�ƿ����
 *�����ֿ��ܳ�Ϊƿ����
 *   һ���߳������ޣ������ͨ�����϶࣬����Ӧ��
 *   ���Ƕ�ȡ���Ͷ��еĶ�ͷ��Ϣʱ��ֻ����һ�̶߳�ȡ������ͬ�����������ܻ�Ӱ��ַ��ٶȡ�
 */
public class SingleChannelQueueThread implements Runnable {
	
	private final Log log = LogFactory.getLog(SingleChannelQueueThread.class);
	private static Object synObject = new Object();

	int count = 0;
	@Override
	public void run() {	
		//��ȡ���Ƴ��������Ͳ��ɹ������ټ���
		LinkedList<ForwardMsg> temp = new LinkedList<ForwardMsg>();
		MsgQueueMgr.getForwardMsg(temp, 20);
		if(temp.size() < 1)
			return;
		
		for(ForwardMsg forwardMsg : temp){
			if(forwardMsg == null) return;
			
			//�ж��Ƿ�ȫ����������������ȥ�������ȼ�����Ϣ������������ֱ�ӷַ�
			int state = MinaUtil.getState();
			if(isState(state, MinaUtil.ILL_TCP)){
				//������ȥ�������ȼ�����Ϣ,Ȼ�󷵻ء�����Ϣ��Ӧ����������������
				MsgQueueMgr.fourPro = 0;
				if(forwardMsg instanceof UDPForwardMsg)
					return;
				
				if(forwardMsg instanceof TCPForwardMsg){
					//���������ȼ����зǿգ�����Ϣ�ǵ������ȼ��ģ��򶪣�ͬʱ��ֹ��Ϣ�����
					if((MsgQueueMgr.PRIMSG & MsgQueueMgr.THIRDPRIMSG) != 0){
						MsgQueueMgr.thirdPro = 0;
						if(forwardMsg.getPriority() == MsgQueueMgr.THIRDPRIMSG)
							return;
					}else if((MsgQueueMgr.PRIMSG & MsgQueueMgr.SECONDPRIMSG) != 0){
						MsgQueueMgr.secPro = 0;
						if(forwardMsg.getPriority() == MsgQueueMgr.SECONDPRIMSG)
							return;
					}
				}
				
			}
			if(isState(state, MinaUtil.ILL_UDP)){
				//��udpͨ���������أ��򶪰�
				MsgQueueMgr.fourPro = 0;
				if(forwardMsg instanceof UDPForwardMsg)
					return;
			}
			//��������ֱ�ӷַ�
			forwardQueueMsg(forwardMsg);
		}
		
	}	//finally, it is over~~~	
		
		
	@SuppressWarnings("rawtypes")
	protected void forwardQueueMsg(ForwardMsg forwardMsg){
		if(forwardMsg == null) return;		
		IoSession session = null;
		
		
		//�ȿ�dest_session���Ƿ���session��������ʹ��session�������½�connector
		ConcurrentHashMap destmap = MsgQueueMgr.getDest_session();

		for(int i=0;i<forwardMsg.getDestination().size();i++){

			
		if(destmap.containsKey(forwardMsg.getDestination().get(i).toString())){
			session = (IoSession) destmap.get(forwardMsg.getDestination().get(i).toString());
			
		}else{
			synchronized(synObject){//�߳�ͬ����ͬһʱ��ֻ����һ���߳������²�����DoubleCheck ��ֹ��ͬһ��ip���ɶ��session

				if(destmap.containsKey(forwardMsg.getDestination().get(i).toString())){
					session = (IoSession) destmap.get(forwardMsg.getDestination().get(i).toString());
				}else{
					
					String ip = forwardMsg.getDestination().get(i).getAddr();
					int port = forwardMsg.getDestination().get(i).getPort();
					
					System.out.println("�½���ip�ǣ�\n"+ip+"�½���port�ǣ�"+port);
					
			//		if(isIn(ip)){
					if(forwardMsg instanceof TCPForwardMsg){
							NioSocketConnector connector = MinaUtil.createSocketConnector();
							ConnectFuture cf = connector.connect(new InetSocketAddress(ip, port));//��������   
							cf.awaitUninterruptibly();//�ȴ����Ӵ������   

							try {
								session = cf.getSession();
							} catch (Exception e) {
								//System.out.println("��������ʧ�ܣ�����"+ip+"�ڵ㣡");
								Start.removeAimAddr(ip);
								return;
								// TODO: handle exception
							}
							    session.setAttribute("addr",forwardMsg.getDestination().get(i).toString());
							    MsgQueueMgr.addDestMsg(forwardMsg.getDestination().get(i).toString(),session);
					}else if(forwardMsg instanceof UDPForwardMsg){
						NioDatagramConnector connector = MinaUtil.createDatagramConnector();
						ConnectFuture cf = connector.connect(new InetSocketAddress(ip, port));//��������   
						cf.awaitUninterruptibly();								
						session = cf.getSession();	
						session.setAttribute("addr", forwardMsg.getDestination().get(i).toString());
						MsgQueueMgr.addDestMsg(forwardMsg.getDestination().get(i).toString(), session);
					}
					//����session��destination��	
					/*}else{
						return;
					}*/
				}				
			
		}
		}
		
		

		//���ǵ�һ���ȼ���Ϣ��ֱ�ӷ���
		if(forwardMsg.getPriority() != MsgQueueMgr.FIRSTPRIMSG){
			//���������أ���sessionΪdead�����÷ַ�
			int state = MinaUtil.getState();
	    	 //136 = 8 + 128
	    	 if(((state & 136) != 0) && 
	    			 ((String)session.getAttribute("state")).equals(MinaUtil.SDead)){
	    		 return;
	    	 }
	    	 
	    	    int lowestPri = 2;
	 		    int curPri = forwardMsg.getPriority();
	 		
			//�����ľ������
	    	 int lossCount = (Integer) session.getAttribute("lossCount");
	    	 if(lossCount != 0){   		
	    		if(curPri >= lowestPri)  //������Ϣ�����ȼ��ͣ�������������
	    		{
	    			//log.info("drop the data");
	    			return;
	    		}	   		
	    	 }
			
			if(curPri > lowestPri){
				lowestPri = curPri;
			}
			//�����ȼ����ж�ʱ����Ҫ�ٿ��ǡ�
			if((MsgQueueMgr.PRIMSG & lowestPri) == 0){
				lowestPri = MsgQueueMgr.LOWESTPRI;
			}
			session.setAttribute("lowestPriority", lowestPri);
		}
		
		try {
			//�˴�Ӧ���ǽ���Ϣ����������,send message
			if(session != null)
			    session.write(forwardMsg.getMsg());
		} catch (Exception e) {
			System.out.println("��Ϣ�ַ���������");
			// TODO: handle exception
		}
		//�������ݼ�1
		    int count;
		    if(session != null){
			    count = (Integer) session.getAttribute("count");
			    ++count;
			    session.setAttribute("count", count);
		    }
		    
		}

	}
	
	protected boolean isState(int state, int targetState){
		return ((state & targetState) != 0);
	}

	public boolean isIn(String ip){
			if(Start.forwardIP.contains(ip)){
				return true;
			}
		return false;
	}
}
