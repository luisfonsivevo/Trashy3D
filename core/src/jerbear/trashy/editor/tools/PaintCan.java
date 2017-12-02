package jerbear.trashy.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

import jerbear.trashy.editor.Editor;
import jerbear.trashy.loader.Undoable.PaintShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.*;

public class PaintCan extends Tool
{
	public Material mat;
	
	private Editor editor;
	private World world;
	
	private SpriteBatch batch;
	private Texture crshair;
	
	public PaintCan(Editor editor, World world, Color colMat)
	{
		this(editor, world, new Material(ColorAttribute.createDiffuse(colMat), new BlendingAttribute()));
	}
	
	public PaintCan(Editor editor, World world, Material mat)
	{
		this.mat = mat;
		this.editor = editor;
		this.world = world;
		
		batch = new SpriteBatch();
		
		Pixmap pix = new Pixmap(24, 24, Format.RGBA4444);
		pix.setColor(Color.WHITE);
		pix.drawLine(0, 10, 20, 10);
		pix.drawLine(10, 0, 10, 20);
		crshair = new Texture(pix);
		pix.dispose();
	}
	
	@Override
	public void draw()
	{
		batch.begin();
		batch.draw(crshair, (Gdx.graphics.getWidth() - crshair.getWidth()) / 2, (Gdx.graphics.getHeight() - crshair.getHeight()) / 2);
		batch.end();
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		Vector3 hit = new Vector3();
		ShapeInstance inst = world.rayCastClosest(5, hit);
		
		if(inst != null)
		{
			int side;
			Shape shape = inst.getShape();
			if(shape instanceof Box)
			{
				Box box = (Box) shape;
				Vector3 dim = new Vector3();
				Vector3 pos = inst.getPosition(new Vector3());
				Matrix4 transform = inst.getTransform(new Matrix4());
				
				//front
				dim.set(0, 0, box.getDimensions(dim).z / 2f).mul(transform);
				Vector3 nor = new Vector3(0, 0, 1).mul(transform).sub(pos);
				Plane f = new Plane(nor, dim);
				
				//back
				dim.set(0, 0, -box.getDimensions(dim).z / 2f).mul(transform);
				nor.set(0, 0, -1).mul(transform).sub(pos);
				Plane b = new Plane(nor, dim);
				
				//left
				dim.set(-box.getDimensions(dim).x / 2f, 0, 0).mul(transform);
				nor.set(-1, 0, 0).mul(transform).sub(pos);
				Plane l = new Plane(nor, dim);
				
				//right
				dim.set(box.getDimensions(dim).x / 2f, 0, 0).mul(transform);
				nor.set(1, 0, 0).mul(transform).sub(pos);
				Plane r = new Plane(nor, dim);
				
				//bottom
				dim.set(0, -box.getDimensions(dim).y / 2f, 0).mul(transform);
				nor.set(0, -1, 0).mul(transform).sub(pos);
				Plane d = new Plane(nor, dim);
				
				//top
				dim.set(0, box.getDimensions(dim).y / 2f, 0).mul(transform);
				nor.set(0, 1, 0).mul(transform).sub(pos);
				Plane u = new Plane(nor, dim);
				
				Plane min;
				min = Math.abs(b.distance(hit)) < Math.abs(f.distance(hit)) ? b : f;
				min = Math.abs(l.distance(hit)) < Math.abs(min.distance(hit)) ? l : min;
				min = Math.abs(r.distance(hit)) < Math.abs(min.distance(hit)) ? r : min;
				min = Math.abs(d.distance(hit)) < Math.abs(min.distance(hit)) ? d : min;
				min = Math.abs(u.distance(hit)) < Math.abs(min.distance(hit)) ? u : min;
				
				if(min == f)
					side = Box.SIDE_FRONT;
				else if(min == b)
					side = Box.SIDE_BACK;
				else if(min == l)
					side = Box.SIDE_LEFT;
				else if(min == r)
					side = Box.SIDE_RIGHT;
				else if(min == d)
					side = Box.SIDE_BOTTOM;
				else //if(min == u)
					side = Box.SIDE_TOP;
			}
			else if(shape instanceof Cylinder)
			{
				Cylinder cyl = (Cylinder) shape;
				Vector3 pos = inst.getPosition(new Vector3());
				Matrix4 transform = inst.getTransform(new Matrix4());
				
				//bottom
				Vector3 dimD = cyl.getDimensions(new Vector3());
				dimD.set(0, -dimD.y / 2f, 0).mul(transform);
				Vector3 nor = new Vector3(0, -1, 0).mul(transform).sub(pos);
				Plane d = new Plane(nor, dimD);
				
				//top
				Vector3 dimU = cyl.getDimensions(new Vector3());
				dimU.set(0, dimU.y / 2f, 0).mul(transform);
				nor = nor.set(0, 1, 0).mul(transform).sub(pos);
				Plane u = new Plane(nor, dimU);
				
				Plane min = Math.abs(d.distance(hit)) < Math.abs(u.distance(hit)) ? d : u;
				
				float pdst = Math.abs(min.distance(hit));
				float rdst = Math.abs(pointToLineDst(hit, dimD, dimU) - (cyl.getDimensions(dimD).x / 2f)); //assumes uniform radius
				
				if(min == d)
				{
					if(pdst < rdst)
						side = Cylinder.SIDE_BOTTOM;
					else
						side = Cylinder.SIDE_CYL;	
				}
				else
				{
					if(pdst < rdst)
						side = Cylinder.SIDE_TOP;
					else
						side = Cylinder.SIDE_CYL;	
				}
			}
			else
			{
				side = 0;
			}
			
			Material matPrv = inst.getMaterial(side);
			
			boolean isColor = mat.has(ColorAttribute.Diffuse);
			boolean isTexture = mat.has(TextureAttribute.Diffuse);
			boolean isColorPrv = matPrv.has(ColorAttribute.Diffuse);
			boolean isTexturePrv = matPrv.has(TextureAttribute.Diffuse);
			long maskNoBlend = mat.getMask() & ~BlendingAttribute.Type;
			
			if(maskNoBlend != ColorAttribute.Diffuse && maskNoBlend != TextureAttribute.Diffuse)
			{
				throw new IllegalArgumentException("Material must have either ColorAttribute.Diffuse or TextureAttribute.Diffuse");
			}
			else if(isColor && isColorPrv)
			{
				Color col = ((ColorAttribute) mat.get(ColorAttribute.Diffuse)).color;
				Color colPrv = ((ColorAttribute) matPrv.get(ColorAttribute.Diffuse)).color;
				
				if(!col.equals(colPrv))
					setMat(inst, side);
				//else there is no need to change the material to a duplicate
			}
			else if(isTexture && isTexturePrv)
			{
				Texture tex = ((TextureAttribute) mat.get(TextureAttribute.Diffuse)).textureDescription.texture;
				Texture texPrv = ((TextureAttribute) matPrv.get(TextureAttribute.Diffuse)).textureDescription.texture;
				
				TextureData texData = tex.getTextureData();
				TextureData texDataPrv = texPrv.getTextureData();
				
				if(texData instanceof FileTextureData && texDataPrv instanceof FileTextureData)
				{
					FileHandle texFile = ((FileTextureData) texData).getFileHandle();
					FileHandle texFilePrv = ((FileTextureData) texDataPrv).getFileHandle();
					
					if(!texFile.equals(texFilePrv))
						setMat(inst, side);
				}
				else
				{
					setMat(inst, side); //this should not happen
				}
			}
			else
			{
				setMat(inst, side);
			}
		}
		
		return true;
	}
	
	@Override
	public void dispose()
	{
		crshair.dispose();
		batch.dispose();
	}
	
	private void setMat(ShapeInstance inst, int side)
	{
		editor.undoAdd(new PaintShape(inst, side, mat));
		inst.setMaterial(side, mat);
	}
	
	//smallest perpendicular distance from a point to a line
	//thx https://math.stackexchange.com/a/1905794
	private float pointToLineDst(Vector3 point, Vector3 l1, Vector3 l2)
	{
		Vector3 v1 = new Vector3(l2).sub(l1).scl(1f / l2.dst(l1));
		Vector3 v2 = new Vector3(point).sub(l1);
		
		float t = v2.dot(v1);
		v2.set(l1).add(v1.scl(t));
		return v2.dst(point);
	}
}