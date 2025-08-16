package player;

/**
 * 당구 게임의 AI 플레이어를 구현하는 클래스
 * 일타싸피 로직 적용 - 시뮬레이터 최적화 버전
 */
public class Player {
	private final int stage;
	private final double[][] balls;

	// 공 반지름 및 지름
	private final double BALL_RADIUS = 5.73 / 2;
	private final double BALL_DIAMETER = 5.73;

	// 포켓 위치 정보
	private final int[][] HOLES = {
			{ 0, 0 }, { 127, 0 }, { 254, 0 },
			{ 0, 127 }, { 127, 127 }, { 254, 127 }
	};

	// 현재 목표 공 번호
	private int nowTargetBallNumber = 1;

	// 디버깅용 변수들
	private double lastHitPointX = 0;
	private double lastHitPointY = 0;
	private int lastTargetHole = 0;

	public Player(int stage, double[][] balls) {
		this.stage = stage;
		this.balls = balls;

		// 스테이지에 따라 선공/후공 설정
		int order = (stage == 6) ? 2 : 1;
		nowTargetBallNumber = order;

		System.out.println("[Player] 초기화 - 스테이지: " + stage + ", 공 개수: " + balls.length);
	}

	/**
	 * 흰 공을 칠 각도를 결정합니다.
	 * @return 각도 (0-360도)
	 */
	public double getAngle() {
		// 현재 목표 공이 포켓에 들어갔으면 다음 목표 공으로 변경
		nowTargetBallNumber = updateTargetBall(nowTargetBallNumber);

		// 흰 공의 좌표
		double whiteBall_x = balls[0][0];
		double whiteBall_y = balls[0][1];

		// 기본 각도
		double angle = 0.0;

		// 목적구의 좌표
		if (nowTargetBallNumber < balls.length && isValidBall(nowTargetBallNumber)) {
			double targetBall_x = balls[nowTargetBallNumber][0];
			double targetBall_y = balls[nowTargetBallNumber][1];

			System.out.println("[Player] 목표 공: " + nowTargetBallNumber + " 위치: (" + targetBall_x + ", " + targetBall_y + ")");
			System.out.println("[Player] 흰 공 위치: (" + whiteBall_x + ", " + whiteBall_y + ")");

			// 목표 공과 가장 가까운 포켓 찾기
			int targetHoleIdx = findBestPocket(targetBall_x, targetBall_y);
			double hole_x = HOLES[targetHoleIdx][0];
			double hole_y = HOLES[targetHoleIdx][1];
			lastTargetHole = targetHoleIdx;

			System.out.println("[Player] 타겟 포켓: " + targetHoleIdx + " 위치: (" + hole_x + ", " + hole_y + ")");

			// 목표 공을 포켓에 넣기 위한 벡터 계산
			double dx_target_hole = hole_x - targetBall_x;
			double dy_target_hole = hole_y - targetBall_y;
			double dist_target_hole = Math.sqrt(dx_target_hole * dx_target_hole + dy_target_hole * dy_target_hole);

			// 목적구가 포켓에 너무 가까우면 hitPoint를 목적구 위치로 설정
			double hitPoint_x, hitPoint_y;
			if (dist_target_hole < BALL_DIAMETER) {
				hitPoint_x = targetBall_x;
				hitPoint_y = targetBall_y;
			} else {
				// 목표 공에서 포켓을 향하는 단위 벡터의 반대 방향으로 2반지름 거리의 지점
				hitPoint_x = targetBall_x - (dx_target_hole / dist_target_hole) * BALL_DIAMETER;
				hitPoint_y = targetBall_y - (dy_target_hole / dist_target_hole) * BALL_DIAMETER;
			}

			lastHitPointX = hitPoint_x;
			lastHitPointY = hitPoint_y;

			System.out.println("[Player] 타격 지점: (" + hitPoint_x + ", " + hitPoint_y + ")");

			// 흰 공에서 타격 지점까지의 벡터
			double dx_white_hit = hitPoint_x - whiteBall_x;
			double dy_white_hit = hitPoint_y - whiteBall_y;

			// 각도 계산 - 시뮬레이터 좌표계에 맞게 조정
			double radian = Math.atan2(dy_white_hit, dx_white_hit);
			angle = Math.toDegrees(radian);

			// 시뮬레이터 각도 조정 (시뮬레이터에 맞게 수정)
			// 시뮬레이터에서는 동쪽이 0도로 추측됨

			// 각도 보정 - 스테이지별 특별 처리
			if (stage == 1) {
				// 스테이지 1은 각도 조정이 덜 필요
				angle += 0; // 필요시 미세 조정
			} else if (stage == 2 || stage == 3) {
				// 스테이지 2, 3은 약간 더 조정 필요
				angle += 0; // 필요시 미세 조정
			}

			System.out.println("[Player] 계산된 각도: " + angle + "도");
		} else {
			System.out.println("[Player] 유효한 목표 공이 없습니다.");
		}

		return angle;
	}

