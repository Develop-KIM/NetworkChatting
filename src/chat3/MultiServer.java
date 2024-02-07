package chat3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	static ServerSocket serverSocket = null;
	static Socket socket = null;
	static PrintWriter out = null;
	static BufferedReader in = null;
	static String s = "";

	// 생성자
	public MultiServer() {
		// 실행부 없음
	}

	// 서버의 초기화를 담당할 메서드로 main()의 모든 내용이 포함되어있다.
	public static void init() {
		// 클라이언트의 이름을 저장
		String name = "";

		try {
			// 클라이언트의 접속 대기
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			// 접속 허가
			socket = serverSocket.accept();
			System.out.println(socket.getInetAddress() + ":" + socket.getPort());
			// 스트림 생성
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// 클라이언트의 최초 메세지인 대화명을 Echo
			if (in != null) {
				name = in.readLine();
				System.out.println(name + " 접속");
				out.println("> " + name + "님이 접속했습니다.");
			}

			// 클라이언트의 메세지를 지속적으로 읽어 Echo
			while (in != null) {
				s = in.readLine();
				if (s == null) {
					break;
				}
				System.out.println(name + " ==> " + s);
				// 클라이언트 측으로 Echo 할 때 메서드를 호출한다.
				sendAllMsg(name, s);
			}
			// 루프를 탈출하면 프로그램 종료
			System.out.println("Bye...!!!");
		} catch (Exception e) {
			System.out.println("예외1: " + e);
			// e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
				serverSocket.close();
			} catch (Exception e) {
				System.out.println("예외2: " + e);
			}
		}
	}

	// 서버가 클라이언트 측으로 메세지를 Echo 해준다.
	public static void sendAllMsg(String name, String msg) {
		try {
			out.println(">  " + name + " ==> " + msg);
		} catch (Exception e) {
			System.out.println("예외: " + e);
		}
	}

	// main()은 프로그램의 출방점의 역할만 하는 게 좋다.
	public static void main(String[] args) {
		init();
	}

}
