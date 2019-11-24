precision highp float;

attribute vec4 inVertex;
attribute float pointSize;
attribute vec2 aScale;
attribute float aBlue;
//uniform float pointSize;
varying vec2 vScale;
varying float vBlue;

void main(){
	gl_Position = inVertex;
    gl_PointSize = pointSize;
    vScale = aScale;
    vBlue = aBlue;
}

