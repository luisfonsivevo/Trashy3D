package jerbear.trashy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.bullet.Bullet;

import jerbear.util2d.dialog.Dialog;
import jerbear.util3d.World;

public class Game extends ApplicationAdapter
{
	private static Game singleton;
	
	public EditorMenu menu;
	public World world;
	
	private FPSPlayer player;
	
	private Grid grid;
	
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
		
		player = new FPSPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.5f, 0, 1);
		world = new World(player, 15);
		grid = new Grid(world, 5, 1);
		menu = new EditorMenu(grid);
		
		Gdx.input.setInputProcessor(grid);
		menu.newf(true);
	}
	
	@Override
	public void render()
	{
		world.draw();
		grid.draw();
		Dialog.draw();
		menu.shortcutCheck();
		
		if(Gdx.input.isKeyJustPressed(Keys.ALT_LEFT))
		{
			if(Gdx.input.getInputProcessor() == grid)
			{
				Dialog.setFocus();
				Gdx.input.setCursorCatched(false);
				player.pause = true;
			}
			else
			{
				Gdx.input.setInputProcessor(grid);
				Gdx.input.setCursorCatched(true);
				player.pause = false;
			}
		}
	}
	
	@Override
	public void dispose()
	{
		menu.dispose();
		grid.dispose();
		world.dispose();
		Dialog.dispose();
	}
}
