package ele.handler;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import ele.util.MemInit;
import ele.util.RedisUtil;

public class FoodsHandler extends BaseHandler {
	public static String foodsCheck(String token)
	{
		int checkToken = checkAccessToken(token);
		if (checkToken != 0)
			return getErrorMessageJson(checkToken);

		List<HashMap<String,Integer>> foodsList = new ArrayList<>();
		for( int i = 1; i <= 100; ++i)
		{
			HashMap<String,Integer> tmp = new HashMap<String,Integer>();
			tmp.put("id", i);
			tmp.put("price", MemInit.foodMsg[i][0]);
			tmp.put("stock", MemInit.foodMsg[i][1]);
			foodsList.add(tmp);
		}
		return ele.util.InterfaceHelper.getURLResult(200, foodsList);
	}
	public static void main(String [] argv) throws ClassNotFoundException, SQLException, SocketException, UnknownHostException {
		MemInit.run("root", "toor", "jdbc:mysql://127.0.0.1:3306/eleme");
		System.out.println(FoodsHandler.foodsCheck("ROOT"));
		System.out.println(FoodsHandler.foodsCheck("ROOdT"));
	}
}