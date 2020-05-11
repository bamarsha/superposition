uniform sampler2D u_texture;

varying vec2 texCoords;

void main() {
    gl_FragColor = texture2D(u_texture, texCoords);
}
