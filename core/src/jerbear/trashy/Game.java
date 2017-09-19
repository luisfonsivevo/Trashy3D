package jerbear.trashy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.kotcrab.vis.ui.VisUI;

import jerbear.util2d.dialog.Dialog;
import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;

public class Game extends ApplicationAdapter
{
	private World world;
	private FPSPlayer player;
	
	private Grid grid;
	private EditMenu menu;
	
	@Override
	public void create()
	{
		Bullet.init();
		VisUI.load("skin-vis-x1/uiskin.json");
		Dialog.setSkin(Gdx.files.internal("skin-vis-x1/uiskin.json"));
		
		player = new FPSPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.5f, 0, 1);
		world = new World(player);
		grid = new Grid(world, 5, 1);
		menu = new EditMenu(grid);
		
		Gdx.input.setInputProcessor(grid);
		
		new BoxInstance(new Box(world, 2, 1, 2, Color.RED), 0, -0.5f, 0, CollisionFlags.CF_STATIC_OBJECT, 0);
	}
	
	@Override
	public void render()
	{
		world.draw();
		grid.draw();
		Dialog.draw();
		
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
		
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
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
