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
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btTriangleShape;

import jerbear.util3d.World;

public class Triangle implements Shape
{
	public static final int SIDE_BOTTOM = 0;
	public static final int SIDE_TOP = 1;
	
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
		this(world, v1, v2, v3, nullMat());
	}
	
	public Triangle(World world, Vector3 v1, Vector3 v2, Vector3 v3, Color colMat)
	{
		this(world, v1, v2, v3, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Triangle(World world, Vector3 v1, Vector3 v2, Vector3 v3, Texture texMat, boolean manageTex)
	{
		this(world, v1, v2, v3, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Triangle(World world, Vector3 v1, Vector3 v2, Vector3 v3, Material mat)
	{
		if(!init) init();
		this.v1 = new Vector3(v1);
		this.v2 = new Vector3(v2);
		this.v3 = new Vector3(v3);
		this.world = world;
		
		if(mat == null)
			return;
		
		VertexInfo vinf1 = new VertexInfo();
		vinf1.position.set(v3).sub(v1); //dirty haxx - use pos as a tmp vector for calculating the normal
		vinf1.setNor(v2).normal.sub(v1).crs(vinf1.position).nor();
		vinf1.setPos(v1);
		
		VertexInfo vinf2 = new VertexInfo();
		vinf2.setNor(vinf1.normal);
		vinf2.setPos(v2);
		
		VertexInfo vinf3 = new VertexInfo();
		vinf3.setNor(vinf1.normal);
		vinf3.setPos(v3);
		
		modelBuilder.begin();
		modelBuilder.part("tri", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(vinf1, vinf2, vinf3);
		
		vinf1.normal.scl(-1);
		vinf2.normal.scl(-1);
		vinf3.normal.scl(-1);
		
		modelBuilder.part("tri", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(vinf3, vinf2, vinf1);
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
	
	private static Material nullMat()
	{
		return null;
	}
	
	public static class TriangleInstance extends ShapeInstance
	{
		public TriangleInstance(Triangle shape, float x, float y, float z)
		{
			this(shape, x, y, z, -1, 0);
		}
		
		public TriangleInstance(Triangle shape, float x, float y, float z, int collisionFlags, float mass)
		{
			if(shape.model == null)
			{
				construct(shape.world, shape, null, shape.v1, shape.v2, shape.v3, collisionFlags, mass);
				return;
			}
			
			ModelInstance modelInst = new ModelInstance(shape.model);
			modelInst.transform.setToTranslation(x, y, z);
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