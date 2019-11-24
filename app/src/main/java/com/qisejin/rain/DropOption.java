package com.qisejin.rain;

/**
 * 2019-11-12
 *
 * @author zhengliao
 */
public class DropOption {
    private int minR = 40;
    private int maxR = 80;
    private int maxDrops = 900;
    private int maxDropletsCount = maxDrops * 3;
    private float rainChance = 0.35f;
    private int rainLimit = 6;
    //1帧50个
    private int dropletsRate = 50;
    private float dropletsCleaningRadiusMultiplier = 0.56f;
    private boolean raining = true;
    private float globalTimeScale = 1f;
    private int trailRate = 1;
    private boolean autoShrink = true;
    private float[] spawnArea = {-0.1f, 0.95f};
    private float[] trailScaleRange = {0.2f, 0.5f};
    private float collisionRadius = 0.65f;
    private float collisionRadiusIncrease = 0.02f;
    private float dropFallMultiplier = 2.0f;
    private float collisionBoostMultiplier = 0.05f;
    private int collisionBoost = 1;
    private int dropletsCounter = 0;
}
