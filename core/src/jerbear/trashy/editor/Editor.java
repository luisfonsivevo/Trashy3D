package jerbear.trashy.editor;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooser.Mode;
import com.kotcrab.vis.ui.widget.file.FileChooser.SelectionMode;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;

import jerbear.trashy.editor.tools.*;
import jerbear.trashy.editor.tools.Grid.GridShape;
import jerbear.trashy.loader.T3DFile;
import jerbear.trashy.loader.Undoable;
import jerbear.trashy.loader.Undoable.AddShape;
import jerbear.util2d.dialog.Dialog;
import jerbear.util2d.dialog.ExceptionDialog;
import jerbear.util2d.dialog.YesNoDialog;
import jerbear.util3d.ShapeInstance;
import jerbear.util3d.World;
import jerbear.util3d.shapes.Box;
import jerbear.util3d.shapes.Shape;

public class Editor extends ApplicationAdapter
{
	//keep references for enabling/disabling
	private MenuBar menuBar;
	private MenuItem menuFileSave;
	private MenuItem menuEditUndo;
	private MenuItem menuEditRedo;
	
	private T3DFile file;
	private LinkedList<Undoable> redos = new LinkedList<Undoable>();
	private int asterisk = 0;
	
	private Tool curTool;
	private Grid grid;
	private PaintCan paintCan;
	
	private boolean dialogOpen;
	private boolean ignoreEscape;
	
