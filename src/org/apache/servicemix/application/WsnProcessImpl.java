package org.apache.servicemix.application;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.Endpoint;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.servicemix.jmsImpl.JmsNotificationBrokerImpl;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.application.IWsnProcess;
import org.apache.servicemix.wsn.EndpointRegistrationException;
import org.apache.servicemix.wsn.push.ListItem;
import org.apache.servicemix.wsn.router.mgr.RtMgr;
import org.apache.servicemix.wsn.AbstractCreatePullPoint;
import org.apache.servicemix.wsn.AbstractNotificationBroker;
import org.apache.servicemix.wsn.AbstractSubscription;
import org.apache.servicemix.wsn.jms.JmsCreatePullPoint;
import org.apache.servicemix.wsn.jms.JmsSubscription;
import org.apache.servicemix.wsn.jms.JmsNotificationBroker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import javax.jms.Message;
import org.oasis_open.docs.wsn.b_2.CreatePullPointResponse;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;

import com.bupt.wangfu.ldap.Ldap;
import com.bupt.wangfu.ldap.TopicEntry;

@WebService(endpointInterface="org.apache.servicemix.application.IWsnProcess",
serviceName="IWsnProcess")
public class WsnProcessImpl implements IWsnProcess{
	private static int counter = 0;
	private JmsCreatePullPoint createpullpoint = null;
	private JmsNotificationBroker notificationbroker = null;
	private ConnectionFactory connectionFactory = null;
	private Connection connection = null;
	private Session session = null;
	private Topic topic = null;
	public static List<ListItem> localtable;
	public static Queue<String> mes = new LinkedList<String>();
	private MessageProducer producer = null;
	private static Map<String, MessageProducer> topicMap = new LinkedHashMap<String, MessageProducer>();
	private static Log log = LogFactory.getLog(WsnProcessImpl.class);
    protected JAXBContext jaxbContext;
    protected Set<Class> endpointInterfaces ;
    
    public static WSNTopicObject topicTree;
    
    public static String newsubscribeaddree;
    
