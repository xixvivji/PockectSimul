package controller;

import model.Ball;
import model.Constant;
import player.Player;
import view.Display;

import java.awt.*;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Game {
	public static Ball[] Balls;
	public static double[][] balls; // [i][0]=x, [i][1]=y

	public Toolkit toolkit = Toolkit.getDefaultToolkit();
	public static final int[][] HOLES = {
			{ 0, 0 }, { 127, 0 }, { 254, 0 },
			{ 0, 127 }, { 127, 127 }, { 254, 127 }
	};

	private final int stage;
	private final boolean hasEightBall;
	private int order = 0;
	private int turnCount = 0;
	private boolean isPlaying;
	private boolean pocketNotObject = false;

	private int[] fouls;
	private int[] playerBallCount; // 플레이어별 남은 목적구 수

	private LocalTime time;

	private Player[] players;
	private final Display display;

	public Game(int stage){
		this.stage = stage;
		this.hasEightBall = (stage >= 5);

		generateBallsByStage(stage);
		generatePlayers(1);

		this.display = new Display("Pocket Ball", Constant.TABLE_WIDTH, Constant.TABLE_HEIGHT, Constant.SIZE_UNIT);
		this.display.setBalls(Balls);

		isPlaying = true;
		turnCount = 2;

		playerBallCount = new int[1];
		playerBallCount[0] = Balls.length - 1 - (hasEightBall ? 1 : 0); // 흰/8 제외

		printStartMessage();
		play();
	}

	private void generateBallsByStage(int stage) {
		// 좌표: 이미지 기준 ‘고정’
		double[][] stageBalls;
		switch(stage) {
			case 1: // White + 1 object
				stageBalls = new double[][] {
						{ 89, 51 },  // White
						{ 230, 24 }  // Obj1
				};
				break;
			case 2:
				stageBalls = new double[][] {
						{ 85, 57 },  // White
						{ 118, 82 }  // Obj1
				};
				break;
			case 3:
				stageBalls = new double[][] {
						{ 100, 55 }, // White
						{ 60, 100 }  // Obj1
				};
				break;
			case 4:
				stageBalls = new double[][] {
						{ 110, 62 }, // White
						{ 40, 28 },  // Obj1
						{ 215, 30 }  // Obj2
				};
				break;
			case 5: // White + 2 obj + 8
				stageBalls = new double[][] {
						{ 100, 63 }, // White
						{ 164, 56 }, // Obj1
						{ 168, 72 }, // Obj2
						{ 178, 63 }  // 8-ball
				};
				break;
			case 6: // White + 4 obj + 8
				stageBalls = new double[][] {
						{ 100, 63 }, // White
						{ 172, 53 }, // Obj1
						{ 166, 64 }, // Obj2
						{ 184, 56 }, // Obj3
						{ 182, 70 }, // Obj4
						{ 175, 63 }  // 8-ball
				};
				break;
			default:
				throw new IllegalArgumentException("스테이지 정의 오류!");
		}

		balls = new double[stageBalls.length][2];
		Balls = new Ball[stageBalls.length];
		for (int i = 0; i < stageBalls.length; i++) {
			balls[i][0] = stageBalls[i][0];
			balls[i][1] = stageBalls[i][1];
			boolean isEight = hasEightBall && (i == stageBalls.length - 1);
			Balls[i] = new Ball(i, balls[i][0], balls[i][1], isEight);
		}
	}

	private void generatePlayers(int playerCount){
		players = new Player[playerCount];
		fouls = new int[playerCount];
		for (int i = 0; i < playerCount; i++) players[i] = new Player(stage, balls);
	}

	private void printStartMessage(){
		System.out.println("--------------- 게임 시작 ---------------");
		for (int i = 0; i < Balls.length; i++){
			if (i == 0) {
				System.out.println(" 0: 흰공");
			} else if (hasEightBall && i == Balls.length - 1) {
				System.out.println(" " + i + ": 8번(검정)");
			} else {
				System.out.println(" " + i + ": 목적구");
			}
		}
		System.out.println("-----------------------------------------");
	}

	private void play() {
		while (isPlaying) {
			time = LocalTime.now();
			System.out.println("[턴] Player:" + (order+1) + "  Shot:" + (turnCount/2) + "  남은 목적구:" + playerBallCount[0]);

			// 흰공 재배치
			if (!Balls[0].isValid()) {
				System.out.println("흰공 재배치 (센터).");
				Balls[0].setValid(true);
				Balls[0].setPos(Constant.TABLE_WIDTH / 2.0, Constant.TABLE_HEIGHT / 2.0);
				balls[0][0] = Balls[0].getX();
				balls[0][1] = Balls[0].getY();
			}

			double angle = players[order].getAngle();
			double power = players[order].getPower();
			if (power > Constant.MAX_POWER) power = Constant.MAX_POWER;
			if (power < Constant.MIN_POWER) power = Constant.MIN_POWER;
			power *= Constant.POWER_UNIT;

			Balls[0].addPower(power, angle);

			boolean isMoving = true;
			int whiteFirstHitIdx = 0;
			int pocketFlag = 0;
			boolean continueOrder = false;
			pocketNotObject = false;

			while (isMoving) {
				isMoving = false;
				if (time.until(LocalTime.now(), ChronoUnit.MILLIS) < Constant.SKIP_TICKS) {
					isMoving = true;
					continue;
				}

				// 다음 위치 계산
				for (Ball b : Balls)
					if (b.isValid()) b.calcNext();

				// 충돌 처리
				for (int i = 0; i < Balls.length; i++) {
					Ball bi = Balls[i];
					if (!bi.isValid()) continue;
					for (int j = i + 1; j < Balls.length; j++) {
						Ball bj = Balls[j];
						if (!bj.isValid()) continue;
						if (bi.collides(bj)) {
							if ((i == 0 || j == 0) && whiteFirstHitIdx == 0) {
								whiteFirstHitIdx = (i == 0 ? j : i);
							}
						}
					}
					bi.collidesTable();
				}

				int u = updatePositionsAndCheckPockets(); // 위치 확정 및 포켓 판정
				if (pocketFlag == 0) pocketFlag = u;
				else if (u < 0 && pocketFlag > 0) pocketFlag = u;

				display.draw();
				time = LocalTime.now();

				for (Ball b : Balls)
					if (b.isMoving()) { isMoving = true; break; }
			}

			// 턴 종료 판정
			if (isPlaying) {
				if (whiteFirstHitIdx == 0) foul("아무 공도 맞히지 못함");
				else if (!isObjectBall(whiteFirstHitIdx)) foul("목적구 아닌 공(" + whiteFirstHitIdx + ") 먼저 접촉");
				else if (pocketNotObject) foul("목적구 아닌 공 포켓");

				if (fouls[order] >= Constant.MAX_FOUL) {
					System.out.println("파울 누적 패배");
					isPlaying = false;
				}

				if (pocketFlag > 0) continueOrder = true;

				if (playerBallCount[0] == 0) {
					if (!hasEightBall) {
						System.out.println("목적구 모두 처리! 승리!");
						isPlaying = false;
					} else {
						System.out.println("8번공만 남았습니다!");
					}
				}

				if (!continueOrder) nextOrder();
			}
			System.out.println("-----------------------------------------");
		}
		System.out.println("게임 종료.");
	}

	private void foul(String msg){
		fouls[order]++;
		System.out.println("파울: " + msg + " (누적 " + fouls[order] + ")");
	}

	private void nextOrder(){
		order = (order + 1) % 1;
		turnCount++;
	}

	private int updatePositionsAndCheckPockets() {
		int pocket = 0;
		for (int i = 0; i < Balls.length; i++) {
			Ball b = Balls[i];
			if (!b.isValid()) continue;
			b.updatePos();
			balls[i][0] = b.getX();
			balls[i][1] = b.getY();
			int h = pocketCheck(i);
			if (pocket == 0) pocket = h;
			else if (h < 0 && pocket > 0) pocket = -1;
		}
		return pocket;
	}

	private boolean isObjectBall(int idx){
		if (idx == 0) return false; // 흰공
		if (hasEightBall && idx == Balls.length - 1) return false; // 8번
		return true;
	}

	private int pocketCheck(int idx){
		Ball b = Balls[idx];
		if (!b.isValid()) return 0;

		double x = b.getX();
		double y = b.getY();
		for (int[] hole : HOLES) {
			double dx = x - hole[0];
			double dy = y - hole[1];
			if (Math.sqrt(dx*dx + dy*dy) < Constant.HOLE_SIZE) {
				System.out.printf("%d번 공 포켓!\n", idx);
				b.setValid(false);
				b.setVeloc(0,0);
				b.setPos(0,0);
				balls[idx][0] = balls[idx][1] = 0;

				if (!isObjectBall(idx)) {
					// 8번 조기 포켓 패배 조건
					if (hasEightBall && idx == Balls.length - 1 && playerBallCount[0] != 0) {
						System.out.println("목적구 남은 상태에서 8번 포켓 -> 패배");
						isPlaying = false;
					} else if (idx != 0) {
						// (현재 설계상 발생 X) placeholder 없음
						pocketNotObject = true;
					}
					return -1;
				}

				// 목적구 정상 포켓
				if (playerBallCount[0] > 0) playerBallCount[0]--;
				if (hasEightBall && playerBallCount[0] == 0 && idx != Balls.length -1) {
					System.out.println("이제 8번공만 남았습니다.");
				}
				return 1;
			}
		}
		return 0;
	}
}