#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;
varying float v_alpha;

uniform sampler2D u_texture;
uniform vec3 u_color;

void main()
{
    vec4 texture = texture2D(u_texture, v_texCoords);
	gl_FragColor = vec4(u_color, step(0.7, texture.a) * v_alpha);
}