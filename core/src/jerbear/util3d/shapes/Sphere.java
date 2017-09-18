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
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

import jerbear.util3d.World;

public class Sphere implements Shape
{
	private static boolean init = false;
	private static ModelBuilder modelBuilder;
	
	private World world;
	private Model model;
	private float radius;
	
	private static void init()
	{
		modelBuilder = new ModelBuilder();
		init = true;
	}
	
	public Sphere(World world, float radius)
	{
		this(world, radius, 0, 0, nullMat());
	}
	
	public Sphere(World world, float radius, int div, Color colMat)
	{
		this(world, radius, div, div, colMat);
	}
	
	public Sphere(World world, float radius, int div, Texture texMat, boolean manageTex)
	{
		this(world, radius, div, div, texMat, manageTex);
	}
	
	public Sphere(World world, float radius, int divU, int divV, Color colMat)
	{
		this(world, radius, divU, divV, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Sphere(World world, float radius, int divU, int divV, Texture texMat, boolean manageTex)
	{
		this(world, radius, divU, divV, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Sphere(World world, float radius, int divU, int divV, Material mat)
	{
		if(!init) init();
		this.radius = radius;
		this.world = world;
		
		if(mat == null)
			return;
		
		model = modelBuilder.createSphere(radius * 2, radius * 2, radius *2, divU, divV, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		
		if(world != null)
			world.disposables.add(this);
	}
	
	public float getRadius()
	{
		return radius;
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
	
	public static class SphereInstance extends ShapeInstance
	{
		public SphereInstance(Sphere shape, float x, float y, float z)
		{
			this(shape, x, y, z, -1, 0);
		}
		
		public SphereInstance(Sphere shape, float x, float y, float z, int collisionFlags, float mass)
		{
			if(shape.model == null)
			{
				construct(shape.world, null, shape.radius, collisionFlags, mass);
				return;
			}
			
			ModelInstance modelInst = new ModelInstance(shape.model);
			modelInst.transform.setToTranslation(x, y, z);
			construct(shape.world, modelInst, shape.radius, collisionFlags, mass);
		}
		
		private void construct(World world, ModelInstance modelInst, float radius, int collisionFlags, float mass)
		{
			btSphereShape shape = null;
			if(collisionFlags != -1)
				shape = new btSphereShape(radius);
			
			super.construct(world, modelInst, shape, collisionFlags, mass);
		}
	}
}