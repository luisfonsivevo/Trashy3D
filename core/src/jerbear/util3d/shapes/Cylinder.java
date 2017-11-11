package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;

import jerbear.util3d.World;

public class Cylinder implements Shape
{
	private Vector3 dim;
	private Model model;
	private btCylinderShape colShape;
	
	public Cylinder(float width, float height, float depth)
	{
		this(width, height, depth, 0, (Material) null);
	}
	
	public Cylinder(float width, float height, float depth, int div, Color colMat)
	{
		this(width, height, depth, div, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public Cylinder(float width, float height, float depth, int div, Texture texMat, boolean manageTex)
	{
		this(width, height, depth, div, new Material(TextureAttribute.createDiffuse(texMat)));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Cylinder(float width, float height, float depth, int div, Material mat)
	{
		dim = new Vector3(width / 2f, height / 2f, depth / 2f);
		colShape = new btCylinderShape(new Vector3(dim));
		
		if(mat != null)
			model = modelBuilder.createCylinder(width, height, depth, div, mat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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