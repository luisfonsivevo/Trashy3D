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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;

import jerbear.util3d.World;

public class Cylinder implements Shape
{
	private static boolean init = false;
	private static ModelBuilder modelBuilder;
	
	private World world;
	private Model model;
	private Vector3 dim;
	
	private static void init()
	{
		modelBuilder = new ModelBuilder();
		init = true;
	}
	
	public Cylinder(World world, float width, float height, float depth)
	{
		this(world, width, height, depth, 0, nullMat());
	}
	
	public Cylinder(World world, float width, float height, float depth, int div, Color colMat)
	{
		this(world, width, height, depth, div, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Cylinder(World world, float width, float height, float depth, int div, Texture texMat, boolean manageTex)
	{
		this(world, width, height, depth, div, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Cylinder(World world, float width, float height, float depth, int div, Material mat)
	{
		if(!init) init();
		dim = new Vector3(width / 2f, height / 2f, depth / 2f);
		this.world = world;
		
		if(mat == null)
			return;
		
		model = modelBuilder.createCylinder(width, height, depth, div, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		
		if(world != null)
			world.disposables.add(this);
	}
	
	public Vector3 getDimensions(Vector3 out)
	{
		return out.set(dim).scl(2);
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
	
	public static class CylinderInstance extends ShapeInstance
	{
		public CylinderInstance(Cylinder shape, float x, float y, float z)
		{
			this(shape, x, y, z, -1, 0);
		}
		
		public CylinderInstance(Cylinder shape, float x, float y, float z, int collisionFlags, float mass)
		{
			if(shape.model == null)
			{
				construct(shape.world, null, shape.dim, collisionFlags, mass);
				return;
			}
			
			ModelInstance modelInst = new ModelInstance(shape.model);
			modelInst.transform.setToTranslation(x, y, z);
			construct(shape.world, modelInst, shape.dim, collisionFlags, mass);
		}
		
		private void construct(World world, ModelInstance modelInst, Vector3 dim, int collisionFlags, float mass)
		{
			btCylinderShape shape = null;
			if(collisionFlags != -1)
				shape = new btCylinderShape(new Vector3(dim));
			
			super.construct(world, modelInst, shape, collisionFlags, mass);
		}
	}
}