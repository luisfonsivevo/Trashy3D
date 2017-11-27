package jerbear.trashy.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;

import jerbear.trashy.editor.Editor;
import jerbear.trashy.loader.Undoable.AddShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Capsule;
import jerbear.util3d.shapes.Cone;
import jerbear.util3d.shapes.Cylinder;
import jerbear.util3d.shapes.Rectangle;
import jerbear.util3d.shapes.Sphere;
import jerbear.util3d.shapes.Triangle;

public class Grid extends Tool
{
	private static Vector3 tmp1 = new Vector3();
	private static Vector3 tmp2 = new Vector3();
	private static Vector3 tmp3 = new Vector3();
	private static Matrix4 tmpM = new Matrix4();
	
	public int size;
	public float zoom;
	
	public Color color = Color.RED;
	public boolean flipAxis;
	
	private Editor menu;
	private World world;
	
	private ShapeRenderer rend;
	private GridShape shape = GridShape.BOX;
	
	private Vector3 selected = new Vector3();
	private Vector3 first = new Vector3();
	private Vector3 second = new Vector3();
	
	private boolean isSelected;
	private boolean isFirst;
	private boolean isSecond;
	
	public static enum GridShape
	{
		BOX, RAMP, WALL, TRIANGLE, SPHERE, CYLINDER, CONE, CAPSULE
	}
	
