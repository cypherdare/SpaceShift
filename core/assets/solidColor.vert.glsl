attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;
varying float v_alpha;

uniform mat4 u_projTrans;

void main()
{
	v_texCoords = a_texCoord0;
	v_alpha = a_color.a;
	gl_Position =  u_projTrans * a_position;
}
