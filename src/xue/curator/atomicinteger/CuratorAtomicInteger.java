package xue.curator.atomicinteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

/**
 * 分布式计数器功能
 *  网站的人数统计 汇总
 * @author Administrator
 *
 */
public class CuratorAtomicInteger {
	
	/** zookeeper连接地址**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session超时时间**/
	static final int SEESION_OUTTIME = 5000;//ms
	
	public static void main(String[] args) throws Exception {
		//1.重试策略: 初试时间为1s 重试10次
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.通过工程创建连接
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SEESION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.开启连接
		cf.start();
		
		//4.使用DistributedAtomicInteger
		DistributedAtomicInteger atomicInteger = 
				new DistributedAtomicInteger(cf, "/super", new RetryNTimes(3, 1000));
		
		AtomicValue<Integer> value = atomicInteger.get();
		
		atomicInteger.increment();
		//atomicInteger.forceSet(0); //初始化为0
	
		//AtomicValue<Integer> value = atomicInteger.add(1);
		
		
		
		System.out.println(value.succeeded());
		System.out.println(value.postValue());//最新值
		System.out.println(value.preValue()); //原始值
		
	}
}
