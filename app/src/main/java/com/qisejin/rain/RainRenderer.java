package com.qisejin.rain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 2019/11/7
 *
 * @author zhengliao
 */
public class RainRenderer implements GLSurfaceView.Renderer {

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

    protected Context mContext;
    protected GLSurfaceView glSurfaceView;
    protected float surfaceWidth;
    protected float surfaceHeight;

    protected FloatBuffer mGLCoverTextureBuffer = ByteBuffer.allocateDirect(OpenGLUtils.TEXTURE_ROTATED_REVERSE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected FloatBuffer mGLCubeBuffer = ByteBuffer.allocateDirect(OpenGLUtils.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected FloatBuffer mScaleBuffer;
    protected FloatBuffer mBlueBuffer;

    protected int[] texturesId = new int[2];
    protected int[] fboIds = new int[2];

    private int mDropAlphaId = -1;
    private int dropColorId = -1;
    private int programPoint;
    private int attribPosition;
    private int attributeScale;
    private int attributeBlue;
    private int attributePointSize;
    private int uniformTexture;
    private int uniformTextureDropColor;
    //    private int uniformMVPMatrix;
    protected FloatBuffer pointBuffer;
    protected FloatBuffer pointSizeBuffer;

    private int defaultProgId;
    private int mAttrDefaultPositionId;
    private int mAttrDefaultTextureId;
    private int mUniformTextureId;
    private float mScale = 1.0f;

    private ArrayList<Drop> drops = new ArrayList<>();
    private ArrayList<Drop> dropletsArray = new ArrayList<>();
    private ArrayList<Drop> trailDrops = new ArrayList<>();

    private void updateDroplets(float timeScale) {
        int times = dropletsCounter;
        if (dropletsArray.size() < maxDropletsCount) {
            int count = Math.min(times, maxDropletsCount - dropletsArray.size());
            for (int i = 0; i < count; i++) {
                int x = random((int) (surfaceWidth * mScale));
                int y = random((int) (surfaceHeight * mScale));
                Drop drop = new Drop();
                drop.x = x;
                drop.y = y;
                drop.r = randomSqure(5, 10);
                dropletsArray.add(drop);
            }
        }
    }


    private void addBuffer() {
//        Log.e("zhengliao", "size = " + dropletsArray.size());
        Log.e("zhengliao", "drop size = " + drops.size());
        for (Drop drop : dropletsArray) {
            initScaleAndBlue(drop);
        }
        for (Drop drop : drops) {
            initScaleAndBlue(drop);
        }

    }

    private void initScaleAndBlue(Drop drop) {
        float x = drop.x;
        float y = drop.y;
        float r = drop.r;
        float spreadX = drop.spreadX;
        float spreadY = drop.spreadY;
        float scaleX = 1f;
        float scaleY = 1.5f;
        double d = Math.max(0, Math.min(1, ((r - minR) / getDeltaR() * 0.9f)));
        d *= 1 / ((drop.spreadX + drop.spreadY) * 0.5 + 1);
        int df = (int) Math.floor(d * (254));
        float z = (float) (df / 255);
        float width = ((r * 2 * scaleX * (spreadX + 1)) * 2) * mScale;
        float height = ((r * 2 * scaleY * (spreadY + 1)) * 2) * mScale;

        pointSizeBuffer.put(width);

        mScaleBuffer.put(height / width);
        mScaleBuffer.put(1.0f);

        mBlueBuffer.put(z);

        pointBuffer.put(2 * x / surfaceWidth - 1);
        pointBuffer.put(2 * y / surfaceHeight - 1);
    }

    private float getDeltaR() {
        return maxR - minR;
    }

    private int random(int seed) {

        return (int) (Math.random() * seed);

    }

    private int random(int from, int to) {

        return from + (int) (Math.random() * (to - from));
    }

    private float random(float from, float to) {

        return from + (float) (Math.random() * (to - from));
    }

    private float random(float seed) {
        return (float) (Math.random() * seed);
    }

    private int randomWithPow3(int from, int to) {
        double delta = to - from;

        double fra = Math.pow(Math.random(), 3);
        return from + (int) (fra * delta);

    }

    private int randomSqure(int from, int to) {
        int delta = to - from;
        double fra = Math.random();
        fra = fra * fra;
        return from + (int) (fra * delta);

    }


    private Bitmap dropColor, dropAlpha;

    protected float[] projectionMatrix = new float[16];

    private RainDropLayer mRainDropLayer;

    public RainRenderer(Context mContext, GLSurfaceView glSurfaceView) {
        this.mContext = mContext;
        this.glSurfaceView = glSurfaceView;
        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(OpenGLUtils.CUBE);
        mGLCoverTextureBuffer.clear();
        mGLCoverTextureBuffer.put(OpenGLUtils.TEXTURE_ROTATED_REVERSE);

    }

    protected void clearBuffer() {
        mScaleBuffer.clear();
        mBlueBuffer.clear();
        pointBuffer.clear();
        pointSizeBuffer.clear();
        mScaleBuffer.position(0);
        mBlueBuffer.position(0);
        pointBuffer.position(0);
        pointSizeBuffer.position(0);
    }


    protected void logMatrix() {
        Log.d("Matrix", "-------start log Matrix------");
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < projectionMatrix.length; i++) {
            if (i % 4 == 0) {
                builder.append("\n");
            }
            builder.append(projectionMatrix[i]).append(",");
        }
        builder.append("}");
        builder.append("\n");
        Log.d("Matrix", builder.toString());

        Log.d("Matrix", "-------end log Matrix------");
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1f, 1f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glDisable(GL10.GL_DITHER);
        initShader();
        dropAlpha = Utils.decodePixelBitmapFromResource(mContext, R.drawable.drop_alpha);
        dropColor = Utils.decodePixelBitmapFromResource(mContext, R.drawable.drop_color);
        mDropAlphaId = OpenGLUtils.loadTexture(dropAlpha, OpenGLUtils.NO_TEXTURE, false);
        dropColorId = OpenGLUtils.loadTexture(dropColor,
                OpenGLUtils.NO_TEXTURE, false);
        mScaleBuffer = ByteBuffer.allocateDirect((maxDropletsCount + maxDrops * 2) * 4 * 2)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mScaleBuffer.position(0);
        mBlueBuffer = ByteBuffer.allocateDirect((maxDropletsCount + maxDrops * 2) * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointBuffer = ByteBuffer.allocateDirect((maxDropletsCount + maxDrops * 2) * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointBuffer.position(0);
        pointSizeBuffer = ByteBuffer.allocateDirect((maxDropletsCount + maxDrops * 2) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointSizeBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        surfaceWidth = width;
        surfaceHeight = height;
        initTexturesAndFBO();
        mRainDropLayer = new RainDropLayer(mContext, width, height, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.texture_city));
        mRainDropLayer.setAlphaMultiply(6);
        mRainDropLayer.setAlphaSubtract(3);
        mRainDropLayer.setParallaxFg(10);
        mRainDropLayer.setBrightness(1.1f);
    }

    private long lastRender = 0L;

    @Override
    public void onDrawFrame(GL10 gl) {
        clearBuffer();
        long now = System.currentTimeMillis();
        if (lastRender == 0L)
            lastRender = now;
        long delta = now - lastRender;
        float timeScale = ((int) delta) / ((1f / 60f) * 1000f);

        if (timeScale > 1.0f)
            timeScale = 1.0f;
        timeScale *= globalTimeScale;

        lastRender = now;
        updateDroplets(timeScale);
        updateDrops(timeScale);
        addBuffer();
        drawCircle();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mRainDropLayer.draw(texturesId[0]);
    }

    protected void initShader() {
        programPoint = OpenGLUtils.loadProgram(OpenGLUtils.readShaderFromRawResource(mContext, R.raw.point_vertex), OpenGLUtils.readShaderFromRawResource(mContext, R.raw.point_frag));

        this.attribPosition = GLES20.glGetAttribLocation(programPoint, "inVertex");
        this.attributeScale = GLES20.glGetAttribLocation(this.programPoint, "aScale");
        this.attributeBlue = GLES20.glGetAttribLocation(this.programPoint, "aBlue");
        this.attributePointSize = GLES20.glGetAttribLocation(programPoint, "pointSize");

        this.uniformTexture = GLES20.glGetUniformLocation(this.programPoint, "uTexturePoint");
        this.uniformTextureDropColor = GLES20.glGetUniformLocation(this.programPoint, "uTextureDropColor");


        defaultProgId = OpenGLUtils.loadProgram(OpenGLUtils.readShaderFromRawResource(mContext, R.raw.custom_vertex), OpenGLUtils.readShaderFromRawResource(mContext, R.raw.frag_matrix));
        mAttrDefaultPositionId = GLES20.glGetAttribLocation(defaultProgId, "position");
        mAttrDefaultTextureId = GLES20.glGetAttribLocation(defaultProgId, "inputTextureCoordinate");
        mUniformTextureId = GLES20.glGetUniformLocation(defaultProgId, "inputImageTexture");
    }


    protected void drawCircle() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboIds[0]);
        //关联
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texturesId[0], 0);
        //绘制
        GLES20.glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(programPoint);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDropAlphaId);
        GLES20.glUniform1i(uniformTexture, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, dropColorId);
        GLES20.glUniform1i(uniformTextureDropColor, 1);


