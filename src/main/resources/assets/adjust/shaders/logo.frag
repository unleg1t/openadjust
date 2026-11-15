#version 120

uniform sampler2D u_Texture;
uniform vec4 u_Color1;
uniform vec4 u_Color2;

void main() {
    vec4 texColor = texture2D(u_Texture, gl_TexCoord[0].st);
    if (texColor.a < 0.1) {
        discard;
    }
    float factor = 1.0 - texColor.r;
    vec4 mixedColor = mix(u_Color1, u_Color2, factor);
    vec3 finalRGB = mixedColor.rgb * texColor.g;
    float finalAlpha = texColor.a;
    gl_FragColor = vec4(finalRGB, finalAlpha);
}