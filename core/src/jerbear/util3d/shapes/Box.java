package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;

import jerbear.util3d.World;

public class Box implements Shape
{
	public static final int SIDE_FRONT = 0;
	public static final int SIDE_BACK = 1;
	public static final int SIDE_LEFT = 2;
	public static final int SIDE_RIGHT = 3;
	public static final int SIDE_BOTTOM = 4;
	public static final int SIDE_TOP = 5;
	
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
	
	public Box(World world, float width, float height, float depth)
	{
		this(world, width, height, depth, nullMat());
	}
	
	public Box(World world, float width, float height, float depth, Color colMat)
	{
		this(world, width, height, depth, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Box(World world, float width, float height, float depth, Texture texMat, boolean manageTex)
	{
		this(world, width, height, depth, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Box(World world, float width, float height, float depth, Material mat)
	{
		if(!init) init();
		dim = new Vector3(width / 2f, height / 2f, depth / 2f);
		this.world = world;
		
		if(mat == null)
			return;
		
		modelBuilder.begin();
		
		//BR, UR, UL, BL
		//BL BR UR UL
		
		//front 
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, -height / 2f, depth / 2f,
				width / 2f, -height / 2f, depth / 2f, 
				width / 2f, height / 2f, depth / 2f,
				-width / 2f, height / 2f, depth / 2f, 0, 0, 1);
		
		//back
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				width / 2f, -height / 2f, -depth / 2f,
				-width / 2f, -height / 2f, -depth / 2f,
				-width / 2f, height / 2f, -depth / 2f,
				width / 2f, height / 2f, -depth / 2f, 0, 0, -1);
		
		//left
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, -height / 2f, -depth / 2f,
				-width / 2f, -height / 2f, depth / 2f,
				-width / 2f, height / 2f, depth / 2f,
				-width / 2f, height / 2f, -depth / 2f, -1, 0, 0);
		
		//right
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				width / 2f, -height / 2f, depth / 2f,
				width / 2f, -height / 2f, -depth / 2f,
				width / 2f, height / 2f, -depth / 2f,
				width / 2f, height / 2f, depth / 2f, 1, 0, 0);
		
		//bottom
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, -height / 2f, -depth / 2f,
				width / 2f, -height / 2f, -depth / 2f,
				width / 2f, -height / 2f, depth / 2f,
				-width / 2f, -height / 2f, depth / 2f, 0, -1, 0);
		
		//top
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, height / 2f, depth / 2f,
				width / 2f, height / 2f, depth / 2f,
				width / 2f, height / 2f, -depth / 2f,
				-width / 2f, height / 2f, -depth / 2f, 0, 1, 0);
		
		model = modelBuilder.end();
		
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
	
	public Material getMaterial(int side)
	{
		return model.nodes.get(0).parts.get(side).material;
	}
	
	public Material setMaterial(int side, Material mat)
	{
		model.nodes.get(0).parts.get(side).material = mat;
		return mat;
	}
	
	private static Material nullMat()
	{
		return null;
	}
	
	public static class BoxInstance extends ShapeInstance
	{
		public BoxInstance(Box shape, float x, float y, float z)
		{
			this(shape, x, y, z, -1, 0);
		}
		
		public BoxInstance(Box shape, float x, float y, float z, int collisionFlags, float mass)
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
			btBoxShape shape = null;
			if(collisionFlags != -1)
				shape = new btBoxShape(new Vector3(dim));
			
			super.construct(world, modelInst, shape, collisionFlags, mass);
		}
		
		public Material getMaterial(int side)
		{
			return getInstance().nodes.get(0).parts.get(side).material;
		}
		
		public Material setMaterial(int side, Material mat)
		{
			getInstance().nodes.get(0).parts.get(side).material = mat;
			return mat;
		}
	}
}