import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ele.handler.CartCreationHandler;
import ele.handler.FoodAddtionHandler;
import ele.handler.FoodsHandler;
import ele.handler.LoginHandler;
import ele.handler.OrdersHandler;
import ele.handler.ThreadPool;
import ele.util.InterfaceHelper;
import ele.util.MemInit;
import ele.util.MessageHelper;

public class Server {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		ServerSocket serverSocket = null;
		try {
			ThreadPoolExecutor threadPoolExecutor = ThreadPool.getThreadPool();
			Map<String, String> sysEnv = System.getenv();
			//serverSocket = new ServerSocket(Integer.parseInt(sysEnv.get("APP_PORT")));
			serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			MemInit.run( sysEnv.get("DB_USER"), 
					sysEnv.get("DB_PASS"), 
					"jdbc:mysql://" + sysEnv.get("DB_HOST") + ":" + sysEnv.get("DB_PORT") + "/" + sysEnv.get("DB_NAME")
					);
			for (int i = 1; i < 101; i ++) {
				String channel = "" + i;
				threadPoolExecutor.execute(new MessageHelper(channel));
			}
			while (true) {
				Socket client = serverSocket.accept();
				try{
					threadPoolExecutor.execute(new Handler(client));
				} catch (Exception e) {
					;
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	private static class Handler implements Runnable {

		private Socket socket;

		Handler(Socket socket) {
			this.socket = socket;
		}
		
		private Map<String, String> getHeaderMsg(BufferedReader in, String content) throws IOException {
			Map<String, String> res = new HashMap<String, String>();
			String token = "";
			String inputLine = "";
			String dataLenStr = "";
        	boolean flag = false;
        	while (!(inputLine = in.readLine()).equals("")) {
        		String[] ctts = inputLine.split(":");
        		if (ctts[0].equals("Content-Length")) {
        			dataLenStr = ctts[1].trim();
        		}else if (ctts[0].equals("Access-Token")) {
            		token = ctts[1].trim();
            		flag = true;
            	}
            }
        	if (!flag) {
        		String contents[] = content.split("=");
        		if (contents.length > 1)
        			token = content.split("=")[1].trim();
        		else
        			token = "";
        	}
        	res.put("token", token);
        	res.put("datalen", dataLenStr);
        	return res;
		}

		@Override
		public void run() {
			long curThreadId = Thread.currentThread().getId();
			//System.out.println(curThreadId);
			if(!MemInit.thread2Lock.containsKey(curThreadId)) {
				ReentrantLock lock = new ReentrantLock();
				MemInit.thread2Lock.put(curThreadId, lock);
				MemInit.threadCondition.put(curThreadId, lock.newCondition());
			}
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String inputLine = in.readLine();
				String[] contents = inputLine.split(" ");
                if ("GET".equals(contents[0]) && "/".equals(contents[1])) {
                    socket.getOutputStream().write("Hello world!".getBytes());
					socket.getOutputStream().flush();
				}
                else if ("POST".equals(contents[0]) && "/login".equals(contents[1])) {
                	int dataLen = Integer.parseInt(getHeaderMsg(in, contents[1]).get("datalen"));
                    char[] buffer = new char[dataLen];
                    in.read(buffer);
                    String msg = new String(buffer);
                    String res = LoginHandler.login(msg);
                    
                    //System.out.println("POST /login : " + res);
                    socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("GET".equals(contents[0]) && contents[1].toLowerCase().startsWith("/foods")) {
                	String token = getHeaderMsg(in, contents[1]).get("token");
                	String res = FoodsHandler.foodsCheck(token);
                	
                	//System.out.println("GET /foods : " + res);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("POST".equals(contents[0]) && contents[1].startsWith("/carts")) 
                {
                	String token = getHeaderMsg(in, contents[1]).get("token");
                	String res = CartCreationHandler.createCarts(token);
                	
                	
                	//System.out.println("POST /carts : " + res);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("PATCH".equals(contents[0]) && contents[1].startsWith("/carts")) 
                {
                    String cartId = "";
                    if( contents[1].indexOf("?") == -1 ) 
                    {
                    	StringTokenizer st = new StringTokenizer(contents[1], "/");
                    	int tmp = 0;
                    	while (st.hasMoreElements()) {
                    		cartId = st.nextToken();
                    		if (tmp ++ == 1) {
                    			break;
                    		}
                    	}
//                    	cartId = contents[1].split("/")[2].trim();
                    }
                    else 
                    {
                    	StringTokenizer st = new StringTokenizer(contents[1], "/");
                    	String tmpContent = "";
                    	int tmp = 0;
                    	while (st.hasMoreElements()) {
                    		tmpContent = st.nextToken();
                    		if (tmp ++ == 1) {
                    			break;
                    		}
                    	}
                    	st = new StringTokenizer(tmpContent, "?");
                    	cartId = st.nextToken();
//                    	cartId = contents[1].split("/")[2].trim().split("\\?")[0].trim();
                    }
                	
                	Map<String, String> ret = getHeaderMsg(in, contents[1]);
                	String token = ret.get("token");
                	int dataLen = Integer.parseInt(ret.get("datalen"));
                    char[] buffer = new char[dataLen];
                    in.read(buffer);
                    String jsonData = new String(buffer);
                    
                    
                    //System.out.println("PATCH /carts : " + token + " " + cartId + " " + jsonData);
                    String res = FoodAddtionHandler.addFood(token, cartId, jsonData);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("POST".equals(contents[0]) && contents[1].startsWith("/orders"))
                {
                	Map<String, String> ret = getHeaderMsg(in, contents[1]);
                	String token = ret.get("token");
                	int dataLen = Integer.parseInt(ret.get("datalen"));
                    char[] buffer = new char[dataLen];
                    in.read(buffer);
                    String jsonData = new String(buffer);
                    
                    //System.out.println("POST /orders : " + token + " " + jsonData);
                    String res = OrdersHandler.placeOrder(token, jsonData);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("GET".equals(contents[0]) && contents[1].startsWith("/orders")) 
                {
                	String token = getHeaderMsg(in, contents[1]).get("token");
                    
                    //System.out.println("GET /orders : " + token);
                    String res = OrdersHandler.queryOrder(token);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }
                else if ("GET".equals(contents[0]) && contents[1].startsWith("/admin/orders")) 
                {
                	String token = getHeaderMsg(in, contents[1]).get("token");
                    
                    //System.out.println("GET /admin/orders : " + token);
                    String res = OrdersHandler.rootQueryOrder(token);
                	socket.getOutputStream().write(res.getBytes("utf-8"));
                    socket.getOutputStream().flush();
                }

			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}

			}
		}
	}
}

