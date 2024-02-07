package chat6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiServer {

	// 멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	// 클라이언트 정보를 저장하기 위한 Map 컬렉션 생성
	Map<String, PrintWriter> clientMap;

	// 생성자
	public MultiServer() {
		/*
		 * 클라이언트의 이름과 접속시 생성한 출력스트림을 저장할 HashMap 인스턴스 생성
		 */
		clientMap = new HashMap<String, PrintWriter>();
		/*
		 * HashMap 동기화 설정. 쓰레드가 사용자 정보에 동시접근하는 것을 차단한다.
		 */
		Collections.synchronizedMap(clientMap);
	}

	public void init() {
		try {
			// 서버소켓 생성
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			/*
			 * 1명의 클라이언트가 접속할 때마다 허용해주고 동시에 쓰레드를 생성한다.
			 */
			while (true) {
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress() + "의" + socket.getPort() + "포트에 " + socket.getLocalAddress()
						+ "의 " + socket.getLocalPort() + "포트로 연결되었습니다.");
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

	/*
	 * 인스턴스 생성 후 초기화 메서드를 호출한다.
	 */
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}

	/*
	 * 접속된 모든 클라이언트 측으로 서버의 메세지를 Echo 해주는 역할을 수행한다. (이전 단계에서는 보낸 사람에게만 Echo 되었다.)
	 */
	public void sendAllMsg(String name, String msg) {
		/*
		 * Map에 저장된 클라이언트의 key를 얻어온다. key에는 대화명이 저장되어 있다.
		 */
		Iterator<String> it = clientMap.keySet().iterator();
		// 앞에서 얻어온 대화명(key값)의 갯수만큼 반복한다.
		while (it.hasNext()) {
			try {
				// 각 클라이언트의 PrintWriter 인스턴스를 추출한다.
				PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
				/*
				 * 클라이언트에게 메세지를 전달할 때 매개변수로 name이 있는 경우와 없는 경우를 구분해서 전달한다.
				 */
				if (name.equals("")) {
					/*
					 * 입장 혹은 퇴장에서 사용되는 부분
					 */
					it_out.println(msg);
				} else {
					/*
					 * 메세지를 보낼 때 사용되는 부분
					 */
					it_out.println("[" + name + "]: " + msg);
				}
			} catch (Exception e) {
				System.out.println("예외: " + e);
			}
		}
	}

	/*
	 * 내부클래스: init()에 기술되었던 스트림을 생성 후 메세지를 읽기/쓰기 하던 부분이 내부클래스로 이동되었다.
	 */
	class MultiServerT extends Thread {
		// 멤버변수
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;

		/*
		 * 내부클래스의 생성자 : 1명의 클라이언트가 접속할 때 생성했던 Socket 인스턴스를 매개변수로 전달받아 이를 기반으로 입출력 스트림을
		 * 생성한다.
		 */
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			} catch (Exception e) {
				System.out.println("예외: " + e);
			}
		}

		/*
		 * 쓰레드로 동작할 run()에서는 클라이언트의 접속자명과 메세지를 지속적으로 읽어 Echo 해주는 역할을 한다.
		 */
		@Override
		public void run() {
			String name = "";
			String s = "";

			try {
				/*
				 * 클라이언트가 보내는 최초 메세지는 대화명이므로 접속에 대한 부분을 출력하고 Echo 한다.
				 */

				name = in.readLine();

				sendAllMsg("", name + "님이 입장하셨습니다.");

				clientMap.put(name, out);

				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는" + clientMap.size() + "명 입니다.");

				/*
				 * 두 번째부터는 실제 메세지이므로 지속적으로 읽어서 Echo 한다.
				 */
				while (in != null) {
					s = in.readLine();
					if (s == null)
						break;

					System.out.println(name + " >> " + s);
					sendAllMsg(name, s);
				}
			} catch (Exception e) {
				System.out.println("예외: " + e);
			} finally {
				/*
				 * try문의 while()문을 탈출하는 것이 해당 쓰레드가 종료된다는 의미이므로 아래와 같이 쓰레드의 이름을 콘솔에 출력한다.
				 */
				clientMap.remove(name);
				sendAllMsg("", name + "님이 퇴장하셨습니다.");
				System.out.println(name + " [" + Thread.currentThread().getName() + "] 퇴장");
				System.out.println("현재 접속자 수는" + clientMap.size() + "명 입니다.");
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