precision highp float;
uniform sampler2D uTexturePoint;
uniform sampler2D uTextureDropColor;
varying vec2 vScale;
varying float vBlue;


vec3 blend(vec3 bg,vec3 fg,float a){
    vec3 result = bg *(1.0 - a) + a * fg;
    return result;
}

void main(){
    vec2 coord =  vec2(gl_PointCoord.x * vScale.x,gl_PointCoord.y * vScale.y);
    vec4 fragColor = texture2D(uTexturePoint, coord);
    vec4 dropColor = texture2D(uTextureDropColor, coord);
//    vec4 targetColor = vec4(dropColor.rg , 1.0, fragColor.a);
    gl_FragColor = vec4(dropColor.rg , vBlue, fragColor.a);
//    gl_FragColor = vec4(dropColor.rg , 1.0, 1.0);
}