package jerbear.util2d.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public abstract class Dialog
{
	private static Skin skin;
	private static Stage stage;
	
	private static BitmapFont font;
	private static String str;
	private static GlyphLayout strSize = new GlyphLayout();
	
	private Window window;
	private boolean open = false;
	
	public static Skin getSkin()
	{
		return skin;
	}
	
	public static void setSkin(FileHandle skin)
	{
		if(stage == null)
		{
			stage = new Stage(new ScreenViewport());
			Gdx.input.setInputProcessor(stage);
			strSize = new GlyphLayout();
		}
		
		if(Dialog.skin != null)
			Dialog.skin.dispose();
		
		Dialog.skin = new Skin(skin);
	}
	
	public static void addWidget(Widget widget)
	{
		stage.addActor(widget);
	}
	
	public static void addWidget(WidgetGroup widget)
	{
		stage.addActor(widget);
	}
	
	public static void draw()
	{
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
	}
	
	public static void setFocus()
	{
		Gdx.input.setInputProcessor(stage);
	}
	
	public static boolean isDialogOpen()
	{
		return stage.getActors().size != 0;
	}
	
	public static float getStringWidth(BitmapFont font, String str)
	{
		if(Dialog.font != font || Dialog.str != str)
			strSize.setText(font, str);
		
		return strSize.width;
	}
	
	public static float getStringHeight(BitmapFont font, String str)
	{
		if(Dialog.font != font || Dialog.str != str)
			strSize.setText(font, str);
		
		return strSize.height;
	}
	
	public static void dispose()
	{
		stage.dispose();
		skin.dispose();
	}
	
	public Dialog(String title)
	{
		window = new Window(title, skin);
	}
	
	public Dialog(String title, int width, int height)
	{
		window = new Window(title, skin);
		window.setSize(width, height);
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public void open()
	{
		stage.addActor(window);
		window.setPosition((Gdx.graphics.getWidth() - window.getWidth()) / 2, (Gdx.graphics.getHeight() - window.getHeight()) / 2);
		open = true;
	}
	
	public void close()
	{
		window.remove();
		open = false;
	}
	
	protected Window getWindow()
	{
		return window;
	}
}