#version 330

in vec2 TexCoords;

out vec4 FragColor;

uniform vec4 color;
uniform sampler2D tex;

void main() {
    FragColor = color * texture(tex, TexCoords);
}