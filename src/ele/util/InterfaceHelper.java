package ele.util;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author infinity
 * 将前台传来的Json转化为Map，将Map转化为Json
 * 返回URL Code
 */
public class InterfaceHelper {
	private static final HashMap<Integer, String> URLCODE = new HashMap<>();
	public static final HashMap<String, String> INVALID_ACCESS_TOKEN = new HashMap<>();
	public static final HashMap<String, String> EMPTY_REQUEST = new HashMap<>();
	public static final HashMap<String, String> MALFORMED_JSON = new HashMap<>();
	public static final HashMap<String, String> USER_AUTH_FAIL = new HashMap<>();
	public static final HashMap<String, String> CART_NOT_FOUND = new HashMap<>();
	public static final HashMap<String, String> NOT_AUTHORIZED_TO_ACCESS_CART = new HashMap<>();
	public static final HashMap<String, String> FOOD_OUT_OF_STOCK = new HashMap<>();
	public static final HashMap<String, String> FOOD_OUT_OF_LIMIT = new HashMap<>();
	public static final HashMap<String, String> FOOD_NOT_FOUND = new HashMap<>();
	public static final HashMap<String, String> ORDER_OUT_OF_LIMIT = new HashMap<>();

	static {
		URLCODE.put(200, "OK");
		URLCODE.put(204, "No content");
		URLCODE.put(400, "Bad Request");
		URLCODE.put(401, "Unauthorized");
		URLCODE.put(403, "Forbidden");
		URLCODE.put(404, "Not Found");
		
		INVALID_ACCESS_TOKEN.put("code", "INVALID_ACCESS_TOKEN");
		INVALID_ACCESS_TOKEN.put("message", "无效的令牌");
		
		EMPTY_REQUEST.put("code", "EMPTY_REQUEST");
		EMPTY_REQUEST.put("message", "请求体为空");
		
		MALFORMED_JSON.put("code", "MALFORMED_JSON");
		MALFORMED_JSON.put("message", "格式错误");

		USER_AUTH_FAIL.put("code", "USER_AUTH_FAIL");
		USER_AUTH_FAIL.put("message", "用户名或密码错误");

		CART_NOT_FOUND.put("code", "CART_NOT_FOUND");
		CART_NOT_FOUND.put("message", "篮子不存在");

		NOT_AUTHORIZED_TO_ACCESS_CART.put("code", "NOT_AUTHORIZED_TO_ACCESS_CART");
		NOT_AUTHORIZED_TO_ACCESS_CART.put("message", "无权限访问指定的篮子");

		FOOD_OUT_OF_STOCK.put("code", "FOOD_OUT_OF_STOCK");
		FOOD_OUT_OF_STOCK.put("message", "食物库存不足");

		FOOD_OUT_OF_LIMIT.put("code", "FOOD_OUT_OF_LIMIT");
		FOOD_OUT_OF_LIMIT.put("message", "篮子中食物数量超过了三个");

		FOOD_NOT_FOUND.put("code", "FOOD_NOT_FOUND");
		FOOD_NOT_FOUND.put("message", "食物不存在");

		ORDER_OUT_OF_LIMIT.put("code", "ORDER_OUT_OF_LIMIT");
		ORDER_OUT_OF_LIMIT.put("message", "每个用户只能下一单");
	}

	/**
	 * 判断json串格式是否正确
	 * @param json
	 * @return 
	 */
	public static boolean isGoodJson(String json) {
		try {
			new JSONObject(json);
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}

	/**
	 * 将Json串转化为数据
	 * @param json Json串
	 * @return Map对象
	 * @throws JSONException
	 */
	public static HashMap json2Map(String json) throws JSONException {
		JSONObject dataJson = new JSONObject(json);
		HashMap result = new HashMap();
		Iterator it = dataJson.keys();
		String key = null;
		String value = null;

		while (it.hasNext()) {
			key = (String) it.next();
			value = dataJson.getString(key);
			result.put(key, value);
		}
		return result;
	}

	/**
	 * 将返回结果转化为Json串
	 * @param map Map对象
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String rst2Json(Map map) throws UnsupportedEncodingException {
		JSONObject dataJson = new JSONObject(map);
		String json = dataJson.toString();
		return json;
	}

	/**
	 * 将返回结果转化为Json串，重载List
	 * @param arr List对象
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String rst2Json(List arr) throws UnsupportedEncodingException {
		JSONArray dataJson = new JSONArray(arr);
		String json = dataJson.toString();
		return json;
	}

	/**
	 * 将返回结果转化为Json串，重载普通Obj
	 * @param obj
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String rst2Json(Object obj) throws UnsupportedEncodingException {
		HashMap result = new HashMap();
        Method[] methods = obj.getClass().getDeclaredMethods();

        for (Method method : methods) {

            try {

                if (method.getName().startsWith("get")) {

                    String field = method.getName();
                    field = field.substring(field.indexOf("get") + 3);
                    field = field.toLowerCase().charAt(0) + field.substring(1);

                    Object value = method.invoke(obj, (Object[]) null);
//                    if (value.getClass().getName() ==)
                    result.put(field, null == value ? "" : value.toString());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
		JSONObject dataJson = new JSONObject(result);
		String json = dataJson.toString();
		return json;
	}

	/**
	 * 根据code返回结果
	 * @param code URL code
	 * @param map 结果信息
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String getURLResult(int code, Map map) {
		String result = "HTTP/1.1 " + code + " " + URLCODE.get(code) + "\r\n";
		if (code != 204) {
			try {
				result += "Content-Type:application/json;charset=UTF-8\r\n";
				String mapJson = rst2Json(map);
				result += "Content-Length:" + mapJson.getBytes("utf-8").length + "\r\n\r\n";
				result += mapJson;
				return result;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return getURLResult(code);
	}

	/**
	 * 根据code返回结果
	 * @param code URL code
	 * @param map 结果信息
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String getURLResult(int code, List arr) {
		String result = "HTTP/1.1 " + code + " " + URLCODE.get(code) + "\r\n";
		if (code != 204) {
			try {
				result += "Content-Type:application/json;charset=UTF-8\r\n";
				String arrJson = rst2Json(arr);
				result += "Content-Length:" + arrJson.getBytes("utf-8").length + "\r\n\r\n";
				result += arrJson;
				return result;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return getURLResult(code);
	}
	
	/**
	 * 当code == 204时返回结果
	 * @param code URL code
	 * @param map 结果信息
	 * @return Json串
	 * @throws UnsupportedEncodingException
	 */
	public static String getURLResult(int code) {
		if (code != 204) {
			try {
				throw new Exception("Code != 204");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String result = "HTTP/1.1 " + code + " " + URLCODE.get(code) + "\r\n\r\n";
		return result;
	}

	public static void main(String[] argvs) throws UnsupportedEncodingException {
		
	}
}
