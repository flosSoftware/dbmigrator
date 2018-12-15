package com.flossoftware.dbmigrator;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class FieldsGui extends Composite {

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */

	final public Shell shell;

	final public Combo fromTableSelect;
	final public Combo toTableSelect;

	final public Table fieldTbl;
	final public TableColumn fieldTblCol1;
	final public TableColumn fieldTblCol2;
	final public TableColumn fieldTblCol3;

	final public Table tableTbl;
	final public TableColumn tableTblCol1;
	final public TableColumn tableTblCol2;

	private int fieldsGuiListIndex;
	
	public FieldsGui(final Composite parent, final Shell shell, int style,
			final ConfigGui configGuiFrom, final ConfigGui configGuiTo, final List<FieldsGui> fieldsGuiList) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		this.shell = shell;
		 
		final FieldsGui dis = this;
		
		Button removeBtn = new Button(this, SWT.NONE);
		removeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fieldsGuiList.remove(fieldsGuiListIndex);
				
				for (FieldsGui fieldsGui : fieldsGuiList) {
					if(fieldsGui.fieldsGuiListIndex > fieldsGuiListIndex)
						fieldsGui.fieldsGuiListIndex--;
				}
				
				dis.dispose();
				
				parent.layout(true, true);
			}});
		removeBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		removeBtn.setText("Remove Mapping");
		
		tableTbl = new Table(this, SWT.BORDER | SWT.V_SCROLL);
		tableTbl.setHeaderVisible(true);
		tableTbl.setLinesVisible(true);

		tableTblCol1 = new TableColumn(tableTbl, SWT.NONE);
		tableTblCol1.setText("Source Table");

		tableTblCol2 = new TableColumn(tableTbl, SWT.NONE);
		tableTblCol2.setText("Destination Table");

		fromTableSelect = new Combo(tableTbl, SWT.NONE);

		toTableSelect = new Combo(tableTbl, SWT.NONE);

		TableItem tableItem = new TableItem(tableTbl, SWT.NONE);

		TableEditor editor = new TableEditor(tableTbl);
		editor.grabHorizontal = true;
		editor.setEditor(fromTableSelect, tableItem, 0);

		editor = new TableEditor(tableTbl);
		editor.grabHorizontal = true;
		editor.setEditor(toTableSelect, tableItem, 1);
		
		

		Button btnAddField = new Button(this, SWT.NONE);
		btnAddField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				DBMetadata dbMeta;
				List<String> l1;
				String[] a1;

				try {

					final TableItem tableItem = new TableItem(fieldTbl, SWT.NONE);

					TableEditor editor = new TableEditor(fieldTbl);
					Combo cb = new Combo(fieldTbl, SWT.NONE);
					editor.grabHorizontal = true;
					editor.setEditor(cb, tableItem, 0);
					tableItem.setData("cb", cb);

					if (fromTableSelect.getSelectionIndex() >= 0) {

						dbMeta = new DBMetadata(configGuiFrom.getPropHashMap());

						l1 = dbMeta.getColumnsForTable(fromTableSelect.getItem(fromTableSelect
								.getSelectionIndex()));

						a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						cb.setItems(a1);

					}

					editor = new TableEditor(fieldTbl);
					Combo cb2 = new Combo(fieldTbl, SWT.NONE);
					editor.grabHorizontal = true;
					editor.setEditor(cb2, tableItem, 1);
					tableItem.setData("cb2", cb2);

					if (toTableSelect.getSelectionIndex() >= 0) {
						dbMeta = new DBMetadata(configGuiTo.getPropHashMap());

						l1 = dbMeta.getColumnsForTable(toTableSelect.getItem(fromTableSelect
								.getSelectionIndex()));

						a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						cb2.setItems(a1);

					}
					
					int rows = fieldTbl.getItemCount();
					
					editor = new TableEditor(fieldTbl);
					Button btnRmField = new Button(fieldTbl, SWT.NONE);
					btnRmField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
									true));
					btnRmField.setText("Remove");
					
					btnRmField.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							
							 // Identify the selected row
		                    
		                   System.out.println("Row Index:"+e.widget.getData());
		                    
		                   int idx = (int)e.widget.getData();
		                    
		                   Combo cb = (Combo) fieldTbl.getItem(idx).getData("cb");		                   
		                   Combo cb2 = (Combo) fieldTbl.getItem(idx).getData("cb2");		                   
		                   Button btn = (Button) fieldTbl.getItem(idx).getData("btn");
		                   
		                   cb.dispose();
		                   cb2.dispose();
		                   btn.dispose();
		                   
		                   fieldTbl.remove(idx);
		                   fieldTbl.redraw();
							
						}});
					
					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(btnRmField, tableItem, 2);
					btnRmField.setData(rows-1);
					tableItem.setData("btn", btnRmField);

					fieldTbl.setSize(fieldTbl.getSize().x,
							fieldTbl.getSize().y + tableItem.getBounds().height
									+ fieldTbl.getBorderWidth());
					fieldTbl.redraw();

					int oldWidth = shell.getSize().x;

					shell.layout(true, true);

					final Point newSize = shell.computeSize(SWT.DEFAULT,
							SWT.DEFAULT, true);
					newSize.x = oldWidth;

					shell.setSize(newSize);

				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}
			}
		});

		btnAddField.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 1, 1));
		btnAddField.setText("Add Field");

		fieldTbl = new Table(this, SWT.BORDER | SWT.V_SCROLL);
		fieldTbl.setHeaderVisible(true);
		fieldTbl.setLinesVisible(true);

		fieldTblCol1 = new TableColumn(fieldTbl, SWT.NONE);
		fieldTblCol1.setText("Source Field");

		fieldTblCol2 = new TableColumn(fieldTbl, SWT.NONE);
		fieldTblCol2.setText("Destination Field");
		
		fieldTblCol3 = new TableColumn(fieldTbl, SWT.NONE);
		fieldTblCol3.setText("");

		fromTableSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TableItem[] items = fieldTbl.getItems();
				DBMetadata dbMeta;

				try {

					dbMeta = new DBMetadata(configGuiFrom.getPropHashMap());

					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb");

						List<String> l1 = dbMeta.getColumnsForTable(fromTableSelect
								.getItem(fromTableSelect.getSelectionIndex()));

						String[] a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						c.setItems(a1);
					}

				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}

			}
		});

		toTableSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TableItem[] items = fieldTbl.getItems();
				DBMetadata dbMeta;

				try {

					dbMeta = new DBMetadata(configGuiTo.getPropHashMap());

					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb2");

						List<String> l1 = dbMeta.getColumnsForTable(toTableSelect
								.getItem(toTableSelect.getSelectionIndex()));

						String[] a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						c.setItems(a1);
					}

				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
				}

			}
		});

		
		fieldsGuiList.add(this);
		fieldsGuiListIndex = fieldsGuiList.size()-1;
	}

	public void size(Rectangle shellSize) {

		Rectangle area = shellSize;

		int width = area.width;

		//System.out.println(width);

		tableTbl.setSize(width, area.height);
		tableTblCol1.setWidth(width / 2);
		tableTblCol2.setWidth(width - tableTblCol1.getWidth());
		
		fieldTbl.setSize(width, area.height);
		
		fieldTblCol3.setWidth(80);
		fieldTblCol1.setWidth((width - 80) / 2);
		fieldTblCol2.setWidth(width - fieldTblCol1.getWidth() - fieldTblCol3.getWidth());
		
		

	}

	public void setFromConfig(PropertiesConfiguration p, DBMetadata d1,
			DBMetadata d2, String tableFrom, String tableTo)
			throws SQLException {

		List<String> l1 = d1.getTables();

		String[] a1 = new String[l1.size()];
		a1 = l1.toArray(a1);

		fromTableSelect.setItems(a1);

		fromTableSelect.select(l1.indexOf(tableFrom));

		l1 = d2.getTables();

		a1 = new String[l1.size()];
		a1 = l1.toArray(a1);
		toTableSelect.setItems(a1);

		toTableSelect.select(l1.indexOf(tableTo));

		l1 = d1.getColumnsForTable(tableFrom);
		List<String> l2 = d2.getColumnsForTable(tableTo);
		
		int row = 0;

		for (Iterator<String> iterator = p.getKeys(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String value = p.getString(key);

			TableItem tableItem = new TableItem(fieldTbl, SWT.NONE);

			TableEditor editor = new TableEditor(fieldTbl);
			Combo combo_2 = new Combo(fieldTbl, SWT.NONE);
			a1 = new String[l1.size()];
			a1 = l1.toArray(a1);

			combo_2.setItems(a1);

			combo_2.select(l1.indexOf(value));

			editor.grabHorizontal = true;
			editor.setEditor(combo_2, tableItem, 0);
			tableItem.setData("cb", combo_2);

			editor = new TableEditor(fieldTbl);
			Combo combo_3 = new Combo(fieldTbl, SWT.NONE);

			a1 = new String[l2.size()];
			a1 = l2.toArray(a1);

			combo_3.setItems(a1);

			combo_3.select(l2.indexOf(key));

			editor.grabHorizontal = true;
			editor.setEditor(combo_3, tableItem, 1);
			tableItem.setData("cb2", combo_3);
			
			editor = new TableEditor(fieldTbl);
			
			
			Button btnRmField = new Button(fieldTbl, SWT.NONE);
			btnRmField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));
			btnRmField.setText("Remove");
			
			btnRmField.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					
					 // Identify the selected row
                    
                    //System.out.println("Row Index:"+e.widget.getData());
                    
                   int idx = (int)e.widget.getData();
                    
                   Combo cb = (Combo) fieldTbl.getItem(idx).getData("cb");		                   
                   Combo cb2 = (Combo) fieldTbl.getItem(idx).getData("cb2");		                   
                   Button btn = (Button) fieldTbl.getItem(idx).getData("btn");
                   
                   cb.dispose();
                   cb2.dispose();
                   btn.dispose();
                   
                   fieldTbl.remove(idx);
                   fieldTbl.redraw();
					
				}});
			
			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.setEditor(btnRmField, tableItem, 2);
			btnRmField.setData(row++);
//			System.out.println(combo_2.getItemHeight());
//			btnRmField.setSize(fieldTblCol3.getWidth(), combo_2.getItemHeight());
			tableItem.setData("btn", btnRmField);
			

			// table.setSize(
			// table.getSize().x,
			// table.getSize().y + tableItem.getBounds().height
			// + table.getBorderWidth());
			// table.redraw();

			// getParent().layout(true, true);
			// final Point newSize = getParent().computeSize(SWT.DEFAULT,
			// SWT.DEFAULT, true);
			//
			// getParent().setSize(newSize);

		}

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}