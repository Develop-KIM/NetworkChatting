package chat1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {

		// 클라이언트는 최초 접속시 대화명을 입력한다.
		System.out.print("이름을 입력하세요: ");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		// 서버와의 통신을 위해 IO스트림 인스턴스 변수 선언
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			/*
			 * 클라이언트 실행시 별도의 접속IP가 없으면 localhost(루프백)으로 고정된다. 만약 IP주소를 지정하고 싶다면 다음과 같이 하면된다.
			 * c:/bin> java chat1.MultiClient IP주소
			 */
			String ServerIP = "localhost";
			// 실행시 인수가 있는 상태라면 해당 주소로 설정한다.
			if (args.length > 0) {
				ServerIP = args[0];
			}
			/*
			 * IP주소와 Port를 기반으로 Socket 인스턴스를 생성하여 서버에 접속요청을 한다. (이때 서버는 미리 실행되어 있어야 한다.)
			 */
			Socket socket = new Socket(ServerIP, 9999);
			/*
			 * 서버에서 클라이언트의 접속을 허가하기 위해 accept()메서드가 실행되면 접속이 완료된다.
			 */
			System.out.println("서버와 연결되었습니다...");
			/*
			 * 서버가 보내는 내용을 읽고, 서버로 메세지를 보낼 때 사용하는 IO(입출력) 스트림을 생성한다. 여기서 사용되는 클래스는 바이트스트림과
			 * 문자스트림의 상호변환을 제공하는 입출력스트림으로 읽어 지정된 문자 인코딩에 따라 문자로 변환하는데 사용된다.
			 */
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// 대화명을 서버측으로 출력스트림을 통해 전송한다.
			out.println(s_name);
			/* 서버에 보내준(Echo해준) 메세지를 라인단위로 읽어 콘솔에 출력한다. */
			System.out.println("Receive: " + in.readLine());

			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.out.println("예외발생[MultiClient]" + e);
		}
	}

}
