attribute vec4 a_position;
attribute vec2 a_texCoord;

uniform float strength;

varying vec2 vBlurTexCoords[%size%];

void main(void) {
    gl_Position = CC_PMatrix * a_position;
    %blur%
}
