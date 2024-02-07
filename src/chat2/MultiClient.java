package chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {
		// 클라이언트의 대화명을 입력한다.
		System.out.print("이름을 입력하세요: ");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		// 통신을 위해 IO스트림 선언
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			// 인수의 유무에 따라 IP주소를 설정
			String ServerIP = "localhost";
			if (args.length > 0) {
				ServerIP = args[0];
			}
			// 서버로 접속 요청
			Socket socket = new Socket(ServerIP, 9999);
			// 서버가 accept() 하면 연결 성공
			System.out.println("서버와 연결되었습니다...");

			// 입출력 스트림 인스턴스 생성
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// 클라이언트의 "대화명"을 제일 먼저 서버로 전송한다.
			out.println(s_name);
			/*
			 * 소켓이 Close 되기 전이라면 클라이언트는 지속적으로 서버측으로 메세지를 전송할 수 있다.
			 */
			while (out != null) {
				try {
					// 서버가 Echo해준 내용을 라인단위로 읽어와서 콘솔에 출력한다.
					if (in != null) {
						System.out.println("Receive: " + in.readLine());
					}

					// 서버로 전송할 메세지를 입력하기 위한 대기상태
					String s2 = scanner.nextLine();
					// 만약 q를 입력하면 즉시 while 루프를 탈출하여 클라이언트의 자원을 해제한 후 종료한다.
					if (s2.equals("q") || s2.equals("Q")) {
						break;
					} else {
						// q가 아니라면 서버가 메세지를 전송한다.
						out.println(s2);
					}

				} catch (Exception e) {
					System.out.println("예외: " + e);
				}
			}
			// 소켓과 IO스트림을 자원해제한다.
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("예외발생[MultiClient]" + e);
		}
	}

}
