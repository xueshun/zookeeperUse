package xue.zookeeper.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

/**
 * Zookeeper 节点授权
 * @author Administrator
 *	ACL(Access ControlList) Zookeeper作为一个分布式协调框架，其内部存储的都是一些关于分布式系统
 *	运行时状态的元数据，尤其是设计到一下分布式锁、Master选举和协调等应用场景。我们需要有效的保证Zookeeper中的数据安全
 * 
 * ZK提供了三种模式。权限模式、授权对象、权限。
 * 
 *  权限模式：scheme，开发最多使用的如下四中权限模式
 *  
 *  IP：ip模式通过ip地址力度来进行控制权限，例如配置了：IP:192.168.1.191 即表示权限控制都是针对这个ip地址。
 *  	同时也支持按网段分配：比如：192.168.1.*
 *  Digest: digest是最常用的权限控制模式，也更符合我们对权限的认识，器类似于"username:password"形式
 *  	的权限标识进行全新配置。ZK会对形成的权限标识先后进行两次编码吃力，分别是SHA-1加密算法、Base64编码
 *  World:world是一直最开发的权限控制模式，这种模式可以看做为特殊的Digest,他仅仅是一个标识而已。
 *  Super ： 超级用户模式，在超级用户模式下可以对ZK任意进行操作。
 *  
 *  权限对象：指的是权限赋予的用户或者一个指定的实体，例如ip地址或机器等。在不同模式下，授权对象是不同的。这种模式和权限对象一一对应
 *  
 *  权限 : 权限就是指那些通过权限检测后课被允许执行的操作，在zk中，对数据的操作权限分为以下五大类：
 *  	Create delete read write admin
 *
 */
public class ZookeeperAuth implements Watcher{
	/** 连接地址 */
	final static String CONNECT_ADDR = "192.168.1.191:2181";
	/** 测试路径 */
	final static String PATH = "/testAuth";
	final static String PATH_DEL = "/testAuth/delNode";
	/** 认证类型 */
	final static String authentication_type = "digest";
	/** 认证正确方法 */
	final static String correctAuthentication = "123456";
	/** 认证错误方法 */
	final static String badAuthentication = "654321";

	static ZooKeeper zk = null;
	/** 计时器 */
	AtomicInteger seq = new AtomicInteger();
	/** 标识 */
	private static final String LOG_PREFIX_OF_MAIN = "【Main】";

	private CountDownLatch connectedSemaphore = new CountDownLatch(1);

