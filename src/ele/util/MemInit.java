package ele.util;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import redis.clients.jedis.Jedis;

public class MemInit {
	public static HashMap<String, String[]> userMsg = new HashMap<>();		// token -> userName, userId, password
	public static HashMap<String, String> userName2Token = new HashMap<>();		// userName -> token
	public static int[][] foodMsg = new int[101][2];		// foodId -> [price, stock]
//	public static HashMap<String, int[]> cartMsg = new HashMap<>();		// cartId -> [foodId, count]
//	public static HashMap<String, String> cartBelonger = new HashMap<>();		// cartId -> userId
	public static HashMap<String, String> finishedUser2Order = new HashMap<>();		// token -> cartId
	public static Set<String> finishedOrder = new HashSet<>();
	
	public static int [][][] cartMsg = null;
	public static int [] num = null;
	public static int ipInt = 0;
	public static String ip = null;
	public static HashMap<Long, Condition> threadCondition = new HashMap<>();	// threadId -> Condition
	
	public static HashMap<Long, ReentrantLock> thread2Lock = new HashMap<>();
	//public final static Lock lock = new ReentrantLock();
	
	public final static boolean isTokenValid(String token) {
		return userMsg.containsKey(token);
	}
	
	public final static boolean isFoodValid(int foodId) {
		return 1 <= foodId && foodId <= 100;
	}
	
	public final static String username2Token(String username) {
		return username.toUpperCase();
	}
	
	public final static String token2Username(String token) {
		return token.toLowerCase();
	}
	
	public static void run(String sqlUser, String sqlPass, String sqlDbUrl)
			throws SQLException, ClassNotFoundException, SocketException,
			UnknownHostException {
		Jedis jedis = RedisUtil.getJedis();
		//jedis.flushAll();
		if (!jedis.exists("ServerId")) {
			jedis.set("ServerId", "0");
		}
		ip = "" + jedis.incr("ServerId");
		ipInt = Integer.parseInt(ip) - 1;
		RedisUtil.returnResource(jedis);

		Class.forName("com.mysql.jdbc.Driver");
		java.sql.Connection conn = DriverManager.getConnection(sqlDbUrl,
				sqlUser, sqlPass);
		Statement stmt = null;
		stmt = conn.createStatement();
		String sql;
		sql = "select * from user";
		ResultSet rs;
		rs = stmt.executeQuery(sql);
		
		int userNum = 0;
		while (rs.next()) {
			String uid = rs.getString("id");
			String name = rs.getString("name");
			String token = username2Token(name);
			String password = rs.getString("password");
			userMsg.put(token, new String[]{
					name, 
					uid, 
					password
			});
			userName2Token.put(name, token);
			++ userNum;
		}
		cartMsg = new int[userNum+1][][];
		for(int i = 0; i <= userNum; ++i) {
			cartMsg[i] = new int[9][];
			for(int j = 0; j<9; ++j)
				cartMsg[i][j] = new int[6];
		}
		num = new int[userNum + 1];
		sql = "select * from food";
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			int id = Integer.parseInt(rs.getString("id"));
			int price = Integer.parseInt(rs.getString("price"));
			int stock = Integer.parseInt(rs.getString("stock"));
			foodMsg[id][0] = price;
			foodMsg[id][1] = stock;
		}
		conn.close();
	}
	
	public static void main(String[] argv) throws Exception {
		MemInit.run("root", "toor", "jdbc:mysql://127.0.0.1:3306/eleme");
		System.out.println(ip);
		/*System.out.println(InterfaceHelper.rst2Json(userMsg));
		System.out.println(InterfaceHelper.rst2Json(userName2Token));
		System.out.println(InterfaceHelper.rst2Json(foodMsg));*/
	}
}
