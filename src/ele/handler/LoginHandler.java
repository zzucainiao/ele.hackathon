package ele.handler;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import ele.util.InterfaceHelper;
import ele.util.MemInit;

public class LoginHandler extends BaseHandler {
	public static String login(String jsonData) {
		int checkedJsonData = checkJsonData(jsonData);
		String username = "";
		String password = "";
		if (checkedJsonData != 0)
			return getErrorMessageJson(checkedJsonData);
		else {
			try {
				Map<String, String> map = InterfaceHelper.json2Map(jsonData);
				username = map.get("username");
				password = map.get("password");
				if (username == null || password == null)
					return getErrorMessageJson(MALFORMED_JSON);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return getErrorMessageJson(MALFORMED_JSON);
			}
		}
		
		String token = MemInit.username2Token(username);
		if (!MemInit.isTokenValid(token)) {
			return InterfaceHelper.getURLResult(403,
					InterfaceHelper.USER_AUTH_FAIL);
		}
		String userPwd = MemInit.userMsg.get(token)[2];
		/*
		 * class LoginMsg { private int u }
		 */
		HashMap loginMsg = new HashMap<>();
		String msg;
		if (userPwd != null && password.equals(userPwd)) {
			int id = Integer.parseInt(MemInit.userMsg.get(token)[1]); 
			loginMsg.put("user_id", id);
			loginMsg.put("username", username);
			loginMsg.put("access_token", token);
			msg = InterfaceHelper.getURLResult(200, loginMsg);
		} else {
			msg = InterfaceHelper.getURLResult(403,
					InterfaceHelper.USER_AUTH_FAIL);
		}
		return msg;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException, SocketException, UnknownHostException {
		MemInit.run("root", "toor", "jdbc:mysql://127.0.0.1:3306/eleme");
		String json = "{\"username\" : \"root\", \"password\" : \"toor\"}";
		String msg = login(json);
		System.out.println(msg);
		json = "{\"username\" : \"roasdfot\", \"password\" : \"toor\"}";
		msg = login(json);
		System.out.println(msg);
	}
}