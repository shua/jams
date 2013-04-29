#include "bounce.h"
#include <stdlib.h>

Vector4 playerVel = {{0.1, 0, 01, 0.0}};
float gravity = -.01;
int seed = 0;
int jumpCount = 0;
float platforms[20][5];
float highscore[4] = {0};
double random_scale = 20 / (double)RAND_MAX;
int playerCollide(int);
void shiftLevel(float);
void setupLevel(int);

int pause = 2;
int captureMouse;
int spacebar = 0;
int p_key = 0;
int mouse_left = 0;
int dead;
int difficulty = 0;
int diff_display = 0;
float diff_color[4][3] = {
	{0, 1, 0},
	{1, 1, 0},
	{1, 0.5, 0},
	{1, 0, 0}
};
float levelOffset;
float maxheight;

void updatePlayer() {
	maxheight += (cameraPos.y > maxheight) * (cameraPos.y - maxheight);
	playerVel.y += gravity;
	moveCamera(playerVel.x, playerVel.y, playerVel.z);
	if(cameraPos.y > 30) {
		float offset = cameraPos.y - 30;
		shiftLevel(offset);
		cameraPos.y -= offset;
		maxheight += offset;
	}
	if(cameraPos.y < 0) {
		dead = (maxheight > 30);
		pause |= dead;
		cameraPos.y = 0;
		if(glfwGetKey(window, GLFW_KEY_LSHIFT) == GLFW_PRESS)
			playerVel.y = 0;
		else
			playerVel.y = .4;
		jumpCount = 10;
	}
	if(cameraPos.x > 5 || cameraPos.x < -5) {
		cameraPos.x = (cameraPos.x < 0) ? -5 : 5;
	}
	if(cameraPos.z > 5 || cameraPos.z < -5) {
		cameraPos.z = (cameraPos.z < 0) ? -5 : 5;
	}
	int level = (int)((cameraPos.y + levelOffset) / 5) ;
	if(playerVel.y < 0 && platforms[level][4] > .1 && playerCollide(level)) {
		if(glfwGetKey(window, GLFW_KEY_LSHIFT) == GLFW_PRESS)
			playerVel.y = 0;
		else {
			playerVel.y = .4;
		}
		if(platforms[level][3] == 0) {
			platforms[level][3] = 500;
			jumpCount += (jumpCount < 10) ? 1 : 0;
		}
		platforms[level][4] -= (difficulty != 0);
		cameraPos.y = platforms[level][1] + .5 - levelOffset;
	}
}

void shiftLevel(float amt) {
	levelOffset += amt;
	if(levelOffset > 5) {
		for(int i = 0; i < 19; ++i) {
			platforms[i][0] = platforms[i+1][0];
			platforms[i][2] = platforms[i+1][2];
			platforms[i][3] = platforms[i+1][3];
			platforms[i][4] = platforms[i+1][4];
		}
		platforms[19][0] = ((int)(rand() * random_scale))/2 - 5;
		platforms[19][1] = (19 * 5)-.1;
		platforms[19][2] = ((int)(rand() * random_scale))/2 - 5;
		platforms[19][3] = 0;
		platforms[19][4] = 3 - (difficulty > 1) - (difficulty > 2);
		levelOffset -= 5;
	}
}

void drawHud() {
	hudDrawOn();
	int digits = ((int)log10(maxheight));
	float number = maxheight / pow(10, digits);
	setFillColor(0.4, 0.4, 0.4);
	for(int i = digits; i >= 0; --i)
		drawCube(35, -25 + i * 1.5, 0, 1, 1, 1);
	setFillColor(0, 0, 1);
	for(int i = digits; i >= 0; --i) {
		if(i == 0)
			number = round(number);
		int j = (int)number;
		number = (number - j) * 10.f;
		for(; j > 0; --j) {
			drawCube(35 - j * 1.5, -25 + i * 1.5, 0, 1, 1, 1);
		}
	}
	setFillColor(0, 1, 0);
	for(int i = jumpCount; i > 0; --i) {
		drawCube(-35, -25 + i * 1.5 + (i > 5), 0, 1, 1, 1);
	}
	hudDrawOff();
}

