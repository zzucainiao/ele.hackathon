package ele.handler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import ele.util.InterfaceHelper;
import ele.util.MemInit;
import ele.util.RedisUtil;

public class OrdersHandler extends BaseHandler {
	/**
	 * 下单
	 * @param token
	 * @param jsonData
	 * @return
	 */
	public static String placeOrder(String token, String jsonData) {
		int checkToken = checkAccessToken(token);
		if (checkToken != 0) 
			return getErrorMessageJson(checkToken);
		int checkJson = checkJsonData(jsonData);
		if (checkJson != 0)
			return getErrorMessageJson(checkJson);
		String cartId = "";
		try {
			HashMap<String, String> map = InterfaceHelper.json2Map(jsonData);
			cartId = map.get("cart_id");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 篮子不存在
		if ( cartId.length() < 3 ) {
			return InterfaceHelper.getURLResult(403, InterfaceHelper.CART_NOT_FOUND);
		}
		
		// 篮子不属于当前用户
		if ( !token.equals(cartId.substring(1)) ) {
			return InterfaceHelper.getURLResult(401, InterfaceHelper.NOT_AUTHORIZED_TO_ACCESS_CART);
		}

		// 此用户已经下过单
		if ( MemInit.finishedUser2Order.containsKey(token) ) {
			return InterfaceHelper.getURLResult(403, InterfaceHelper.ORDER_OUT_OF_LIMIT);
		}
		
		//int[] cartMsg = MemInit.cartMsg.get(cartId);
		int userId = Integer.parseInt( MemInit.userMsg.get(token)[1] );
		int[] cartMsg = MemInit.cartMsg[userId][ cartId.charAt(0)-'0' ];	
		int i = 3;
		while (i < 6 && cartMsg[i] != 0) {
			int orderEndFlag = 0;
			if (5 == i || cartMsg[i + 1] == 0)
				orderEndFlag = 1;
			long threadId = Thread.currentThread().getId();
			MemInit.thread2Lock.get(threadId).lock();
			Jedis jedis = RedisUtil.getJedis();
			jedis.publish("" + cartMsg[i - 3], "MakeOrder"
					+ "_" + MemInit.ip
					+ "_" + threadId
					+ "_" + cartId
					+ "_" + orderEndFlag
					+ "_" + (i - 3));
			RedisUtil.returnResource(jedis);
			try {
				MemInit.threadCondition.get(threadId).await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				MemInit.thread2Lock.get(threadId).unlock();	
			}
			i ++;
		}
		
		/*long threadId = Thread.currentThread().getId();
		MemInit.thread2Lock.get(threadId).lock();
		Jedis jedis = RedisUtil.getJedis();
		jedis.publish("MakeOrder", "MakeOrder"
					+ "_" + MemInit.ip
					+ "_" + threadId
					+ "_" + cartId
					+ "_" + token);
		RedisUtil.returnResource(jedis);
		try {
			MemInit.threadCondition.get(threadId).await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			MemInit.thread2Lock.get(threadId).unlock();	
		}*/
		
		if ( !MemInit.finishedOrder.contains(cartId)) {
			return InterfaceHelper.getURLResult(403, InterfaceHelper.FOOD_OUT_OF_STOCK);
		} else {
			Map<String, String> msg = new HashMap<>();
			msg.put("id", cartId);
			return InterfaceHelper.getURLResult(200, msg);
		}
	}
	
	public static String queryOrder(String token) {
		int checkToken = checkAccessToken(token);
		if (checkToken != 0) 
			return getErrorMessageJson(checkToken);
		
		List<HashMap> msg = new ArrayList<>();
		if( !MemInit.finishedUser2Order.containsKey(token) ) {
			return InterfaceHelper.getURLResult(200, msg);
		}
		String cartId = MemInit.finishedUser2Order.get(token);
		
		Map<String, String> cartMap = null;
		List<Map<String, Integer>> items = new ArrayList<>();
		//int [] cartMsg = MemInit.cartMsg.get(cartId);
		int userId = Integer.parseInt( MemInit.userMsg.get(token)[1] );
		int[] cartMsg = MemInit.cartMsg[userId][ cartId.charAt(0)-'0' ];
		int total = 0;
		for (int i = 0; i < 3; ++i) {
			if(cartMsg[i + 3] == 0)
				continue;
			HashMap<String, Integer> tmpMap = new HashMap<>();
			tmpMap.put("food_id", cartMsg[i]);
			tmpMap.put("count", cartMsg[i+3]);
			items.add(tmpMap);
			// 获取食物单价
			int price = MemInit.foodMsg[ cartMsg[i] ][0];
			total += price * cartMsg[i+3];
		}
		HashMap order = new HashMap();
		order.put("id", cartId);
		order.put("items", items);
		order.put("total", total);
		msg.add(order);
		return InterfaceHelper.getURLResult(200, msg);
	}
	
	public static String rootQueryOrder(String token) {
		int checkToken = checkAccessToken(token);
		if (checkToken != 0) 
			return getErrorMessageJson(checkToken);
		// 判断token是否为root用户
		if (!"ROOT".equals(token))
			return getErrorMessageJson(INVALID_ACCESS_TOKEN);
		List<HashMap> msg = new ArrayList<>();
		for(Entry<String, String> en: MemInit.finishedUser2Order.entrySet()) {
			//int [] cartMsg = MemInit.cartMsg.get(en.getValue());
			int userId = Integer.parseInt( MemInit.userMsg.get(en.getKey())[1] );
			int[] cartMsg = MemInit.cartMsg[userId][ en.getValue().charAt(0)-'0' ];
			List<Map<String, Integer>> items = new ArrayList<>();
			int total = 0;
			for (int i = 0; i < 3; ++i) {
				if(cartMsg[i+3] == 0)
					continue;
				HashMap<String, Integer> tmpMap = new HashMap<>();
				tmpMap.put("food_id", cartMsg[i]);
				tmpMap.put("count", cartMsg[i+3]);
				items.add(tmpMap);
					// 获取食物单价
				int price = MemInit.foodMsg[ cartMsg[i] ][0];
				total += price * cartMsg[i+3];
			}
			HashMap order = new HashMap();
			order.put("id", en.getValue());
			order.put("user_id", userId);
			order.put("items", items);
			order.put("total", total);
			msg.add(order);
		}
		return InterfaceHelper.getURLResult(200, msg);
	}
	
	public static void main(String[] args) {
		/*String uuid = UUID.randomUUID().toString();
		String[] codes = uuid.split("-");
		String code = "";
		for (String s : codes)
			code += s;
		System.out.println(code);*/
		String jsonData = "{\"cart_id\": \"c370abf104774a1090872f5476fa3f67\"}";
		System.out.println(rootQueryOrder("userroot"));
	}
}