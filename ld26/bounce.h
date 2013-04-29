#include <GL/glew.h>
#include <GL/glfw3.h>

#include <stdio.h>
#include <string.h>

#include "linearAlg.h"

#ifdef DEBUG
#define glCheck(call) (call); glCheckError(__FILE__, __LINE__)
void glCheckError(const char* file, int line);
#else
#define glCheck(call) (call)
#endif

extern GLFWwindow* window;
extern int quit;
extern Matrix projection_matrix, view_matrix, model_matrix;
extern Vector4 cameraPos, cameraFoc, cameraSid;
int setupGL();
int shutdownGL();

void moveCamera(float x, float y, float z);
void focusMoveCamera(float forward, float side);
void rotateCamera(float yaw, float pitch);
void setFillColor(float r, float g, float b);
void setLayerAlpha(float a);
void hudDrawOn();
void hudDrawOff();
void drawCube(float x, float y, float z, float sx, float sy, float sz);