	/**
	 * 흰 공을 칠 파워를 결정합니다.
	 * @return 파워 (0-100)
	 */
	public double getPower() {
		// 흰 공의 좌표
		double whiteBall_x = balls[0][0];
		double whiteBall_y = balls[0][1];

		// 기본 파워
		double power = 50.0; // 기본값 설정

		// 목적구의 좌표
		if (nowTargetBallNumber < balls.length && isValidBall(nowTargetBallNumber)) {
			// 흰 공에서 타격 지점까지의 벡터
			double dx_white_hit = lastHitPointX - whiteBall_x;
			double dy_white_hit = lastHitPointY - whiteBall_y;

			// 거리 계산
			double distance = Math.sqrt(dx_white_hit * dx_white_hit + dy_white_hit * dy_white_hit);

			// 시뮬레이터에 맞게 파워 조정
			// 원래: power = distance * 1.2 + 20;
			if (stage == 1) {
				// 스테이지 1: 약간 더 강한 파워
				power = distance * 1.5 + 35;
			} else {
				// 다른 스테이지: 기본 파워
				power = distance * 1.5 + 30;
			}

			// 특정 포켓에 대한 파워 조정
			if (lastTargetHole == 0 || lastTargetHole == 3) {
				// 왼쪽 포켓은 더 약한 파워
				power *= 0.9;
			} else if (lastTargetHole == 2 || lastTargetHole == 5) {
				// 오른쪽 포켓은 더 강한 파워
				power *= 1.1;
			}

			// 파워 제한
			power = Math.max(30.0, Math.min(power, 90.0));

			System.out.println("[Player] 거리: " + distance + ", 계산된 파워: " + power);
		}

		return power;
	}

	/**
	 * 목표 공에 가장 적합한 포켓을 찾습니다.
	 */
	private int findBestPocket(double x, double y) {
		// 기본 전략: 가장 가까운 포켓 선택
		int nearest = 0;
		double minDistance = Double.MAX_VALUE;

		for (int i = 0; i < HOLES.length; i++) {
			double dx = x - HOLES[i][0];
			double dy = y - HOLES[i][1];
			double dist = Math.sqrt(dx * dx + dy * dy);

			if (dist < minDistance) {
				minDistance = dist;
				nearest = i;
			}
		}

		// 스테이지 1에서는 특정 포켓을 선호 (왼쪽 위 또는 오른쪽 위)
		if (stage == 1) {
			double targetBall_x = balls[nowTargetBallNumber][0];

			// 목표 공이 오른쪽에 있으면 오른쪽 위 포켓(2)을 선호
			if (targetBall_x > 127) {
				return 2; // 오른쪽 위 포켓
			}
			// 왼쪽에 있으면 왼쪽 위 포켓(0)을 선호
			else {
				return 0; // 왼쪽 위 포켓
			}
		}

		return nearest;
	}

	/**
	 * 포켓에 들어간 목표 공을 확인하고 다음 목표 공으로 업데이트
	 */
	private int updateTargetBall(int currentTarget) {
		int nextTarget = currentTarget;
		int order = (stage == 6) ? 2 : 1; // 스테이지 6에서는 후공으로 시작

		// 현재 목표 공이 포켓에 들어갔으면 다음 공으로
		if (currentTarget < balls.length && !isValidBall(currentTarget)) {
			System.out.println("[Player] 목표 공 " + currentTarget + "이(가) 포켓에 들어감, 다음 공 탐색");

			if (order == 1) { // 선공: 홀수 공(1,3,5)
				for (int i = 1; i < balls.length; i += 2) {
					if (isValidBall(i)) return i;
				}
			} else { // 후공: 짝수 공(2,4) 및 8번(5) 공
				for (int i = 2; i < balls.length; i += 2) {
					if (isValidBall(i)) return i;
				}
				if (balls.length > 5 && isValidBall(5)) return 5;
			}

			// 순서에 맞는 공이 없으면 남아있는 공 중 아무거나
			for (int i = 1; i < balls.length; i++) {
				if (isValidBall(i)) return i;
			}
		}

		return nextTarget;
	}

	/**
	 * 공이 유효한지 확인 (포켓에 들어가지 않았는지)
	 */
	private boolean isValidBall(int ballIdx) {
		if (ballIdx < 0 || ballIdx >= balls.length) return false;
		double x = balls[ballIdx][0];
		double y = balls[ballIdx][1];
		return !((x == 0 && y == 0) || (x == -1 && y == -1));
	}

	/**
	 * 목표 공에 가장 가까운 포켓 찾기
	 */
	private int findNearestHole(double x, double y) {
		int nearest = 0;
		double minDistance = Double.MAX_VALUE;

		for (int i = 0; i < HOLES.length; i++) {
			double dx = x - HOLES[i][0];
			double dy = y - HOLES[i][1];
			double dist = Math.sqrt(dx * dx + dy * dy);

			if (dist < minDistance) {
				minDistance = dist;
				nearest = i;
			}
		}

		return nearest;
	}
}