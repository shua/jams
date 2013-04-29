#include "bounce.h"

GLFWwindow* window;
Matrix projection_matrix, hud_projection_matrix, view_matrix, model_matrix;
GLuint shader = 0;
GLuint colorUniLoc, hudUniLoc, alphaUniLoc, mdlMatUniLoc, viwMatUniLoc, prjMatUniLoc;
int quit = 0;

#ifdef DEBUG
void glCheckError(const char* file, int line) {
	GLenum error = glGetError();
	if(error != GL_NO_ERROR) {
		printf("Error: Opengl failed in %s : %d; %s \n", file, line, gluErrorString(error));
	}
}
#endif

void cb_resize(GLFWwindow* win, int w, int h) {
	glViewport(0,0,w,h);
}

void cb_close(GLFWwindow* win) {
	quit = 1;
}

int createShader();
void createCube();
int setupGL() {
	if(!glfwInit()) {
		printf("glfwInit failed \n");
		return 1;
	}

	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
	glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

	window = glfwCreateWindow(800, 600, "bounce", NULL, NULL);
	if(!window) {
		glfwTerminate();
		printf("window creation failed \n");
		return 1;
	}

	glewExperimental = GL_TRUE;
	if(glewInit() != GLEW_OK) {
		glfwTerminate();
		printf("glewInit failed \n");
		return 1;
	}

	glfwSetInputMode(window, GLFW_STICKY_KEYS, GL_TRUE);
	glfwSetWindowSizeCallback(window, cb_resize);
	glfwSetWindowCloseCallback(window, cb_close);
	glfwSwapInterval(1);

	glEnable(GL_DEPTH_TEST);
	glDepthFunc(GL_LESS);
	glEnable(GL_CULL_FACE);
	glFrontFace(GL_CCW);
	glCheck(glClearColor(0.06, 0.06, 0.06, 0.f)); // One check right at the end

	GLuint vao;
	glGenVertexArrays(1, &vao);
	glBindVertexArray(vao);
	if(createShader() != 0) {
		glfwTerminate();
		printf("shader creation failed \n");
		return 1;
	}

	projection_matrix = perspective(60.f, (float)800/600, 0.1f, 100.f);
	hud_projection_matrix = orthogonal(-40, 40, -30, 30);
	glCheck(glUniformMatrix4fv(prjMatUniLoc, 1, GL_FALSE, &(projection_matrix.m[0])));
	view_matrix = IDENTITY_MATRIX;
	model_matrix = IDENTITY_MATRIX;
	glCheck(createCube());
	return 0;
}

