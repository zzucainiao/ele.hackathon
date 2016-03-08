package ele.handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	static int MIN_SIZE = 300;
	static int MAX_SIZE = 400;
	static long KEEP_ALIVE_TIME = 30;
/*	public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
			    MIN_SIZE, MAX_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			    new ArrayBlockingQueue<Runnable>(100),
			    new ThreadPoolExecutor.AbortPolicy());*/
	public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,   
			60L,  TimeUnit.SECONDS,   
			new SynchronousQueue<Runnable>()); 
	public static ThreadPoolExecutor getThreadPool() {
		return threadPoolExecutor;
	}
}
