#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_sourceTop;
uniform float u_sourceBottom;
uniform float u_cornerAngle;

#ifdef DAMAGE
const float SHIFT = 0.0075;
const vec3 WASHOUT_TARGET = vec3(1.0);
const float MAX_WASHOUT = 0.3;
uniform float u_damageIntensity;
#endif

void main()
{
    vec2 coords = vec2(
        atan(v_texCoords.x / v_texCoords.y) / u_cornerAngle * 0.5 + 0.5,
       (-length(v_texCoords) - u_sourceBottom) / (u_sourceTop - u_sourceBottom)
    );

	#ifdef DAMAGE
	vec3 color;
	float shiftSize = SHIFT * u_damageIntensity;
	color.b = texture2D(u_texture, coords).b;
	color.r = texture2D(u_texture, vec2(coords.x - shiftSize, coords.y)).r;
	color.g = texture2D(u_texture, vec2(coords.x + shiftSize, coords.y)).g;
	color = mix(color, WASHOUT_TARGET, u_damageIntensity * MAX_WASHOUT);
	gl_FragColor = vec4(color, 1.0);
	#else
	gl_FragColor = texture2D(u_texture, coords);
	#endif
}