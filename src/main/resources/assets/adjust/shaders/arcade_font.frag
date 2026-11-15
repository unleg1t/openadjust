#version 120

uniform sampler2D u_Texture;
uniform vec4 u_Palette[7];

void main() {
    vec4 texColor = texture2D(u_Texture, gl_TexCoord[0].st);

    if (texColor.a < 0.1 || (texColor.r > 0.9 && texColor.g > 0.9 && texColor.b > 0.9)) {
        discard;
    }

    bool r = bool(texColor.r > 0.5);
    bool g = bool(texColor.g > 0.5);
    bool b = bool(texColor.b > 0.5);

    int slot = 0;

    if (r && !g && !b)      slot = 1;
    if (r && !g && b)       slot = 2;
    if (!r && !g && b)      slot = 3;
    if (!r && g && b)       slot = 4;
    if (!r && g && !b)      slot = 5;
    if (r && g && !b)       slot = 6;

    if (slot == 0) {
        discard;
    }

    gl_FragColor = u_Palette[slot];
}