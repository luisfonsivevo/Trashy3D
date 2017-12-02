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
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;

import jerbear.util3d.World;

public class Cone implements Shape
{
	public static final int SIDE_BASE = 0;
	public static final int SIDE_CONE = 1;
	
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
		this(radius, height, div, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public Cone(float radius, float height, int div, Texture texMat, boolean manageTex)
	{
		this(radius, height, div, new Material(TextureAttribute.createDiffuse(texMat), new BlendingAttribute()));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Cone(float radius, float height, int div, Material mat)
	{
		this.radius = radius;
		this.height = height;
		colShape = new btConeShape(radius, height);
		
		if(mat == null)
			return;
		
		modelBuilder.begin();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("base", GL20.GL_TRIANGLES), mat);
		EllipseShapeBuilder.build(meshBuilder, radius * 2, radius * 2, 0, 0, div, 0, -height / 2f, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, -180, 180);
		meshBuilder.end();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("cone", GL20.GL_TRIANGLES), mat);
		ConeShapeBuilder.build(meshBuilder, radius * 2, height, radius * 2, div, 0, 360, false);
		meshBuilder.end();
		
		model = modelBuilder.end();
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