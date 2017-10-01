package jerbear.trashy;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;

import jerbear.trashy.Grid.GridShape;
import jerbear.trashy.Undoable.AddShape;
import jerbear.util2d.dialog.Dialog;
import jerbear.util2d.dialog.ExceptionDialog;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Box.BoxInstance;

public class EditorMenu implements Disposable
{
	public File save;
	
	private MenuBar menuBar;
	private LinkedList<Undoable> undos = new LinkedList<Undoable>();
	
	//keep references for enabling/disabling
	private MenuItem menuFileSave;
	private MenuItem menuEditUndo;
	private int asterisk = 0;
	
	public EditorMenu(Grid grid)
	{
		menuBar = new MenuBar();
		menuBar.getTable().setPosition(0, Gdx.graphics.getHeight() - 12);
		
		Menu menuFile = new Menu("File");
		MenuItem menuFileNew = new MenuItem("New").setShortcut(Keys.CONTROL_LEFT, Keys.N);
		MenuItem menuFileOpen = new MenuItem("Open").setShortcut(Keys.CONTROL_LEFT, Keys.O);
		menuFileSave = new MenuItem("Save").setShortcut(Keys.CONTROL_LEFT, Keys.S);
		MenuItem menuFileSaveAs = new MenuItem("Save As...").setShortcut(Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.S);
		MenuItem menuFileExit = new MenuItem("Exit").setShortcut(Keys.ESCAPE);
		menuFile.addItem(menuFileNew);
		menuFile.addItem(menuFileOpen);
		menuFile.addItem(menuFileSave);
		menuFile.addItem(menuFileSaveAs);
		menuFile.addSeparator();
		menuFile.addItem(menuFileExit);
		menuBar.addMenu(menuFile);
		
		Menu menuEdit = new Menu("Edit");
		menuEditUndo = new MenuItem("Undo").setShortcut(Keys.CONTROL_LEFT, Keys.Z);
		menuEditUndo.setDisabled(true);
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
		MenuItem menuShapesWall = new MenuItem("Wall");
		MenuItem menuShapesTri = new MenuItem("Triangle");
		MenuItem menuShapesSphere = new MenuItem("Sphere");
		MenuItem menuShapesCylinder = new MenuItem("Cylinder");
		MenuItem menuShapesCone = new MenuItem("Cone");
		MenuItem menuShapesCapsule = new MenuItem("Capsule");
		menuShapes.addItem(menuShapesBox);
		menuShapes.addItem(menuShapesRamp);
		menuShapes.addItem(menuShapesWall);
		menuShapes.addItem(menuShapesTri);
		menuShapes.addItem(menuShapesSphere);
		menuShapes.addItem(menuShapesCylinder);
		menuShapes.addItem(menuShapesCone);
		menuShapes.addItem(menuShapesCapsule);
		menuBar.addMenu(menuShapes);
		
		menuFileNew.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				newf(true);
			}
		});
		
		menuFileOpen.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				open();
			}
		});
		
		menuFileSave.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				save();
			}
		});
		
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
				undo();
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
				grid.setShape(GridShape.BOX);
			}
		});
		
		menuShapesRamp.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.RAMP);
			}
		});
		
		menuShapesWall.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.WALL);
			}
		});
		
		menuShapesTri.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.TRIANGLE);
			}
		});
		
		menuShapesSphere.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.SPHERE);
			}
		});
		
		menuShapesCylinder.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CYLINDER);
			}
		});
		
		menuShapesCone.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CONE);
			}
		});
		
		menuShapesCapsule.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CAPSULE);
			}
		});
		
		Dialog.addWidget(menuBar.getTable());
	}
	
	public void shortcutCheck()
	{
		boolean ctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		//boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.N))
			newf(true);
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.O))
			open();
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.S))
			save();
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.Z) && !undos.isEmpty())
			undo();
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			Gdx.app.exit();
	}

	public void newf(boolean firstBox)
	{
		while(!undos.isEmpty())
			undo();
		
		if(firstBox)
		{
			undoAdd(new AddShape(new BoxInstance(new Box(Game.game().world, 2, 1, 2, Color.RED), 0, -0.5f, 0, CollisionFlags.CF_STATIC_OBJECT, 0)));
			Gdx.graphics.setTitle("Trashy 3D - *(Untitled)");
		}
		else
		{
			Gdx.graphics.setTitle("Trashy 3D - (Untitled)");
			menuFileSave.setDisabled(true);
		}
		
		asterisk = 0;
		save = new File("test.t3d"); //TODO file save/open dialogs - this should be null
	}
	
	public void open()
	{
		newf(false);
		
		try
		{
			byte[] data = Files.readAllBytes(save.toPath());
			int remaining = data.length;
			
			while(remaining > 0)
			{
				Undoable undo;
				switch(data[0])
				{
					case 0:
						undo = new AddShape(data, Game.game().world);
						break;
					default:
						throw new IOException("Invalid serialization ID: " + data[0]);
				}

				undoAdd(undo);
				byte[] serialization = undo.serialize();
				remaining -= serialization.length;
				System.arraycopy(data, serialization.length, data, 0, remaining);
			}
		}
		catch(IOException oops)
		{
			new ExceptionDialog(oops, "Failed to load " + save.getName()).open();
			
			Dialog.setFocus();
			Gdx.input.setCursorCatched(false);
			Game.game().player.pause = true;
			
			newf(true);
		}
		
		asterisk = undos.size();
		Gdx.graphics.setTitle("Trashy 3D - " + save.getName());
		menuFileSave.setDisabled(true);
	}
	
	public void save()
	{
		if(menuFileSave.isDisabled())
			return;
		
		try
		{
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(save));
			for(Undoable undo : undos)
			{
				stream.write(undo.serialize());
			}
			
			stream.close();
		}
		catch(IOException oops)
		{
			new ExceptionDialog(oops, "Failed to save " + save.getName()).open();
			
			Dialog.setFocus();
			Gdx.input.setCursorCatched(false);
			Game.game().player.pause = true;
		}
		
		asterisk = undos.size();
		Gdx.graphics.setTitle("Trashy 3D - " + save.getName());
		menuFileSave.setDisabled(true);
	}
	
	public void undoAdd(Undoable undo)
	{
		undos.add(undo);
		menuEditUndo.setDisabled(false);
		
		Gdx.graphics.setTitle("Trashy 3D - *" + (save == null ? "(Untitled)" : save.getName()));
		menuFileSave.setDisabled(false);
		if(undos.size() == asterisk)
			asterisk = -1;
	}
	
	public void undo()
	{
		undos.getLast().undo();
		undos.removeLast();
		menuEditUndo.setDisabled(undos.isEmpty());
		
		if(undos.size() == asterisk)
		{
			Gdx.graphics.setTitle("Trashy 3D - " + (save == null ? "(Untitled)" : save.getName()));
			menuFileSave.setDisabled(true);
		}
		else
		{
			Gdx.graphics.setTitle("Trashy 3D - *" + (save == null ? "(Untitled)" : save.getName()));
			menuFileSave.setDisabled(false);
		}
	}
	
	@Override
	public void dispose()
	{
		menuBar.getTable().remove();
	}
}