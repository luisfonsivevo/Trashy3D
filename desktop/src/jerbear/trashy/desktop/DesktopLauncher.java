package jerbear.trashy.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import jerbear.trashy.Game;

public class DesktopLauncher
{
	public static void main(String[] arg)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.title = "Trashy 3D";
		config.width = 800;
		config.height = 600;
		config.resizable = false;
		
		new LwjglApplication(new Game(), config);
	}
}