package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btTriangleShape;

import jerbear.util3d.World;

public class Triangle implements Shape
{
	private static boolean init = false;
	private static ModelBuilder modelBuilder;
	
	private World world;
	private Model model;
	private Vector3 v1, v2, v3;
	
	private static void init()
	{
		modelBuilder = new ModelBuilder();
		init = true;
	}
	
	public Triangle(World world, Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this(world, v1, v2, v3, null);
	}
	
	public Triangle(World world, Vector3 v1, Vector3 v2, Vector3 v3, Color col)
	{
		if(!init) init();
		this.v1 = new Vector3(v1);
		this.v2 = new Vector3(v2);
		this.v3 = new Vector3(v3);
		this.world = world;
		
		if(col == null)
			return;
		
		modelBuilder.begin();
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(col))).triangle(v1, Color.WHITE, v2, Color.WHITE, v3, Color.WHITE);
		modelBuilder.part("rect", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(col))).triangle(v3, Color.WHITE, v2, Color.WHITE, v1, Color.WHITE);
		model = modelBuilder.end();
		
		if(world != null)
			world.disposables.add(this);
	}
	
	public Vector3 getPoint(Vector3 out, int num)
	{
		switch(num)
		{
			case 0:
				return out.set(v1);
			case 1:
				return out.set(v2);
			case 2:
				return out.set(v3);
			default:
				throw new IllegalArgumentException("num arg must be 0, 1, or 2");
		}
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
	
	public static class TriangleInstance extends ShapeInstance
	{
		public TriangleInstance(Triangle shape)
		{
			this(shape, -1, 0);
		}
		
		public TriangleInstance(Triangle shape, int collisionFlags, float mass)
		{
			if(shape.model == null)
			{
				construct(shape.world, shape, null, shape.v1, shape.v2, shape.v3, collisionFlags, mass);
				return;
			}
			
			ModelInstance modelInst = new ModelInstance(shape.model);
			construct(shape.world, shape, modelInst, shape.v1, shape.v2, shape.v3, collisionFlags, mass);
		}
		
		private void construct(World world, Shape shape, ModelInstance modelInst, Vector3 v1, Vector3 v2, Vector3 v3, int collisionFlags, float mass)
		{
			btTriangleShape shapeCol = null;
			if(collisionFlags != -1)
				shapeCol = new btTriangleShape(v1, v2, v3);
			
			super.construct(world, shape, modelInst, shapeCol, collisionFlags, mass);
		}
	}
}