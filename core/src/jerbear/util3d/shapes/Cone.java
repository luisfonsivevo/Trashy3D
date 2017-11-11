package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;

import jerbear.util3d.World;

public class Cone implements Shape
{
	private float radius;
	private float height;
	private Model model;
	private btConeShape colShape;
	
	public Cone(float radius, float height)
	{
		this(radius, height, 0, (Material) null);
	}
	
	public Cone(float radius, float height, int div, Color colMat)
	{
		this(radius, height, div, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Cone(float radius, float height, int div, Texture texMat, boolean manageTex)
	{
		this(radius, height, div, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Cone(float radius, float height, int div, Material mat)
	{
		this.radius = radius;
		this.height = height;
		colShape = new btConeShape(radius, height);
		
		if(mat != null)
			model = modelBuilder.createCone(radius * 2, height, radius * 2, div, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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