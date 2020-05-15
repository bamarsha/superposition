uniform sampler2D u_texture;
uniform vec4 color;

varying vec2 texCoords;

void main() {
    vec4 texColor = texture2D(u_texture, texCoords);
    gl_FragColor.rgb = mix(texColor.rgb, color.rgb, color.a);
    gl_FragColor.a = texColor.a;
}
