package com.bupt.wangfu.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

public class LdapUtil {
	//用于存储连接数据库的信息 
	private Hashtable<String, String> env = null;
	//Ldap 上下文操作对象
	private LdapContext ctx = null;
	public LdapUtil(){
		env = new Hashtable<String, String>();
	}
	//连接一个OpenLdap数据库服务器
	public void connectLdap() throws NamingException{
		//set the initializing information of the context
		env.put(Context.INITIAL_CONTEXT_FACTORY,  "com.sun.jndi.ldap.LdapCtxFactory");
		//set the URL of ldap server
		env.put(Context.PROVIDER_URL, "ldap://localhost:389");
		//set the authentication mode
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		//set user of ldap server
		env.put(Context.SECURITY_PRINCIPAL, "cn=Manager,dc=maxcrc,dc=com");
		//set password of user
		env.put(Context.SECURITY_CREDENTIALS, "123456");
		
		//initialize the ldap context
		ctx = new InitialLdapContext(env, null);
	}
	//关闭一个OpenLdap连接
	public void closeContext() throws NamingException{
		ctx.close();
	}
	//获取一个Ldap上下文操作对象
	public LdapContext getContext(){
		return  this.ctx;
	}
	
	public void isExist(TopicEntry te) throws NamingException{
		ctx.lookup(te.getTopicPath());
	}
	/**
	 * 新建一个ou条目
	 * @param newOUName	新建条目的名字
	 * @param dest_path	新建条目的父节点
	 * @param ctx	LdapContext操作对象
	 */
	public void createOUEntry(TopicEntry new_topicEntry){
		Attributes attrs = new BasicAttributes();
		attrs.put("ou", new_topicEntry.getTopicName());
		BasicAttribute objectclassSet = new BasicAttribute("objectclass");
		objectclassSet.add("top");
		objectclassSet.add("organizationalUnit");
		attrs.put(objectclassSet);
		try {
			ctx.createSubcontext(new_topicEntry.getTopicPath(),attrs);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 新建一个带编码属性的ou条目
	 * @param newOUName	新建条目的名字
	 * @param dest_path	新建条目的父节点
	 * @param topicCode	该条目的编码
	 * @param ctx	LdapContext操作对象
	 */
	public void createOUEntryWithTopicCode(TopicEntry new_topicEntry){
		Attributes attrs = new BasicAttributes();
		attrs.put("ou", new_topicEntry.getTopicName());
		BasicAttribute objectclassSet = new BasicAttribute("objectclass");
		objectclassSet.add("top");
		objectclassSet.add("organizationalUnit");
		attrs.put(objectclassSet);
		attrs.put("description", new_topicEntry.getTopicCode());
		try {
			ctx.createSubcontext(new_topicEntry.getTopicPath(),attrs);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 删除一个指定条目（该条目必须是叶子节点）
	 * @param goal_path	被删除条目的位置
	 * @param ctx	Ldap 上下文操作对象
	 */
	public void delete(TopicEntry te){
		try {
			ctx.destroySubcontext(te.getTopicPath());
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 删除一个指定条目及其所有子条目（该条目不必是叶子节点）
	 * @param goal_path	被删除条目的位置
	 * @param lc	Ldap 上下文操作对象
	 * @throws NamingException
	 */
	public void deleteWithAllChildrens(TopicEntry te) throws NamingException{
		List<TopicEntry> delete = getWithAllChildrens(te);
		for(int i=delete.size()-1;i>=0;i--){
			ctx.destroySubcontext(delete.get(i).getTopicPath());
		}
	}
	/**
	 * 获取主题字符串
	 * @param nameWithOU	带ou的主题字符串，例如："ou=topic"
	 * @return	不带ou的主题字符串，例如："topic"
	 */
	public String getTopicString(String nameWithOU){
		return nameWithOU.substring(3, nameWithOU.length());
	}
	/**
	 * 包装方法
	 * @param path	OpenLDAP中条目的路径，如："ou=subTopic,ou=topic"
	 * @return	完整的OpenLDAP路径，如："ou=subTopic,ou=topic,dc=wsn,dc=com"
	 */
	public String wrapper(String path, String _wrapper){
		return path + "," + _wrapper;
	}
	/**
	 * 返回给定条目及其所有子条目
	 * @param topic_path	给定条目的路径
	 * @param lc	Ldap 上下文操作对象
	 * @return	将给定条目及其所有子条目存储在列表中返回
	 * @throws NamingException 
	 */
	public List<TopicEntry> getWithAllChildrens(TopicEntry te) throws NamingException{
		List<TopicEntry> childrens = new ArrayList<TopicEntry>();
		//将当前条目加入到列表中
		TopicEntry root = new TopicEntry();
		root.setTopicName(getTopicString(te.getTopicPath().split(",")[0]));
		Attributes attrs = ctx.getAttributes(te.getTopicPath());
		root.setTopicCode(attrs.get("description").toString().split(": ")[1]);
		root.setTopicPath(te.getTopicPath());
		childrens.add(root);
		//将当前条目的所有子条目按层添加到列表中
		Queue<TopicEntry> queue = new LinkedList<TopicEntry>();
		queue.offer(root);
		while(!queue.isEmpty()){
			TopicEntry temp = queue.poll();
			List<TopicEntry> list = getSubLevel(temp);
			if(!list.isEmpty()){
				for(int i=0;i<list.size();i++){
					childrens.add(list.get(i));
					queue.offer(list.get(i));
				}
			}
		}
		return childrens;
	}
	/**
	 * 返回给定条目下一级的所有子条目
	 * @param topic_path	给定条目的路径
	 * @param lc	Ldap 上下文操作对象
	 * @return	将给定条目下一级的所有子条目存储在列表中返回
	 */
	public List<TopicEntry> getSubLevel(TopicEntry te) throws NamingException{
		List<TopicEntry> sub = new ArrayList<TopicEntry>();
		String sub_path = null;
		
		NamingEnumeration<NameClassPair> x = ctx.list(te.getTopicPath());
		while(x.hasMore()){
			TopicEntry _te = new TopicEntry();
			_te.setTopicName(getTopicString(x.next().getName()));
			
			sub_path = "ou=" + _te.getTopicName() + "," + te.getTopicPath();
			Attributes attrs = ctx.getAttributes(sub_path);
			
			_te.setTopicCode(attrs.get("description").toString().split(": ")[1]);
			_te.setTopicPath(sub_path);
			
			sub.add(_te);
		}
		return sub;
	}

	/**
	 * 添加主题编码作为主题的属性
	 * @param goal_path	要添加编码属性的主题
	 * @param topicCode	编码
	 * @param lc	LdapContext操作对象
	 * @throws NamingException	
	 */
	public void addTopicCode(TopicEntry te, String topicCode) throws NamingException{
		 ModificationItem[] mods =new ModificationItem[1];
		 Attribute attr = new BasicAttribute("description");
		 attr.add(topicCode);
		 mods[0] = new ModificationItem(LdapContext.ADD_ATTRIBUTE, attr);
		 
		 ctx.modifyAttributes(te.getTopicPath(), mods);
	}
	/**
	 * 修改主题的编码属性
	 * @param goal_path	要修改的主题
	 * @param new_topicCode	新的编码
	 * @param lc	LdapContext操作对象
	 * @throws NamingException
	 */
	public void modifyTopicCode(TopicEntry te, String new_topicCode) throws NamingException{
		 ModificationItem[] mods =new ModificationItem[1];
		 Attribute attr = new BasicAttribute("description");
		 attr.add(new_topicCode);
		 mods[0] = new ModificationItem(LdapContext.REPLACE_ATTRIBUTE, attr);
		 
		 ctx.modifyAttributes(te.getTopicPath(), mods);
	}
	/**
	 * 删除主题的编码属性
	 * @param goal_path	被删除编码属性的主题
	 * @param lc	LdapContext操作对象
	 * @throws NamingException
	 */
	public void removeTopicCode(TopicEntry te) throws NamingException{
		 ModificationItem[] mods =new ModificationItem[1];
		 Attribute attr = new BasicAttribute("description");
		 mods[0] = new ModificationItem(LdapContext.REMOVE_ATTRIBUTE, attr); 
		 ctx.modifyAttributes(te.getTopicPath(), mods);
	}
	/**
	 * 重命名一个叶子节点
	 * @param te	重命名目标节点（必须是叶子节点，否则返回空指针）
	 * @param new_name	新名字
	 * @param lc	LdapContext 上下文操作对象
	 * @return	返回重命名之后的结点
	 * @throws NamingException
	 */
	public TopicEntry rename(TopicEntry te, String new_name) throws NamingException{
		List<TopicEntry> ls = getSubLevel(te);
		if(ls.isEmpty()){
			String old_name = te.getTopicName();
			String old_path = te.getTopicPath();
			String new_path = old_path.replaceFirst(old_name, new_name);
			ctx.rename(old_path, new_path);
			te.setTopicName(new_name);
			te.setTopicPath(new_path);
			return te;
		}else{
			return null;
		}
	}
}


















