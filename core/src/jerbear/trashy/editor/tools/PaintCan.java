package jerbear.trashy.editor.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import jerbear.trashy.editor.Editor;
import jerbear.trashy.loader.Undoable.PaintShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;

public class PaintCan extends Tool
{
	public Material mat;
	
	private Editor menu;
	private World world;
	
	private SpriteBatch batch;
	private Texture crshair;
	
	public PaintCan(Editor menu, World world, Color colMat)
	{
		this(menu, world, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public PaintCan(Editor menu, World world, Material mat)
	{
		this.mat = mat;
		this.menu = menu;
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
		ShapeInstance inst = world.rayCastClosest(5, null);
		if(inst != null)
		{
			Material matPrv = inst.getMaterial(0);
			Color colPrv = ((ColorAttribute) matPrv.get(ColorAttribute.Diffuse)).color;
			Color col = ((ColorAttribute) mat.get(ColorAttribute.Diffuse)).color;
			
			if(!col.equals(colPrv))
			{
				menu.undoAdd(new PaintShape(inst, 0, mat, matPrv));
				inst.setMaterial(0, mat);
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
}