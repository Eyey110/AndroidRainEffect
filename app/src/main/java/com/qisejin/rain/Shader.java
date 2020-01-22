package com.qisejin.rain;

import android.opengl.GLES20;

/**
 * 2019-11-30
 *
 * @author zhengliao
 */
public abstract class Shader {

    public void draw(int fboId, int width, int height) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(getProgramId());
        realDraw();
    }


    protected abstract void realDraw();

    protected abstract int getProgramId();
}
