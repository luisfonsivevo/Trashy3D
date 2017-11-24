package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;

import jerbear.util3d.World;

public interface Shape extends Disposable
{
	public static final ModelBuilder modelBuilder = new ModelBuilder();
	
	public Model getModel();
	public btCollisionShape getCollisionShape();
	
	public Material getMaterial(int part);
	public Material setMaterial(int part, Material mat);
	
	public Shape disposeByWorld(World world);
}