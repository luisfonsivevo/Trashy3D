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
import jerbear.util3d.shapes.Cylinder;
import jerbear.util3d.shapes.Cylinder.CylinderInstance;
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
	public boolean flipAxis;
	
	private ShapeRenderer rend;
	private Shape shape = Shape.BOX;
	
	private Vector3 selected = new Vector3();
	private Vector3 first = new Vector3();
	private Vector3 second = new Vector3();
	
	private boolean isSelected;
	private boolean isFirst;
	private boolean isSecond;
	
	public static enum Shape
	{
		BOX, RAMP, TRIANGLE, SPHERE, CYLINDER, CONE, CAPSULE
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
		
		isSelected = false;
		Vector2 center = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		boolean renderFirst = isFirst, renderSecond = isSecond;
		
		for(float x = 0; x < size; x += zoom)
		{
			for(float y = 0; y < size; y += zoom)
			{
				for(float z = 0; z < size; z += zoom)
				{
					float offset = size / 2f - 0.5f;
					Vector3 pos = cam.position;
					tmp1.set(Math.round((x - offset + pos.x) * (1f / zoom)) * zoom, Math.round((y - offset + pos.y) * (1f / zoom)) * zoom, Math.round((z - offset + pos.z) * (1f / zoom)) * zoom);
					
					if(shape == Shape.CYLINDER || shape == Shape.CONE || shape == Shape.CAPSULE)
					{
						if(isFirst && !isSecond)
						{
							//only draw planes
							if(!(tmp1.x == first.x || tmp1.y == first.y || tmp1.z == first.z))
								continue;
						}
						else if(isSecond)
						{
							getCrsAxis(first, second, 2); //outputs to tmp3
							
							//only draw through the axis
							if(!((tmp3.y == 0 && tmp3.z == 0 && tmp1.y == first.y && tmp1.z == first.z) || 
									(tmp3.x == 0 && tmp3.z == 0 && tmp1.x == first.x && tmp1.z == first.z) || 
									(tmp3.x == 0 && tmp3.y == 0 && tmp1.x == first.x && tmp1.y == first.y)))
								continue;
						}
					}
					
					Color shade;
					if(isFirst && tmp1.equals(first))
					{
						renderFirst = false;
						shade = Color.GREEN;
					}
					else if(isSecond && tmp1.equals(second))
					{
						renderSecond = false;
						shade = Color.GREEN;
					}
					else
					{
						shade = new Color(0, 1 - (pos.dst(tmp1) / 2.5f / offset), 0, 1);
					}
					
					if(cam.frustum.pointInFrustum(tmp1))
					{
						cam.project(tmp2.set(tmp1));
						
						if(center.dst(tmp2.x, tmp2.y) < 30)
						{
							if(!isSelected)
							{
								if((isFirst && tmp1.equals(first)) || (isSecond && tmp1.equals(second)))
									rend.setColor(Color.MAGENTA);
								else
									rend.setColor(Color.BLUE);
								
								selected.set(tmp1);
								isSelected = true;
							}
							else if(selected.dst(pos) > tmp1.dst(pos))
							{
								//recolor the old selected
								if((isFirst && selected.equals(first)) || (isSecond && selected.equals(second)))
									rend.setColor(Color.GREEN);
								else
									rend.setColor(shade);
								
								renderBox(selected);
								
								if((isFirst && tmp1.equals(first)) || (isSecond && tmp1.equals(second)))
									rend.setColor(Color.MAGENTA);
								else
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
						
						renderBox(tmp1);
					}
				}
			}
		}
		
		rend.setColor(Color.GREEN);
		
		if(renderFirst)
			renderBox(first);
		
		if(renderSecond)
			renderBox(second);
		
		rend.end();
		
		if((isSelected || ((shape == Shape.CYLINDER || shape == Shape.CONE || shape == Shape.CAPSULE) && isFirst)) && isFirst)
		{
			rend.begin(ShapeType.Line);
			rend.setColor(Color.YELLOW);
			
			switch(shape)
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
				case TRIANGLE:
					rend.line(first, selected);
					if(isSecond)
					{
						rend.line(first, second);
						rend.line(second, selected);
					}
					break;
				case SPHERE:
					float radiusLen = first.dst(selected);
					
					tmp1.set(first).x += radiusLen;
					renderCircle(first, tmp1, 0);
					
					tmp1.set(first).z += radiusLen;
					renderCircle(first, tmp1, 0);
					
					tmp1.set(first).z += radiusLen;
					renderCircle(first, tmp1, 1);
					
					rend.line(first, selected);
					break;
				case CYLINDER:
					if((isFirst && isSelected) || isSecond)
					{
						renderCircle(first, isSecond ? second : selected, 2);
						
						if(isSecond && isSelected)
						{
							tmp1.set(second).add(selected).sub(first); //second radius
							renderCircle(selected, tmp1, 2);
							
							tmp2.set(second).sub(first).rotate(tmp3, 90).add(selected); //a point on the circle
							tmp1.set(tmp2).sub(selected).add(first);
							rend.line(tmp2, tmp1);
							
							tmp2.sub(selected).rotate(tmp3, 90).add(selected);
							tmp1.set(tmp2).sub(selected).add(first);
							rend.line(tmp2, tmp1);
							
							tmp2.sub(selected).rotate(tmp3, 90).add(selected);
							tmp1.set(tmp2).sub(selected).add(first);
							rend.line(tmp2, tmp1);
							
							tmp2.sub(selected).rotate(tmp3, 90).add(selected);
							tmp1.set(tmp2).sub(selected).add(first);
							rend.line(tmp2, tmp1);
						}
						
						if(isSelected)
							rend.line(first, selected);
					}
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
							break;
						
						switch(shape)
						{
							case BOX:
								float width = Math.abs(selected.x - first.x);
								float height = Math.abs(selected.y - first.y);
								float depth = Math.abs(selected.z - first.z);
								
								if(width * height * depth > 0)
								{
									new BoxInstance(new Box(world, width, height, depth, color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									resetPoints();
								}
								
								break;
							case RAMP: //TODO give rectangle its own shape/shapeinstance
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
									box.setTransform(box.getTransform().rotateRad(-1, 0, 0, (float) Math.atan2(selected.y - first.y, selected.z - first.z)));
								}
								else
								{
									box = new BoxInstance(new Box(world, (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.x - first.x) * (selected.x - first.x)), 0, Math.abs(selected.z - first.z), color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									box.setTransform(box.getTransform().rotateRad(0, 0, 1, (float) Math.atan2(selected.y - first.y, selected.x - first.x)));
								}
								
								resetPoints();
								break;
							case TRIANGLE:
								if(!isSecond)
								{
									second.set(selected);
									isSecond = true;
								}
								else
								{
									if(!second.equals(selected))
									{
										new TriangleInstance(new Triangle(world, first, second, selected, color), physicsMode, physicsMode == 0 ? 1 : 0);
										resetPoints();
									}
								}
								break;
							case SPHERE:
								float radiussph = first.dst(selected);
								if(radiussph > 0)
								{
									new SphereInstance(new Sphere(world, radiussph, 10, 10, color), first.x, first.y, first.z, physicsMode, physicsMode == 0 ? 1 : 0);
									resetPoints();
								}
								
								break;
							case CYLINDER:
								if(!isSecond)
								{
									second.set(selected);
									isSecond = true;
								}
								else
								{
									float circcyl = first.dst(second) * 2f;
									float heightcyl = first.dst(selected);
									CylinderInstance cyl = new CylinderInstance(new Cylinder(world, circcyl, heightcyl, circcyl, 10, color), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, physicsMode, physicsMode == 0 ? 1 : 0);
									
									if(selected.x - first.x != 0)
										cyl.setTransform(cyl.getTransform().rotate(Vector3.Z, 90));
									else if(selected.z - first.z != 0)
										cyl.setTransform(cyl.getTransform().rotate(Vector3.X, 90));
									
									resetPoints();
								}
								break;
							case CONE:
								if(!isSecond)
								{
									second.set(selected);
									isSecond = true;
								}
								else
								{
									//TODO finish dis
									//if(!second.equals(selected))
										//make cone
									
									resetPoints();
								}
								break;
							case CAPSULE:
								if(!isSecond)
								{
									second.set(selected);
									isSecond = true;
								}
								else
								{
									//TODO finish dis
									//if(!second.equals(selected))
										//make capsule
									
									resetPoints();
								}
								break;
						}
					}
				}
				break;
			case Buttons.RIGHT:
				resetPoints();
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		if(keycode == Keys.Q)
			flipAxis = !flipAxis;
		
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
	
	public Shape getShape()
	{
		return shape;
	}
	
	public void setShape(Shape shape)
	{
		this.shape = shape;
		resetPoints();
	}
	
	public void resetPoints()
	{
		isFirst = false;
		isSecond = false;
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
		
		return ((dir > 45 && dir < 135) || (dir > 225 && dir < 315)) ^ flipAxis;
	}
	
	private void renderBox(Vector3 pos)
	{
		rend.box(pos.x - 0.025f, pos.y - 0.025f, pos.z + 0.025f, 0.05f, 0.05f, 0.05f);
	}
	
	//warning: uses all 3 tmp vectors (safe to pass tmp1 as radius)
	//mode: 0 = normal, 1 = xz plane, 2 = cylinder
	private void renderCircle(Vector3 center, Vector3 radius, int mode)
	{
		tmp1.set(radius); //tmp1 = previous line
		getCrsAxis(center, radius, mode);
		
		for(int i = 0; i <= 360; i += 8)
		{
			tmp2.rotate(tmp3, 8).add(center); //rotate sub around crs to get new point
			rend.line(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z); //line from prv to new
			tmp1.set(tmp2); //prv = new
			tmp2.sub(center);
		}
	}
	
	//sets tmp2 to radius - center, tmp3 to perpendicular vector
	private void getCrsAxis(Vector3 center, Vector3 radius, int mode)
	{
		tmp2.set(radius).sub(center);
		if(tmp2.x == 0 && tmp2.z == 0) //vertical selection can't be crossed
		{
			if(facingAxis())
				tmp3.set(Vector3.Z);
			else
				tmp3.set(Vector3.X);
		}
		else
		{
			tmp3.set(Vector3.Y);
			if(mode == 0 || (mode == 2 && ((!flipAxis && (tmp2.x == 0 || tmp2.z == 0)) || radius.y != center.y)))
				tmp3.crs(tmp2); //tmp3 = crs
		}
	}
}