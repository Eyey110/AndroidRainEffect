package com.qisejin.rain;

import android.content.Context;
import android.opengl.GLES20;

/**
 * 2019-11-30
 *
 * @author zhengliao
 */
public class RainDropShader extends Shader {
    private int mPointProgramId;
    private int attribPosition;
    private int attributeScale;
    private int attributeBlue;
    private int attributePointSize;
    private int uniformTexture;
    private int uniformTextureDropColor;

    private Context mContext;

    private float mTimeScale;

    public RainDropShader(float timeScale) {
        mTimeScale = timeScale;
    }

    public RainDropShader(Context context) {
        mContext = context;
        initShader();
    }

    void initShader() {
        mPointProgramId = OpenGLUtils.loadProgram(OpenGLUtils.readShaderFromRawResource(mContext, R.raw.point_vertex), OpenGLUtils.readShaderFromRawResource(mContext, R.raw.point_frag));

        this.attribPosition = GLES20.glGetAttribLocation(mPointProgramId, "inVertex");
        this.attributeScale = GLES20.glGetAttribLocation(this.mPointProgramId, "aScale");
        this.attributeBlue = GLES20.glGetAttribLocation(this.mPointProgramId, "aBlue");
        this.attributePointSize = GLES20.glGetAttribLocation(mPointProgramId, "pointSize");

        this.uniformTexture = GLES20.glGetUniformLocation(this.mPointProgramId, "uTexturePoint");
        this.uniformTextureDropColor = GLES20.glGetUniformLocation(this.mPointProgramId, "uTextureDropColor");
    }

    @Override
    protected void realDraw() {

    }

    @Override
    protected int getProgramId() {
        return mPointProgramId;
    }
}