	@Override
	public void create()
	{
		Bullet.init();
		Dialog.setSkin(Gdx.files.internal("skin-vis-x1/uiskin.json"));
		FileChooser.setDefaultPrefsName("jerbear.trashy3d.filechooser");
		
		file = new T3DFile(new World(new EditorPlayer(0.5f, 2, 0.5f, 0, 1, 0, 1.5f, 0, 1), 15));
		
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
		MenuItem menuToolsPaint = new MenuItem("Paint Can");
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
		PopupMenu menuToolsPaintSub = new PopupMenu();
		MenuItem menuToolsPaintColor = new MenuItem("Color");
		MenuItem menuToolsPaintTexture = new MenuItem("Texture");
		menuToolsPaintSub.addItem(menuToolsPaintColor);
		menuToolsPaintSub.addItem(menuToolsPaintTexture);
		menuToolsPaint.setSubMenu(menuToolsPaintSub);
		menuTools.addItem(menuToolsPaint);
		menuBar.addMenu(menuTools);
		
		grid = new Grid(this, file.world, 5, 1);
		paintCan = new PaintCan(this, file.world, Color.RED);
		
		curTool = grid;
		Gdx.input.setInputProcessor(curTool);
		
		menuFileNew.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				newFile(true, false);
			}
		});
		
		menuFileOpen.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				openFileDialog(false);
			}
		});
		
		menuFileSave.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				saveFile(null);
			}
		});
		
		menuFileSaveAs.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				saveFileDialog(null);
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
		
		
		
		menuToolsGrid.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.BOX);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridRamp.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.RAMP);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridWall.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.WALL);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridTri.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.TRIANGLE);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridSphere.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.SPHERE);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridCylinder.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CYLINDER);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridCone.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CONE);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsGridCapsule.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				grid.setShape(GridShape.CAPSULE);
				curTool = grid;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsPaint.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				curTool = paintCan;
				Gdx.input.setInputProcessor(curTool);
			}
		});
		
		menuToolsPaintColor.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				popup(new ColorPicker(new ColorPickerAdapter()
				{
					@Override
					public void finished(Color color)
					{
						dialogOpen = false;
						
						paintCan.mat = new Material(ColorAttribute.createDiffuse(color), new BlendingAttribute());
						curTool = paintCan;
						Gdx.input.setInputProcessor(curTool);
					}
					
					@Override
					public void canceled(Color oldColor)
					{
						dialogOpen = false;
						ignoreEscape = true;
					}
				}));
			}
		});
		
		menuToolsPaintTexture.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				//supported: https://github.com/libgdx/libgdx/blob/master/gdx/jni/gdx2d/stb_image.h
				FileTypeFilter filter = new FileTypeFilter(true);
				filter.addRule("Image files", "jpg", "jpeg", "png", "tga", "bmp", "psd", "gif", "hdr", "pic", "pnm", "cim");
				filter.addRule("JPEG images (*.jpg;*.jpeg)", "jpg", "jpeg");
				filter.addRule("PNG images (*.png)", "png");
				filter.addRule("TGA images (*.tga)", "tga");
				filter.addRule("BMP images (*.bmp)", "bmp");
				filter.addRule("GIF images (*.gif)", "gif");
				filter.addRule("HDR images (*.hdr)", "hdr");
				filter.addRule("PIC images (*.pic)", "pic");
				filter.addRule("PNM images (*.pnm)", "pnm");
				filter.addRule("Photoshop documents (*.psd)", "psd");
				filter.addRule("libGDX pixmaps (*.cim)", "cim");
				
				FileChooser chooser = new FileChooser(Mode.OPEN); //TODO image previews?
				chooser.setSelectionMode(SelectionMode.FILES);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileTypeFilter(filter);
				chooser.getTitleLabel().setText("Open texture");
				
				chooser.setListener(new FileChooserAdapter()
				{
					@Override
					public void selected(Array<FileHandle> file)
					{
						dialogOpen = false;
						
						paintCan.mat = new Material(TextureAttribute.createDiffuse(Editor.this.file.world.getTexture(file.get(0))), new BlendingAttribute());
						curTool = paintCan;
						Gdx.input.setInputProcessor(curTool);
					}
					
					@Override
					public void canceled()
					{
						dialogOpen = false;
						ignoreEscape = true;
					}
				});
				
				popup(chooser);
			}
		});
		
		Dialog.addWidget(menuBar.getTable());
		newFile(true, true);
	}
	
	@Override
	public void render()
	{
		file.world.draw(Color.BLACK);
		
		boolean ctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		//boolean alt = Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
		boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		
		if((Gdx.input.isKeyJustPressed(Keys.ALT_LEFT)))
		{
			if(Gdx.input.getInputProcessor() == curTool)
			{
				Dialog.setFocus();
				Gdx.input.setCursorCatched(false);
				player().pause = true;
			}
			else
			{
				Gdx.input.setInputProcessor(curTool);
				Gdx.input.setCursorCatched(true);
				player().pause = false;
			}
		}
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.N))
			newFile(true, false);
		
		if(Gdx.input.isKeyJustPressed(Keys.ESCAPE) && !dialogOpen && !ignoreEscape)
			exit();	
		
		if(ctrl && Gdx.input.isKeyJustPressed(Keys.O))
			openFileDialog(false);
		
		if(ctrl && !shift && Gdx.input.isKeyJustPressed(Keys.S) && !menuFileSave.isDisabled())
			saveFile(null);
		
		if(ctrl && shift && Gdx.input.isKeyJustPressed(Keys.S))
			saveFileDialog(null);
		
		if(ctrl && !shift && Gdx.input.isKeyJustPressed(Keys.Z) && !file.undos.isEmpty())
			undo();
		
		if(ctrl && shift && Gdx.input.isKeyJustPressed(Keys.Z) && !redos.isEmpty())
			redo();
		
		ignoreEscape = false;
		curTool.draw();
		
		Dialog.draw();
	}
	
	public void newFile(boolean firstBox, boolean force)
	{
		if(menuFileSave.isDisabled() || force)
		{
			while(!file.undos.isEmpty())
				undo();
			
			if(firstBox)
				file.file = null; //TODO kinda messy but works
			
			redos.clear();
			
			if(firstBox)
			{
				undoAdd(new AddShape(file.world.addShape(new ShapeInstance(new Box(2, 1, 2, Color.RED).disposeByWorld(file.world), 0, -0.5f, 0, CollisionFlags.CF_STATIC_OBJECT, 0))));
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
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + file.getFileName() + "\"?", true)
			{
				@Override
				public void onClose(int answer)
				{
					dialogOpen = false;
					
					if(answer == 0)
					{
						newFile(firstBox, true);
					}
					else if(answer == 1)
					{
						saveFile(new SaveEvent()
						{
							@Override
							public void onSave()
							{
								newFile(firstBox, true);
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
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + file.getFileName() + "\"?", true)
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
						saveFile(new SaveEvent()
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
	
	public void openFileDialog(boolean force)
	{
		if(menuFileSave.isDisabled() || force)
		{
			FileTypeFilter filter = new FileTypeFilter(true);
			filter.addRule("Trashy3D files (*.t3d)", "t3d");
			
			FileChooser chooser = new FileChooser(Mode.OPEN);
			chooser.setSelectionMode(SelectionMode.FILES);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileTypeFilter(filter);
			chooser.getTitleLabel().setText("Open game");
			
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
					ignoreEscape = true;
				}
			});
			
			popup(chooser);
		}
		else
		{
			popup(new YesNoDialog("Trashy3D", "Save changes to \"" + file.getFileName() + "\"", true)
			{
				@Override
				public void onClose(int answer)
				{
					dialogOpen = false;
					
					if(answer == 0)
					{
						openFileDialog(true);
					}
					else if(answer == 1)
					{
						saveFile(new SaveEvent()
						{
							@Override
							public void onSave()
							{
								openFileDialog(true);
							}
						});
					}
				}
			});
		}
	}
	
	public void openFile(File file)
	{
		newFile(false, true);
		
		try
		{
			T3DFile newFile = new T3DFile(this.file.world, file);
			this.file = newFile;
			
			asterisk = this.file.undos.size();
			
			Gdx.graphics.setTitle(file.getName() + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		catch(IOException oops)
		{
			popup(new ExceptionDialog(oops, "Failed to open " + file.getName())
			{
				@Override
				public void close()
				{
					super.close();
					dialogOpen = false;
				}
			});
			
			newFile(true, true);
		}
	}
	
	public void saveFileDialog(SaveEvent event)
	{
		FileTypeFilter filter = new FileTypeFilter(true);
		filter.addRule("Trashy3D files (*.t3d)", "t3d");
		
		FileChooser chooser = new FileChooser(Mode.SAVE);
		chooser.setSelectionMode(SelectionMode.FILES);
		chooser.setFileTypeFilter(filter);
		chooser.getTitleLabel().setText("Save game");
		
		chooser.setListener(new FileChooserAdapter()
		{
			@Override
			public void selected(Array<FileHandle> file)
			{
				dialogOpen = false;
				Editor.this.file.file = file.get(0).file();
				saveFile(event);
			}
			
			@Override
			public void canceled()
			{
				dialogOpen = false;
				ignoreEscape = true;
			}
		});
		
		popup(chooser);
	}
	
	public void saveFile(SaveEvent event)
	{
		if(file.file == null)
		{
			saveFileDialog(event);
			return;
		}
		
		try
		{
			file.saveFile();
			Gdx.graphics.setTitle(file.getFileName() + " - Trashy 3D");
		}
		catch(IOException oops)
		{
			popup(new ExceptionDialog(oops, "Failed to save " + file.getFileName())
			{
				@Override
				public void close()
				{
					super.close();
					dialogOpen = false;
				}
			});
		}
		
		asterisk = file.undos.size();
		menuFileSave.setDisabled(true);
		
		if(event != null)
			event.onSave();
	}
	
	public void undoAdd(Undoable undo)
	{
		file.undos.add(undo);
		redos.clear();
		menuEditUndo.setDisabled(false);
		menuEditRedo.setDisabled(true);
		
		Gdx.graphics.setTitle("*" + file.getFileName() + " - Trashy 3D");
		menuFileSave.setDisabled(false);
		if(file.undos.size() == asterisk)
			asterisk = -1;
	}
	
	public void undo()
	{
		file.undos.getLast().undo(file.world);
		redos.add(file.undos.getLast());
		file.undos.removeLast();
		menuEditRedo.setDisabled(false);
		menuEditUndo.setDisabled(file.undos.isEmpty());
		
		if(file.undos.size() == asterisk)
		{
			Gdx.graphics.setTitle(file.getFileName() + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		else
		{
			Gdx.graphics.setTitle("*" + file.getFileName() + " - Trashy 3D");
			menuFileSave.setDisabled(false);
		}
	}
	
	public void redo()
	{
		Undoable copy = redos.getLast().redo(file.world);
		file.undos.add(copy);
		redos.removeLast();
		menuEditUndo.setDisabled(false);
		menuEditRedo.setDisabled(redos.isEmpty());
		
		if(file.undos.size() == asterisk)
		{
			Gdx.graphics.setTitle(file.getFileName() + " - Trashy 3D");
			menuFileSave.setDisabled(true);
		}
		else
		{
			Gdx.graphics.setTitle("*" + file.getFileName() + " - Trashy 3D");
			menuFileSave.setDisabled(false);
		}
	}
	
	public boolean hasShape(Shape shape)
	{
		//TODO implement and move to world class
		return false;
	}
	
	@Override
	public void dispose()
	{
		file.dispose();
		Dialog.dispose();
	}
	
	private void popup(Dialog dialog)
	{
		if(dialogOpen)
			return;
		
		dialog.open();
		Dialog.setFocus();
		
		Gdx.input.setCursorCatched(false);
		player().pause = true;
		dialogOpen = true;
	}
	
	private void popup(WidgetGroup widgets)
	{
		if(dialogOpen)
			return;
		
		Dialog.addWidget(widgets);
		Dialog.setFocus();
		
		Gdx.input.setCursorCatched(false);
		player().pause = true;
		dialogOpen = true;
	}
	
	private EditorPlayer player()
	{
		return (EditorPlayer) file.world.player;
	}
	
	public static interface SaveEvent
	{
		public void onSave();
	}
}