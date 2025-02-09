package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;

    private List<Cube> cubes = new ArrayList<>();

    private Cube currentCube;

    private boolean rotating = false;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = glfwCreateWindow(800, 600, "3DGame", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_REPEAT){
                switch (key){
                    case GLFW_KEY_UP -> currentCube.y += 0.05f;
                    case GLFW_KEY_DOWN -> currentCube.y -= 0.05f;
                    case GLFW_KEY_RIGHT -> currentCube.x += 0.05f;
                    case GLFW_KEY_LEFT -> currentCube.x -= 0.05f;
                    case GLFW_KEY_W -> currentCube.z += 0.05f;
                    case GLFW_KEY_S -> currentCube.z -= 0.05f;
                    case GLFW_KEY_ENTER -> {cubes.add(currentCube);
                        currentCube = new Cube(0, 0, -5, 0);
                    }
                    case GLFW_KEY_R -> rotating = !rotating;
                }
            }
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        currentCube = new Cube(0,0, -5, 0);

        for (int i = 0; i < 6; i++) {
            float z = -4.1f - i * 0.3f;
            for (int j = 0; j < 25; j++) {
                float x = -3.4f + j * 0.3f;
                cubes.add(new Cube(x, -1.7f, z, 0));
            }
        }


        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);


        glfwShowWindow(window);
    }
    private void gradientBg(){
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 0, 600,-1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA);

        glDepthMask(false);

        glBegin(GL_QUADS);

        glColor3f(0.0f, 0.3f, 0.7f);
        glVertex2f(0, 0);
        glVertex2f(800, 0);

        glColor3f(0.0f, 0.7f, 1f);
        glVertex2f(800, 600);
        glVertex2f(0, 600);

        glEnd();

        glDepthMask(true);
        glDisable(GL_BLEND);
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

    }
    private void loop() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0), 800.0f / 600.0f, 0.1f, 100f);
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        projection.get(fb);

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);

            gradientBg();

            for (Cube  cube : cubes){
                drawCube(cube);
            }

            drawCube(currentCube);

            if(rotating) {
                currentCube.angle += 0.5f;
            }


            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    private void drawCube(Cube cube){
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity(); // Reset the modelview matrix
        glTranslatef(cube.x, cube.y, cube.z);
        glRotatef(cube.angle, 0f, 1f, 0f);
        glScalef(0.33f, 0.33f, 0.33f);

        // Render the cube
        glBegin(GL_QUADS);
        //Front
        glColor3f(144/255f, 69/255f, 21/255f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        //Back
        glColor3f(145/255f, 72/255f, 16/255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        //Top face
        glColor3f(52/255f, 149/255f, 18/255f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        //bottom
        glColor3f(110/255f, 48/255f, 10/255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        //Right
        glColor3f(138/255f, 65/255f, 10/255f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        //left
        glColor3f(138/255f, 58/255f, 8/255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);

        glEnd();

    }
    public static void main(String[] args) {
        new Main().run();
    }

}