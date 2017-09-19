package jerbear.trashy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
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
import jerbear.util3d.shapes.Sphere;
import jerbear.util3d.shapes.Sphere.SphereInstance;
import jerbear.util3d.shapes.Triangle;
import jerbear.util3d.shapes.Triangle.TriangleInstance;;

public class Grid extends InputAdapter implements Disposable
{
	private static Vector3 tmp1 = new Vector3();
	private static Vector3 tmp2 = new Vector3();
	private static Vector3 tmp3 = new Vector3();
	
	public World world;
	public int size;
	public float zoom;
	
	public int physicsMode = CollisionFlags.CF_STATIC_OBJECT;
	public Color color = Color.RED;
	public Shape shapeMode = Shape.BOX;
	
	private ShapeRenderer rend;
	
	private Vector3 selected = new Vector3();
	private Vector3 first = new Vector3();
	private Vector3 second = new Vector3();
	
	private boolean isSelected;
	private boolean isFirst;
	private boolean isSecond;
	
	private boolean flipFacing;
	
	public static enum Shape
	{
		BOX, RAMP, TRI, SPHERE
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
					tmp1.set(Math.round((x - offset + pos.x) * (1f / zoom)) * zoom, Math.round((y - offset + pos.y) * (1f / zoom)) * zoom, Math.round((z - offset + pos.z) * (1f / zoom)) * zoom);
					
					Color shade = new Color(0, 1 - (pos.dst(tmp1) / 2.5f / offset), 0, 1);
					
					if(cam.frustum.pointInFrustum(tmp1))
					{
						cam.project(tmp2.set(tmp1));
						
						if(center.dst(tmp2.x, tmp2.y) < 30)
						{
							if(!isSelected)
							{
								rend.setColor(Color.BLUE);
								selected.set(tmp1);
								isSelected = true;
							}
							else if(selected.dst(pos) > tmp1.dst(pos))
							{
								//recolor the old selected
								rend.setColor(shade);
								rend.box(selected.x - 0.025f, selected.y - 0.025f, selected.z + 0.025f, 0.05f, 0.05f, 0.05f);
								
								rend.setColor(Color.BLUE);
								selected.set(tmp1);
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
						
						rend.box(tmp1.x - 0.025f, tmp1.y - 0.025f, tmp1.z + 0.025f, 0.05f, 0.05f, 0.05f);
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
					
					rend.line(first, selected);
					break;
				case TRI:
					rend.line(first, selected);
					if(isSecond)
					{
						rend.line(first, second);
						rend.line(second, selected);
					}
					break;
				case SPHERE:
					tmp1.set(selected); //tmp1 = previous line
					for(int i = 0; i <= 360; i += 8)
					{
						tmp2.set(selected).sub(first); //tmp2 = sub
						
						if(tmp2.x == 0 && tmp2.z == 0) //vertical selection can't be crossed
						{
							if(facingAxis())
								tmp3.set(Vector3.Z);
							else
								tmp3.set(Vector3.X);
						}
						else
						{
							tmp3.set(Vector3.Y).crs(tmp2); //tmp3 = crs
						}
						
						tmp2.rotate(tmp3, i).add(first); //rotate sub around crs to get new point
						rend.line(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z); //line from prv to new
						tmp1.set(tmp2); //prv = new
					}
					
					tmp1.set(selected).sub(first).rotate(Vector3.Y, 90).add(first);
					for(int i = 0; i <= 360; i += 8)
					{
						tmp2.set(selected).sub(first); //tmp2 = sub
						
						if(tmp2.x == 0 && tmp2.z == 0) //vertical selection can't be crossed
						{
							if(facingAxis())
								tmp3.set(Vector3.Z);
							else
								tmp3.set(Vector3.X);
						}
						else
						{
							tmp3.set(Vector3.Y).crs(tmp2); //tmp3 = crs
						}
						
						tmp2.rotate(tmp3, i).rotate(Vector3.Y, 90).add(first); //rotate sub around crs to get new point
						rend.line(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z); //line from prv to new
						tmp1.set(tmp2); //prv = new
					}
					
					tmp1.set(selected).sub(first).rotate(Vector3.X, 90).add(first);
					for(int i = 0; i <= 360; i += 8)
					{
						tmp2.set(selected).sub(first); //tmp2 = sub
						
						if(tmp2.x == 0 && tmp2.z == 0) //vertical selection can't be crossed
						{
							if(facingAxis())
								tmp3.set(Vector3.Z);
							else
								tmp3.set(Vector3.X);
						}
						else
						{
							tmp3.set(Vector3.Y).crs(tmp2); //tmp3 = crs
						}
						
						tmp2.rotate(tmp3, i).rotate(Vector3.X, 90).add(first); //rotate sub around crs to get new point
						rend.line(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z); //line from prv to new
						tmp1.set(tmp2); //prv = new
					}
					
					rend.line(first, selected);
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
						if(first.equals(selected))
						{
							isFirst = false;
							isSecond = false;
							break;
						}
						
						switch(shapeMode)
						{
							case BOX:
								new BoxInstance(new Box(world, Math.abs(selected.x - first.x), Math.abs(selected.y - first.y), Math.abs(selected.z - first.z), color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
								isFirst = false;
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
								
								BoxInstance box;
								if(facingAxis())
								{
									box = new BoxInstance(new Box(world, Math.abs(selected.x - first.x), 0, (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.z - first.z) * (selected.z - first.z)), color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									if(top.z > bottom.z && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(1, 0, 0, -45));
									else if(top.z < bottom.z && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(1, 0, 0, 45));
									else if(top.z == bottom.z)
										box.setTransform(box.getTransform().rotate(1, 0, 0, 90));
								}
								else
								{
									box = new BoxInstance(new Box(world, (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.x - first.x) * (selected.x - first.x)), 0, Math.abs(selected.z - first.z), color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									if(top.x > bottom.x && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(0, 0, 1, 45));
									else if(top.x < bottom.x && top.y != bottom.y)
										box.setTransform(box.getTransform().rotate(0, 0, 1, -45));
									else if(top.x == bottom.x)
										box.setTransform(box.getTransform().rotate(0, 0, 1, 90));
								}
								isFirst = false;
								break;
							case TRI:
								if(!isSecond)
								{
									second.set(selected);
									isSecond = true;
								}
								else
								{
									if(!second.equals(selected))
										new TriangleInstance(new Triangle(world, first, second, selected, color), physicsMode, physicsMode == 0 ? 1 : 0);
									
									isFirst = false;
									isSecond = false;
								}
								break;
							case SPHERE:
								new SphereInstance(new Sphere(world, first.dst(selected), 10, 10, color), first.x, first.y, first.z, physicsMode, physicsMode == 0 ? 1 : 0);
								isFirst = false;
								break;
						}
					}
				}
				break;
			case Buttons.RIGHT:
				isFirst = false;
				isSecond = false;
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		if(keycode == Keys.Q)
			flipFacing = !flipFacing;
		
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
		
		return ((dir > 45 && dir < 135) || (dir > 225 && dir < 315)) ^ flipFacing;
	}
}