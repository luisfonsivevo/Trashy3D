package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import jerbear.util3d.World;

public class Capsule implements Shape
{
	private float radius;
	private float height;
	private Model model;
	private btCapsuleShape colShape;
	
	public Capsule(float radius, float height)
	{
		this(radius, height, 0, (Material) null);
	}
	
	public Capsule(float radius, float height, int div, Color colMat)
	{
		this(radius, height, div, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Capsule(float radius, float height, int div, Texture texMat, boolean manageTex)
	{
		this(radius, height, div, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Capsule(float radius, float height, int div, Material mat)
	{
		this.radius = radius;
		this.height = height;
		colShape = new btCapsuleShape(radius, height - 2 * radius);
		
		if(mat != null)
			model = modelBuilder.createCapsule(radius, height, div, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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
	public btCollisionShape getCollisionShape()
	{
		return colShape;
	}
	
	@Override
	public Material getMaterial(int part)
	{
		return model.nodes.get(0).parts.get(part).material;
	}
	
	@Override
	public Material setMaterial(int part, Material mat)
	{
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