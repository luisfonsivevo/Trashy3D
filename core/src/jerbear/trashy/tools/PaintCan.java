package jerbear.trashy.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import jerbear.trashy.EditorMenu;
import jerbear.trashy.loader.Undoable.PaintShape;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;

public class PaintCan extends Tool
{
	public Material mat;
	
	private EditorMenu menu;
	private World world;
	
	public PaintCan(EditorMenu menu, World world, Color colMat)
	{
		this(menu, world, new Material(ColorAttribute.createDiffuse(colMat)));
	}
	
	public PaintCan(EditorMenu menu, World world, Material mat)
	{
		this.mat = mat;
		this.menu = menu;
		this.world = world;
	}
	
	@Override
	public void draw()
	{
		
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		ShapeInstance inst = world.rayCastClosest(5, null);
		if(inst != null)
		{
			menu.undoAdd(new PaintShape(inst, 0, mat, inst.getMaterial(0)));
			inst.setMaterial(0, mat);
		}
		
		return true;
	}
	
	@Override
	public void dispose()
	{
		
	}
}