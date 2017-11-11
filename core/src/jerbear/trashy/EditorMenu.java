package jerbear.trashy;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import jerbear.trashy.loader.Loader;
import jerbear.trashy.loader.Undoable;
import jerbear.trashy.loader.Undoable.AddShape;
import jerbear.trashy.tools.Grid;
import jerbear.trashy.tools.Grid.GridShape;
import jerbear.trashy.tools.Tool;
import jerbear.util2d.dialog.Dialog;
import jerbear.util2d.dialog.ExceptionDialog;
import jerbear.util2d.dialog.YesNoDialog;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;

public class EditorMenu implements Disposable
{
	public File save;
	
	private MenuBar menuBar;
	private LinkedList<Undoable> undos = new LinkedList<Undoable>();
	private LinkedList<Undoable> redos = new LinkedList<Undoable>();
	
	//keep references for enabling/disabling
	private MenuItem menuFileSave;
	private MenuItem menuEditUndo;
	private MenuItem menuEditRedo;
	private int asterisk = 0;
	
	private Tool tool;
	
	private boolean dialogOpen;
	
	public EditorMenu()
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
		menuEditRedo = new MenuItem("Redo").setShortcut(Keys.CONTROL_LEFT, Keys.SHIFT_LEFT, Keys.Z);
		menuEditRedo.setDisabled(true);
		menuEdit.addItem(menuEditUndo);
		menuEdit.addItem(menuEditRedo);
		menuBar.addMenu(menuEdit);
		
		Menu menuTools = new Menu("Tools");
		MenuItem menuToolsGrid = new MenuItem("Grid");
		PopupMenu menuToolsGridSub = new PopupMenu();
		MenuItem menuToolsGridBox = new MenuItem("Box");
		MenuItem menuToolsGridRamp = new MenuItem("Ramp");
		MenuItem menuToolsGridWall = new MenuItem("Wall");
		MenuItem menuToolsGridTri = new MenuItem("Triangle");
		MenuItem menuToolsGridSphere = new MenuItem("Sphere");
		MenuItem menuToolsGridCylinder = new MenuItem("Cylinder");
		MenuItem menuToolsGridCone = new MenuItem("Cone");
		MenuItem menuToolsGridCapsule = new MenuItem("Capsule");
		menuToolsGridSub.addItem(menuToolsGridBox);
		menuToolsGridSub.addItem(menuToolsGridRamp);
		menuToolsGridSub.addItem(menuToolsGridWall);
		menuToolsGridSub.addItem(menuToolsGridTri);
		menuToolsGridSub.addItem(menuToolsGridSphere);
		menuToolsGridSub.addItem(menuToolsGridCylinder);
		menuToolsGridSub.addItem(menuToolsGridCone);
		menuToolsGridSub.addItem(menuToolsGridCapsule);
		menuToolsGrid.setSubMenu(menuToolsGridSub);
		menuTools.addItem(menuToolsGrid);
		menuBar.addMenu(menuTools);
		
		tool = new Grid(Game.game().world, 5, 1);
		Gdx.input.setInputProcessor(tool);
		
