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
import jerbear.trashy.loader.AddShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.*;

public class Grid extends Tool
{
	private static Vector3 tmp1 = new Vector3();
	private static Vector3 tmp2 = new Vector3();
	private static Vector3 tmp3 = new Vector3();
	private static Matrix4 tmpM = new Matrix4();
	
	public int size;
	public float zoom;
	public final Vector3 offset = new Vector3();
	
	public Color color = Color.RED;
	public boolean flipAxis;
	
	private Editor editor;
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
		BOX, RAMP, HULL, SPHERE, CYLINDER, CONE, CAPSULE
	}
	
	public Grid(Editor editor, World world, int size, float zoom)
	{
		this.editor = editor;
		this.world = world;
		
		this.size = size;
		this.zoom = zoom;
		
		float offset = 0.5f - (size / 2f);
		this.offset.set(offset, offset, offset);
		
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
					Vector3 pos = cam.position;
					tmp1.set(Math.round((x + pos.x) / zoom), Math.round((y + pos.y) / zoom), Math.round((z + pos.z) / zoom)).scl(zoom).add(offset);
					
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
					else if(shape == GridShape.RAMP && isSecond)
					{
						//only draw 8 corners
						if(!(tmp1.equals(tmp2.set(first.x, first.y, first.z)) ||
								tmp1.equals(tmp2.set(first.x, first.y, second.z)) ||
								tmp1.equals(tmp2.set(first.x, second.y, first.z)) ||
								tmp1.equals(tmp2.set(second.x, first.y, first.z)) ||
								tmp1.equals(tmp2.set(second.x, second.y, second.z)) ||
								tmp1.equals(tmp2.set(second.x, second.y, first.z)) ||
								tmp1.equals(tmp2.set(second.x, first.y, second.z)) ||
								tmp1.equals(tmp2.set(first.x, second.y, second.z))))
							continue;
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
						shade = new Color(0, 1 - (pos.dst(tmp1) / 2f / offset.len()), 0, 1);
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
		
		if((isSelected || ((shape == GridShape.RAMP || shape == GridShape.CYLINDER || shape == GridShape.CONE || shape == GridShape.CAPSULE) && isFirst)) && isFirst)
		{
			rend.begin(ShapeType.Line);
			rend.setColor(Color.YELLOW);
			
			switch(shape)
			{
				case BOX:
					rend.box(first.x, first.y, first.z, selected.x - first.x, selected.y - first.y, -selected.z + first.z);
					break;
				case RAMP:
					if((isFirst && isSelected) || isSecond)
					{
						if(facingAxis())
						{
							Vector3 other = isSecond ? second : selected;
							rend.line(other.x, other.y, other.z, first.x, other.y, other.z);
							rend.line(first.x, other.y, other.z, first.x, first.y, first.z);
							rend.line(first.x, first.y, first.z, other.x, first.y, first.z);
							rend.line(other.x, first.y, first.z, other.x, other.y, other.z);
							
							if(isSecond && isSelected)
							{
								if(selected.y == first.y)
								{
									rend.line(second.x, second.y, second.z, second.x, first.y, second.z);
									rend.line(first.x, second.y, second.z, first.x, first.y, second.z);
									rend.line(first.x, first.y, first.z, first.x, first.y, second.z);
									rend.line(second.x, first.y, first.z, second.x, first.y, second.z);
									rend.line(first.x, first.y, second.z, second.x, first.y, second.z);
								}
								else if(selected.y == second.y)
								{
									rend.line(first.x, first.y, first.z, first.x, second.y, first.z);
									rend.line(second.x, first.y, first.z, second.x, second.y, first.z);
									rend.line(second.x, second.y, second.z, second.x, second.y, first.z);
									rend.line(first.x, second.y, second.z, first.x, second.y, first.z);
									rend.line(first.x, second.y, first.z, second.x, second.y, first.z);
								}
							}
						}
						else
						{
							Vector3 other = isSecond ? second : selected;
							rend.line(other.x, other.y, other.z, other.x, other.y, first.z);
							rend.line(other.x, other.y, first.z, first.x, first.y, first.z);
							rend.line(first.x, first.y, first.z, first.x, first.y, other.z);
							rend.line(first.x, first.y, other.z, other.x, other.y, other.z);
							
							if(isSecond && isSelected)
							{
								if(selected.y == first.y)
								{
									rend.line(second.x, second.y, second.z, second.x, first.y, second.z);
									rend.line(second.x, second.y, first.z, second.x, first.y, first.z);
									rend.line(first.x, first.y, first.z, second.x, first.y, first.z);
									rend.line(first.x, first.y, second.z, second.x, first.y, second.z);
									rend.line(second.x, first.y, first.z, second.x, first.y, second.z);
								}
								else if(selected.y == second.y)
								{
									rend.line(first.x, first.y, first.z, first.x, second.y, first.z);
									rend.line(first.x, first.y, second.z, first.x, second.y, second.z);
									rend.line(second.x, second.y, second.z, first.x, second.y, second.z);
									rend.line(second.x, second.y, first.z, first.x, second.y, first.z);
									rend.line(first.x, second.y, first.z, first.x, second.y, second.z);
								}
							}
						}
						
						if(!isSecond)
							rend.line(first, selected);
					}
					break;
				case HULL:
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
								float widthbox = Math.abs(selected.x - first.x);
								float heightbox = Math.abs(selected.y - first.y);
								float depthbox = Math.abs(selected.z - first.z);
								
								if(widthbox * heightbox * depthbox > 0)
								{
									ShapeInstance box = world.addShape(new ShapeInstance(new Box(widthbox, heightbox, depthbox, color).disposeByWorld(world), (selected.x + first.x) / 2f, (selected.y + first.y) / 2f, (selected.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									editor.undoAdd(new AddShape(box));
									resetPoints();
								}
								
								break;
							case RAMP:
								if(!isSecond && selected.x != first.x && selected.y != first.y && selected.z != first.z)
								{
									second.set(selected);
									isSecond = true;
								}
								else if(isSecond)
								{
									float widthramp = Math.abs(second.x - first.x);
									float heightramp = Math.abs(second.y - first.y);
									float depthramp = Math.abs(second.z - first.z);
								
									if(widthramp * heightramp * depthramp > 0)
									{
										ShapeInstance ramp;
										if(facingAxis())
										{
											ramp = world.addShape(new ShapeInstance(new Ramp(widthramp, heightramp, depthramp, color).disposeByWorld(world), (second.x + first.x) / 2f, (second.y + first.y) / 2f, (second.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
											if((first.z < second.z && second.y > first.y) || (second.z < first.z && first.y > second.y))
												ramp.setTransform(ramp.getTransform(tmpM).rotate(Vector3.Y, 180));
										}
										else
										{
											ramp = world.addShape(new ShapeInstance(new Ramp(depthramp, heightramp, widthramp, color).disposeByWorld(world), (second.x + first.x) / 2f, (second.y + first.y) / 2f, (second.z + first.z) / 2f, CollisionFlags.CF_STATIC_OBJECT, 0));
											if((first.x < second.x && second.y > first.y) || (second.x < first.x && first.y > second.y))
												ramp.setTransform(ramp.getTransform(tmpM).rotate(Vector3.Y, -90));
											else
												ramp.setTransform(ramp.getTransform(tmpM).rotate(Vector3.Y, 90));
										}
										
										if((first.y < second.y && second.y == selected.y) || (second.y < first.y && first.y == selected.y))
											ramp.setTransform(ramp.getTransform(tmpM).rotate(Vector3.X, 180));
										
										editor.undoAdd(new AddShape(ramp));
										resetPoints();
									}
								}
								
								break;
							case HULL:
								break;
							case SPHERE:
								float radiussphere = first.dst(selected);
								if(radiussphere > 0)
								{
									ShapeInstance sphere = world.addShape(new ShapeInstance(new Sphere(radiussphere, 10, 10, color).disposeByWorld(world), first, CollisionFlags.CF_STATIC_OBJECT, 0));
									
									editor.undoAdd(new AddShape(sphere));
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
									
									editor.undoAdd(new AddShape(cyl));
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
									
									editor.undoAdd(new AddShape(cone));
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
									
									editor.undoAdd(new AddShape(cap));
									resetPoints();
								}
								break;
						}
					}
				}
				break;
			case Buttons.RIGHT:
				//TODO only undo one point - useful for hulls
				resetPoints();
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode)
	{
		switch(keycode)
		{
			case Keys.Q:
				flipAxis = !flipAxis;
				break;
			case Keys.LEFT:
				int ldir = facingAxisDir();
				switch(ldir)
				{
					case 0:
						offset.z -= zoom / 4f;
						break;
					case 1:
						offset.x -= zoom / 4f;
						break;
					case 2:
						offset.z += zoom / 4f;
						break;
					case 3:
						offset.x += zoom / 4f;
						break;
				}
				break;
			case Keys.RIGHT:
				int rdir = facingAxisDir();
				switch(rdir)
				{
					case 0:
						offset.z += zoom / 4f;
						break;
					case 1:
						offset.x += zoom / 4f;
						break;
					case 2:
						offset.z -= zoom / 4f;
						break;
					case 3:
						offset.x -= zoom / 4f;
						break;
				}
				break;
			case Keys.UP:
				offset.y += zoom / 4f;
				break;
			case Keys.DOWN:
				offset.y -= zoom / 4f;
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
		return ((dir > 45 && dir < 135) || (dir > -135 && dir < -45)) ^ flipAxis;
	}
	
	//0 = +x, 1 = -z, 2 = -x, 3 = +z
	private int facingAxisDir()
	{
		float dir = (float) (MathUtils.radDeg * Math.atan2(-world.player.getCamera().direction.z, world.player.getCamera().direction.x));
		if(dir < 0) dir += 360;
		
		if(dir < 45 || dir > 315)
			return 0;
		else if(dir > 45 && dir < 135)
			return 1;
		else if(dir > 135 && dir < 225)
			return 2;
		else if(dir > 225 && dir < 315)
			return 3;
		else
			return 69; //help
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