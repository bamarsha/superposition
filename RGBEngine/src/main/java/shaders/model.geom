#version 330

vec3 OFFSET[6] = vec3[](
    vec3(0., 0., 0.),
    vec3(1., 0., 0.),
    vec3(0., 0., 0.),
    vec3(0., 1., 0.),
    vec3(0., 0., 0.),
    vec3(0., 0., 1.)
);
vec3 NORMAL_TO_DIR1[6] = vec3[](
    vec3(0., 1., 0.),
    vec3(0., 1., 0.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.)
);
vec3 NORMAL_TO_DIR2[6] = vec3[](
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 1., 0.),
    vec3(0., 1., 0.)
);

uniform float maxFogDist = 1000;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform int normal;

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

in vec3[] color;
in vec4[] occlusion;

out vec3 fragColor;
out float fragOcclusion;
out float fragFog;
out vec3 fragPos;

void main()
{
    vec3 pos = gl_in[0].gl_Position.xyz + OFFSET[normal];
    vec3 dir1 = NORMAL_TO_DIR1[normal];
    vec3 dir2 = NORMAL_TO_DIR2[normal];
    float fog = 1 - pow(.01, pow(length(modelViewMatrix * vec4(pos + dir1/2 + dir2/2, 1.)) / maxFogDist, 2));
    mat4 mvp = projectionMatrix * modelViewMatrix;

    if (occlusion[0].x + occlusion[0].z < occlusion[0].y + occlusion[0].w) {
        gl_Position = mvp * vec4(pos, 1);
        fragColor = color[0];
        fragOcclusion = occlusion[0].x;
        fragFog = fog;
        fragPos = pos;
        EmitVertex();

        gl_Position = mvp * vec4(pos + dir1, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].y;
        fragFog = fog;
        fragPos = pos + dir1;
        EmitVertex();

        gl_Position = mvp * vec4(pos + dir2, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].w;
        fragFog = fog;
        fragPos = pos + dir2;
        EmitVertex();

        gl_Position = mvp * vec4(pos + dir1 + dir2, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].z;
        fragFog = fog;
        fragPos = pos + dir1 + dir2;
        EmitVertex();
    }
    else {
        gl_Position = mvp * vec4(pos + dir1, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].y;
        fragFog = fog;
        fragPos = pos + dir1;
        EmitVertex();

        gl_Position = mvp * vec4(pos, 1);
        fragColor = color[0];
        fragOcclusion = occlusion[0].x;
        fragFog = fog;
        fragPos = pos;
        EmitVertex();

        gl_Position = mvp * vec4(pos + dir1 + dir2, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].z;
        fragFog = fog;
        fragPos = pos + dir1 + dir2;
        EmitVertex();

        gl_Position = mvp * vec4(pos + dir2, 1.);
        fragColor = color[0];
        fragOcclusion = occlusion[0].w;
        fragFog = fog;
        fragPos = pos + dir2;
        EmitVertex();
    }

    EndPrimitive();
}