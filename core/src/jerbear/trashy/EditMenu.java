package jerbear.trashy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuBar.MenuBarStyle;
import com.kotcrab.vis.ui.widget.MenuItem;

import jerbear.trashy.Grid.Shape;
import jerbear.util2d.dialog.Dialog;

public class EditMenu implements Disposable
{
	MenuBar menuBar;
	
	public EditMenu(Grid grid)
	{
		menuBar = new MenuBar(getStyle(MenuBarStyle.class));
		menuBar.getTable().setPosition(0, Gdx.graphics.getHeight() - 12);
		
		Menu menuFile = new Menu("File");
		MenuItem menuFileExit = new MenuItem("Exit").setShortcut(Keys.ESCAPE);
		menuFile.addItem(menuFileExit);
		menuBar.addMenu(menuFile);
		
		Menu menuEdit = new Menu("Edit");
		MenuItem menuEditUndo = new MenuItem("Undo").setShortcut(Keys.CONTROL_LEFT, Keys.Z);
		menuEdit.addItem(menuEditUndo);
		menuBar.addMenu(menuEdit);
		
		Menu menuPhysics = new Menu("Physics");
		MenuItem menuPhysicsDis = new MenuItem("Disabled");
		MenuItem menuPhysicsDyn = new MenuItem("Dynamic");
		MenuItem menuPhysicsKin = new MenuItem("Kinematic");
		MenuItem menuPhysicsStc = new MenuItem("Static");
		menuPhysics.addItem(menuPhysicsDis);
		menuPhysics.addItem(menuPhysicsDyn);
		menuPhysics.addItem(menuPhysicsKin);
		menuPhysics.addItem(menuPhysicsStc);
		menuBar.addMenu(menuPhysics);
		
		Menu menuShapes = new Menu("Shapes");
		MenuItem menuShapesBox = new MenuItem("Box");
		MenuItem menuShapesRamp = new MenuItem("Ramp");
		MenuItem menuShapesTri = new MenuItem("Triangle");
		MenuItem menuShapesSphere = new MenuItem("Sphere");
		MenuItem menuShapesCylinder = new MenuItem("Cylinder");
		MenuItem menuShapesCone = new MenuItem("Cone");
		MenuItem menuShapesCapsule = new MenuItem("Capsule");
		menuShapes.addItem(menuShapesBox);
		menuShapes.addItem(menuShapesRamp);
		menuShapes.addItem(menuShapesTri);
		menuShapes.addItem(menuShapesSphere);
		menuShapes.addItem(menuShapesCylinder);
		menuShapes.addItem(menuShapesCone);
		menuShapes.addItem(menuShapesCapsule);
		menuBar.addMenu(menuShapes);
		
		menuFileExit.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				Gdx.app.exit();
			}
		});
		
		menuEditUndo.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				
			}
		});
		
		menuPhysicsDis.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.physicsMode = -1;
			}
		});
		
		menuPhysicsDyn.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.physicsMode = 0;
			}
		});
		
		menuPhysicsKin.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.physicsMode = CollisionFlags.CF_KINEMATIC_OBJECT;
			}
		});
		
		menuPhysicsStc.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.physicsMode = CollisionFlags.CF_STATIC_OBJECT;
			}
		});
		
		menuShapesBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.BOX);
			}
		});
		
		menuShapesRamp.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.RAMP);
			}
		});
		
		menuShapesTri.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.TRIANGLE);
			}
		});
		
		menuShapesSphere.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.SPHERE);
			}
		});
		
		menuShapesCylinder.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.CYLINDER);
			}
		});
		
		menuShapesCone.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.CONE);
			}
		});
		
		menuShapesCapsule.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(Shape.CAPSULE);
			}
		});
		
		Dialog.addWidget(menuBar.getTable());
	}
	
	public void shortcutCheck()
	{
		if(Gdx.input.isKeyPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}
	
	@Override
	public void dispose()
	{
		menuBar.getTable().remove();
	}
	
	private <T> T getStyle(Class<T> style)
	{
		return Dialog.getSkin().get("default", style);
	}
}