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
	
	public int physicsMode = CollisionFlags.CF_STATIC_OBJECT;
	public Shape shapeMode = Shape.BOX;
	
	private ShapeRenderer rend;
	
	private Vector3 selected;
	private Vector3 first;
	
	private boolean isSelected;
	private boolean isFirst;
	
	public static enum Shape
	{
		BOX, RAMP
	}
	
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
					
					Color shade = new Color(0, 1 - (pos.dst(tmpWorld) / 2.5f / offset), 0, 1);
					
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
								rend.setColor(shade);
								rend.box(selected.x - 0.025f, selected.y - 0.025f, selected.z + 0.025f, 0.05f, 0.05f, 0.05f);
								
								rend.setColor(Color.BLUE);
								selected.set(tmpWorld);
								isSelected = true;
							}
							else
							{
								rend.setColor(shade);
							}
						}
						else
						{
							rend.setColor(shade);
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
			
			switch(shapeMode)
			{
				case BOX:
					rend.box(first.x, first.y, first.z, selected.x - first.x, selected.y - first.y, -selected.z + first.z);
					break;
				case RAMP:
					if(facingAxis())
					{
						rend.line(selected.x, selected.y, selected.z, first.x, selected.y, selected.z);
						rend.line(first.x, selected.y, selected.z, first.x, first.y, first.z);
						rend.line(first.x, first.y, first.z, selected.x, first.y, first.z);
						rend.line(selected.x, first.y, first.z, selected.x, selected.y, selected.z);
					}
					else
					{
						rend.line(selected.x, selected.y, selected.z, selected.x, selected.y, first.z);
						rend.line(selected.x, selected.y, first.z, first.x, first.y, first.z);
						rend.line(first.x, first.y, first.z, first.x, first.y, selected.z);
						rend.line(first.x, first.y, selected.z, selected.x, selected.y, selected.z);
					}
					
					rend.line(selected.x, selected.y, selected.z, first.x, first.y, first.z);
					break;
			}
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
						switch(shapeMode)
						{
							case BOX:
								new BoxInstance(new Box(world, Math.abs(selected.x - first.x), Math.abs(selected.y - first.y), Math.abs(selected.z - first.z), Color.RED), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
								break;
							case RAMP:
								Vector3 top, bottom;
								if(first.y > selected.y)
								{
									top = first;
									bottom = selected;
								}
								else
								{
									top = selected;
									bottom = first;
								}
								
								if(facingAxis())
								{
									BoxInstance box = new BoxInstance(new Box(world, Math.abs(selected.x - first.x), 0, (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.z - first.z) * (selected.z - first.z)), Color.RED), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									if(top.z > bottom.z && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(1, 0, 0, -45));
									else if(top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(1, 0, 0, 45));
								}
								else
								{
									BoxInstance box = new BoxInstance(new Box(world, (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.x - first.x) * (selected.x - first.x)), 0, Math.abs(selected.z - first.z), Color.RED), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									if(top.x > bottom.x && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(0, 0, 1, 45));
									else if(top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(0, 0, 1, -45));
								}
								break;
						}
						
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
	
	//true for z, false for x
	private boolean facingAxis()
	{
		float dir = (float) (MathUtils.radDeg * Math.atan2(-world.player.getCamera().direction.z, world.player.getCamera().direction.x));
		if(dir < 0) dir += 360;
		
		return (dir > 45 && dir < 135) || (dir > 225 && dir < 315);
	}
}