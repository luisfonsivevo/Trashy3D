package jerbear.trashy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.kotcrab.vis.ui.widget.file.FileChooser;

import jerbear.util2d.dialog.Dialog;
import jerbear.util3d.World;

public class Game extends ApplicationAdapter
{
	private static Game singleton;
	
	public EditorMenu menu;
	public World world;
	public FPSPlayer player;
	
	private SpriteBatch batch;
	private Texture crshair;
	
	public static Game launch()
	{
		if(singleton != null)
			singleton.dispose();
		
		singleton = new Game();
		return singleton;
	}
	
	public static Game game()
	{
		return singleton;
	}
	
	private Game() {}
	
	@Override
	public void create()
	{
		Bullet.init();
		Dialog.setSkin(Gdx.files.internal("skin-vis-x1/uiskin.json"));
		FileChooser.setDefaultPrefsName("jerbear.trashy3d.filechooser");
		
		player = new FPSPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.5f, 0, 1);
		world = new World(player, 15);
		menu = new EditorMenu();
		
		batch = new SpriteBatch();
		
		Pixmap pix = new Pixmap(24, 24, Format.RGBA4444);
		pix.setColor(Color.WHITE);
		pix.drawLine(0, 10, 20, 10);
		pix.drawLine(10, 0, 10, 20);
		crshair = new Texture(pix);
		pix.dispose();
		
		menu.newf(true, true);
	}
	
	@Override
	public void render()
	{
		world.draw();
		
		batch.begin();
		batch.draw(crshair, (Gdx.graphics.getWidth() - crshair.getWidth()) / 2, (Gdx.graphics.getHeight() - crshair.getHeight()) / 2);
		batch.end();

		menu.draw();
		Dialog.draw();
	}
	
	@Override
	public void dispose()
	{
		crshair.dispose();
		batch.dispose();
		menu.dispose();
		world.dispose();
		Dialog.dispose();
	}
}