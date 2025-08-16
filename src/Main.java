import controller.Game;
import java.util.Scanner;

/**
 * 일타싸피 게임 시뮬레이션의 메인 클래스
 *
 * [학생용 과제 안내]
 * 1. Player 클래스의 getAngle()과 getPower() 메소드를 구현하세요.
 * 2. 각 스테이지별 목표:
 *    - 스테이지 1-3: 한 개의 목적구를 2턴 이하로 포켓에 넣기
 *    - 스테이지 4: 두 개의 목적구를 순서대로 4턴 이하로 포켓에 넣기
 *    - 스테이지 5: 세 개의 목적구를 6턴 이하로 포켓에 넣기
 *    - 스테이지 6: 경쟁 모드에서 승리 조건 달성하기
 * 3. 주의사항:
 *    - 당구대 크기는 254x127cm
 *    - 공의 직경은 5.73cm
 *    - 포켓 크기는 직경 8cm
 */
public class Main {
	public static void main(String[] args) throws Exception{
		Scanner sc = new Scanner(System.in);

		System.out.println("===== 일타싸피 포켓볼 시뮬레이션 =====");
		System.out.println("플레이할 스테이지 번호(1~6)를 입력해주세요:");
		int stage = sc.nextInt();

		if (stage < 1 || stage > 6) {
			System.out.println("올바른 스테이지 번호(1~6)만 입력하세요.");
			return;
		}

		System.out.println("스테이지 " + stage + "를 시작합니다.");
		Game game = new Game(stage); // 스테이지 기반 게임 생성
	}
}