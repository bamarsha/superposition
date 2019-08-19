#version 330

layout (location=0) in vec3 position_in;
layout (location=1) in vec3 color_in;
layout (location=2) in vec4 occlusion_in;

out vec3 color;
out vec4 occlusion;

void main() {
    gl_Position = vec4(position_in, 1);
    color = color_in;
    occlusion = occlusion_in;
}