void drawLevel() {
	setFillColor(0.1, 0.1, 0.1);
	drawCube(0, -1, 0, 10, 1, 10);
	if(maxheight > 30) {
		setFillColor(1, 0, 0);
		for(int i = 0; i < 5; ++i) {
			for(int j = 0; j < 5; ++j) {
				drawCube((2 * i) - 4, 0, (2 * j) - 4, 1, 1, 1);
			}
		}
	}
	setFillColor(0.9, 0.9, 0.9);
	for(int i = 0; i < 20; ++i) {
		if(platforms[i][3] > 0) {
			if(platforms[i][3] < 1) platforms[i][3] = 0;
			else {
				float r = .9 - (platforms[i][3] * (.9 - diff_color[difficulty][0]) / 500);
				float g = .9 - (platforms[i][3] * (.9 - diff_color[difficulty][1]) / 500);
				float b = .9 - (platforms[i][3] * (.9 - diff_color[difficulty][2]) / 500);
				setFillColor(r, g, b);
			}
		}
		drawCube(
			platforms[i][0],
			platforms[i][1] - levelOffset,
			platforms[i][2],
			platforms[i][4],
			0.1,
			platforms[i][4]);
		if(platforms[i][3] > 0) {
			setFillColor(0.9, 0.9, 0.9);
			platforms[i][3] -= 1;
		}
	}
	for(int i = 0; i < 20; ++i) {
		setFillColor(0.1, 0.1, 0.1);
		if((int)((cameraPos.y + levelOffset) / 5) == i)
			setFillColor(diff_color[difficulty][0], diff_color[difficulty][1], diff_color[difficulty][2]);
		drawCube(5.5 * ((difficulty == 0) - (difficulty == 2)), (i * 5) - levelOffset + 2.5, 5.5 * ((difficulty == 1) - (difficulty == 3)), 1, 4, 1);
	}
	if(maxheight < 30) {
		setFillColor(diff_color[0][0], diff_color[0][1], diff_color[0][2]);
		drawCube(5.5, 0 - levelOffset, 0, 1, 1, 1);
		setFillColor(diff_color[1][0], diff_color[1][1], diff_color[1][2]);
		drawCube(0, 0 - levelOffset, 5.5, 1, 1, 1);
		setFillColor(diff_color[2][0], diff_color[2][1], diff_color[2][2]);
		drawCube(-5.5, 0 - levelOffset, 0, 1, 1, 1);
		setFillColor(diff_color[3][0], diff_color[3][1], diff_color[3][2]);
		drawCube(0, 0 - levelOffset, -5.5, 1, 1, 1);
	}
}

int mouseContained(float x, float y, float sx, float sy) {
	int xpos, ypos;
	glfwGetCursorPos(window, &xpos, &ypos);
	int x1 = ((x - .5 * sx) * 10) + 400;
	int x2 = ((x + .5 * sx) * 10) + 400;
	int y1 = ((y + .5 * sy) * -10) + 300;
	int y2 = ((y - .5 * sy) * -10) + 300;
	return (xpos > x1 && xpos < x2 && ypos > y1 && ypos < y2);
}

void drawPauseMenu() {
	drawLevel();
	hudDrawOn();
	setFillColor(0.8, 0.8, 0.8); // background
	setLayerAlpha(0.8);
	drawCube(0, 0, -1, 80, 60, 1);
	setLayerAlpha(1.0);
	// indicator cube
	if(maxheight >= highscore[difficulty]) { 
		setFillColor(1, 1, 0);
	} else {
		setFillColor(0, 0, 1);
	}
	drawCube(-18.5, 10, 0, 20, 20, 1);
	// buttons
	int contained = 0;
	int clicked = mouse_left;
	mouse_left = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT);
	clicked &= !mouse_left;
	if(dead)
		setFillColor(0.2, 0.2, 0.2);
	else {
		contained = mouseContained(0, -20, 30, 10);
		setFillColor(0, .5 + .5 * contained, 0);
	}
	drawCube((contained && mouse_left), -20 - (contained && mouse_left), 0, 30, 10, 1);
	pause = !(contained && clicked);
	contained = mouseContained(22, -20, 10, 10);
	pause = (contained && clicked) ? 2 : pause;
	setFillColor(.5 + .5 * contained, 0, 0);
	drawCube(22 + (contained && mouse_left), -20 - (contained && mouse_left), 0, 10, 10, 1);
	contained = mouseContained(-22, -20, 10, 10);
	setFillColor(0, 0, .5 + .5 * contained);
	drawCube(-22 + (contained && mouse_left), -20 - (contained && mouse_left), 0, 10, 10, 1);
	if(contained && clicked) {
		setupLevel(seed);
		pause = 0;
	}
	for(int i = 0; i < 4; ++i) {
		contained = mouseContained(35, i * 6 + 2, 5, 5);
		float factor = (2 + contained) * .25;
		if(contained && clicked)
			diff_display = i;
		factor = (diff_display == i) ? 1 : factor;
		setFillColor(diff_color[i][0] * factor, diff_color[i][1] * factor, diff_color[i][2] * factor);
		drawCube(35, i * 6 + 2, 0, 5, 5, 1);
	}
	// score report
	int digits = ((int)log10(highscore[diff_display]));
	float number = highscore[diff_display] / pow(10, digits);
	setFillColor(0.4, 0.4, 0.4);
	for(int i = 0; i < 20; ++i)
		drawCube(25 - i * 1.5, 10, 0, 1, 1, 1);
	setFillColor(diff_color[diff_display][0], diff_color[diff_display][1], diff_color[diff_display][2]);
	for(int i = digits; i >= 0; --i) {
		if(i == 0)
			number = round(number);
		int j = (int)number;
		number = (number - j) * 10.f;
		for(; j > 0; --j) {
			drawCube(25 - i * 1.5, 10 + j * 1.5, 0, 1, 1, 1);
		}
	}
	digits = ((int)log10(maxheight));
	number = maxheight / pow(10, digits);
	setFillColor(0, 0, 1);
	for(int i = digits; i >= 0; --i) {
		if(i == 0)
			number = round(number);
		int j = (int)number;
		number = (number - j) * 10.f;
		for(; j > 0; --j) {
			drawCube(25 - i * 1.5, 10 - j * 1.5, 0, 1, 1, 1);
		}
	}
	hudDrawOff();
}

