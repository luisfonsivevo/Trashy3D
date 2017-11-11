package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

import jerbear.util3d.World;

public class Sphere implements Shape
{
	private float radius;
	private Model model;
	private btSphereShape colShape;
	
	public Sphere(float radius)
	{
		this(radius, 0, 0, (Material) null);
	}
	
	public Sphere(float radius, int div, Color colMat)
	{
		this(radius, div, div, colMat);
	}
	
	public Sphere(float radius, int div, Texture texMat, boolean manageTex)
	{
		this(radius, div, div, texMat, manageTex);
	}
	
	public Sphere(float radius, int divU, int divV, Color colMat)
	{
		this(radius, divU, divV, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Sphere(float radius, int divU, int divV, Texture texMat, boolean manageTex)
	{
		this(radius, divU, divV, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Sphere(float radius, int divU, int divV, Material mat)
	{
		this.radius = radius;
		colShape = new btSphereShape(radius);
		
		if(mat != null)
			model = modelBuilder.createSphere(radius * 2, radius * 2, radius *2, divU, divV, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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
	public btCollisionShape getCollisionShape()
	{
		return colShape;
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