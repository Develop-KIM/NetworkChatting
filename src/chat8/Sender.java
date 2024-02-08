package chat8;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// 클라이언트가 입력한 메세지를 서버로 전송해주는 쓰레드 클래스
public class Sender extends Thread {
	// 멤버변수
	Socket socket;
	PrintWriter out = null;
	String name;

	// 생성자 : 클라이언트가 서버에 접속시 생성했던 Socket 인스턴스를 기반으로 출력스트림을 생성한다.
	public Sender(Socket socket, String name) {
		this.socket = socket;
		try {
			// 스트림생성 및 대화명을 초기화한다.
			out = new PrintWriter(this.socket.getOutputStream(), true);
			this.name = name;
		} catch (Exception e) {
			System.out.println("예외 > Sender > 생성자: " + e);
		}
	}

	@Override
	public void run() {
		Scanner s = new Scanner(System.in);

		try {
			// 최초 서버로 전송하는 메세지는 대화명
			out.println(name);

			// 두 번째부터는 메세지이므로 입력내용을 서버로 전송한다.
			while (out != null) {
				try {
					String s2 = s.nextLine();
					// 문자열 비교시 대소문자를 구분하지 않는다.
					if (s2.equalsIgnoreCase("Q")) {
						// q가 입력되면 while 루프 탈출
						break;
					} else {
						// 나머지는 서버로 즉시 전송한다.
						out.println(s2);
					}
				} catch (Exception e) {
					System.out.println("예외 > Sender > run1: " + e);
				}
			}
			out.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("예외 > Sender > run2: " + e);
		}
	}
}
