uniform sampler2D u_texture;
uniform vec4 color;
uniform vec4 tintColor;

varying vec2 texCoords;

void main() {
    vec4 texColor = texture2D(u_texture, texCoords) * color;
    gl_FragColor.rgb = mix(texColor.rgb, tintColor.rgb, tintColor.a);
    gl_FragColor.a = texColor.a;
}
