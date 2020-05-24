#version 130

uniform sampler2D u_texture;
uniform vec4 color;
uniform vec4 tintColor;
uniform bool[16] state;

in vec2 texCoords;

out vec4 gl_FragColor;

void main() {
    vec4 texColor = texture2D(u_texture, texCoords);
    if (length(texColor.rg - vec2(1, 0)) < 1e-6 && texColor.b > .65) {
        int bitId = int(round((1.0 - texColor.b) * 51.0));
        texColor = state[bitId] ? vec4(1, 1, 1, 1) : vec4(0, 0, 0, 1);
    }
    texColor = texColor * color;
    gl_FragColor.rgb = mix(texColor.rgb, tintColor.rgb, tintColor.a);
    gl_FragColor.a = texColor.a;
}
