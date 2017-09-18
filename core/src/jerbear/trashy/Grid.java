package jerbear.trashy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;

public class Grid extends InputAdapter implements Disposable
{
	private static Vector3 tmpWorld = new Vector3();
	private static Vector3 tmpScrn = new Vector3();
	
	public World world;
	public int size;
	public float zoom;
	
	private ShapeRenderer rend;
	
	private Vector3 selected;
	private Vector3 first;
	
	private boolean isSelected;
	private boolean isFirst;
	
	public Grid(World world, int size, float zoom)
	{
		this.world = world;
		this.size = size;
		this.zoom = zoom;
		
		rend = new ShapeRenderer();
		selected = new Vector3();
		first = new Vector3();
	}
	
	public void draw()
	{
		Camera cam = world.player.getCamera();
		
		rend.setProjectionMatrix(cam.combined);
		rend.begin(ShapeType.Filled);
		rend.setColor(Color.GREEN);
		
		isSelected = false;
		Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		
		for(float x = 0; x < size; x += zoom)
		{
			for(float y = 0; y < size; y += zoom)
			{
				for(float z = 0; z < size; z += zoom)
				{
					float offset = size / 2f - 0.5f;
					Vector3 pos = cam.position;
					tmpWorld.set(Math.round((x - offset + pos.x) * (1f / zoom)) * zoom, Math.round((y - offset + pos.y) * (1f / zoom)) * zoom, Math.round((z - offset + pos.z) * (1f / zoom)) * zoom);
					
					if(cam.frustum.pointInFrustum(tmpWorld))
					{
						cam.project(tmpScrn.set(tmpWorld));
						
						if(center.dst(tmpScrn.x, tmpScrn.y) < 30)
						{
							if(!isSelected)
							{
								rend.setColor(Color.BLUE);
								selected.set(tmpWorld);
								isSelected = true;
							}
							else if(selected.dst(pos) > tmpWorld.dst(pos))
							{
								//recolor the old selected
								rend.setColor(Color.GREEN);
								rend.box(selected.x - 0.025f, selected.y - 0.025f, selected.z + 0.025f, 0.05f, 0.05f, 0.05f);
								
								rend.setColor(Color.BLUE);
								selected.set(tmpWorld);
								isSelected = true;
							}
							else
							{
								rend.setColor(Color.GREEN);
							}
						}
						else
						{
							rend.setColor(Color.GREEN);
						}
						
						rend.box(tmpWorld.x - 0.025f, tmpWorld.y - 0.025f, tmpWorld.z + 0.025f, 0.05f, 0.05f, 0.05f);
					}
				}
			}
		}
		
		rend.end();
		
		if(isSelected && isFirst && !first.equals(selected))
		{
			rend.begin(ShapeType.Line);
			rend.setColor(Color.YELLOW);
			rend.box(first.x, first.y, first.z, selected.x - first.x, selected.y - first.y, -selected.z + first.z);
			rend.end();
		}
		
		rend.setProjectionMatrix(rend.getProjectionMatrix().idt());
		rend.begin(ShapeType.Line);
		rend.setColor(Color.WHITE);
		rend.line(-0.02f, 0, 0.02f, 0);
		rend.line(0, -0.02f, 0, 0.02f);
		rend.end();
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		switch(button)
		{
			case Buttons.LEFT:
				if(isSelected)
				{
					if(!isFirst)
					{
						first.set(selected);
						isFirst = true;
					}
					else
					{
						new BoxInstance(new Box(world, Math.abs(selected.x - first.x), Math.abs(selected.y - first.y), Math.abs(selected.z - first.z), Color.RED), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0);
						isFirst = false;
					}
				}
				break;
			case Buttons.RIGHT:
				isFirst = false;
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean scrolled(int amount)
	{
		if(amount < 0)
			zoom /= 2f;
		else
			zoom *= 2f;
		
		zoom = MathUtils.clamp(zoom, 0.5f, 4);
		
		return true;
	}
	
	@Override
	public void dispose()
	{
		rend.dispose();
	}
}