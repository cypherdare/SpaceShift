/* ---------------------------------------------------------
   Adapted from https://github.com/mattdesl/glsl-fxaa, which
   uses the MIT License: https://opensource.org/licenses/MIT
   ------------------------------------------------------ */

#ifdef GL_ES
#define LOWP lowp
#define MEDP mediump
precision mediump float;
#else
#define LOWP
#define MEDP
#endif

#ifndef FXAA_REDUCE_MIN
    #define FXAA_REDUCE_MIN   (1.0/ 128.0)
#endif
#ifndef FXAA_REDUCE_MUL
    #define FXAA_REDUCE_MUL   (1.0 / 8.0)
#endif
#ifndef FXAA_SPAN_MAX
    #define FXAA_SPAN_MAX     4.0
#endif

varying vec2 v_nwTexCoords;
varying vec2 v_swTexCoords;
varying vec2 v_neTexCoords;
varying vec2 v_seTexCoords;
uniform vec2 u_invResolution;

varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main() {
    vec4 color;
    vec3 rgbNW = texture2D(u_texture, v_nwTexCoords).xyz;
    vec3 rgbNE = texture2D(u_texture, v_neTexCoords).xyz;
    vec3 rgbSW = texture2D(u_texture, v_swTexCoords).xyz;
    vec3 rgbSE = texture2D(u_texture, v_seTexCoords).xyz;
    vec4 texColor = texture2D(u_texture, v_texCoords);
    vec3 rgbM  = texColor.xyz;
    vec3 luma = vec3(0.299, 0.587, 0.114);
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

    MEDP vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) *
                          (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);

    float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2(FXAA_SPAN_MAX, FXAA_SPAN_MAX),
              max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
              dir * rcpDirMin)) * u_invResolution;

    vec3 rgbA = 0.5 * (
        texture2D(u_texture, gl_FragCoord.xy * u_invResolution + dir * (1.0 / 3.0 - 0.5)).xyz +
        texture2D(u_texture, gl_FragCoord.xy * u_invResolution + dir * (2.0 / 3.0 - 0.5)).xyz);
    vec3 rgbB = rgbA * 0.5 + 0.25 * (
        texture2D(u_texture, gl_FragCoord.xy * u_invResolution + dir * -0.5).xyz +
        texture2D(u_texture, gl_FragCoord.xy * u_invResolution + dir * 0.5).xyz);

    float lumaB = dot(rgbB, luma);
    if ((lumaB < lumaMin) || (lumaB > lumaMax))
        color = vec4(rgbA, texColor.a);
    else
        color = vec4(rgbB, texColor.a);
    gl_FragColor = color;
}
