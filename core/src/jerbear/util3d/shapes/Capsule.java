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
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

import jerbear.util3d.World;

public class Capsule implements Shape
{
	public static final int SIDE_BOTTOM = 0;
	public static final int SIDE_CAPSULE = 1;
	public static final int SIDE_TOP = 2;
	
	private static Matrix4 tmpMat = new Matrix4();
	
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
		this(radius, height, div, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public Capsule(float radius, float height, int div, Texture texMat, boolean manageTex)
	{
		this(radius, height, div, new Material(TextureAttribute.createDiffuse(texMat), new BlendingAttribute()));
		if(manageTex)
			model.manageDisposable(texMat);
	}
	
	public Capsule(float radius, float height, int div, Material mat)
	{
		if (height < 2f * radius) throw new IllegalArgumentException("Height must be at least twice the radius");
		
		this.radius = radius;
		this.height = height;
		colShape = new btCapsuleShape(radius, height - 2 * radius);
		
		if(mat == null)
			return;
		
		float d = 2f * radius;
		
		modelBuilder.begin();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("d", GL20.GL_TRIANGLES), mat);
		meshBuilder.setVertexTransform(tmpMat.setToTranslation(0, -0.5f * (height - d), 0));
		SphereShapeBuilder.build(meshBuilder, d, d, d, div, div, 0, 360, 90, 180);
		meshBuilder.end();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("cyl", GL20.GL_TRIANGLES), mat);
		CylinderShapeBuilder.build(meshBuilder, d, height - d, d, div, 0, 360, false);
		meshBuilder.end();
		
		meshBuilder.begin(MeshBuilder.createAttributes(Usage.Position | Usage.Normal | Usage.TextureCoordinates));
		modelBuilder.part(meshBuilder.part("u", GL20.GL_TRIANGLES), mat);
		tmpMat.val[Matrix4.M13] *= -1;
		meshBuilder.setVertexTransform(tmpMat);
		SphereShapeBuilder.build(meshBuilder, d, d, d, div, div, 0, 360, 0, 90);
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