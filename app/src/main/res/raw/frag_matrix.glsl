precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D inputImageTexture;

void main(){
    mat4 colorMatrix =mat4(1,0,0,0,
                           0,1,0,0,
                           0,0,1,0,
                           0,0,0,18);
    vec4 alphaBias = vec4(0, 0, 0, -7);
    vec4 color = texture2D(inputImageTexture, textureCoordinate);
    vec4 result = colorMatrix * color + alphaBias;
    //feColorMatrix end

    gl_FragColor = color;
}