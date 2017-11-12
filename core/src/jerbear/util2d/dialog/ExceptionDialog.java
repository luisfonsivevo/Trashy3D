
package jerbear.util2d.dialog;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;

public class ExceptionDialog extends Dialog
{
	public ExceptionDialog(Exception exception, String detail)
	{
		super("Exception caught: " + exception.getClass().getSimpleName());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    exception.printStackTrace(ps);
	    ps.close();
		
	    TextButton x = new TextButton("X", getSkin());
		Label label = new Label(detail, getSkin());
		Label text = new Label(baos.toString(), getSkin());
		ScrollPane pane = new ScrollPane(text, getSkin());
		TextButton ok = new TextButton("OK", getSkin());
		
		GlyphLayout layout = new GlyphLayout(text.getStyle().font, text.getText());
		pane.setFlickScroll(true);
		pane.setOverscroll(false, false);
		pane.layout();
		
		window.getTitleTable().add(x);
		
		//first row - label
		window.row();
		window.add(label).colspan(3).align(Align.left);
		
		//second row - exception
		window.row().fill().expandX().padTop(15);
		window.add(pane).colspan(3).prefHeight(200);
		
		//third row - button
		window.row().fill().expandX().padTop(30);
		window.add().fill().expandX().padTop(30);
		window.add(ok).maxWidth(100).align(Align.right);
		
		window.setSize(Math.min(Gdx.graphics.getWidth() - 50, layout.width), window.getPrefHeight());
		
		x.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				close();
			}
		});
		
		ok.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				close();
			}
		});
	}
}