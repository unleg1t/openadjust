#version 120

uniform sampler2D DiffuseSampler;
uniform sampler2D depthtex;
uniform sampler2D EntityMaskSampler;

uniform mat4 modelViewInverse;
uniform mat4 projectionInverse;
uniform mat4 previousProjection;
uniform mat4 previousModelView;

uniform vec3 cameraPosition;
uniform vec3 previousCameraPosition;

uniform float strength;

varying vec2 texCoord;

bool isProtectedEntity(vec2 coord) {
    return texture2D(EntityMaskSampler, coord).a > 0.001;
}

void main() {
    vec3 color = texture2D(DiffuseSampler, texCoord).rgb;

    float depth = texture2D(depthtex, texCoord).x;
    if (isProtectedEntity(texCoord)) {
        gl_FragColor = vec4(color, 1.0);
        return;
    }

    vec4 H = vec4(texCoord, depth, 1.0) * 2.0 - 1.0;

    vec4 worldpos = projectionInverse * H;
    worldpos = modelViewInverse * worldpos;
    worldpos /= worldpos.w;
    worldpos.xyz += cameraPosition;

    vec4 prevpos = worldpos;
    prevpos.xyz -= previousCameraPosition;
    prevpos = previousModelView * prevpos;
    prevpos = previousProjection * prevpos;
    prevpos /= prevpos.w;

    vec2 vel = (H - prevpos).st;
    vel = vel / (1.0 + length(vel)) * strength * 0.01;
    vec2 coord = texCoord + vel;

    float count = 1;
    for (int i = 0; i < 30; ++i, coord += vel) {
        if (coord.s > 1.0 || coord.t > 1.0 || coord.s < 0.0 || coord.t < 0.0) break;
        if (isProtectedEntity(coord)) continue;
        color += texture2D(DiffuseSampler, coord).xyz;
        count += 1;
    }
    color /= count;

    gl_FragColor = vec4(color.rgb, 1);
}
