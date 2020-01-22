package com.qisejin.rain;

/**
 * 2019-11-12
 *
 * @author zhengliao
 */
public class RainOption {
    int minR = 40;
    int maxR = 80;
    int maxDrops = 900;
    int maxDropletsCount = maxDrops * 3;
    float rainChance = 0.35f;
    int rainLimit = 6;
    int dropletsRate = 50;
    float dropletsCleaningRadiusMultiplier = 0.56f;
    boolean raining = true;
    int trailRate = 1;
    boolean autoShrink = true;
    float[] spawnArea = {-0.1f, 0.95f};
    float[] trailScaleRange = {0.2f, 0.5f};
    float collisionRadius = 0.65f;
    float collisionRadiusIncrease = 0.02f;
    float dropFallMultiplier = 2.0f;
    float collisionBoostMultiplier = 0.05f;
    int collisionBoost = 1;
}
