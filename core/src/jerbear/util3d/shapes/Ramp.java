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
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;

import jerbear.util3d.World;

public class Ramp implements Shape
{
	public static final int SIDE_RAMP = 0;
	public static final int SIDE_BACK = 1;
	public static final int SIDE_LEFT = 2;
	public static final int SIDE_RIGHT = 3;
	public static final int SIDE_BOTTOM = 4;

	private Vector3 dim;
	private Model model;
	private btConvexHullShape colShape;
	
	public Ramp(float width, float height, float depth)
	{
		this(width, height, depth, (Material) null);
	}
	
	public Ramp(float width, float height, float depth, Color colMat)
	{
		this(width, height, depth, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public Ramp(float width, float height, float depth, Texture texMat, boolean manageTex)
	{
		this(width, height, depth, new Material(TextureAttribute.createDiffuse(texMat), new BlendingAttribute()));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Ramp(float width, float height, float depth, Material mat)
	{
		dim = new Vector3(width / 2f, height / 2f, depth / 2f);
		colShape = new btConvexHullShape();
		
		colShape.addPoint(new Vector3(-width / 2f, -height / 2f, -depth / 2f), false);
		colShape.addPoint(new Vector3(width / 2f, -height / 2f, -depth / 2f), false);
		colShape.addPoint(new Vector3(width / 2f, -height / 2f, depth / 2f), false);
		colShape.addPoint(new Vector3(-width / 2f, -height / 2f, depth / 2f), false);
		colShape.addPoint(new Vector3(-width / 2f, height / 2f, -depth / 2f), false);
		colShape.addPoint(new Vector3(width / 2f, height / 2f, -depth / 2f), true);
		
		if(mat == null)
			return;
		
		modelBuilder.begin();
		
		//BL BR TR TL
		
		//ramp
		modelBuilder.part("r", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, -height / 2f, depth / 2f,
				width / 2f, -height / 2f, depth / 2f,
				width / 2f, height / 2f, -depth / 2f,
				-width / 2f, height / 2f, -depth / 2f, 0, 0, 1);
		
		//back
		modelBuilder.part("b", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				width / 2f, -height / 2f, -depth / 2f,
				-width / 2f, -height / 2f, -depth / 2f,
				-width / 2f, height / 2f, -depth / 2f,
				width / 2f, height / 2f, -depth / 2f, 0, 0, -1);
		
		//left
		VertexInfo vinf1 = new VertexInfo();
		vinf1.setNor(-1, 0, 0);
		vinf1.setPos(-width / 2f, -height / 2f, -depth / 2f);
		
		VertexInfo vinf2 = new VertexInfo();
		vinf2.setNor(-1, 0, 0);
		vinf2.setPos(-width / 2f, -height / 2f, depth / 2f);
		
		VertexInfo vinf3 = new VertexInfo();
		vinf3.setNor(-1, 0, 0);
		vinf3.setPos(-width / 2f, height / 2f, -depth / 2f);
		
		modelBuilder.part("l", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(
				vinf1, vinf2, vinf3);
		
		//right
		vinf1.setNor(1, 0, 0);
		vinf1.setPos(width / 2f, -height / 2f, depth / 2f);
		
		vinf2.setNor(1, 0, 0);
		vinf2.setPos(width / 2f, -height / 2f, -depth / 2f);
		
		vinf3.setNor(1, 0, 0);
		vinf3.setPos(width / 2f, height / 2f, -depth / 2f);
		
		modelBuilder.part("l", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).triangle(
				vinf1, vinf2, vinf3);
		
		//bottom
		modelBuilder.part("d", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).rect(
				-width / 2f, -height / 2f, -depth / 2f,
				width / 2f, -height / 2f, -depth / 2f,
				width / 2f, -height / 2f, depth / 2f,
				-width / 2f, -height / 2f, depth / 2f, 0, -1, 0);
		
		model = modelBuilder.end();
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