        pointSizeBuffer.position(0);
        GLES20.glEnableVertexAttribArray(attributePointSize);
        GLES20.glVertexAttribPointer(attributePointSize, 1, GLES20.GL_FLOAT, true, 0, pointSizeBuffer);

        pointBuffer.position(0);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glVertexAttribPointer(attribPosition, 2, GLES20.GL_FLOAT, true, 0, pointBuffer);

        mBlueBuffer.position(0);
        GLES20.glEnableVertexAttribArray(attributeBlue);
        GLES20.glVertexAttribPointer(attributeBlue, 1, GLES20.GL_FLOAT, true, 0, mBlueBuffer);

        mScaleBuffer.position(0);
        GLES20.glEnableVertexAttribArray(attributeScale);
        GLES20.glVertexAttribPointer(attributeScale, 2, GLES20.GL_FLOAT, true, 0, mScaleBuffer);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, dropletsArray.size() + drops.size());
        GLES20.glDisableVertexAttribArray(attribPosition);
    }


    protected void drawDefaultProg() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboIds[1]);
        //关联
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texturesId[1], 0);
        GLES20.glViewport(0, 0, (int) surfaceWidth, (int) surfaceHeight);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(defaultProgId);
        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttrDefaultPositionId, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttrDefaultPositionId);
        mGLCoverTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttrDefaultTextureId, 2, GLES20.GL_FLOAT, false, 0, mGLCoverTextureBuffer);
        GLES20.glEnableVertexAttribArray(mAttrDefaultTextureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturesId[0]);
        GLES20.glUniform1i(mUniformTextureId, 1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mAttrDefaultPositionId);
        GLES20.glDisableVertexAttribArray(mAttrDefaultTextureId);
    }


    protected void initTexturesAndFBO() {
        //生成空纹理
        texturesId[0] = OpenGLUtils.createEmptyTexture((int) surfaceWidth, (int) surfaceHeight);
        texturesId[1] = OpenGLUtils.createEmptyTexture((int) surfaceWidth, (int) surfaceHeight);
        GLES20.glGenFramebuffers(2, fboIds, 0);
    }

    private void updateDrops(float timeScale) {
        drops.clear();
        drops.addAll(trailDrops);
        trailDrops.clear();
        updateRain(timeScale);
        Collections.sort(drops);
        for (int i = 0; i < drops.size(); i++) {
            Drop drop = drops.get(i);
            if (!drop.killed) {
                // update gravity
                // (chance of drops "creeping down")
                if (chance((drop.r - (minR * dropFallMultiplier)) * (0.1f / getDeltaR()) * timeScale)) {
                    drop.momentum += random((drop.r / (float) maxR) * 4);
                }
                // clean small drops
                if (autoShrink && drop.r <= minR && chance(0.05f * timeScale)) {
                    drop.shrink += 0.01f;
                }
                //update shrinkage
                drop.r = drop.r - (int) (drop.shrink * timeScale);
//                drop.r -= drop.shrink*timeScale;
                if (drop.r <= 0)
                    drop.killed = true;
                // update trails
                if (raining) {
                    drop.lastSpawn += drop.momentum * timeScale * trailRate;
                    if (drop.lastSpawn > drop.nextSpawn) {
                        if (canCreateDrop()) {
                            Drop trailDrop = new Drop();
                            trailDrop.x = drop.x + (int) (random(-drop.r, drop.r) * 0.1f);
                            trailDrop.y = drop.y - (int) (drop.r * 0.01f);
                            trailDrop.r = (int) (drop.r * random(trailScaleRange[0], trailScaleRange[1]));
                            trailDrop.spreadY = drop.momentum * 0.1f;
                            trailDrop.parent = drop;


                            trailDrops.add(trailDrop);
                            drop.r = (int) (drop.r * Math.pow(0.97, timeScale));
//                            drop.r*=Math.pow(0.97,timeScale);
                            drop.lastSpawn = 0f;
                            drop.nextSpawn = random(minR, maxR) - (drop.momentum * 2 * trailRate) + (maxR - drop.r);
                        }


                    }
                }
                //normalize spread
                drop.spreadX = drop.spreadX * (float) Math.pow(0.4f, timeScale);
                drop.spreadY = drop.spreadY * (float) Math.pow(0.7f, timeScale);
                //update position
                boolean moved = drop.momentum > 0;
                if (moved && !drop.killed) {
                    drop.y = (int) ((drop.y + (drop.momentum * globalTimeScale)));
                    drop.x = drop.x + (int) (drop.momentumX * globalTimeScale);
                    if (drop.y > (surfaceHeight) + drop.r) {
                        drop.killed = true;
                    }
                }
                boolean checkCollision = (moved || drop.isNew) && !drop.killed;
                drop.isNew = false;


                if (checkCollision) {
                    int size = drops.size();
                    int start = i + 1;
                    if (start > size)
                        start = size;
                    int end = i + 70;
                    if (end > size)
                        end = size;
                    ArrayList<Drop> slice = new ArrayList<>(drops.subList(start, end));
                    for (Drop drop2 : slice) {
                        if (drop != drop2 &&
                                drop.r > drop2.r &&
                                drop.parent != drop2 &&
                                drop2.parent != drop &&
                                !drop2.killed) {

                            int dx = drop2.x - drop.x;
                            int dy = drop2.y - drop.y;
                            double d = Math.sqrt((dx * dx) + (dy * dy));
                            //if it's within acceptable distance
                            if (d < (drop.r + drop2.r) * (collisionRadius + (drop.momentum * collisionRadiusIncrease * timeScale))) {
                                double pi = Math.PI;
                                int r1 = drop.r;
                                int r2 = drop2.r;
                                double a1 = pi * (r1 * r1);
                                double a2 = pi * (r2 * r2);
                                double targetR = Math.sqrt((a1 + (a2 * 0.8)) / pi);
                                if (targetR > maxR) {
                                    targetR = maxR;
                                }
                                drop.r = (int) targetR;
                                drop.momentumX += dx * 0.01f;
                                drop.spreadX = 0f;
                                drop.spreadY = 0f;
                                drop2.killed = true;
                                drop.momentum = (float) Math.max(drop2.momentum, Math.min(40, drop.momentum + (targetR * collisionBoostMultiplier) + collisionBoost));
                            }
                        }
                    }
                }

                //slowdown momentum
//                drop.momentum -= Math.max(1, (minR * 0.5f) - drop.momentum) * 0.1f * timeScale;
                if (drop.momentum < 0)
                    drop.momentum = 0;
                drop.momentumX *= ((float) Math.pow(0.7, timeScale));

                if (drop.r <= 0)
                    drop.killed = true;

                if (!drop.killed) {
                    trailDrops.add(drop);
                    if (moved && dropletsRate > 0) {
                        clearDroplets(drop.x, drop.y, (int) (drop.r * dropletsCleaningRadiusMultiplier));
                    }
                }
            }
        }

    }

    private void clearDroplets(int x, int y, int r) {
        int left = (int) ((x - r) * mScale);
        int top = (int) ((y - r) * mScale);
        int width = (int) ((r * 2) * mScale);
        int height = (int) ((r * 2) * mScale * 1.5);
        Rect rect = new Rect(left, top, left + width, top + height);
        Iterator<Drop> iterable = dropletsArray.iterator();
        while (iterable.hasNext()) {
            Drop drop = iterable.next();
            if (rect.contains(drop.x, drop.y)) {
                iterable.remove();
            }
        }
    }

    private void updateRain(float timeScale) {
        if (raining) {
            int limit = (int) (rainLimit * timeScale * getAreaMultiplier());
            int count = 0;
            while (chance(rainChance * timeScale * getAreaMultiplier()) && count < limit) {
                count++;
                int r = randomWithPow3(minR, maxR);
                if (canCreateDrop()) {
                    Drop drop = new Drop();
                    drop.x = random((int) (surfaceWidth));
                    drop.y = random((int) ((surfaceHeight) * spawnArea[0]), (int) ((surfaceHeight) * spawnArea[1]));
                    drop.r = r;
                    drop.momentum = 1 + (r - minR) * 0.1f + random(2f);
                    drop.spreadX = 1.5f;
                    drop.spreadY = 1.5f;
                    drops.add(drop);
                }
            }
        }
    }

    private float getArea() {
        return surfaceWidth * surfaceHeight / mScale;
    }


    private float getAreaMultiplier() {
//        return (float) Math.sqrt(getArea() / (1024 * 768));
        return 1.0f;
    }


    private boolean chance(float c) {
        return ((float) Math.random()) <= c;
    }

    private boolean canCreateDrop() {
        return drops.size() < maxDrops;
    }

    private boolean canCreateDroplets() {
        return dropletsArray.size() < maxDropletsCount;
    }

}
