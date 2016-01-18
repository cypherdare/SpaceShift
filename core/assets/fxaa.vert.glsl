/* ---------------------------------------------------------
   Adapted from https://github.com/mattdesl/glsl-fxaa, which
   uses the MIT License: https://opensource.org/licenses/MIT
   ------------------------------------------------------ */

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

varying vec2 v_texCoords;
varying vec2 v_nwTexCoords;
varying vec2 v_swTexCoords;
varying vec2 v_neTexCoords;
varying vec2 v_seTexCoords;
uniform vec2 u_invResolution;

uniform mat4 u_projTrans;

void main()
{
	v_texCoords = a_texCoord0;

	v_nwTexCoords = a_texCoord0 + vec2(-1.0, -1.0) * u_invResolution;
	v_neTexCoords = a_texCoord0 + vec2(1.0, -1.0) * u_invResolution;
	v_swTexCoords = a_texCoord0 + vec2(-1.0, 1.0) * u_invResolution;
	v_seTexCoords = a_texCoord0 + vec2(1.0, 1.0) * u_invResolution;

	gl_Position =  u_projTrans * a_position;
}
