package com.flossoftware.dbmigrator;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jxl.write.WriteException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

public class Gui {

	public static List<FieldsGui> fieldsGuiList = new ArrayList<FieldsGui>();
	public static boolean hasConnected = false;

	private static void saveProps(final ConfigGui composite,
			final ConfigGui composite2) throws IOException {
		composite.saveFile();
		composite2.saveFile();
		
		// delete map/ contents
		
		Path directory = Paths.get("map");
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		       Files.delete(file);
		       return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		       Files.delete(dir);
		       return FileVisitResult.CONTINUE;
		   }
		});


		try {
			PropertiesConfiguration tableProp = new PropertiesConfiguration();
			for (FieldsGui fieldsGui : fieldsGuiList) {
				Combo combo = fieldsGui.fromTableSelect;
				Combo combo_1 = fieldsGui.toTableSelect;

				if (combo_1.getSelectionIndex() >= 0
						&& combo.getSelectionIndex() >= 0) {

					tableProp.setProperty(
							combo_1.getItem(combo.getSelectionIndex()),
							combo.getItem(combo.getSelectionIndex()));

					PropertiesConfiguration tableProp2 = new PropertiesConfiguration();

					TableItem[] items = fieldsGui.fieldTbl.getItems();

					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb");
						Combo c2 = (Combo) tItem.getData("cb2");

						if (c.getSelectionIndex() >= 0
								&& c2.getSelectionIndex() >= 0) {

							tableProp2.setProperty(
									c2.getItem(c2.getSelectionIndex()),
									c.getItem(c.getSelectionIndex()));
						}
					}

					tableProp2.save("map/"
							+ combo_1.getItem(combo.getSelectionIndex())
							+ ".properties");
					
					
				}

			}

			tableProp.save("tableMap.properties");

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			SQLException {

		try {
			
			PropertiesConfiguration p = new PropertiesConfiguration(
					"tableMap.properties");

			DBMetadata d1 = new DBMetadata(new PropertiesConfiguration(
					"config/from-db.properties"));

			DBMetadata d2 = new DBMetadata(new PropertiesConfiguration(
					"config/to-db.properties"));

			Display display = new Display();
			final Shell shell = new Shell(display, SWT.SHELL_TRIM
			// & (~SWT.RESIZE)
			);

			shell.setLayout(new GridLayout(2, false));

			Menu menu = new Menu(shell, SWT.BAR);
			shell.setMenuBar(menu);

			MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
			mntmFile.setText("File...");

			Menu menu_1 = new Menu(mntmFile);
			mntmFile.setMenu(menu_1);

			MenuItem mntmConnect = new MenuItem(menu_1, SWT.NONE);
			mntmConnect.setText("Connect to DB");

			MenuItem mntmAdd = new MenuItem(menu_1, SWT.NONE);
			mntmAdd.setText("Add mapping");

			MenuItem mntmGenerate = new MenuItem(menu_1, SWT.NONE);
			mntmGenerate.setText("Save and get scripts");


			final ConfigGui composite = new ConfigGui(shell, SWT.NONE,
					"config/from-db.properties", false);
			composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
					false, 1, 1));

			final ConfigGui composite2 = new ConfigGui(shell, SWT.NONE,
					"config/to-db.properties", true);
			composite2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
					false, 1, 1));
			
			shell.pack();


//			final Rectangle shellSize = shell.getClientArea();
			final Rectangle shellSize = shell.getBounds();
			
			final ScrolledComposite sc = new ScrolledComposite(shell,
					SWT.H_SCROLL | SWT.V_SCROLL);
			
			sc.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
					false, 2, 1));

			final Composite c = new Composite(sc, SWT.NONE);
			c.setLayout(new GridLayout(1, false));



			for (Iterator<String> iterator = p.getKeys(); iterator.hasNext();) {
				String tableTo = (String) iterator.next();
				String tableFrom = p.getString(tableTo);

				p = new PropertiesConfiguration("map/" + tableTo
						+ ".properties");

				FieldsGui composite3 = new FieldsGui(c, shell, SWT.NONE,
						composite, composite2, fieldsGuiList);
				composite3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
						true, false, 1, 1));
				composite3.setFromConfig(p, d1, d2, tableFrom, tableTo);
				//fieldsGuiList.add(composite3);

			}
			
			sc.setContent(c);
			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			
			

			for (FieldsGui fGui : fieldsGuiList) {
				fGui.size(shellSize);
			}

			shell.pack();

			shell.open();
			
			
			
			mntmConnect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					composite.dbConnect(false);
					composite2.dbConnect(false);
					hasConnected = true;
				}
			});

			mntmAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {

					FieldsGui composite3 = new FieldsGui(c, shell, SWT.NONE,
							composite, composite2, fieldsGuiList);
					composite3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
							false, false, 2, 1));
					composite3.size(shellSize);

					//fieldsGuiList.add(composite3);
					shell.pack();

					if (hasConnected) {
						composite.dbConnect(true);
						composite2.dbConnect(true);
					}
				}

			});

			mntmGenerate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					try {

						saveProps(composite, composite2);

						DBMigrator.doTheJob();

					} catch (ConfigurationException | ClassNotFoundException
							| WriteException | IOException | SQLException e) {
						e.printStackTrace();
					}
				}
			});

			

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();

		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