int createShader() {
	const GLchar* v_shader = {" \n \
#version 330 core \n \
layout(location=0) in vec3 vert; \n \
uniform vec3 fillColor; \n \
uniform bool hudDraw; \n \
uniform mat4 model_space; \n \
uniform mat4 camera_space; \n \
uniform mat4 projection_matrix; \n \
out vec3 colorShade; \n \
void main() { \n \
	if(hudDraw) { \n \
		gl_Position = projection_matrix * model_space * vec4(vert, 1.0); \n \
		colorShade = fillColor; \n \
		return; \n \
	} \n \
	mat4 mvp = projection_matrix * camera_space * model_space; \n \
	gl_Position = mvp * vec4(vert, 1.0); \n \
	float factor = clamp(length(camera_space * model_space * vec4(vert, 1.0)) - 5, 0.0, 25.0) / 25; \n \
	colorShade = mix(fillColor, vec3(0.3, 0.3, 0.3), factor); \n \
}"};

	const GLchar* f_shader = {" \n \
#version 330 core \n \
in vec3 colorShade; \n \
uniform float alpha; \n \
out vec4 color; \n \
void main() { \n \
	color = vec4(colorShade, alpha); \n \
}"};

	shader = glCreateProgram();
	GLuint vertH = glCreateShader(GL_VERTEX_SHADER);
	glShaderSource(vertH, 1, &v_shader, NULL);
	glCompileShader(vertH);

	{
		GLint success;
		glCheck(glGetShaderiv(vertH, GL_COMPILE_STATUS, &success));
		if(success == GL_FALSE) {
			char log[1024];
			glCheck(glGetShaderInfoLog(vertH, sizeof(log), 0, log));
			printf("vert shader compile info: \n %s \n", log);
			glCheck(glDeleteShader(vertH));
			glCheck(glDeleteProgram(shader));
			shader = 0;
			return 1;
		}
	}

	glCheck(glAttachShader(shader, vertH));
	glCheck(glDeleteShader(vertH));

	GLuint fragH = glCreateShader(GL_FRAGMENT_SHADER);
	glShaderSource(fragH, 1, &f_shader, NULL);
	glCompileShader(fragH);

	{
		GLint success;
		glCheck(glGetShaderiv(fragH, GL_COMPILE_STATUS, &success));
		if(success == GL_FALSE) {
			char log[1024];
			glCheck(glGetShaderInfoLog(fragH, sizeof(log), 0, log));
			printf("frag shader compile info: \n %s \n", log);
			glCheck(glDeleteShader(fragH));
			glCheck(glDeleteProgram(shader));
			shader = 0;
			return 1;
		}
	}

	glCheck(glAttachShader(shader, fragH));
	glCheck(glDeleteShader(fragH));

	glCheck(glLinkProgram(shader));

	GLint success;
	glCheck(glGetProgramiv(shader, GL_LINK_STATUS, &success));
	if(success == GL_FALSE) {
		char log[1024];
		glCheck(glGetProgramInfoLog(shader, sizeof(log), 0, log));
		printf("shader link info: \n %s \n", log);
		glCheck(glDeleteProgram(shader));
		shader = 0;
		return 1;
	}

	glCheck(glUseProgram(shader));
	colorUniLoc = glGetUniformLocation(shader, "fillColor");
	hudUniLoc = glGetUniformLocation(shader, "hudDraw");
	alphaUniLoc = glGetUniformLocation(shader, "alpha");
	glCheck(glUniform1f(alphaUniLoc, 1));
	mdlMatUniLoc = glGetUniformLocation(shader, "model_space");
	viwMatUniLoc = glGetUniformLocation(shader, "camera_space");
	prjMatUniLoc = glGetUniformLocation(shader, "projection_matrix");
	glCheck(glUniformMatrix4fv(mdlMatUniLoc, 1, GL_FALSE, &(IDENTITY_MATRIX.m[0])));
	glCheck(glUniformMatrix4fv(viwMatUniLoc, 1, GL_FALSE, &(IDENTITY_MATRIX.m[0])));
	glCheck(glUniformMatrix4fv(prjMatUniLoc, 1, GL_FALSE, &(IDENTITY_MATRIX.m[0])));
	return 0;
}

typedef struct BufferData {
	GLuint buffer;
	GLuint size;
} BufferData;

BufferData cube;
void createCube() {
	float verts[] = {
		//front
		-0.5f, -0.5f,  0.5f,    0.5f,  0.5f,  0.5f,   -0.5f,  0.5f,  0.5f,
		-0.5f, -0.5f,  0.5f,    0.5f, -0.5f,  0.5f,    0.5f,  0.5f,  0.5f,
		//bottom
		-0.5f, -0.5f, -0.5f,    0.5f, -0.5f,  0.5f,   -0.5f, -0.5f,  0.5f,
		-0.5f, -0.5f, -0.5f,    0.5f, -0.5f, -0.5f,    0.5f, -0.5f,  0.5f,
		//left
		-0.5f, -0.5f, -0.5f,   -0.5f,  0.5f,  0.5f,   -0.5f,  0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f,   -0.5f, -0.5f,  0.5f,   -0.5f,  0.5f,  0.5f,
		//right
		 0.5f,  0.5f, -0.5f,    0.5f,  0.5f, 0.5f,    0.5f, -0.5f,  0.5f,
		 0.5f, -0.5f,  0.5f,    0.5f, -0.5f, -0.5f,    0.5f,  0.5f, -0.5f,
		//top
		-0.5f,  0.5f,  0.5f,    0.5f,  0.5f, -0.5f,   -0.5f,  0.5f, -0.5f,
		-0.5f,  0.5f,  0.5f,    0.5f,  0.5f,  0.5f,    0.5f,  0.5f, -0.5f,
		//back
		 0.5f, -0.5f, -0.5f,   -0.5f,  0.5f, -0.5f,    0.5f,  0.5f, -0.5f,
		 0.5f, -0.5f, -0.5f,   -0.5f, -0.5f, -0.5f,   -0.5f,  0.5f, -0.5f
	};

	cube.size = sizeof(verts)/(3 * sizeof(float));
	glGenBuffers(1, &cube.buffer);
	glBindBuffer(GL_ARRAY_BUFFER, cube.buffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(verts), verts, GL_STATIC_DRAW);
	glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(float), 0);
	glEnableVertexAttribArray(0);
}

