package chat8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MultiServer {

	static final int MAX_CONNECTIONS = 5;

	static ServerSocket serverSocket = null;
	static Socket socket = null;

	Map<String, PrintWriter> clientMap;
	Set<String> blackList;
	Set<String> forbiddenWords;
	Map<String, Set<String>> blockedUsersMap = new HashMap<>();

	public MultiServer() {
		clientMap = new HashMap<String, PrintWriter>();

		blackList = new HashSet<String>();
		blackList.add("성우리");

		forbiddenWords = new HashSet<String>();
		forbiddenWords.add("ㅗ");
		forbiddenWords.add("ㅅㅂ");

		Collections.synchronizedSet(blackList);
		Collections.synchronizedMap(clientMap);
		Collections.synchronizedSet(forbiddenWords);

	}

	public void init() {

		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			while (true) {
				socket = serverSocket.accept();

				if (clientMap.size() >= MAX_CONNECTIONS) {
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println("최대 접속 인원수를 초과했습니다.");
					out.close();
					socket.close();
					continue;
				}

				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}

	public void sendAllMsg(String name, String msg) {
		Iterator<String> it = clientMap.keySet().iterator();

		while (it.hasNext()) {
			try {
				String clientName = it.next();
				PrintWriter it_out = (PrintWriter) clientMap.get(clientName);

				Set<String> blockedUsers = blockedUsersMap.get(clientName);
				if (blockedUsers != null && blockedUsers.contains(name)) {
					continue;
				}

				if (name.equals("")) {
					it_out.println(msg);
				} else {
					it_out.println("[" + name + "]" + msg);
				}
			} catch (Exception e) {
				System.out.println("예외:" + e);
			}
		}
	}

	public void sendAllMsg(String name, String msg, String receiveName) {
		Iterator<String> it = clientMap.keySet().iterator();

		while (it.hasNext()) {
			try {
				String clientName = it.next();
				PrintWriter it_out = (PrintWriter) clientMap.get(clientName);

				if (clientName.equals(receiveName)) {
					Set<String> blockedUsers = blockedUsersMap.get(receiveName);
					if (blockedUsers != null && blockedUsers.contains(name)) {
						// 차단된 사용자에게는 메시지를 보내지 않음
						continue;
					}

					it_out.println("[귓속말]" + name + ":" + msg);
				}
			} catch (Exception e) {
				System.out.println("예외:" + e);
			}
		}
	}

	class MultiServerT extends Thread {
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;
		private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
		private static final String DB_USER = "study";
		private static final String DB_PASSWORD = "1234";
		String fixedReceiver = null;

		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (Exception e) {
				System.out.println("예외:" + e);
			}
		}

		private void showClientList() {
			String name = null;

			if (clientMap.isEmpty()) {
				out.println("현재 접속한 사용자가 없습니다.");
			} else {
				StringBuilder clients = new StringBuilder("현재 접속자 리스트: ");
				for (String client : clientMap.keySet()) {
					if (!client.equals(name))
						clients.append(client).append(", ");
				}
				clients.setLength(clients.length() - 2);
				out.println(clients);
			}
		}

		@Override
		public void run() {
			String name = "";
			String s = "";

			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

				name = in.readLine();

				if (blackList.contains(name)) {
					this.socket.close();
					return;
				}

				while (clientMap.containsKey(name)) {
					out.println("이미 존재하는 대화명입니다. 다른 대화명을 입력해주세요.");
					name = in.readLine();
				}

				sendAllMsg("", name + "님이 입장하셨습니다.");
				clientMap.put(name, out);
				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는" + clientMap.size() + "명 입니다.");

				while (in != null) {
					s = in.readLine();

					if (s.trim().isEmpty())
						continue;

					String[] words = s.split(" ");
					boolean isProhibited = false;
					for (String word : words) {
						if (forbiddenWords.contains(word)) {
							out.println("금칙어를 사용하셨습니다. 메시지가 전송되지 않았습니다.");
							isProhibited = true;
							break;
						}
					}

					if (isProhibited)
						continue;

					if (s == null)
						break;

					System.out.println(name + " >> " + s);

					PreparedStatement pstmt = null;
					try {
						String sql = "insert into chat_talking (대화명, 대화내용, 입력날짜) values (?, ?, current_date)";
						pstmt = connection.prepareStatement(sql);
						pstmt.setString(1, name);
						pstmt.setString(2, s);
						pstmt.executeUpdate();
					} finally {
						if (pstmt != null) {
							pstmt.close();
						}
					}

					if (s.charAt(0) == '/') {
						String[] strArr = s.split(" ");

						if (strArr[0].equals("/list")) {
							showClientList();
							continue;
						}

						if (strArr[0].equals("/block")) {
							Set<String> blockedUsers = blockedUsersMap.getOrDefault(name, new HashSet<>());
							blockedUsers.add(strArr[1]);
							blockedUsersMap.put(name, blockedUsers);
							out.println(strArr[1] + "님을 차단하였습니다.");
							continue;
						}
						if (strArr[0].equals("/unblock")) {
							Set<String> blockedUsers = blockedUsersMap.get(name);
							if (blockedUsers != null) {
								blockedUsers.remove(strArr[1]);
							}
							out.println(strArr[1] + "님의 차단을 해제하였습니다.");
							continue;
						}

						String msgContent = "";
						for (int i = 2; i < strArr.length; i++) {
							msgContent += strArr[i] + " ";
						}
						if (strArr[0].equals("/to")) {
							sendAllMsg(name, msgContent, strArr[1]);
						} else if (strArr[0].equals("/fixto")) {
							fixedReceiver = strArr[1];
							out.println(fixedReceiver + "님에게 귓속말 고정 설정을 완료했습니다.");
						} else if (strArr[0].equals("/unfixto")) {
							fixedReceiver = null;
							out.println("귓속말 고정 설정이 해제되었습니다.");
						}

					} else {
						if (fixedReceiver != null) {
							sendAllMsg(name, s, fixedReceiver);
						} else {
							sendAllMsg(name, s);
						}
					}
				}
			} catch (Exception e) {
				System.out.println("예외:" + e);
			} finally {
				clientMap.remove(name);
				sendAllMsg("", name + "님이 퇴장하셨습니다.");
				System.out.println(Thread.currentThread().getName() + " 종료");
				System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");

				try {
					in.close();
					out.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
