#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;

uniform sampler2D u_glassTexture;
uniform sampler2D u_texture;
uniform vec2 u_resolution;

const float MAX_OFFSET = 0.03;

void main()
{
    vec4 glass = texture2D(u_glassTexture, v_texCoords);
    vec2 baseCoord = gl_FragCoord.xy / u_resolution;
    vec3 color = texture2D(u_texture, baseCoord + glass.rg * MAX_OFFSET).rgb;

	gl_FragColor = vec4(color + max(0.0, glass.a - glass.b) - glass.b * 0.05, 1.0);
}