package jerbear.trashy.editor.tools;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.utils.Disposable;

public abstract class Tool extends InputAdapter implements Disposable
{
	public abstract void draw();
}