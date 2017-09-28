package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;

import jerbear.util3d.World;

public class Capsule implements Shape
{
	private static boolean init = false;
	private static ModelBuilder modelBuilder;
	
	private World world;
	private Model model;
	private float radius;
	private float height;
	
	private static void init()
	{
		modelBuilder = new ModelBuilder();
		init = true;
	}
	
	public Capsule(World world, float radius, float height)
	{
		this(world, radius, height, 0, nullMat());
	}
	
	public Capsule(World world, float radius, float height, int div, Color colMat)
	{
		this(world, radius, height, div, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Capsule(World world, float radius, float height, int div, Texture texMat, boolean manageTex)
	{
		this(world, radius, height, div, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Capsule(World world, float radius, float height, int div, Material mat)
	{
		if(!init) init();
		this.radius = radius;
		this.height = height;
		this.world = world;
		
		if(mat == null)
			return;
		
		model = modelBuilder.createCapsule(radius, height, div, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		
		if(world != null)
			world.disposables.add(this);
	}
	
	public float getRadius()
	{
		return radius;
	}
	
	public float getHeight()
	{
		return height;
	}
	
	@Override
	public Model getModel()
	{
		return model;
	}
	
	@Override
	public void dispose()
	{
		model.dispose();
	}
	
	public Material getMaterial()
	{
		return model.nodes.get(0).parts.get(0).material;
	}
	
	public Material setMaterial(Material mat)
	{
		model.nodes.get(0).parts.get(0).material = mat;
		return mat;
	}
	
	private static Material nullMat()
	{
		return null;
	}
	
	public static class CapsuleInstance extends ShapeInstance
	{
		public CapsuleInstance(Capsule shape, float x, float y, float z)
		{
			this(shape, x, y, z, -1, 0);
		}
		
		public CapsuleInstance(Capsule shape, float x, float y, float z, int collisionFlags, float mass)
		{
			if(shape.model == null)
			{
				construct(shape.world, shape, null, shape.radius, shape.height, collisionFlags, mass);
				return;
			}
			
			ModelInstance modelInst = new ModelInstance(shape.model);
			modelInst.transform.setToTranslation(x, y, z);
			construct(shape.world, shape, modelInst, shape.radius, shape.height, collisionFlags, mass);
		}
		
		private void construct(World world, Shape shape, ModelInstance modelInst, float radius, float height, int collisionFlags, float mass)
		{
			btCapsuleShape shapeCol = null;
			if(collisionFlags != -1)
				//Bullet only counts height of the cylinder, so subtract off the hemispheres
				shapeCol = new btCapsuleShape(radius, height - 2 * radius);
			
			super.construct(world, shape, modelInst, shapeCol, collisionFlags, mass);
		}
	}
}