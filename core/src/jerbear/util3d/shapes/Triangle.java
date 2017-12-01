package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleShape;

import jerbear.util3d.World;

//TODO make this "Polygon" not "Triangle"
public class Triangle implements Shape
{
	public static final int SIDE_FRONT = 0;
	public static final int SIDE_BACK = 1;
	
	private Vector3 v1, v2, v3, nor;
	private Model model;
	private btTriangleShape colShape;
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3)
	{
		this(v1, v2, v3, (Material) null);
	}
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3, Color colMat)
	{
		this(v1, v2, v3, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3, Texture texMat, boolean manageTex)
	{
		this(v1, v2, v3, new Material(TextureAttribute.createDiffuse(texMat), new BlendingAttribute()));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Triangle(Vector3 v1, Vector3 v2, Vector3 v3, Material mat)
	{
		colShape = new btTriangleShape(v1, v2, v3);
		this.v1 = new Vector3(v3).sub(v1); //dirty haxx - use v1 as a tmp vector for calculating the normal
		this.nor = new Vector3(v2).sub(v1).crs(this.v1).nor();
		
		this.v1.set(v1);
		this.v2 = new Vector3(v2);
		this.v3 = new Vector3(v3);
		
		if(mat == null)
			return;
		
		VertexInfo vinf1 = new VertexInfo();
		vinf1.setNor(nor);
		vinf1.setPos(v1);
		
		VertexInfo vinf2 = new VertexInfo();
		vinf2.setNor(nor);
		vinf2.setPos(v2);
		
		VertexInfo vinf3 = new VertexInfo();
		vinf3.setNor(nor);
		vinf3.setPos(v3);
		
		modelBuilder.begin();
		modelBuilder.part("tri", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(vinf1, vinf2, vinf3);
		
		vinf1.normal.scl(-1);
		vinf2.normal.scl(-1);
		vinf3.normal.scl(-1);
		
		modelBuilder.part("tri", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(vinf3, vinf2, vinf1);
		model = modelBuilder.end();
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
	
	public Vector3 getNormal(Vector3 out, boolean flip)
	{
		out.set(nor);
		if(flip)
			out.scl(-1);
		
		return out;
	}
	
	@Override
	public Model getModel()
	{
		return model;
	}
	
	@Override
	public btCollisionShape getCollisionShape()
	{
		return colShape;
	}
	
	@Override
	public Material getMaterial(int part)
	{
		if(model != null)
			return model.nodes.get(0).parts.get(part).material;
		else
			return null;
	}
	
	@Override
	public Material setMaterial(int part, Material mat)
	{
		if(model != null)
			model.nodes.get(0).parts.get(part).material = mat;
		
		return mat;
	}
	
	@Override
	public Shape disposeByWorld(World world)
	{
		world.disposables.add(this);
		return this;
	}
	
	@Override
	public void dispose()
	{
		colShape.dispose();
		
		if(model != null)
			model.dispose();
	}
}