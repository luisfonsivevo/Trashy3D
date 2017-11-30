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
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.FileTextureData;

import jerbear.trashy.editor.Editor;
import jerbear.trashy.loader.Undoable.PaintShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;

public class PaintCan extends Tool
{
	public Material mat;
	
	private Editor editor;
	private World world;
	
	private SpriteBatch batch;
	private Texture crshair;
	
	public PaintCan(Editor editor, World world, Color colMat)
	{
		this(editor, world, new Material(ColorAttribute.createDiffuse(colMat)));
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
		int side = 0; //TODO side detection
		
		ShapeInstance inst = world.rayCastClosest(5, null);
		if(inst != null)
		{
			Material matPrv = inst.getMaterial(side);
			
			boolean isColor = mat.has(ColorAttribute.Diffuse);
			boolean isTexture = mat.has(TextureAttribute.Diffuse);
			boolean isColorPrv = matPrv.has(ColorAttribute.Diffuse);
			boolean isTexturePrv = matPrv.has(TextureAttribute.Diffuse);
			
			if(mat.getMask() != ColorAttribute.Diffuse && mat.getMask() != TextureAttribute.Diffuse)
			{
				throw new IllegalArgumentException("Material must have either ColorAttribute.Diffuse or TextureAttribute.Diffuse");
			}
			else if(isColor && isColorPrv) //TODO transparency
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
	
	private void setMat(ShapeInstance inst, int side)
	{
		editor.undoAdd(new PaintShape(inst, side, mat));
		inst.setMaterial(side, mat);
	}
	
	@Override
	public void dispose()
	{
		crshair.dispose();
		batch.dispose();
	}
}