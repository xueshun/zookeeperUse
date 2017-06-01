package xue.zookeeper.watcher;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * zk的watcher只能对父节点的变化进行监听，
 *  而对子节点只有一种状态： childrennodechanged 而对子节点的增删改没有做监听 
 *  再就是watcher 监听是一次性，每次都必须要重新new或者
 * @author Administrator
 *
 */
public class ZooKeeperWacther implements Watcher{
	
	/** 定义原子变量**/
	AtomicInteger seq = new AtomicInteger();
	/** 定义session失效时间**/
	private static final int SESSION_TIMEOUT=10000;
	/**zookeeper服务器地址**/
	private static final String CONNECTION_ADDR = "192.168.1.191:2181";
	/** zk父路径设置**/
	private static final String PARENT_PATH = "/testRoot";
	/** zk自子路径**/
	private static final String CHILDREN_PATH = "/testRoot/children";
	/** 进入标识**/
	private static final String LOG_PREFIX_OF_MAIN = "【Main】";
	/** zk变量**/
	private ZooKeeper zk =null;
	/** 信号量设置，用于等待zookeeper连接建立之后 通知阻塞程序继续向下执行**/
	private CountDownLatch connectedSemaphore = new CountDownLatch(1);
	
	/**
	 * 创建ZK连接
	 * @param connectAddr  ZK服务器地址列表
	 * @param sessionTimeout Session超时时间
	 */
	public void createConnection(String connectAddr, int sessionTimeout){
		this.releaseConnection();
		try {
			zk = new ZooKeeper(CONNECTION_ADDR, SESSION_TIMEOUT, this);
			System.out.println(LOG_PREFIX_OF_MAIN + "开始连接ZK服务器");
			connectedSemaphore.await();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭连接
	 */
	public void releaseConnection(){
		if(this.zk !=null){
			try {
				this.zk.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 创建节点 
	 * @param path 节点路径
	 * @param data 数据内容
	 * @return
	 */
	public boolean createPath(String path, String data){
		try {
			//设置监控(由于zookeeper的监控都是一次性的所以 每次必须设置监控)
			this.zk.exists(path, true);
			System.out.println(LOG_PREFIX_OF_MAIN + "节点创建成功, Path: " + 
							   this.zk.create(	path, /**路径*/ 
									   			data.getBytes(),/**数据*/
								   				Ids.OPEN_ACL_UNSAFE, /**所有可见*/
								   				CreateMode.PERSISTENT ) + 	/**永久存储*/
							   ", content: " + data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 读取指定节点数据内容
	 * @param path 节点路径
	 * @param needWatch 是否需要watch
	 * @return
	 */
	public String readData(String path,boolean needWatch){
		try {
			return new String(this.zk.getData(path, needWatch, null));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 更新指定节点数据内容
	 * @param path 节点路径
	 * @param data 数据内容
	 * @return
	 */
	public boolean updateData(String path,String data){ 
		try {
			System.out.println(LOG_PREFIX_OF_MAIN +
					"更新数据成功，path:" + path
					+",stat:" + this.zk.setData(path, data.getBytes(), -1));
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	/**
	 * 删除指定节点
	 * @param path 节点路径
	 */
	public void deleteNode(String path){
		try {
			this.zk.delete(path, -1);
			System.out.println(LOG_PREFIX_OF_MAIN
					+"删除节点成功，path: " + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断节点是否存在
	 * @param path  节点路径
	 * @param needWatch  是否需要watch
	 * @return
	 */
	public Stat exists(String path, boolean needWatch){
		try {
			return this.zk.exists(path, needWatch);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	
	/**
	 * 获取子节点
	 * @param path 节点路径
	 * @param needWatch
	 * @return
	 */
	private List<String> GetChildren(String path, boolean needWatch){
		try {
			return this.zk.getChildren(path, needWatch);
		}  catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 删除所有节点
	 */
	public void deleteAllTestPath() {
		if(this.exists(CHILDREN_PATH, false) != null){
			this.deleteNode(CHILDREN_PATH);
		}
		if(this.exists(PARENT_PATH, false) != null){
			this.deleteNode(PARENT_PATH);
		}		
	}
	
	/**
	 * 收到来自Server的Watcher通知后的处理
	 */
	@Override
	public void process(WatchedEvent event) {
		System.out.println("进入 process ...... event = " + event);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(event == null){
			return ;
		}
		
		//连接状态
		KeeperState keeperState = event.getState();
		//事件类型
		EventType eventType = event.getType();
		//受影响的path
		String path = event.getPath();
		
		String logPrefix = "【Watcher-" + this.seq.incrementAndGet() + "】";
		
		System.out.println(logPrefix  + "收到Watcher通知");
		System.out.println(logPrefix + "连接状态：\t" + keeperState);
		System.out.println(logPrefix + "事件类型：\t" + eventType.toString());
		
		if(KeeperState.SyncConnected == keeperState){
			//成功连接上ZK服务器
			if(EventType.None == eventType){
				System.out.println(logPrefix + "成功连接上ZK服务器");
				connectedSemaphore.countDown();
			}else if(EventType.NodeCreated == eventType){ //创建节点
				System.out.println(logPrefix + "节点创建");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				this.exists(path, true);
			}else if(EventType.NodeDataChanged == eventType){ //更新节点
				System.out.println(logPrefix + "节点数据更新");
				System.out.println();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "数据内容：" + this.readData(PARENT_PATH, true));
			}else if(EventType.NodeChildrenChanged == eventType){ //更新子节点
				System.out.println(logPrefix + "子节点变更");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println(logPrefix + "子节点列表：" + this.GetChildren(PARENT_PATH, true));
			}else if(EventType.NodeDeleted == eventType){ //删除节点
				System.out.println(logPrefix + "节点" + path + "被删除"); 
			}else ;
		}else if(KeeperState.Disconnected == keeperState){
			System.out.println(logPrefix + "与ZK服务器断开连接");
		}else if(KeeperState.AuthFailed == keeperState){
			System.out.println(logPrefix + "权限检查失败");
		}else if(KeeperState.Expired == keeperState){
			System.out.println(logPrefix + "会话失效");
		}else ;
		System.out.println("---------------------------------------");
	}
	
	public static void main(String[] args) throws Exception {
		//建立Watcher
		ZooKeeperWacther zkWacth = new ZooKeeperWacther();
		//创建连接
		zkWacth.createConnection(CONNECTION_ADDR, SESSION_TIMEOUT);
		
		Thread.sleep(1000);
		
		zkWacth.deleteAllTestPath();
		//创建节点
		if(zkWacth.createPath(PARENT_PATH, System.currentTimeMillis() + "")){
			Thread.sleep(1000);
			
			//读取数据
			System.out.println("--------------read parent----------");
			zkWacth.readData(PARENT_PATH, true);
			
			//创建子节点
			zkWacth.createPath(CHILDREN_PATH, System.currentTimeMillis() + "");
		}
		
		Thread.sleep(5000);
		//清理节点
		/*zkWacth.deleteAllTestPath();*/
		Thread.sleep(1000);
		zkWacth.releaseConnection();
	}

}
