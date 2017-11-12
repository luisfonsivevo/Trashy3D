package jerbear.util2d.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public abstract class YesNoDialog extends Dialog
{
	public YesNoDialog(String title, String question, boolean hasCancel)
	{
		super(title);
		
		Label label = new Label(question, Dialog.getSkin());
		TextButton yes = new TextButton("Yes", Dialog.getSkin());
		TextButton no = new TextButton("No", Dialog.getSkin());
		TextButton cancel = new TextButton("Cancel", Dialog.getSkin());
		
		//first row - label
		window.row();
		window.add(label).colspan(3);
		
		//second row - buttons
		window.row().fill().expandX().padTop(30);
		window.add().prefWidth(window.getWidth() / 4f);
		window.add(yes);
		window.add(no);
		if(hasCancel) window.add(cancel);
		
		window.setSize(Math.min(Gdx.graphics.getWidth() - 50, window.getPrefWidth()), window.getPrefHeight());
		
		yes.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				close();
				onClose(1);
			}
		});
		
		no.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				close();
				onClose(0);
			}
		});
		
		cancel.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				close();
				onClose(-1);
			}
		});
	}
	
	//-1 = cancel, 0 = no, 1 = yes
	public abstract void onClose(int answer);
}