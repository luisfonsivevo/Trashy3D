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
	private EditorMenu menu;
	private World world;
	private FPSPlayer player;
	
	private SpriteBatch batch;
	private Texture crshair;
	
	@Override
	public void create()
	{
		Bullet.init();
		Dialog.setSkin(Gdx.files.internal("skin-vis-x1/uiskin.json"));
		FileChooser.setDefaultPrefsName("jerbear.trashy3d.filechooser");
		
		player = new FPSPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.5f, 0, 1);
		world = new World(player, 15);
		menu = new EditorMenu(world, player);
		
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
		world.draw(Color.BLACK);
		
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