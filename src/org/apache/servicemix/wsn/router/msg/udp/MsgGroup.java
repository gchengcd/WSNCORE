package org.apache.servicemix.wsn.router.msg.udp;

import java.io.Serializable;

import org.apache.servicemix.wsn.router.msg.tcp.GroupUnit;

public class MsgGroup implements Serializable {

	//���м�Ⱥ���뵽����Ⱥʱ������ת������Ϣ
	
	public String sender;//sender's group name
	
	public GroupUnit g = new GroupUnit();
	
}