void drawDifficultySelect() {
	hudDrawOn();
	setFillColor(1, 1, 1);
	drawCube(0, 0, 0, 80, 60, 1);
	for(int i = 0; i < 4; ++i) {
		int contained = mouseContained(-25 + (15 * i), 0, 10, 10);
		setFillColor(diff_color[i][0], diff_color[i][1], diff_color[i][2]);
		drawCube(-25 + (15 * i), 0, 0, 10, 10, 1);
		if(contained && glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT)) {
			difficulty = i;
			setupLevel(seed);
			pause = 0;
		}
	}
	hudDrawOff();
}

int AABBcollide(
	float ax, float ay, float az,
	float asx, float asy, float asz,
	float bx, float by, float bz,
	float bsx, float bsy, float bsz) {
	if(	abs(ax - bx) > (asx + bsx) * .5 ||
		abs(ay - by) > (asy + bsy) * .5 ||
		abs(az - bz) > (asz + bsz) * .5)
		return 0;

	return 1;
}

int playerCollide(int level) {
	return AABBcollide(
		cameraPos.x, cameraPos.y - .5, cameraPos.z,
		0.5, 1, 0.5,
		platforms[level][0], platforms[level][1] - levelOffset, platforms[level][2],
		platforms[level][4], 0.1, platforms[level][4]);
}

void jump() {
	if(jumpCount > 4) {
		playerVel.y = .6;
		jumpCount -= 5;
	}
}

void setupLevel(int s) {
	cameraPos.y = 5;
	cameraPos.x = cameraPos.z = 0;
	playerVel.x = playerVel.y = playerVel.z = 0;
	captureMouse = 0;
	spacebar = 0;
	p_key = 0;
	dead = 0;
	highscore[difficulty] = (maxheight > highscore[difficulty]) ? maxheight : highscore[difficulty];
	levelOffset = 0;
	maxheight = 0;

	srand(s);
	for(int i = 0; i < 20; ++i) {
		platforms[i][0] = ((int)(rand() * random_scale))/2 - 5;
		platforms[i][1] = (i * 5)-.1;
		platforms[i][2] = ((int)(rand() * random_scale))/2 - 5;
		platforms[i][3] = 0;
		platforms[i][4] = 3 - (difficulty > 1) - (difficulty > 2);
	}
}

int main(int argc, char**argv) {
	if(setupGL() == 1)
		return 1;

	char* potato = (argc > 1) ? argv[1] : "potato";
	for(int i = 0; potato[i] != '\0'; ++i) {
		seed += (int)potato[i];
	}
	while(!quit) {
		glfwPollEvents();
		if(glfwGetKey(window, GLFW_KEY_ESC) == GLFW_PRESS && !p_key) {
			if(pause == 2)
				quit = 1;
			pause = dead || !(pause);
			diff_display = difficulty;
			p_key = 1;
		}
		if(glfwGetKey(window, GLFW_KEY_ESC) == GLFW_RELEASE)
			p_key = 0;

		if(pause == 2) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			drawDifficultySelect();
			glfwSwapBuffers();
		}else if(pause) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			drawPauseMenu();
			glfwSwapBuffers();
		} else {
			float movx = (glfwGetKey(window, GLFW_KEY_D) - glfwGetKey(window, GLFW_KEY_A)) * .3;
			float movz = (glfwGetKey(window, GLFW_KEY_W) - glfwGetKey(window, GLFW_KEY_S)) * .3;

			playerVel.x = movz * cameraFoc.x + movx * cameraSid.x;
			playerVel.z = movz * cameraFoc.z + movx * cameraSid.z;
			if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && !spacebar) {
				jump();
				spacebar = 1;
			}
			if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_RELEASE) {
				spacebar = 0;
			}

			updatePlayer();
			
			int xpos = 0, ypos = 0;
			glfwGetCursorPos(window, &xpos, &ypos);
			glfwSetCursorPos(window, 400, 300);
			rotateCamera((xpos - 400) * 0.01, (300 - ypos) * 0.01);


			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			drawLevel();
			drawHud();
			glfwSwapBuffers();
		}
	}
	shutdownGL();
}