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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.EllipseShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;

import jerbear.util3d.World;

public class Cylinder implements Shape
{
	public static final int SIDE_BOTTOM = 0;
	public static final int SIDE_CYL = 1;
	public static final int SIDE_TOP = 2;
	
	private Vector3 dim;
	private Model model;
	private btCylinderShape colShape;
	
	public Cylinder(float width, float height, float depth)
	{
		this(width, height, depth, 0, (Material) null);
	}
	
	public Cylinder(float width, float height, float depth, int div, Color colMat)
	{
		this(width, height, depth, div, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public Cylinder(float width, float height, float depth, int div, Texture texMat, boolean manageTex)
	{
		this(width, height, depth, div, new Material(TextureAttribute.createDiffuse(texMat), new BlendingAttribute()));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Cylinder(float width, float height, float depth, int div, Material mat)
	{
		dim = new Vector3(width / 2f, height / 2f, depth / 2f);
		colShape = new btCylinderShape(new Vector3(dim));
		
		if(mat == null)
			return;
		
		modelBuilder.begin();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("d", GL20.GL_TRIANGLES), mat);
		EllipseShapeBuilder.build(meshBuilder, width, depth, 0, 0, div, 0, -height / 2f, 0, 0, -1, 0, -1, 0, 0, 0, 0, 1, -180, 180);
		meshBuilder.end();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("cyl", GL20.GL_TRIANGLES), mat);
		CylinderShapeBuilder.build(meshBuilder, width, height, depth, div, 0, 360, false);
		meshBuilder.end();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("u", GL20.GL_TRIANGLES), mat);
		EllipseShapeBuilder.build(meshBuilder, width, depth, 0, 0, div, 0, height / 2f, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 360);
		meshBuilder.end();
		
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