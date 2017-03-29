varying vec2 vBlurTexCoords[%size%];

void main(void) {
    gl_FragColor = vec4(0.0);
    %blur%
}
