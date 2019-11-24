package com.qisejin.rain;

/**
 * 2019-11-24
 *
 * @author zhengliao
 */
public class Drop implements Comparable<Drop>{
    public int x = 0;
    public int y = 0;
    public int r = 0;
    public float spreadX = 0f;
    public float spreadY = 0f;
    public float momentum = 0f;
    public float momentumX = 0f;
    public float lastSpawn = 0f;
    public float nextSpawn = 0f;
    public Drop parent = null;
    public boolean isNew = true;
    public boolean killed = false;
    public float shrink = 0;

    @Override
    public String toString() {
        return "Drop{" +
                "x=" + x +
                ", y=" + y +
                ", r=" + r +
                ", spreadX=" + spreadX +
                ", spreadY=" + spreadY +
                ", momentum=" + momentum +
                ", momentumX=" + momentumX +
                ", lastSpawn=" + lastSpawn +
                ", nextSpawn=" + nextSpawn +
                ", parent=" + parent +
                ", isNew=" + isNew +
                ", killed=" + killed +
                ", shrink=" + shrink +
                '}';
    }


    @Override
    public int compareTo(Drop o) {
        Drop drop1 =  this;
        Drop drop2 =  o;
        float v1 = drop1.y  + drop1.x;
        float v2 = drop2.y  + drop2.x;

        return v1 > v2 ? 1 : v1 == v2 ? 0 : -1;
    }
}
