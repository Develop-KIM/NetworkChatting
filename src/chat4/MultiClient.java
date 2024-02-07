package chat4;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {
		// 대화명 입력
		System.out.print("이름을 입력하세요: ");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		// 스트림 생성시 Receiver 클래스로 옮겨진 입력스트림은 제외
		PrintWriter out = null;
		// BufferedReader in = null;

		try {
			// 아이피 설정
			String ServerIP = "localhost";
			if (args.length > 0) {
				ServerIP = args[0];
			}
			// 서버에 접속
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다...");
			/*
			 * 서버가 보내는 메세지를 읽을 Receiver클래스를 통한 쓰레드 인스턴스를 생성한다. 이때 매개변수를 통해 클라이언트가 접속시 사용한
			 * socket인스턴스를 전달한다.
			 */
			Thread receiver = new Receiver(socket);
			// setDaemon(true); => 이와 같이 선언하지 않았으므로 독립쓰레드로 생성된다.
			// 바로 쓰레드를 시작한다.
			receiver.start();
			// output 스트림을 생성한 후 대화명을 서버로 전송한다.
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println(s_name);
			// 대화명 이후에는 메세지를 전송한다.
			while (out != null) {
				try {
					String s2 = scanner.nextLine();
					if (s2.equals("q") || s2.equals("Q")) {
						break;
					} else {
						out.println(s2);
					}
				} catch (Exception e) {
					System.out.println("예외: " + e);
				}
			}
			out.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("예외발생[MultiClient]" + e);
		}
	}

}
