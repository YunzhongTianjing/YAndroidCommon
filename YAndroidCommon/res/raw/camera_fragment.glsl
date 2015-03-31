precision highp float;

const vec2 uvDelta = vec2(0.5 , 0.5);
const mat3 convertMatrix = mat3(1.0 , 1.0 , 1.0 , 0 , -0.39465 , 2.03211 , 1.13983 , -0.58060 , 0);//列矩阵

uniform sampler2D uYTextureSampler;
uniform sampler2D uUVTextureSampler;
varying vec2 vTexCoord;

void main ()
{
		/*
			y = texture2D(uYTextureSampler, vTexCoord).r;
			u = texture2D(uUVTextureSampler, vTexCoord).a - 0.5;
			v = texture2D(uUVTextureSampler, vTexCoord).r - 0.5;
			r = y + 1.13983*v;
			g = y - 0.39465*u - 0.58060*v;
			b = y + 2.03211*u;
		*/
		vec3 yuv = vec3(texture2D(uYTextureSampler, vTexCoord).r , texture2D(uUVTextureSampler, vTexCoord).ar -uvDelta);
		vec3 rgb = convertMatrix * yuv;
		gl_FragColor = vec4(rgb, 1.0);
}