int shutdownGL() {
	glCheck(glDeleteProgram(shader));
	glfwTerminate();
	return 0;
}

Vector4 cameraPos = {{0}};
Vector4 cameraFoc = {{0, 0, -1, 0}};
Vector4 cameraSid = {{1, 0, 0, 0}};
int cameraDamage = 0;
void focusMoveCamera(float forward, float side) {
	cameraPos.x += cameraFoc.x * forward + cameraSid.x * side;
	cameraPos.y += cameraFoc.y * forward + cameraSid.y * side;
	cameraPos.z += cameraFoc.z * forward + cameraSid.z * side;
	cameraDamage = 1;
}

void moveCamera(float x, float y, float z) {
	cameraPos.x += x;
	cameraPos.y += y;
	cameraPos.z += z;
	cameraDamage = 1;
}

float cameraPitch = 0, cameraYaw = 0;
void rotateCamera(float yaw, float pitch) {
	cameraPitch += pitch;
	cameraYaw += yaw;
	const float hpi = 1.5707963;
	if(cameraPitch > hpi) cameraPitch = hpi;
	if(cameraPitch < -hpi) cameraPitch = -hpi;
	float cosp = cos(cameraPitch);
	cameraFoc.x = cosp * sin(cameraYaw);
	cameraFoc.y = sin(cameraPitch);
	cameraFoc.z = -cosp * cos(cameraYaw);
	cameraSid = crossvec4(cameraFoc, Y_AXIS);
	normalizevec4(&cameraSid);
	cameraDamage = 1;
}

void updateCamera() {
	if(cameraDamage) {
		view_matrix = lookAt(cameraPos, cameraFoc);
		glCheck(glUniformMatrix4fv(viwMatUniLoc, 1, GL_FALSE, &(view_matrix.m[0])));
		cameraDamage = 0;
	}
}

void setFillColor(float r, float g, float b) {
	glCheck(glUniform3f(colorUniLoc, r, g, b));
}

void setLayerAlpha(float a) {
	glCheck(glUniform1f(alphaUniLoc, a));
}

void hudDrawOn() {
	glCheck(glUniform1i(hudUniLoc, 1));
	glCheck(glUniformMatrix4fv(prjMatUniLoc, 1, GL_FALSE, &(hud_projection_matrix.m[0])));
	glCheck(glDisable(GL_DEPTH_TEST));
	glCheck(glEnable(GL_BLEND));
	glCheck(glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
}

void hudDrawOff () {
	glCheck(glDisable(GL_BLEND));
	glCheck(glEnable(GL_DEPTH_TEST));
	glCheck(glUniformMatrix4fv(prjMatUniLoc, 1, GL_FALSE, &(projection_matrix.m[0])));
	glCheck(glUniform1i(hudUniLoc, 0));
}

void drawCube(float x, float y, float z, float sx, float sy, float sz) {
	updateCamera();
	model_matrix = IDENTITY_MATRIX;
	if(!(sx == sy && sy == sz && sz == 1))
		scale(&model_matrix, sx, sy, sz);
	if(!(x == y && y == z && z == 0))
		translate(&model_matrix, x, y, z);
	glCheck(glUniformMatrix4fv(mdlMatUniLoc, 1, GL_FALSE, &model_matrix.m[0]));
	glCheck(glBindBuffer(GL_ARRAY_BUFFER, cube.buffer));
	glCheck(glDrawArrays(GL_TRIANGLES, 0, cube.size));
}
