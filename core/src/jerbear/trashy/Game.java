package jerbear.trashy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;

import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;

public class Game extends ApplicationAdapter
{
	private World world;
	private FPSPlayer player;
	
	private Grid grid;
	
	@Override
	public void create()
	{
		Bullet.init();
		
		player = new FPSPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.25f, 0, 1);
		world = new World(player);
		grid = new Grid(world, 5, 1);
		
		Gdx.input.setInputProcessor(grid);
		
		new BoxInstance(new Box(world, 2, 1, 2, Color.RED), 0, -0.5f, 0, CollisionFlags.CF_STATIC_OBJECT, 0);
	}
	
	@Override
	public void render()
	{
		world.draw();
		grid.draw();
		
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}
	
	@Override
	public void dispose()
	{
		grid.dispose();
		world.dispose();
	}
}
