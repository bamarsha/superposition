#version 330

in vec2 TexCoords;

out vec4 FragColor;

uniform vec4 color;
uniform bool outline;
uniform sampler2D tex;

void main() {
    if (outline) {
        FragColor = color * vec4(1, 1, 1, texture(tex, TexCoords).a);
    } else {
        FragColor = color * vec4(1, 1, 1, texture(tex, TexCoords).r);
    }
}