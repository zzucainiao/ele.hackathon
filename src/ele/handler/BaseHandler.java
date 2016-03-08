package ele.handler;

import java.io.UnsupportedEncodingException;

import redis.clients.jedis.Jedis;
import ele.util.InterfaceHelper;
import ele.util.MemInit;
import ele.util.RedisUtil;

public class BaseHandler {
	public final static int EMPTY_REQUEST = 1;
	public final static int MALFORMED_JSON = 2;
	public final static int INVALID_ACCESS_TOKEN = 3;

	public static String getErrorMessageJson(int errorCode) {
		switch (errorCode) {
		case EMPTY_REQUEST:
			return InterfaceHelper.getURLResult(400,
					InterfaceHelper.EMPTY_REQUEST);
		case MALFORMED_JSON:
			return InterfaceHelper.getURLResult(400,
					InterfaceHelper.MALFORMED_JSON);
		case INVALID_ACCESS_TOKEN:
			return InterfaceHelper.getURLResult(401,
					InterfaceHelper.INVALID_ACCESS_TOKEN);
		default:
			return "";
		}
	}

	/**
	 * 判断json串是否为空或者是否格式错误
	 * 
	 * @param jsonData
	 * @return 0:正确 1:EMPTY_REQUEST 2:MALFORMED_JSON
	 * @throws UnsupportedEncodingException
	 */
	public static int checkJsonData(String jsonData) {
		if ("".equals(jsonData)) {
			return EMPTY_REQUEST;
		} else if (!InterfaceHelper.isGoodJson(jsonData)) {
			return MALFORMED_JSON;
		} else {
			return 0;
		}
	}

	/**
	 * 判断access_token是否为空或者无效
	 * 
	 * @param accessToken
	 * @return 如果为空或者无效返回3:INVALID_ACCESS_TOKEN，反之返回0
	 * @throws UnsupportedEncodingException
	 */
	public static int checkAccessToken(String accessToken) {
		if (!"".equals(accessToken)) {
			if (!MemInit.isTokenValid(accessToken)) {
				return INVALID_ACCESS_TOKEN;
			}
		} else {
			return INVALID_ACCESS_TOKEN;
		}
		return 0;
	}
}