    public static String newtopic;
    
	
	public void init() throws JAXBException{
		String url = "tcp://localhost:61616";
		if(connectionFactory == null){
			// ConnectionFactory �����ӹ�����JMS ������������ 
			connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, url);
		}
        createpullpoint = new JmsCreatePullPoint("Brokername");
        createpullpoint.setConnectionFactory(connectionFactory);
        notificationbroker = new JmsNotificationBrokerImpl("Brokername");
        notificationbroker.setConnectionFactory(connectionFactory);
        try {
            createpullpoint.init();
			notificationbroker.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        endpointInterfaces = createEndpointInterfaces();
        jaxbContext = createJAXBContext(endpointInterfaces);
        
      //��openldap���ݿ����������
        try {
			WsnProcessImpl.readTopicTree("ou=all_test,dc=wsn,dc=com");
			WsnProcessImpl.printTopicTree();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //�����Աע��
        RtMgr.getInstance();
        System.out.println("init finished!");
	}
	
	public Set<Class> createEndpointInterfaces()
	{
		Set<Class> Interfaces = new HashSet<Class>();
        // Check additional interfaces
        for (Class pojoClass = createpullpoint.getClass(); pojoClass != Object.class; pojoClass = pojoClass.getSuperclass()) {
            for (Class cl : pojoClass.getInterfaces()) {
                if (getWebServiceAnnotation(cl) != null) {
                    Interfaces.add(cl);
                }
            }
        }
        for (Class pojoClass = notificationbroker.getClass(); pojoClass != Object.class; pojoClass = pojoClass.getSuperclass()) {
            for (Class cl : pojoClass.getInterfaces()) {
                if (getWebServiceAnnotation(cl) != null) {
                    Interfaces.add(cl);
                }
            }
        }
        return Interfaces;
	}
	
	@SuppressWarnings("unchecked")
    protected WebService getWebServiceAnnotation(Class clazz) {
        for (Class cl = clazz; cl != null; cl = cl.getSuperclass()) {
            WebService ws = (WebService) cl.getAnnotation(WebService.class);
            if (ws != null) {
                return ws;
            }
        }
        return null;
    }
	
	public String WsnProcess(String message){	
		
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		
		
		
		Method webMethod = null;
		Object input;
        Object output = "null";
        /*
         * ȡ��������Ϣ�ж�
         * ȡ�����Ĵ���
         */
        if(message.indexOf("wsnt:Unsubscribe")>0){
        	String topicname = splitstring("<wsnt:TopicExpression Dialect=\"http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple\">","</wsnt:TopicExpression>",message).trim();
        	String subscriberAddress = splitstring("<wsnt:SubscriberAddress>","</wsnt:SubscriberAddress>",message).trim();
        	System.out.println("======================== topicName:" + topicname);
        	System.out.println("======================== subscriberAddress:" + subscriberAddress);

        	/**========================================================================================
        	 * topic tree subscribe
        	 * new added at 2013/12/1
        	 */
        	String[] topicPath = topicname.split(":");
        	WSNTopicObject current = WsnProcessImpl.topicTree;
        	int flag = 0;
        	for(int i=0;i<topicPath.length-1;i++){
        		if(current.getTopicentry().getTopicName().equals(topicPath[i])){
        			for( int counter=0;counter<current.getChildrens().size();counter++ ){
        				if(current.getChildrens().get(counter).getTopicentry().getTopicName().equals(topicPath[i+1])){
        					current = current.getChildrens().get(counter);
        					flag++;
        					break;
        				}
        			} 
        		}
        		else{
        			
        			log.error("subscribe faild! there is not this topic in the topic tree!");
        			return "faild";
        		}
        	}
        	System.out.println("match time is: " + flag + "path.length is: " + topicPath.length);
        	if(flag == topicPath.length-1){
        		//�ж϶����Ƿ��Ѿ�����
        		int i;
        		boolean isdele = false;
        		for(i=0;i<current.getSubscribeAddress().size();i++){
        			if(current.getSubscribeAddress().get(i).equals(subscriberAddress)){
        				current.getSubscribeAddress().remove(i);
        				System.out.println("unsubscribe success");
        				isdele = true;
        				break;
        			}
        		}
        		//�����Ĳ�����
        		if(isdele==false){
//        			current.getSubscribeAddress().add(subscriberAddress);
	        		System.out.println("subscribe is not exists in the system,there is no need to do it!");
					log.error("subscribe is not exists in the system,there is no need to do it!");
					return "faild";
        		}
        	}
        	else
        		System.out.println("unsubscribe faild! there is not this topic in the topic tree!");
        	/*
        	 * �ͷſռ�
        	 */
			System.out.println("unsub befor----------------------"+AbstractNotificationBroker.subscriptions);
        	JmsSubscription subscription = AbstractNotificationBroker.subscriptions.get(subscriberAddress+"/"+topicname);
        	try {
				subscription.unsubscribe();
			} catch (UnableToDestroySubscriptionFault e) {
				e.printStackTrace();
			}
        	AbstractNotificationBroker.subscriptions.remove(subscriberAddress+"/"+topicname);
			System.out.println("unsub after----------------------"+AbstractNotificationBroker.subscriptions);

        	return "success";
        }
        
		if(message.indexOf("wsnt:Notify")>0||message.indexOf("wsnt:NotificationMessage")>0){
			try {
System.out.println("notify----------------------"+message);
				fast_Notify(message);
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return "success!";
		}
		else{
	        //���涩�ĵ�ַ
	        newsubscribeaddree = splitstring("<wsnt:SubscriberAddress>","</wsnt:SubscriberAddress>",message).trim();
	        if(message.indexOf("wsnt:TopicExpression")>0)
	        	newtopic = splitstring("<wsnt:TopicExpression Dialect=\"http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple\">","</wsnt:TopicExpression>",message).trim();
	        
			Source source = new StreamSource(new java.io.StringReader(message));
			try {
				input = jaxbContext.createUnmarshaller().unmarshal(source);
				Class inputClass = input.getClass();
		        if (input instanceof JAXBElement) {
		            inputClass = ((JAXBElement) input).getDeclaredType();
		            input = ((JAXBElement) input).getValue(); 
		        }
		        for (Class clazz : endpointInterfaces) {
		            for (Method mth : clazz.getMethods()) {
		                Class[] params = mth.getParameterTypes();
		                if (params.length == 1 && params[0].isAssignableFrom(inputClass)) {
		                    if (webMethod == null) {
		                        webMethod = mth;
		                    } else if (!mth.getName().equals(webMethod.getName())) {
		                        throw new IllegalStateException("Multiple methods matching parameters");
		                    }
		                }
		            }
		        }
		        if (webMethod == null) {
		            throw new IllegalStateException("Could not determine invoked web method");
		        }
//		        System.out.println("************************invoke BEFORE" + new Date().getTime());
		        
		        boolean oneWay = webMethod.getAnnotation(Oneway.class) != null;
		        
		        /**
		         * 
		         * ����createPullPoint��Ϣ��subscribe��Ϣ�����в�ͬ�ĵ��ã�
		         * ���ȡ�����Ĺ���ʱҪ�ڴ˴�����unsubscribe��Ϣ��������Ӧ�Ĵ����߼���
		         */
		        if(webMethod.getName().equals("createPullPoint"))
		        	output = webMethod.invoke(createpullpoint, new Object[] {input });
		        else
		        {	
		        	output = webMethod.invoke(notificationbroker, new Object[] {input });
//		        	for(int i=0;i<WsnProcessImpl.localtable.size();i++){
//		        		System.out.println(i+" "+WsnProcessImpl.localtable.get(i).getTopicName()+" "+WsnProcessImpl.localtable.get(i).getSubscriberAddress());
//		        	}
		        }
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("sub1----------------------"+AbstractNotificationBroker.subscriptions);
			return convertResponse(output,webMethod);
		}
	}
	
	public String convertResponse(Object message,Method webmethod)
	{
		String ans = "";
		if(webmethod.getName().equals("createPullPoint"))
		{
			CreatePullPointResponse temp = (CreatePullPointResponse)message;
			ans = temp.getPullPoint().toString();
		}
		if(webmethod.getName().equals("subscribe"))
		{
			SubscribeResponse temp = (SubscribeResponse)message;
			ans = temp.getSubscriptionReference().toString();
		}
		return ans;
	}
	
	public void fast_Notify(String message) throws JMSException
	{
		//System.out.println("Notificationmessage:  "+message);
		String topicname = splitstring("<wsnt:Topic Dialect=\"http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple\">","</wsnt:Topic>",message).trim();
		String address = "";
		
		Iterator iter = notificationbroker.subscriptions.entrySet().iterator();
		
		System.out.println("1-------------------------");
		
		int fast_counter = 0;
		while (iter.hasNext() && fast_counter <1) {
			fast_counter ++;
			System.out.println("2-------------------------");
			Map.Entry entry = (Map.Entry) iter.next();
			JmsSubscription val = (JmsSubscription)entry.getValue();
			
			TextMessage jmsMessage = val.session.createTextMessage();
			jmsMessage.setText(message);
			System.out.println("3-------------------------");
			val.onMessage(jmsMessage);
			System.out.println("4-------------------------");
		}			
		//System.out.println("topicname:  "+topicname);
//		for(int i=0;i<WsnProcessImpl.localtable.size();i++){
////			System.out.println("WsnProcessImpl.localtable.get(i).getTopicName():" + WsnProcessImpl.localtable.get(i).getTopicName());
//				if(WsnProcessImpl.localtable.get(i).getTopicName().equals(topicname))
//				{	
//					address = WsnProcessImpl.localtable.get(i).getSubscriberAddress();
//					Iterator iter = notificationbroker.subscriptions.entrySet().iterator();
//					while (iter.hasNext()) {
//						Map.Entry entry = (Map.Entry) iter.next();
//						JmsSubscription val = (JmsSubscription)entry.getValue();
//						//System.out.println("subscribeaddress: "+val.subscriberAddress);
//						//System.out.println("address: "+address);
//						if(val.subscriberAddress.equals(address)&&topicname.equals(val.jmsTopic.getTopicName()))
//						{
//							TextMessage jmsMessage = val.session.createTextMessage();
//							jmsMessage.setText(message);
//							val.onMessage(jmsMessage);
//						}
//					}
//				}
//		}
	}
	
	public static JAXBContext createJAXBContext(Iterable<Class> interfaceClasses) throws JAXBException {
        List<Class> classes = new ArrayList<Class>();
        classes.add(XmlException.class);
        for (Class interfaceClass : interfaceClasses) {
            for (Method mth : interfaceClass.getMethods()) {
                WebMethod wm = (WebMethod) mth.getAnnotation(WebMethod.class);
                if (wm != null) {
                    classes.add(mth.getReturnType());
                    classes.addAll(Arrays.asList(mth.getParameterTypes()));
                }
            }
        }
        return JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
    }
	@XmlRootElement(name = "Exception")
    public static class XmlException {
        private String stackTrace;
        public XmlException() {
        }
        public XmlException(Throwable e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }
        public String getStackTrace() {
            return stackTrace;
        }
        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }
        @XmlMixed
        public List getContent() {
            return Collections.singletonList(stackTrace);
        }
    }
	public String splitstring(String s,String e,String string)
    {
    	int start = string.indexOf(s) + s.length();
     	int end = string.indexOf(e);
     	return string.substring(start, end); 
    }
	
	//��OpenLdap���ݿ��ж�ȡһ���������ڵ��������
    public static void readTopicTree(String root_path)throws Exception{
    	Ldap ldap = new Ldap();
	    ldap.connectLdap("10.108.167.193", "cn=Manager,dc=wsn,dc=com", "123456");
	    //����һ�����У����ڴ����ݿ��ж�ȡһ������
	    Queue<WSNTopicObject> queue = new LinkedList<WSNTopicObject>();
	    //�Ը��ڵ�Ϊ��ڶ�ȡһ����
	    TopicEntry rootNode = ldap.getByDN(root_path);
	    topicTree = new WSNTopicObject(rootNode, null); 
		queue.offer(topicTree);
		while(!queue.isEmpty()){
			WSNTopicObject to = queue.poll();
			List<TopicEntry> ls = ldap.getSubLevel(to.getTopicentry());
			if(!ls.isEmpty()){
				List<WSNTopicObject> temp = new ArrayList<WSNTopicObject>();
				for(TopicEntry t : ls){
					WSNTopicObject wto = new WSNTopicObject(t, to);
					temp.add(wto);
					queue.offer(wto);
				}
				to.setChildrens(temp);
			}
		}
    }
    //��ӡ��ǰ�ڴ��д洢��topic tree
    public static void printTopicTree(){
		Queue<WSNTopicObject> printQueue = new LinkedList<WSNTopicObject>();
		printQueue.offer(topicTree);
		while(!printQueue.isEmpty()){
			WSNTopicObject x = printQueue.poll();
			if(x == topicTree)
				System.out.println("root: " + x);
			else
				System.out.println(x + " " + x.getParent());
			List<WSNTopicObject> y = x.getChildrens();
			if(!y.isEmpty()){
				for(WSNTopicObject w : y){
					printQueue.offer(w);
					System.out.print(w + " " + w.getParent() + "   ");
				}
				System.out.println();
			}
		}
    }
    
}

