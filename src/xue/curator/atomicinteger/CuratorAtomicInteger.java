package xue.curator.atomicinteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;

/**
 * �ֲ�ʽ����������
 *  ��վ������ͳ�� ����
 * @author Administrator
 *
 */
public class CuratorAtomicInteger {
	
	/** zookeeper���ӵ�ַ**/
	static final String CONNECT_ADDR = "192.168.1.191:2181,192.168.1.220:2181,192.168.1.221:2181";
	/** session��ʱʱ��**/
	static final int SEESION_OUTTIME = 5000;//ms
	
	public static void main(String[] args) throws Exception {
		//1.���Բ���: ����ʱ��Ϊ1s ����10��
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
		//2.ͨ�����̴�������
		CuratorFramework cf = CuratorFrameworkFactory.builder()
				.connectString(CONNECT_ADDR)
				.sessionTimeoutMs(SEESION_OUTTIME)
				.retryPolicy(retryPolicy)
				.build();
		//3.��������
		cf.start();
		
		//4.ʹ��DistributedAtomicInteger
		DistributedAtomicInteger atomicInteger = 
				new DistributedAtomicInteger(cf, "/super", new RetryNTimes(3, 1000));
		
		AtomicValue<Integer> value = atomicInteger.get();
		
		atomicInteger.increment();
		//atomicInteger.forceSet(0); //��ʼ��Ϊ0
	
		//AtomicValue<Integer> value = atomicInteger.add(1);
		
		
		
		System.out.println(value.succeeded());
		System.out.println(value.postValue());//����ֵ
		System.out.println(value.preValue()); //ԭʼֵ
		
	}
}
