package ele.handler;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import ele.util.InterfaceHelper;
import ele.util.MemInit;
import ele.util.RedisUtil;

public class CartCreationHandler extends BaseHandler{


	public static String createCarts(String token) {
		int checkedAccessToken = checkAccessToken(token);
		if( checkedAccessToken != 0 ) {
			return getErrorMessageJson( checkedAccessToken );
		}
	//	String uuid = UUID.randomUUID().toString();
		String cartId = (MemInit.ipInt * 3 + MemInit.num[ Integer.parseInt( MemInit.userMsg.get(token)[1] ) ] ++) + token;
		HashMap<String, String> cartMsg = new HashMap<>();
		cartMsg.put("cart_id", cartId);
		return InterfaceHelper.getURLResult(200, cartMsg);
	}

	public static void main(String[] args) throws ClassNotFoundException, SocketException, UnknownHostException, SQLException {
		MemInit.run("root", "toor", "jdbc:mysql://127.0.0.1:3306/eleme");
		String m = createCarts("ROOT");
		System.out.println(m);
		m = createCarts("ROOdT");
		System.out.println(m);
	}
}