	/**
	 * 创建ZK连接
	 * @param connectionString ZK服务器连接地址列表
	 * @param sessionTimeout Session超时时间
	 */
	public void createConnection(String connectionString,int sessionTimeout){
		this.releaseConnection();
		try {
			zk = new ZooKeeper(connectionString,sessionTimeout,this);
			//添加节点授权
			zk.addAuthInfo(authentication_type, correctAuthentication.getBytes());
			System.out.println(LOG_PREFIX_OF_MAIN+"开始连接ZK服务器");
			//倒数等待
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭连接
	 */
	public void releaseConnection(){
		if(this.zk!=null){
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void process(WatchedEvent event) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if(event==null){
			return ;
		}
		// 连接状态
		KeeperState keeperState = event.getState();
		// 事件类型
		EventType eventType = event.getType();
		// 受影响的path
		String path = event.getPath();

		String logPrefix = "【Watcher-" + this.seq.incrementAndGet() + "】";

		System.out.println(logPrefix + "收到Watcher通知");
		System.out.println(logPrefix + "连接状态:\t" + keeperState.toString());
		System.out.println(logPrefix + "事件类型:\t" + eventType.toString());
		if (KeeperState.SyncConnected == keeperState) {
			// 成功连接上ZK服务器
			if (EventType.None == eventType) {
				System.out.println(logPrefix + "成功连接上ZK服务器");
				connectedSemaphore.countDown();
			} 
		} else if (KeeperState.Disconnected == keeperState) {
			System.out.println(logPrefix + "与ZK服务器断开连接");
		} else if (KeeperState.AuthFailed == keeperState) {
			System.out.println(logPrefix + "权限检查失败");
		} else if (KeeperState.Expired == keeperState) {
			System.out.println(logPrefix + "会话失效");
		}
		System.out.println("--------------------------------------------");
	}
	/** 获取数据：采用错误的密码**/
	static void getDataByBadAuthentication(){
		String prefix = "[使用错误的授权消息]"; 
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR,2000,null);
			//授权
			badzk.addAuthInfo(authentication_type, badAuthentication.getBytes());
			Thread.sleep(2000);
			System.out.println(prefix + "获取数据：" + PATH);
			System.out.println(prefix + "成功获取数据：" + badzk.getData(PATH, false, null));
		} catch (Exception e) {
			System.err.println(prefix + "获取数据失败，原因：" + e.getMessage());
		}
	}
	
	/** 获取数据：不采用密码**/
	static void getDataByNoAuthentication(){
		String prefix = "[使用错误的授权消息]"; 
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR,2000,null);
			Thread.sleep(2000);
			System.out.println(prefix + "获取数据：" + PATH);
			System.out.println(prefix + "成功获取数据：" + badzk.getData(PATH, false, null));
		} catch (Exception e) {
			System.err.println(prefix + "获取数据失败，原因：" + e.getMessage());
		}
	}
	
	/** 获取数据：使用正确密码**/
	static void getDataByCorrectAuthentication() {
		String prefix = "[使用正确的授权信息]";
		try {
			System.out.println(prefix + "获取数据：" + PATH);
			System.out.println(prefix + "成功获取数据：" + zk.getData(PATH, false, null));
		} catch (Exception e) {
			System.out.println(prefix + "获取数据失败，原因：" + e.getMessage());
		}
	}
	/**
	 * 更新数据：不采用密码
	 */
	static void updateDataByNoAuthentication() {

		String prefix = "[不使用任何授权信息]";

		System.out.println(prefix + "更新数据： " + PATH);
		try {
			ZooKeeper nozk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			Thread.sleep(2000);
			Stat stat = nozk.exists(PATH, false);
			if (stat!=null) {
				nozk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "更新成功");
			}
		} catch (Exception e) {
			System.err.println(prefix + "更新失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 更新数据：采用错误的密码
	 */
	static void updateDataByBadAuthentication() {

		String prefix = "[使用错误的授权信息]";

		System.out.println(prefix + "更新数据：" + PATH);
		try {
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			//授权
			badzk.addAuthInfo(authentication_type,badAuthentication.getBytes());
			Thread.sleep(2000);
			Stat stat = badzk.exists(PATH, false);
			if (stat!=null) {
				badzk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "更新成功");
			}
		} catch (Exception e) {
			System.err.println(prefix + "更新失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 更新数据：采用正确的密码
	 */
	static void updateDataByCorrectAuthentication() {

		String prefix = "[使用正确的授权信息]";

		System.out.println(prefix + "更新数据：" + PATH);
		try {
			Stat stat = zk.exists(PATH, false);
			if (stat!=null) {
				zk.setData(PATH, prefix.getBytes(), -1);
				System.out.println(prefix + "更新成功");
			}
		} catch (Exception e) {
			System.err.println(prefix + "更新失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 不使用密码 删除节点
	 */
	static void deleteNodeByNoAuthentication() throws Exception {

		String prefix = "[不使用任何授权信息]";

		try {
			System.out.println(prefix + "删除节点：" + PATH_DEL);
			ZooKeeper nozk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			Thread.sleep(2000);
			Stat stat = nozk.exists(PATH_DEL, false);
			if (stat!=null) {
				nozk.delete(PATH_DEL,-1);
				System.out.println(prefix + "删除成功");
			}
		} catch (Exception e) {
			System.err.println(prefix + "删除失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 采用错误的密码删除节点
	 */
	static void deleteNodeByBadAuthentication() throws Exception {

		String prefix = "[使用错误的授权信息]";

		try {
			System.out.println(prefix + "删除节点：" + PATH_DEL);
			ZooKeeper badzk = new ZooKeeper(CONNECT_ADDR, 2000, null);
			//授权
			badzk.addAuthInfo(authentication_type,badAuthentication.getBytes());
			Thread.sleep(2000);
			Stat stat = badzk.exists(PATH_DEL, false);
			if (stat!=null) {
				badzk.delete(PATH_DEL, -1);
				System.out.println(prefix + "删除成功");
			}
		} catch (Exception e) {
			System.err.println(prefix + "删除失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 使用正确的密码删除节点
	 */
	static void deleteNodeByCorrectAuthentication() throws Exception {

		String prefix = "[使用正确的授权信息]";

		try {
			System.out.println(prefix + "删除节点：" + PATH_DEL);
			Stat stat = zk.exists(PATH_DEL, false);
			if (stat!=null) {
				zk.delete(PATH_DEL, -1);
				System.out.println(prefix + "删除成功");
			}
		} catch (Exception e) {
			System.out.println(prefix + "删除失败，原因是：" + e.getMessage());
		}
	}

	/**
	 * 使用正确的密码删除节点
	 */
	static void deleteParent() throws Exception {
		try {
			Stat stat = zk.exists(PATH_DEL, false);
			if (stat == null) {
				zk.delete(PATH, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		ZookeeperAuth testAuth = new ZookeeperAuth();
		testAuth.createConnection(CONNECT_ADDR, 2000);
		List<ACL> acls = new ArrayList<ACL>();
		for (ACL ids_acl : Ids.CREATOR_ALL_ACL) {
			acls.add(ids_acl);
		}
		
		try {//创建节点
			if(zk.exists(PATH, true) == null){
				zk.create(PATH, "init content".getBytes(), acls, CreateMode.PERSISTENT);
				System.out.println("使用授权key：" + correctAuthentication + "创建节点"
						+PATH+",初始化内容是：init content");
			}else{
				System.out.println("该节点已经存在。。。。");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getDataByNoAuthentication();
		getDataByBadAuthentication();
		getDataByCorrectAuthentication();
	}

}
