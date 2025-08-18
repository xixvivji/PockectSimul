import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main_Test {

    // 닉네임을 사용자에 맞게 변경해 주세요.
    static final String NICKNAME = "xix";

    // 일타싸피 프로그램을 로컬에서 실행할 경우 변경하지 않습니다.
    static final String HOST = "127.0.0.1";

    // 일타싸피 프로그램과 통신할 때 사용하는 코드값으로 변경하지 않습니다.
    static final int PORT = 1447;
    static final int CODE_SEND = 9901;
    static final int CODE_REQUEST = 9902;
    static final int SIGNAL_ORDER = 9908;
    static final int SIGNAL_CLOSE = 9909;

    // 게임 환경에 대한 상수입니다.
    static final int TABLE_WIDTH = 254;
    static final int TABLE_HEIGHT = 127;
    static final int NUMBER_OF_BALLS = 6;
    static final int[][] HOLES = {{0, 0}, {127, 0}, {254, 0}, {0, 127}, {127, 127}, {254, 127}};
    static final double BALL_RADIUS = 5.73 / 2; // 공의 반지름

    public static void main(String[] args) {

        Socket socket = null;
        String recv_data = null;
        byte[] bytes = new byte[1024];
        float[][] balls = new float[NUMBER_OF_BALLS][2];
        int order = 0;

        // 현재 목표 공 번호 초기화
        int nowTargetBallNumber = 1;

        try {
            socket = new Socket();
            System.out.println("Trying Connect: " + HOST + ":" + PORT);
            socket.connect(new InetSocketAddress(HOST, PORT));
            System.out.println("Connected: " + HOST + ":" + PORT);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            String send_data = CODE_SEND + "/" + NICKNAME + "/";
            bytes = send_data.getBytes("UTF-8");
            os.write(bytes);
            os.flush();
            System.out.println("Ready to play!\n--------------------");

            while (socket != null) {

                // Receive Data
                bytes = new byte[1024];
                int count_byte = is.read(bytes);
                recv_data = new String(bytes, 0, count_byte, "UTF-8");
                System.out.println("Data Received: " + recv_data);

                // Read Game Data
                String[] split_data = recv_data.split("/");
                int idx = 0;
                try {
                    for (int i = 0; i < NUMBER_OF_BALLS; i++) {
                        for (int j = 0; j < 2; j++) {
                            balls[i][j] = Float.parseFloat(split_data[idx++]);
                        }
                    }
                } catch (Exception e) {
                    bytes = (CODE_REQUEST + "/" + CODE_REQUEST).getBytes("UTF-8");
                    os.write(bytes);
                    os.flush();
                    System.out.println("Received Data has been currupted, Resend Requested.");
                    continue;
                }

                // Check Signal for Player Order or Close Connection
                if (balls[0][0] == SIGNAL_ORDER) {
                    order = (int) balls[0][1];
                    // 초기 목표 공 설정 (선공이면 1, 후공이면 2)
                    System.out.println("\n* You will be the " + (order == 1 ? "first" : "second") +
                            " player. *\n");
                    continue;
                } else if (balls[0][0] == SIGNAL_CLOSE) {
                    break;
                }

                // Show Balls' Position
                for (int i = 0; i < NUMBER_OF_BALLS; i++) {
                    System.out.println("Ball " + i + ": " + balls[i][0] + ", " + balls[i][1]);
                }

                float angle = 0.0f;
                float power = 0.0f;

                //////////////////////////////
                // 이 위는 일타싸피와 통신하여 데이터를 주고 받기 위해 작성된 부분이므로 수정하면 안됩니다.

                // 현재 목표 공이 포켓에 들어갔으면 다음 목표 공으로 변경
                nowTargetBallNumber = updateTargetBall(balls, nowTargetBallNumber, order);

                // 흰 공의 좌표
                float whiteBall_x = balls[0][0];
                float whiteBall_y = balls[0][1];

                // 목적구의 좌표
                if (nowTargetBallNumber < NUMBER_OF_BALLS &&
                        isValidBall(balls[nowTargetBallNumber][0], balls[nowTargetBallNumber][1])) {

                    float targetBall_x = balls[nowTargetBallNumber][0];
                    float targetBall_y = balls[nowTargetBallNumber][1];

                    // 목표 공과 가장 가까운 포켓 찾기
                    int targetHoleIdx = findNearestHole(targetBall_x, targetBall_y);
                    float hole_x = HOLES[targetHoleIdx][0];
                    float hole_y = HOLES[targetHoleIdx][1];

                    // 목표 공을 포켓에 넣기 위한 각도 계산
                    float dx_target_hole = hole_x - targetBall_x;
                    float dy_target_hole = hole_y - targetBall_y;
                    float dist_target_hole = (float) Math.hypot(dx_target_hole, dy_target_hole);

                    // 조건문 없이 항상 일반적인 방식으로 타격점 계산
                    float hitPoint_x = targetBall_x -
                            (dx_target_hole / dist_target_hole) * (float) (BALL_RADIUS * 2);
                    float hitPoint_y = targetBall_y -
                            (dy_target_hole / dist_target_hole) * (float) (BALL_RADIUS * 2);

                    // 흰 공에서 타격 지점까지의 벡터
                    float dx_white_hit = hitPoint_x - whiteBall_x;
                    float dy_white_hit = hitPoint_y - whiteBall_y;

                    // 각도 계산
                    double radian = Math.atan2(dy_white_hit, dx_white_hit);
                    angle = (float) Math.toDegrees(radian);

                    // 각도를 0-360도 범위로 변환 (북쪽을 0도로)
                    angle = 90 - angle;
                    if (angle < 0) {
                        angle += 360;
                    }
                    if (angle >= 360) {
                        angle -= 360;
                    }

                    // 거리 계산 및 파워 결정
                    double distance = Math.hypot(dx_white_hit, dy_white_hit);
                    power = (float) (distance * 1.2 + 20);
                    power = Math.max(20f, Math.min(power, 100f));

                    System.out.println("Shot angle: " + angle + ", power: " + power);
                }

                // 주어진 데이터(공의 좌표)를 활용하여 두 개의 값을 최종 결정하고 나면,
                // 나머지 코드에서 일타싸피로 값을 보내 자동으로 플레이를 진행하게 합니다.
                //   - angle: 흰 공을 때려서 보낼 방향(각도)
                //   - power: 흰 공을 때릴 힘의 세기
                //
                // 이 때 주의할 점은 power는 100을 초과할 수 없으며,
                // power = 0인 경우 힘이 제로(0)이므로 아무런 반응이 나타나지 않습니다.
                //
                // 아래는 일타싸피와 통신하는 나머지 부분이므로 수정하면 안됩니다.
                //////////////////////////////

                String merged_data = angle + "/" + power + "/";
                bytes = merged_data.getBytes("UTF-8");
                os.write(bytes);
                os.flush();
                System.out.println("Data Sent: " + merged_data);
            }

            os.close();
            is.close();
            socket.close();
            System.out.println("Connection Closed.\n--------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 포켓에 들어간 목표 공을 확인하고 다음 목표 공으로 업데이트
     */
    private static int updateTargetBall(float[][] balls, int currentTarget, int order) {
        int nextTarget = currentTarget;

        // 현재 목표 공이 포켓에 들어갔으면 다음 공으로
        if (currentTarget < NUMBER_OF_BALLS &&
                !isValidBall(balls[currentTarget][0], balls[currentTarget][1])) {
            if (order == 1) { // 선공: 홀수 공(1,3,5)
                for (int i = 1; i < NUMBER_OF_BALLS; i += 2) {
                    if (isValidBall(balls[i][0], balls[i][1])) {
                        return i;
                    }
                }
            } else { // 후공: 짝수 공(2,4) 및 8번(5) 공
                for (int i = 2; i < NUMBER_OF_BALLS; i += 2) {
                    if (isValidBall(balls[i][0], balls[i][1])) {
                        return i;
                    }
                }
                if (isValidBall(balls[5][0], balls[5][1])) {
                    return 5;
                }
            }
            // 순서에 맞는 공이 없으면 남아있는 공 중 아무거나
            for (int i = 1; i < NUMBER_OF_BALLS; i++) {
                if (isValidBall(balls[i][0], balls[i][1])) {
                    return i;
                }
            }
        }

        return nextTarget;
    }

    /**
     * 공이 유효한지 확인 (포켓에 들어가지 않았는지)
     */
    private static boolean isValidBall(float x, float y) {
        return !((x == 0 && y == 0) || (x == -1 && y == -1));
    }

    /**
     * 목표 공에 가장 가까운 포켓 찾기
     */
    private static int findNearestHole(float x, float y) {
        int nearest = 0;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < HOLES.length; i++) {
            float dx = x - HOLES[i][0];
            float dy = y - HOLES[i][1];
            float dist = (float) Math.hypot(dx, dy);

            if (dist < minDistance) {
                minDistance = dist;
                nearest = i;
            }
        }

        return nearest;
    }
}