package com.albertof.dbmigrator;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.mihalis.opal.notify.Notifier;

import jxl.write.WriteException;

public class Gui {
 
	public List<FieldsGui> fieldsGuiList = new ArrayList<FieldsGui>();
	private DBMetadata d1;
	private DBMetadata d2;

	private void saveProps(final ConfigGui composite, final ConfigGui composite2) throws IOException {
		composite.saveFile();
		composite2.saveFile();

		// delete map/ contents

		FileUtils.deleteDirectory(new File("map"));

		try {
			PropertiesConfiguration tableProp = new PropertiesConfiguration();
			for (FieldsGui fieldsGui : fieldsGuiList) {
				Combo combo = fieldsGui.fromTableSelect;
				Combo combo_1 = fieldsGui.toTableSelect;

				if (combo_1.getSelectionIndex() >= 0 && combo.getSelectionIndex() >= 0) {

					tableProp.setProperty(combo_1.getItem(combo.getSelectionIndex()),
							combo.getItem(combo.getSelectionIndex()));

					PropertiesConfiguration tableProp2 = new PropertiesConfiguration();

					TableItem[] items = fieldsGui.fieldTbl.getItems();

					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb");
						Combo c2 = (Combo) tItem.getData("cb2");

						if (c.getSelectionIndex() >= 0 && c2.getSelectionIndex() >= 0) {

							tableProp2.setProperty(c2.getItem(c2.getSelectionIndex()),
									c.getItem(c.getSelectionIndex()));
						}
					}

					tableProp2.save("map/" + combo_1.getItem(combo.getSelectionIndex()) + ".properties");

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
	 * @throws ConfigurationException
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, ConfigurationException {
		/*
		 * DBMetadata dbMeta = new DBMetadata(new PropertiesConfiguration(
		 * "config/from-db.properties"));
		 * 
		 * List<String> l1 = dbMeta.getTables();
		 * 
		 * for (String string : l1) { System.out.println(string); }
		 */
		final Gui gui = new Gui();

		PropertiesConfiguration p = null;
		
		boolean showNotificationError = false;
		String notificationErrorTitle = null;
		String notificationErrorMsg = null;
		
		try {

			p = new PropertiesConfiguration("tableMap.properties");

			gui.setD1(new DBMetadata(new PropertiesConfiguration("config/from-db.properties")));

			gui.setD2(new DBMetadata(new PropertiesConfiguration("config/to-db.properties")));
			
		} catch (ConfigurationException e) {
			showNotificationError = true;
			notificationErrorTitle = "Configuration error"; 
			notificationErrorMsg = e.getMessage();
		} catch (SQLException e) {
			showNotificationError = true;
			notificationErrorTitle = "Database error"; 
			notificationErrorMsg = "Please check the connection configuration and retry";
		} catch (ClassNotFoundException e) {
			showNotificationError = true;
			notificationErrorTitle = "Driver error"; 
			notificationErrorMsg = "Couldn't load: "+e.getMessage();
		} finally {
		
			Display display = new Display();
	
			final Shell shell = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE));
	
			shell.setLayout(new GridLayout(2, true));
	
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
	
			final ConfigGui composite = new ConfigGui(shell, SWT.NONE, "config/from-db.properties", false, gui);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	
			final ConfigGui composite2 = new ConfigGui(shell, SWT.NONE, "config/to-db.properties", true, gui);
			composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
	
			shell.pack();
	
			// final Rectangle shellSize = shell.getClientArea();
			final Rectangle shellSize = shell.getBounds();
	
			final ScrolledComposite sc = new ScrolledComposite(shell,
					// SWT.H_SCROLL |
					SWT.V_SCROLL);
	
			final Composite c = new Composite(sc, SWT.NONE);
			c.setLayout(new GridLayout(1, true));
	
			for (Iterator<String> iterator = p.getKeys(); iterator.hasNext();) {
				String tableTo = (String) iterator.next();
				String tableFrom = p.getString(tableTo);
	
				p = new PropertiesConfiguration("map/" + tableTo + ".properties");
	
				FieldsGui composite3 = new FieldsGui(c, sc, shell, SWT.NONE, composite, composite2, gui.fieldsGuiList, gui);
				composite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				composite3.setFromConfig(p, gui.getD1(), gui.getD2(), tableFrom, tableTo);
				composite3.size(shellSize);
	
			}
	
			if (gui.fieldsGuiList.size() == 0) {
	
				FieldsGui composite3 = new FieldsGui(c, sc, shell, SWT.NONE, composite, composite2, gui.fieldsGuiList, gui);
				composite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				composite3.size(shellSize);
				gui.fieldsGuiList.add(composite3);
				sc.setContent(composite3);
			}
	
			sc.setContent(c);
			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);
			sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			gd.heightHint = c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
	
			sc.setLayoutData(gd);
	
			((Button) gui.fieldsGuiList.get(0).getData("rmBtn")).setVisible(false);
	
			shell.pack();
	
			shell.setLocation(0, 0);
	
			shell.open();
			
			if(showNotificationError)
				Notifier.notify(notificationErrorTitle, notificationErrorMsg);
	
			mntmConnect.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					boolean error = false;
					try {
						
						if (gui.getD1() != null && gui.getD1().getConnection2() != null)
							gui.getD1().getConnection2().close();
						if (gui.getD2() != null && gui.getD2().getConnection2() != null)
							gui.getD2().getConnection2().close();
	
						gui.setD1(composite.dbConnect(false));
						gui.setD2(composite2.dbConnect(false));
	
					} catch (SQLException e) {
						error = true;
						Notifier.notify("Database error", "Please check the connection configuration and retry");
	
					} catch (ClassNotFoundException e) {
						error = true;
						Notifier.notify("Driver error", e.getMessage());	
					} finally {
						if(!error) {
							Notifier.notify("Success", "The application has established connection to the databases");
						}
					}
	
				}
			});
	
			mntmAdd.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
	
					try {
						if (gui.getD1() == null)
							gui.setD1(composite.dbConnect(true));
						if (gui.getD2() == null)
							gui.setD2(composite2.dbConnect(true));
					} catch (SQLException e) {
						Notifier.notify("Database error", "Please check the connection configuration and retry");
	
					} catch (ClassNotFoundException e) {
						Notifier.notify("Driver error", e.getMessage());
	
					}
	
					FieldsGui composite3 = new FieldsGui(c, sc, shell, SWT.NONE, composite, composite2, gui.fieldsGuiList,
							gui);
					composite3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
					composite3.size(shellSize);
	
					sc.setMinHeight(c.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
					((GridData) sc.getLayoutData()).heightHint = gui.fieldsGuiList.get(0).computeSize(SWT.DEFAULT,
							SWT.DEFAULT).y;
	
					shell.pack();
					shell.redraw();
	
				}
	
			});
	
			mntmGenerate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					try {
	
						gui.saveProps(composite, composite2);
	
						if (gui.fieldsGuiList.size() > 0)
							DBMigrator.doTheJob(gui.d1, gui.d2);
	
					} catch (ConfigurationException | ClassNotFoundException | WriteException | IOException
							| SQLException e) {
						e.printStackTrace();
					}
				}
			});
	
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.dispose();
		}
	}

	public DBMetadata getD1() {
		return d1;
	}

	public void setD1(DBMetadata d1) {
		this.d1 = d1;
	}

	public DBMetadata getD2() {
		return d2;
	}

	public void setD2(DBMetadata d2) {
		this.d2 = d2;
	}

}