		menuFileNew.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				newf(true, false);
			}
		});
		
		menuFileOpen.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				openDialog(false);
			}
		});
		
		menuFileSave.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				save(null);
			}
		});
		
		menuFileSaveAs.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				saveAs(null);
			}
		});
		
		menuFileExit.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				exit();
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
		
		menuEditRedo.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				redo();
			}
		});
		
		
		
		//PHYSICS
		//Disabled: -1
		//Dynamic: 0
		//Kinematic: CollisionFlags.CF_KINEMATIC_OBJECT
		//Static: CollisionFlags.CF_STATIC_OBJECT;
		
		
		
		menuToolsGridBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1); //defaults to box
				else
					((Grid) tool).setShape(GridShape.BOX);
					
			}
		});
		
		menuToolsGridRamp.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.RAMP);
			}
		});
		
		menuToolsGridWall.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.WALL);
			}
		});
		
		menuToolsGridTri.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.TRIANGLE);
			}
		});
		
		menuToolsGridSphere.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.SPHERE);
			}
		});
		
		menuToolsGridCylinder.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.CYLINDER);
			}
		});
		
		menuToolsGridCone.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.CONE);
			}
		});
		
		menuToolsGridCapsule.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				if(!(tool instanceof Grid))
					tool = new Grid(Game.game().world, 5, 1);
				
				((Grid) tool).setShape(GridShape.CAPSULE);
			}
		});
		
		Dialog.addWidget(menuBar.getTable());
	}
	
	public void draw()
	{
		if(Gdx.input.isKeyJustPressed(Keys.ALT_LEFT))
		{
			if(Gdx.input.getInputProcessor() == tool)
			{
				Dialog.setFocus();
				Gdx.input.setCursorCatched(false);
				Game.game().player.pause = true;
			}
			else
			{
				Gdx.input.setInputProcessor(tool);
				Gdx.input.setCursorCatched(true);
				Game.game().player.pause = false;
			}
		}
		
		boolean ctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.N))
			newf(true, false);
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			exit();
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.O))
			openDialog(false);
		
		if(ctrl && !shift && Gdx.input.isKeyJustPressed(Keys.S) && !menuFileSave.isDisabled())
			save(null);
		
		if(ctrl && shift && Gdx.input.isKeyJustPressed(Keys.S))
			saveAs(null);
		
		if(ctrl && !shift && Gdx.input.isKeyJustPressed(Keys.Z) && !undos.isEmpty())
			undo();
		
		if(ctrl && shift && Gdx.input.isKeyJustPressed(Keys.Z) && !redos.isEmpty())
			redo();
		
		tool.draw();
	}
	
	public void newf(boolean firstBox, boolean force)
	{
		if(menuFileSave.isDisabled() || force)
		{
			while(!undos.isEmpty())
				undo();
			
			if(firstBox)
				save = null; //TODO kinda messy but works
			
			redos.clear();
			
			if(firstBox)
			{
				World world = Game.game().world;
				undoAdd(new AddShape(world.addShape(new ShapeInstance(new Box(2, 1, 2, Color.RED).disposeByWorld(world), 0, -0.5f, 0, CollisionFlags.CF_STATIC_OBJECT, 0))));
				Gdx.graphics.setTitle("*(Untitled) - Trashy 3D");
			}
			else
			{
				Gdx.graphics.setTitle("(Untitled) - Trashy 3D");
				menuFileSave.setDisabled(true);
			}
			
			asterisk = 0;
		}
		else
		{
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + (save == null ? "Untitled" : save.getName()) + "\"", true)
			{
				@Override
				public void onClose(int answer)
				{
					dialogOpen = false;
					
					if(answer == 0)
					{
						newf(firstBox, true);
					}
					else if(answer == 1)
					{
						save(new SaveEvent()
						{
							@Override
							public void onSave()
							{
								newf(firstBox, true);
							}
						});
					}
				}
			});
		}
	}
	
	public void exit()
	{
		if(menuFileSave.isDisabled())
		{
			Gdx.app.exit();
		}
		else
		{
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + (save == null ? "Untitled" : save.getName()) + "\"", true)
			{
				@Override
				public void onClose(int answer)
				{
					dialogOpen = false;
					
					if(answer == 0)
					{
						Gdx.app.exit();
					}
					else if(answer == 1)
					{
						save(new SaveEvent()
						{
							@Override
							public void onSave()
							{
								Gdx.app.exit();
							}
						});
					}
				}
			});
		}
	}
	
	public void openDialog(boolean force)
	{
		if(menuFileSave.isDisabled() || force)
		{
			FileTypeFilter filter = new FileTypeFilter(true);
			filter.addRule("Trashy3D files (*.t3d)", "t3d");
			
			FileChooser chooser = new FileChooser(Mode.OPEN);
			chooser.setSelectionMode(SelectionMode.FILES);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileTypeFilter(filter);
			chooser.getTitleLabel().setText("Open file");
			
			chooser.setListener(new FileChooserAdapter()
			{
				@Override
				public void selected(Array<FileHandle> file)
				{
					dialogOpen = false;
					openFile(file.get(0).file());
				}
				
				@Override
				public void canceled()
				{
					dialogOpen = false;
				}
			});
			
			popup(chooser);
		}
		else
		{
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + (save == null ? "Untitled" : save.getName()) + "\"", true)
			{
				@Override
				public void onClose(int answer)
				{
					dialogOpen = false;
					
					if(answer == 0)
					{
						openDialog(true);
					}
					else if(answer == 1)
					{
						save(new SaveEvent()
						{
							@Override
							public void onSave()
							{
								openDialog(true);
							}
						});
					}
				}
			});
		}
	}
	
	public void openFile(File file)
	{
		newf(false, true);
		
		try
		{
			LinkedList<Undoable> newUndos = Loader.loadFile(file);
			for(int i = 0; i < newUndos.size() - 1; i++)
			{
				undos.add(newUndos.get(i));
			}
			
			undoAdd(newUndos.getLast());
			
			save = file;
			Gdx.graphics.setTitle(save.getName() + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		catch(IOException oops)
		{
			popup(new ExceptionDialog(oops, "Failed to open " + file.getName()));
			newf(true, true);
		}
		
		asterisk = undos.size();
	}
	
	public void saveAs(SaveEvent event)
	{
		FileTypeFilter filter = new FileTypeFilter(true);
		filter.addRule("Trashy3D files (*.t3d)", "t3d");
		
		FileChooser chooser = new FileChooser(Mode.SAVE);
		chooser.setSelectionMode(SelectionMode.FILES);
		chooser.setFileTypeFilter(filter);
		chooser.getTitleLabel().setText("Save file");
		
		chooser.setListener(new FileChooserAdapter()
		{
			@Override
			public void selected(Array<FileHandle> file)
			{
				dialogOpen = false;
				save = file.get(0).file();
				save(event);
			}
			
			@Override
			public void canceled()
			{
				dialogOpen = false;
			}
		});
		
		popup(chooser);
	}
	
	public void save(SaveEvent event)
	{
		if(save == null)
		{
			saveAs(event);
			return;
		}
		
		try
		{
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(save));
			stream.write((byte) 'T');
			stream.write((byte) '3');
			stream.write((byte) 'D');
			
			for(Undoable undo : undos)
			{
				stream.write(undo.serialize());
			}
			
			stream.close();

			Gdx.graphics.setTitle(save.getName() + " - Trashy 3D");
		}
		catch(IOException oops)
		{
			popup(new ExceptionDialog(oops, "Failed to save " + save.getName()));
		}
		
		asterisk = undos.size();
		menuFileSave.setDisabled(true);
		
		if(event != null)
			event.onSave();
	}
	
	public void undoAdd(Undoable undo)
	{
		undos.add(undo);
		redos.clear();
		menuEditUndo.setDisabled(false);
		menuEditRedo.setDisabled(true);
		
		Gdx.graphics.setTitle("*" + (save == null ? "(Untitled)" : save.getName()) + " - Trashy 3D");
		menuFileSave.setDisabled(false);
		if(undos.size() == asterisk)
			asterisk = -1;
	}
	
	public void undo()
	{
		undos.getLast().undo();
		redos.add(undos.getLast());
		undos.removeLast();
		menuEditRedo.setDisabled(false);
		menuEditUndo.setDisabled(undos.isEmpty());
		
		if(undos.size() == asterisk)
		{
			Gdx.graphics.setTitle((save == null ? "(Untitled)" : save.getName()) + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		else
		{
			Gdx.graphics.setTitle("*" + (save == null ? "(Untitled)" : save.getName()) + " - Trashy 3D");
			menuFileSave.setDisabled(false);
		}
	}
	
	public void redo()
	{
		Undoable copy = redos.getLast().redo();
		undos.add(copy);
		redos.removeLast();
		menuEditUndo.setDisabled(false);
		menuEditRedo.setDisabled(redos.isEmpty());
		
		if(undos.size() == asterisk)
		{
			Gdx.graphics.setTitle((save == null ? "(Untitled)" : save.getName()) + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		else
		{
			Gdx.graphics.setTitle("*" + (save == null ? "(Untitled)" : save.getName()) + " - Trashy 3D");
			menuFileSave.setDisabled(false);
		}
	}
	
	@Override
	public void dispose()
	{
		menuBar.getTable().remove();
	}
	
	private void popup(Dialog dialog)
	{
		if(dialogOpen)
			return;
		
		dialog.open();
		Dialog.setFocus();
		
		Gdx.input.setCursorCatched(false);
		Game.game().player.pause = true;
		dialogOpen = true;
	}
	
	private void popup(WidgetGroup widgets)
	{
		if(dialogOpen)
			return;
		
		Dialog.addWidget(widgets);
		Dialog.setFocus();
		
		Gdx.input.setCursorCatched(false);
		Game.game().player.pause = true;
		dialogOpen = true;
	}
	
	public static interface SaveEvent
	{
		public void onSave();
	}
}