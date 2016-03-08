package ele.util;

import java.util.concurrent.locks.Lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class MessageHelper implements Runnable {
	
	public String ch = null;
	private class MyPubSub extends JedisPubSub {

		@Override
		public void onMessage(String channel, String message) {
			// TODO Auto-generated method stub
			String msgs[] = message.split("_");
			int foodId = Integer.parseInt(channel);
			String type = msgs[0];
			String ip = msgs[1];
			long threadId = Long.parseLong(msgs[2]);
			String cartId = msgs[3];
			String token = cartId.substring(1);
			if ("AddFood".equals(type)) {
				int foodNum = Integer.parseInt(msgs[4]);
				int userId = Integer.parseInt( MemInit.userMsg.get(token)[1] );
				int[] cartMsg = MemInit.cartMsg[userId][ cartId.charAt(0)-'0' ];
				
				boolean find = false;
				for (int i = 0; i < 3; ++ i) {
					if (foodId == cartMsg[i]) {
						cartMsg[i + 3] += foodNum;
						find = true;
						break;
					}
				}	
				if (!find) {
					for (int i = 0; i < 3; ++ i) {
						if (0 == cartMsg[i + 3]) {
							cartMsg[i] = foodId;
							cartMsg[i + 3] += foodNum;
							break;
						}
					}
				}
				if(MemInit.ip.equals(ip)) {
					Lock lock = MemInit.thread2Lock.get(threadId);
					if ( null != lock) {
						lock.lock();
						try {
							MemInit.threadCondition.get(threadId).signal();
						} finally {
							lock.unlock();
						}
					}
				}
			} else if ("MakeOrder".equals(type)) {
				int orderEndFlag = Integer.parseInt(msgs[4]);
				int foodIdx = Integer.parseInt(msgs[5]);
				int userId = Integer.parseInt( MemInit.userMsg.get(token)[1] );
				int[] cartMsg = MemInit.cartMsg[userId][ cartId.charAt(0)-'0' ];
				boolean ok = true;
				for ( int i = 0; i < 3; ++ i ) {
					if ( cartMsg[i + 3] > MemInit.foodMsg[cartMsg[i]][1]) {
						ok = false;
						break;
					}
				}
				if ( ok ) {
					MemInit.foodMsg[cartMsg[foodIdx]][1] -= cartMsg[foodIdx + 3];
					if (1 == orderEndFlag) {
						MemInit.finishedOrder.add(cartId);
						MemInit.finishedUser2Order.put(token, cartId);
					}
				}
				
				if(MemInit.ip.equals(ip)) {
					Lock lock = MemInit.thread2Lock.get(threadId);
					if ( null != lock) {
						lock.lock();
						try {
							MemInit.threadCondition.get(threadId).signal();
						} finally {
							lock.unlock();
						}
					}
				}
			}
			/*if (channel.startsWith("F")) {
				String cartId = msgs[0];
				int foodId = Integer.parseInt(msgs[1]);
				int foodNum = Integer.parseInt(msgs[2]);
				String ip = msgs[3];
				long threadId = Long.parseLong(msgs[4]);
				if( !MemInit.cartMsg.containsKey(cartId)) {
					MemInit.cartMsg.put(cartId, new int[6]);
				}
				int[] cartMsg = MemInit.cartMsg.get(cartId);
				boolean find = false;
				for (int i = 0; i < 3; ++ i) {
					if (foodId == cartMsg[i]) {
						cartMsg[i + 3] += foodNum;
						find = true;
						break;
					}
				}	
				if (!find) {
					for (int i = 0; i < 3; ++ i) {
						if (0 == cartMsg[i + 3]) {
							cartMsg[i] = foodId;
							cartMsg[i + 3] += foodNum;
							break;
						}
					}
				}
				if(MemInit.ip.equals(ip)) {
					Lock lock = MemInit.thread2Lock.get(threadId);
					if ( null != lock) {
						lock.lock();
						try {
							MemInit.threadCondition.get(threadId).signal();
						} finally {
							lock.unlock();
						}
					}
				}
				MemInit.cartMsg.put(cartId, cartMsg);
			} else if ("AddFood".equals(channel)) {
				String cartId = msgs[0];
				int foodId = Integer.parseInt(msgs[1]);
				int foodNum = Integer.parseInt(msgs[2]);
				String ip = msgs[3];
				long threadId = Long.parseLong(msgs[4]);
				if( !MemInit.cartMsg.containsKey(cartId)) {
					MemInit.cartMsg.put(cartId, new int[6]);
				}
				int[] cartMsg = MemInit.cartMsg.get(cartId);
				boolean find = false;
				for (int i = 0; i < 3; ++ i) {
					if (foodId == cartMsg[i]) {
						cartMsg[i + 3] += foodNum;
						find = true;
						break;
					}
				}	
				if (!find) {
					for (int i = 0; i < 3; ++ i) {
						if (0 == cartMsg[i + 3]) {
							cartMsg[i] = foodId;
							cartMsg[i + 3] += foodNum;
							break;
						}
					}
				}
				if(MemInit.ip.equals(ip)) {
					Lock lock = MemInit.thread2Lock.get(threadId);
					if ( null != lock) {
						lock.lock();
						try {
							MemInit.threadCondition.get(threadId).signal();
						} finally {
							lock.unlock();
						}
					}
				}
				MemInit.cartMsg.put(cartId, cartMsg);
			} else if ( "MakeOrder".equals(channel) ) {
				String ip = msgs[0];
				long threadId = Long.parseLong(msgs[1]);
				String cartId = msgs[2];
				String token = msgs[3];
				
				boolean ok = true;
				if( !MemInit.cartMsg.containsKey(cartId)) {
					MemInit.cartMsg.put(cartId, new int[6]);
				}
				int cartMsg[] = MemInit.cartMsg.get(cartId);
				for ( int i = 0; i < 3; ++ i ) {
					if ( cartMsg[i + 3] > MemInit.foodMsg[cartMsg[i]][1]) {
						ok = false;
						break;
					}
				}
				
				if( ok ) {
					for ( int i = 0; i < 3; ++ i ) {
						if( cartMsg[i+3] != 0) {
							MemInit.foodMsg[cartMsg[i]][1] -= cartMsg[i + 3];
						}
					}
					
					MemInit.finishedOrder.add(cartId);
					MemInit.finishedUser2Order.put(token, cartId);
				}
				if(MemInit.ip.equals(ip)) {
					Lock lock = MemInit.thread2Lock.get(threadId);
					if ( null != lock) {
						lock.lock();
						try {
							MemInit.threadCondition.get(threadId).signal();
						} finally {
							lock.unlock();
						}
					}
				}
			}*/
		}
		
	}

	public MessageHelper(String chan) {
		this.ch = chan;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Jedis jedis = null;
		try{
			jedis = RedisUtil.getJedis();
			MyPubSub ps = new MyPubSub();
			ps.proceed(jedis.getClient(), ch);
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if (null != jedis)
				RedisUtil.returnResource(jedis);
		}
	}

}
