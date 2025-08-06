uniform sampler2D u_texture;
uniform vec4 u_ambient;

varying vec2 v_texCoords;

void main(){
	vec4 color = texture2D(u_texture, v_texCoords);
	gl_FragColor.r = color.r;
	gl_FragColor.g = color.g;
	gl_FragColor.b = color.b;

	gl_FragColor.a = u_ambient.a - color.a;
}