	public Grid(Editor menu, World world, int size, float zoom)
	{
		this.menu = menu;
		this.world = world;
		this.size = size;
		this.zoom = zoom;
		
		rend = new ShapeRenderer();
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
					
					if(shape == GridShape.CYLINDER || shape == GridShape.CONE || shape == GridShape.CAPSULE)
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
		
		if((isSelected || ((shape == GridShape.CYLINDER || shape == GridShape.CONE || shape == GridShape.CAPSULE) && isFirst)) && isFirst)
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
				case WALL:
					rend.line(first.x, first.y, first.z, selected.x, first.y, selected.z);
					rend.line(selected.x, first.y, selected.z, selected.x, selected.y, selected.z);
					rend.line(selected.x, selected.y, selected.z, first.x, selected.y, first.z);
					rend.line(first.x, selected.y, first.z, first.x, first.y, first.z);
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
					float radiusLenSph = first.dst(selected);
					
					tmp1.set(first).x += radiusLenSph;
					renderCircle(first, tmp1, 0);
					
					tmp1.set(first).z += radiusLenSph;
					renderCircle(first, tmp1, 0);
					
					tmp1.set(first).z += radiusLenSph;
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
							
							tmp2.set(second).sub(first).rotate(tmp3, 90).add(selected); //point on the second circle
							tmp1.set(tmp2).sub(selected).add(first); //matching point on the first circle
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
				case CONE:
					if((isFirst && isSelected) || isSecond)
					{
						renderCircle(first, isSecond ? second : selected, 2);
						
						if(isSecond && isSelected)
						{
							tmp1.set(second);
							tmp1.sub(first).rotate(tmp3, 90).add(first); //point on the first circle
							rend.line(selected, tmp1);
							
							tmp1.sub(first).rotate(tmp3, 90).add(first);
							rend.line(selected, tmp1);
							
							tmp1.sub(first).rotate(tmp3, 90).add(first);
							rend.line(selected, tmp1);
							
							tmp1.sub(first).rotate(tmp3, 90).add(first);
							rend.line(selected, tmp1);
						}
						
						if(isSelected)
							rend.line(first, selected);
					}
					break;
				case CAPSULE:
					if((isFirst && isSelected) || isSecond)
					{
						float radiusLenCap = first.dst(isSecond ? second : selected);
						
						if(!isSecond || (isSecond && !isSelected))
						{
							renderCircle(first, isSecond ? second : selected, 2);
						}
						else
						{
							tmp1.set(first).x += radiusLenCap;
							renderCircle(first, tmp1, 0);
							
							tmp1.set(first).z += radiusLenCap;
							renderCircle(first, tmp1, 0);
							
							tmp1.set(first).z += radiusLenCap;
							renderCircle(first, tmp1, 1);
						}
						
						if(isSecond && isSelected)
						{
							tmp1.set(selected).x += radiusLenCap;
							renderCircle(selected, tmp1, 0);
							
							tmp1.set(selected).z += radiusLenCap;
							renderCircle(selected, tmp1, 0);
							
							tmp1.set(selected).z += radiusLenCap;
							renderCircle(selected, tmp1, 1);
							
							getCrsAxis(first, second, 2);
							tmp2.rotate(tmp3, 90).add(selected); //point on the second circle
							tmp1.set(tmp2).sub(selected).add(first); //matching point on the first circle
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
		
		Matrix4 rendMat = rend.getProjectionMatrix();
		rendMat.val[Matrix4.M30] = 0;
		rendMat.val[Matrix4.M31] = 0;
		rendMat.val[Matrix4.M32] = 0;
		rendMat.val[Matrix4.M33] = 1;
		rendMat.val[Matrix4.M23] = 0;
		rendMat.val[Matrix4.M13] = 0;
		rendMat.val[Matrix4.M03] = 0;
		rend.updateMatrices();
		
		rend.begin(ShapeType.Line);
		rend.setColor(Color.RED);
		rend.line(Vector3.Zero, tmp1.set(Vector3.X).scl(0.05f));
		rend.setColor(Color.GREEN);
		rend.line(Vector3.Zero, tmp1.set(Vector3.Y).scl(0.05f));
		rend.setColor(Color.BLUE);
		rend.line(Vector3.Zero, tmp1.set(Vector3.Z).scl(0.05f));
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
									ShapeInstance box = world.addShape(new ShapeInstance(new Box(width, height, depth, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									menu.undoAdd(new AddShape(box));
									resetPoints();
								}
								
								break;
							case RAMP:
								ShapeInstance ramp;
								float lengthramp, pyth;
								
								if(facingAxis())
								{
									lengthramp = Math.abs(selected.x - first.x);
									pyth = (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.z - first.z) * (selected.z - first.z));
									
									if(lengthramp > 0 && pyth > 0)
									{
										ramp = world.addShape(new ShapeInstance(new Rectangle(lengthramp, pyth, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
										ramp.setTransform(ramp.getTransform(tmpM).rotateRad(-1, 0, 0, (float) Math.atan2(selected.y - first.y, selected.z - first.z)));
										
										menu.undoAdd(new AddShape(ramp));
										resetPoints();
									}
								}
								else
								{
									lengthramp = Math.abs(selected.z - first.z);
									pyth = (float) Math.sqrt((selected.y - first.y) * (selected.y - first.y) + (selected.x - first.x) * (selected.x - first.x));
									
									if(lengthramp > 0 && pyth > 0)
									{
										ramp = world.addShape(new ShapeInstance(new Rectangle(pyth, lengthramp, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
										ramp.setTransform(ramp.getTransform(tmpM).rotateRad(0, 0, 1, (float) Math.atan2(selected.y - first.y, selected.x - first.x)));
										
										menu.undoAdd(new AddShape(ramp));
										resetPoints();
									}
								}
								
								break;
							case WALL:
								float lengthwall = Vector3.dst(first.x, first.y, first.z, selected.x, first.y, selected.z);
								float heightwall = Math.abs(selected.y - first.y);
								
								if(lengthwall > 0 && heightwall > 0)
								{
									ShapeInstance wall = world.addShape(new ShapeInstance(new Rectangle(lengthwall, heightwall, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									wall.setTransform(wall.getTransform(tmpM).rotate(Vector3.X, 90).rotateRad(Vector3.Z, (float) Math.atan2(selected.z - first.z, selected.x - first.x)));
									
									menu.undoAdd(new AddShape(wall));
									resetPoints();
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
										tmp1.set(first).add(second).add(selected).scl(1 / 3f); //offset by the centroid to give the triangle a center of rotation
										ShapeInstance tri = world.addShape(new ShapeInstance(new Triangle(first.sub(tmp1), second.sub(tmp1), selected.sub(tmp1), color).disposeByWorld(world), tmp1, CollisionFlags.CF_STATIC_OBJECT, 0));
										
										menu.undoAdd(new AddShape(tri));
										resetPoints();
									}
								}
								break;
							case SPHERE:
								float radiussphere = first.dst(selected);
								if(radiussphere > 0)
								{
									ShapeInstance sphere = world.addShape(new ShapeInstance(new Sphere(radiussphere, 10, 10, color).disposeByWorld(world), first, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									menu.undoAdd(new AddShape(sphere));
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
									float diamcyl = first.dst(second) * 2f;
									float heightcyl = first.dst(selected);
									
									ShapeInstance cyl = world.addShape(new ShapeInstance(new Cylinder(diamcyl, heightcyl, diamcyl, 10, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									if(selected.x - first.x != 0)
										cyl.setTransform(cyl.getTransform(tmpM).rotate(Vector3.Z, 90));
									else if(selected.z - first.z != 0)
										cyl.setTransform(cyl.getTransform(tmpM).rotate(Vector3.X, 90));
									
									menu.undoAdd(new AddShape(cyl));
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
									float radiuscone = first.dst(second);
									float heightcone = first.dst(selected);
									
									ShapeInstance cone = world.addShape(new ShapeInstance(new Cone(radiuscone, heightcone, 10, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									if(selected.x - first.x != 0)
										cone.setTransform(cone.getTransform(tmpM).rotate(Vector3.Z, selected.x - first.x > 0 ? -90 : 90));
									else if(selected.z - first.z != 0)
										cone.setTransform(cone.getTransform(tmpM).rotate(Vector3.X, selected.z - first.z > 0 ? 90 : -90));
									else if(selected.y < first.y)
										cone.setTransform(cone.getTransform(tmpM).rotate(Vector3.X, 180));
									
									menu.undoAdd(new AddShape(cone));
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
									float radiuscap = first.dst(second);
									float heightcap = first.dst(selected) + 2 * radiuscap;
									
									ShapeInstance cap = world.addShape(new ShapeInstance(new Capsule(radiuscap, heightcap, 10, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									if(selected.x - first.x != 0)
										cap.setTransform(cap.getTransform(tmpM).rotate(Vector3.Z, 90));
									else if(selected.z - first.z != 0)
										cap.setTransform(cap.getTransform(tmpM).rotate(Vector3.X, 90));
									
									menu.undoAdd(new AddShape(cap));
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
	
	public GridShape getShape()
	{
		return shape;
	}
	
	public void setShape(GridShape shape)
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
	//mode: 0 = normal, 1 = xz plane, 2 = cylinder
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