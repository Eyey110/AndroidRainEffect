/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qisejin.rain;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RainDropImageShader extends Shader {

    private int renderShadow = 0;
    private int renderShine = 0;
    private int minRefraction = 256;
    private int maxRefraction = 512;
    private float brightness = 1f;

    private int alphaMultiply = 20;
    private int alphaSubtract = 5;
    private int parallaxBg = 5;
    private int parallaxFg = 20;
    private float parallaX = 0f;
    private float parallaY = 0f;

    private int mResolutionHandler;
    private int mTextureRatio;
    private int mRenderShine;
    private int mRenderShadow;
    private int mMinRefraction;
    private int mRefractionDelta;
    private int mBrightnessHanlder;
    private int mAlphaMultiply;
    private int mAlphaSubtract;
    private int mParallaxBg;
    private int mParallaxFg;
    private float mWidth;
    private float mHeight;
    private int mParallax;

    protected FloatBuffer mGLCoverTextureBuffer = ByteBuffer.allocateDirect(OpenGLUtils.TEXTURE_ROTATED_REVERSE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    protected FloatBuffer mGLCubeBuffer = ByteBuffer.allocateDirect(OpenGLUtils.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    private FloatBuffer mCubeBuffer;
    private FloatBuffer mTextureBuffer;

    private Context mContext;
    private int mProgrammerHandle;
    private int mBackgroundTextureHandle;
    private int mForegroundTextureHandle;
    private int mDropShineHandle;
    private int mRainMapTextureHandle;

    private int mPositionHandle;
    private int mTexCoordHandle;

    private int mTextureUniformHandleWaterMap;
    private int mTextureUniformHandleShine;
    private int mTextureUniformHandleFg;
    private int mTextureUniformHandleBg;


    public void setRainMapTextureHandle(int textureId) {
        mRainMapTextureHandle = textureId;
    }


    RainDropImageShader(Context context, int width, int height, Bitmap srcImage) {
        mContext = context;

        mWidth = width;
        mHeight = height;

        mCubeBuffer = ByteBuffer.allocateDirect(OpenGLUtils.CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeBuffer.put(OpenGLUtils.CUBE);
        mCubeBuffer.position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(OpenGLUtils.TEXTURE_ROTATED_REVERSE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.put(OpenGLUtils.TEXTURE_ROTATED_REVERSE);
        mTextureBuffer.position(0);

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(OpenGLUtils.CUBE);
        mGLCoverTextureBuffer.clear();
        mGLCoverTextureBuffer.put(OpenGLUtils.TEXTURE_NO_ROTATED);

        Bitmap bg = Utils.cropCenter(width, height, srcImage);
        Bitmap fg = Bitmap.createScaledBitmap(bg, (int) (bg.getWidth() / 2f), (int) (bg.getHeight() / 2f), false);

        mBackgroundTextureHandle = OpenGLUtils.loadTexture(bg, OpenGLUtils.NO_TEXTURE);
        mForegroundTextureHandle = OpenGLUtils.loadTexture(fg, OpenGLUtils.NO_TEXTURE);
        mDropShineHandle = OpenGLUtils.loadTexture(Utils.decodePixelBitmapFromResource(context, R.drawable.drop_shine), OpenGLUtils.NO_TEXTURE);
        initProgram();
    }


    private void initProgram() {
        mProgrammerHandle = OpenGLUtils.loadProgram(Utils.readShaderFromRawResource(mContext, R.raw.simple_vertex_shader), Utils.readShaderFromRawResource(mContext, R.raw.water_fragment_shader));

        mTextureUniformHandleWaterMap = GLES20.glGetUniformLocation(mProgrammerHandle, "u_waterMap");
        mTextureUniformHandleShine = GLES20.glGetUniformLocation(mProgrammerHandle, "u_textureShine");
        mTextureUniformHandleFg = GLES20.glGetUniformLocation(mProgrammerHandle, "u_textureFg");
        mTextureUniformHandleBg = GLES20.glGetUniformLocation(mProgrammerHandle, "u_textureBg");

        mPositionHandle = GLES20.glGetAttribLocation(mProgrammerHandle, "a_position");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgrammerHandle, "a_texCoord");

        mResolutionHandler = GLES20.glGetUniformLocation(mProgrammerHandle, "u_resolution");
        mParallax = GLES20.glGetUniformLocation(mProgrammerHandle, "u_parallax");
        mTextureRatio = GLES20.glGetUniformLocation(mProgrammerHandle, "u_textureRatio");
        mRenderShine = GLES20.glGetUniformLocation(mProgrammerHandle, "u_renderShine");
        mRenderShadow = GLES20.glGetUniformLocation(mProgrammerHandle, "u_renderShadow");
        mMinRefraction = GLES20.glGetUniformLocation(mProgrammerHandle, "u_minRefraction");
        mRefractionDelta = GLES20.glGetUniformLocation(mProgrammerHandle, "u_refractionDelta");
        mBrightnessHanlder = GLES20.glGetUniformLocation(mProgrammerHandle, "u_brightness");
        mAlphaMultiply = GLES20.glGetUniformLocation(mProgrammerHandle, "u_alphaMultiply");
        mAlphaSubtract = GLES20.glGetUniformLocation(mProgrammerHandle, "u_alphaSubtract");
        mParallaxBg = GLES20.glGetUniformLocation(mProgrammerHandle, "u_parallaxBg");
        mParallaxFg = GLES20.glGetUniformLocation(mProgrammerHandle, "u_parallaxFg");
    }

    @Override
    protected void realDraw() {
        GLES20.glClearColor(1f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        mGLCoverTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mGLCoverTextureBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);


        GLES20.glUniform2f(mResolutionHandler, mWidth, mHeight);

        GLES20.glUniform2f(mParallax, parallaX, parallaY);
        GLES20.glUniform1f(mTextureRatio, mWidth / mHeight);
        GLES20.glUniform1i(mRenderShine, renderShine);
        GLES20.glUniform1i(mRenderShadow, renderShadow);
        GLES20.glUniform1f(mMinRefraction, minRefraction);
        GLES20.glUniform1f(mRefractionDelta, maxRefraction - minRefraction);
        GLES20.glUniform1f(mBrightnessHanlder, brightness);
        GLES20.glUniform1f(mAlphaMultiply, alphaMultiply);
        GLES20.glUniform1f(mAlphaSubtract, alphaSubtract);
        GLES20.glUniform1f(mParallaxBg, parallaxBg);
        GLES20.glUniform1f(mParallaxFg, parallaxFg);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRainMapTextureHandle);
        GLES20.glUniform1i(mTextureUniformHandleWaterMap, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDropShineHandle);
        GLES20.glUniform1i(mTextureUniformHandleShine, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mForegroundTextureHandle);
        GLES20.glUniform1i(mTextureUniformHandleFg, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBackgroundTextureHandle);
        GLES20.glUniform1i(mTextureUniformHandleBg, 3);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    protected int getProgramId() {
        return mProgrammerHandle;
    }

    public int getRenderShadow() {
        return renderShadow;
    }

    public void setRenderShadow(int renderShadow) {
        this.renderShadow = renderShadow;
    }

    public int getRenderShine() {
        return renderShine;
    }

    public void setRenderShine(int renderShine) {
        this.renderShine = renderShine;
    }

    public int getMinRefraction() {
        return minRefraction;
    }

    public void setMinRefraction(int minRefraction) {
        this.minRefraction = minRefraction;
    }

    public int getMaxRefraction() {
        return maxRefraction;
    }

    public void setMaxRefraction(int maxRefraction) {
        this.maxRefraction = maxRefraction;
    }

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public int getAlphaMultiply() {
        return alphaMultiply;
    }

    public void setAlphaMultiply(int alphaMultiply) {
        this.alphaMultiply = alphaMultiply;
    }

    public int getAlphaSubtract() {
        return alphaSubtract;
    }

    public void setAlphaSubtract(int alphaSubtract) {
        this.alphaSubtract = alphaSubtract;
    }

    public int getParallaxBg() {
        return parallaxBg;
    }

    public void setParallaxBg(int parallaxBg) {
        this.parallaxBg = parallaxBg;
    }

    public int getParallaxFg() {
        return parallaxFg;
    }

    public void setParallaxFg(int parallaxFg) {
        this.parallaxFg = parallaxFg;
    }

    public float getParallaX() {
        return parallaX;
    }

    public void setParallaX(float parallaX) {
        this.parallaX = parallaX;
    }

    public float getParallaY() {
        return parallaY;
    }

    public void setParallaY(float parallaY) {
        this.parallaY = parallaY;
    }

}