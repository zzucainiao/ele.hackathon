package ele.handler;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import ele.util.InterfaceHelper;
import ele.util.MemInit;
import ele.util.RedisUtil;
public class FoodAddtionHandler extends BaseHandler{
	public static String addFood(String token, String cartId, String jsonData) {
		int checkedAccessToken = checkAccessToken(token);
		if( checkedAccessToken != 0 ) {
			return getErrorMessageJson(checkedAccessToken);
		}
		int checkedJsonData = checkJsonData(jsonData);
		if( checkedJsonData != 0 ) {
			return getErrorMessageJson(checkedJsonData);
		}
		
		HashMap<String, String> foods = null;
		int foodId = Integer.MAX_VALUE;
		int foodNum = Integer.MAX_VALUE;
		try {
			foods = InterfaceHelper.json2Map(jsonData);
			foodId = Integer.parseInt(foods.get("food_id"));
			foodNum = Integer.parseInt(foods.get("count"));
//			System.out.println("CartId: " + cartId + ", " + jsonData);
			if (Integer.MAX_VALUE == foodId || Integer.MAX_VALUE == foodNum)
				return getErrorMessageJson(MALFORMED_JSON);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return getErrorMessageJson(MALFORMED_JSON);
		}
		
		if( !MemInit.isFoodValid(foodId) ) {
			return InterfaceHelper.getURLResult(404, InterfaceHelper.FOOD_NOT_FOUND);
		}
		
		if ( cartId.length() < 3 ) {
			return InterfaceHelper.getURLResult(404, InterfaceHelper.CART_NOT_FOUND);
		}
		
		if ( !token.equals(cartId.substring(1)) ) {
			System.out.println("token : " + token + " cartId : " + cartId);
			return InterfaceHelper.getURLResult(401, InterfaceHelper.NOT_AUTHORIZED_TO_ACCESS_CART);
		}
		
		if ( MemInit.finishedUser2Order.containsKey(token) ) {
			return InterfaceHelper.getURLResult(204);
		}
		
		int userId = Integer.parseInt( MemInit.userMsg.get(token)[1] );
		int[] cartMsg = MemInit.cartMsg[userId][ cartId.charAt(0)-'0' ];
		if (foodNum + cartMsg[3] + cartMsg[4] + cartMsg[5] > 3) {
			return InterfaceHelper.getURLResult(403, InterfaceHelper.FOOD_OUT_OF_LIMIT);
		}
		
		long threadId = Thread.currentThread().getId();
		MemInit.thread2Lock.get(threadId).lock();
		Jedis jedis = RedisUtil.getJedis();
		jedis.publish("" + foodId, "AddFood"
				+ "_" + MemInit.ip
				+ "_" + threadId
				+ "_" + cartId
				+ "_" + foodNum);
		RedisUtil.returnResource(jedis);
		try {
			MemInit.threadCondition.get(threadId).await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			MemInit.thread2Lock.get(threadId).unlock();	
		}	
		
		return InterfaceHelper.getURLResult(204);
	}

	public static void main(String[] args) throws Exception {
		MemInit.run("root", "toor", "jdbc:mysql://127.0.0.1:3306/eleme");
	}
}