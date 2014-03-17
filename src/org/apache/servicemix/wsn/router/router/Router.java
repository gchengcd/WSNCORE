package org.apache.servicemix.wsn.router.router;

import java.util.ArrayList;

import org.apache.servicemix.wsn.router.mgr.base.MsgSubsForm;
import org.apache.servicemix.wsn.router.mgr.base.SysInfo;
import org.apache.servicemix.wsn.router.wsnPolicy.ShorenUtils;
import org.apache.servicemix.wsn.router.wsnPolicy.msgs.WsnPolicyMsg;
import org.apache.servicemix.wsn.router.wsnPolicy.msgs.TargetGroup;

/**
 * ����ָ�����Ƶ�ת��·��
 * ��Ը����Ƶ����ж��Ľڵ㣬�ɼ�Ⱥ���Ƚ�ѡȡ���ڵ�
 * ���ݵϽ�˹�����㷨�ɸ��ڵ㿪ʼ����ת����
 * ��ת�����ĸ��ڵ㼰���ڵ����һ��ת����¼������·�ɽṹ��
 * @author Sylvia
 *
 */

public class Router extends SysInfo implements IRouter {

	public void route(String topic) {
		if(topic == null || topic.length() == 0)
			return;
		ArrayList<String> goal = new ArrayList<String>();

		// �����ж��Ĵ����Ƶļ�Ⱥ����goal��
		String[] splited = topic.split(":");
		MsgSubsForm msf = groupTableRoot;

		// ����goal�еĶ��ļ�Ⱥ������ָ��ָ����˱������Ӧ��·����ģ��
		for (int i = 0; i < splited.length; i++) {
			if (msf.topicChildList.containsKey(splited[i])) {
				msf = msf.topicChildList.get(splited[i]);
			}  else
				return;
		} 
		//������������Ⱥ�Ĵ�����ַ���뵽goal��
		for(String name : msf.subs)
			goal.add(name);
		String ex = "";
		// ɾ��goal�������Ƶļ�Ⱥ
		if (!goal.isEmpty()) {
			for (int i = 0; i < splited.length; i++) {
				if (i > 0)
					ex += ":";
				ex += splited[i];
				WsnPolicyMsg wpm = ShorenUtils.decodePolicyMsg(ex);
				if (wpm != null) {
					for (TargetGroup tg : wpm.getAllGroups()) {
						if (goal.contains(tg.getName()) && tg.isAllMsg()) {
								goal.remove(tg.getName());
								System.out.println("group "+tg.getName()+" is rejected by "+ex);
								if(goal.isEmpty()){
									break;
								}
						}
					}
				}
			}
		}

		// ���ԭ·��·��'
		msf.routeNext.clear();
		if (!goal.isEmpty()) {

			// ����Ͻ�˹����·��
			Dijkstra dij = new Dijkstra();
			Node start = dij.init(lsdb, goal, groupName);
			if(!start.getName().equals(groupName)) {
				if(groupMap.containsKey(start.getName())) {
				msf.routeRoot = start.getName();
				} else {
					
				}
			}
			else
				msf.routeRoot = "";
			
			System.out.println("calculate "+topic + " root: " +start.getName());
			// ���˼�ȺҲ�����˴���Ϣ��������亢��
			if (goal.contains(groupName) && goal.size() > 1) {
				dij.computePath(start);
				System.out.print("next stop: ");
				for(String next : dij.savePath()) {
					if(next != null) {
						if(groupMap.containsKey(next)) {
							msf.routeNext.add(next);
							System.out.print(next + "  " + groupMap.get(next).addr+"  ");
						} else {
							try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
							route(topic);
						}
					}
				}
				System.out.println("  ");
			}
			else {
				msf.routeNext.clear();
			}
		}
	}
}