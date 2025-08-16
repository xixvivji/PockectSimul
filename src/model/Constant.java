package model;

import java.awt.*;

/**
 * 시뮬레이터 내에서 사용되는 상수 모음
 */
public class Constant {
    public static final Color BALL_COLOR[] = {Color.WHITE, Color.YELLOW, Color.RED, Color.PINK, Color.GREEN, Color.BLACK};
    public static final double HOLE_SIZE = 8f;

    public static final double POWER_UNIT = 0.1f;

    public static final double TABLE_COR = 0.8;

    public static final double FRICTION = 0.01;

    public static final int MAX_FOUL = 3;

    public static final int FPS = 60;

    public static final int SIZE_UNIT = 5;
    public static final int TABLE_WIDTH = 254;
    public static final int TABLE_HEIGHT = 127;
    public static final double VELOC_BOUND = 0.003f;
    public static final int SKIP_TICKS = 1000/FPS;
    public static final double MAX_POWER = 100f;
    public static final double MIN_POWER = 0f;
}