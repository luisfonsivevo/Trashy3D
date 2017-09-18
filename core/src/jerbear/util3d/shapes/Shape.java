package jerbear.util3d.shapes;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

public interface Shape extends Disposable
{
	public Model getModel();
}