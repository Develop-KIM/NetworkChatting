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

	// 최대 인원수
	static final int MAX_CONNECTIONS = 5;

	// 멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;

	// 클라이언트 정보를 저장하기 위한 Map 컬렉션 생성
	Map<String, PrintWriter> clientMap;

	Set<String> blackList;
	Set<String> forbiddenWords;

	// 생성자
	public MultiServer() {
		// 클라이언트의 출력스트림을 저장할 HashMap 인스턴스 생성
		clientMap = new HashMap<String, PrintWriter>();

		// 블랙리스트 생성 및 사용자 추가
		blackList = new HashSet<String>();
		blackList.add("성우리");

		// 금칙어 생성 및 추가
		forbiddenWords = new HashSet<String>();
		forbiddenWords.add("ㅗ");
		forbiddenWords.add("ㅅㅂ");

		// HashMap, 블랙리스트, 금칙어 동기화 설정.
		Collections.synchronizedSet(blackList);
		Collections.synchronizedMap(clientMap);
		Collections.synchronizedSet(forbiddenWords);

	}

	// 채팅 서버 초기화
	public void init() {

		try {
			// 서버소켓 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			/*
			 * 1명의 클라이언트가 접속할때마다 허용해주고 동시에 쓰레드를 생성한다.
			 */
			while (true) {
				socket = serverSocket.accept();

				if (clientMap.size() >= MAX_CONNECTIONS) {
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println("최대 접속 인원수를 초과했습니다.");
					out.close();
					socket.close();
					continue; // 접속을 허용하지 않고 다음 루프로 이동합니다.
				}

				/*
				 * 클라이언트 1명당 하나의 쓰레드가 생성되어 메세지 전송 및 수신을 담당한다.
				 */
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

	/* 인스턴스 생성 후 초기화 메서드를 호출한다. */
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}

	/*
	 * 접속된 모든 클라이언트 측으로 서버의 메세지를 Echo 해주는 역할을 수행한다.(이전 단계에서는 보낸 사람에게만 Echo 되었다)
	 */
	public void sendAllMsg(String name, String msg) {
		/*
		 * Map에 저장된 클라이언트의 key를 얻어온다. key에는 대화명이 저장되어 있다.
		 */
		Iterator<String> it = clientMap.keySet().iterator();

		// 앞에서 얻어온 대화면(key값)의 갯수만큼 반복한다.
		while (it.hasNext()) {
			try {
				// 각 클라이언트의 PrintWriter 인스턴스를 추출한다.
				PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
				/*
				 * 클라이언트에게 메세지를 전달할때 매개변수로 name이 있는 경우와 없는경우를 구분해서 전달한다.
				 */
				if (name.equals("")) {
					/* 입장 혹은 퇴장에서 사용되는 부분 */
					it_out.println(msg);
				} else {
					/* 메세지를 보낼때 사용되는 부분 */
					it_out.println("[" + name + "]" + msg);
				}
			} catch (Exception e) {
				System.out.println("예외:" + e);
			}
		}
	}

	// 귓속말 전송 : 발신자대화명, 메세지, 수신자 대화명
	public void sendAllMsg(String name, String msg, String receiveName) {
		Iterator<String> it = clientMap.keySet().iterator();

		while (it.hasNext()) {
			try {
				/*
				 * HashMap에는 Key로 대화명, Value로 PrintWriter 인스턴스가 저장되어 있다.
				 */
				String clientName = it.next();
				PrintWriter it_out = (PrintWriter) clientMap.get(clientName);

				/*
				 * 해당 루프에서의 클라이언트 이름과 귓속말을 받을 사람의 대화명이 일치하는지 확인한다.
				 */
				if (clientName.equals(receiveName)) {
					// 일치하면 한 사람에게만 귓속말을 보낸다.
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

		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (Exception e) {
				System.out.println("예외:" + e);
			}
		}

		@Override
		public void run() {
			String name = "";
			String s = "";

			try {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

				// 첫번째 메세지는 대화명이므로 접속을 알린다.
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

				// 두번째 메세지부터는 "대화내용"
				while (in != null) {
					s = in.readLine();

					if (s.trim().isEmpty()) // 사용자가 아무것도 입력하지 않은 경우를 체크합니다.
						continue; // 아무것도 하지 않고 다음 루프로 넘어갑니다.

					// 입력 받은 문자열을 공백으로 분리하여 각 단어를 검사
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
					// 서버의 콘솔에는 메세지를 그대로 출력한다.
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

					/*
					 * 귓속말형식 => /to 수신자명 대화내용 블라블라
					 */
					if (s.charAt(0) == '/') {
						// 슬러쉬로 시작하면 명령어로 판단
						/*
						 * split() 으로 문자열을 분리한다. 여기서 사용하는 구분자는 스페이스 이다.
						 */
						String[] strArr = s.split(" ");
						/*
						 * 문자열을 스페이스로 분리하면 0번 인덱스는 명령어, 1번 인덱스는 수신사 대화명이 되고 2번 인덱스부터 끝까지는 대화내용이 되므로 아래와
						 * 같이 문자열 처리를 해야한다.
						 */
						String msgContent = "";
						for (int i = 2; i < strArr.length; i++) {
							msgContent += strArr[i] + " ";
						}
						/*
						 * 명령어가 /to가 맞는지 확인한다. 명령어에 대한 오타가 있을수도 이쏙, 다른 명령어 일수도 있기때문이다.
						 */
						if (strArr[0].equals("/to")) {
							// 귓속말을 보낸다.
							/*
							 * 기존의 메서드를 오버로딩해서 추가 정의한다. 매개변수는 발신대화명, 메세지, 수신대화명 형태로 작성한다.
							 */
							sendAllMsg(name, msgContent, strArr[1]);

						}
					} else {
						// 슬러쉬가 없다면 일반 대화내용
						sendAllMsg(name, s);
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
