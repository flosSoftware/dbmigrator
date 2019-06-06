package com.albertof.dbmigrator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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

	private Shell shell;

	private Combo fromTableSelect;
	private Combo toTableSelect;

	private Table fieldTbl;
	private TableColumn fieldTblCol1;
	private TableColumn fieldTblCol2;
	private TableColumn fieldTblCol3;

	private Table tableTbl;
	private TableColumn tableTblCol1;
	private TableColumn tableTblCol2;

	private Gui gui;
	private int fieldsGuiListIndex;

	public FieldsGui(final Composite parent, final ScrolledComposite sc, final Shell shell, int style, ConfigGui configGuiFrom,
			ConfigGui configGuiTo, final List<FieldsGui> fieldsGuiList, final Gui gui
	) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		this.gui = gui;

		this.shell = shell;
		
		final FieldsGui dis = this;
		
		
		Button removeBtn = new Button(this, SWT.NONE);
		removeBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//System.out.println("idx " + dis.fieldsGuiListIndex);
				fieldsGuiList.remove(dis.fieldsGuiListIndex);

				for (FieldsGui fieldsGui : fieldsGuiList) {
					if (fieldsGui.fieldsGuiListIndex > fieldsGuiListIndex)
						fieldsGui.fieldsGuiListIndex--;
				}

				dis.dispose();
				
				sc.setMinHeight(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				((GridData)sc.getLayoutData()).heightHint = gui.getFieldsGuiList().get(0).computeSize(SWT.DEFAULT, SWT.DEFAULT).y;


				shell.layout(true, true);
				shell.redraw();
			}
		});
		removeBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		removeBtn.setText("Remove Mapping");
		this.setData("rmBtn", removeBtn);

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

				DBMetadata dbMeta = dis.getGui().getD1();
				DBMetadata dbMeta2 = dis.getGui().getD2();
				List<String> l1;
				String[] a1;

				try {
					
					int itemIdx = getFieldTbl().getItemCount(); // !!! TableItem are 0-indexed !!!
					
					//System.out.println("getItemCount "+fieldTbl.getItemCount());


					final TableItem tableItem = new TableItem(getFieldTbl(), SWT.NONE);

					TableEditor editor = new TableEditor(getFieldTbl());
					Combo cb = new Combo(getFieldTbl(), SWT.NONE);
					editor.grabHorizontal = true;
					editor.setEditor(cb, tableItem, 0);
					tableItem.setData("cb", cb);

					if (fromTableSelect.getSelectionIndex() >= 0) {

						l1 = dbMeta.getColumnsForTable(fromTableSelect.getItem(fromTableSelect.getSelectionIndex()));

						a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						cb.setItems(a1);

					}

					editor = new TableEditor(getFieldTbl());
					Combo cb2 = new Combo(getFieldTbl(), SWT.NONE);
					editor.grabHorizontal = true;
					editor.setEditor(cb2, tableItem, 1);
					tableItem.setData("cb2", cb2);

					if (toTableSelect.getSelectionIndex() >= 0) {

						l1 = dbMeta2.getColumnsForTable(toTableSelect.getItem(toTableSelect.getSelectionIndex()));

						a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						cb2.setItems(a1);

					}
					
					editor = new TableEditor(getFieldTbl());
					Button btnRmField = new Button(getFieldTbl(), SWT.NONE);
					btnRmField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					btnRmField.setText("Remove");

					btnRmField.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {

							// Identify the selected row

							//System.out.println("Row Index:" + e.widget.getData());

							int idx = (int) e.widget.getData();
							
							Combo cb = (Combo) getFieldTbl().getItem(idx).getData("cb");
							Combo cb2 = (Combo) getFieldTbl().getItem(idx).getData("cb2");
							Button btn = (Button) getFieldTbl().getItem(idx).getData("btn");

							cb.dispose();
							cb2.dispose();
							btn.dispose();
							
							getFieldTbl().remove(idx);
							
							// resync btn indexes with row indexes
							for(int i = idx; i < getFieldTbl().getItemCount(); i++) {
								TableItem item = getFieldTbl().getItem(i);
								//System.out.println("setting data of item "+ i + " to "+ i);								
								((Button)item.getData("btn")).setData(i);
							}
							
							sc.setMinHeight(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
							((GridData)sc.getLayoutData()).heightHint = gui.getFieldsGuiList().get(0).computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

							shell.layout(true, true);
							shell.redraw();

						}
					});

					editor.grabHorizontal = true;
					editor.grabVertical = true;
					editor.setEditor(btnRmField, tableItem, 2);
					btnRmField.setData(itemIdx);
					//System.out.println("SET Row Index:" + itemIdx);
					tableItem.setData("btn", btnRmField);

					getFieldTbl().setSize(getFieldTbl().getSize().x,
							getFieldTbl().getSize().y + tableItem.getBounds().height + getFieldTbl().getBorderWidth());

					sc.setMinHeight(parent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
					((GridData)sc.getLayoutData()).heightHint = gui.getFieldsGuiList().get(0).computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

					
					shell.layout(true, true);					
					shell.redraw();

					

				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		});

		btnAddField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnAddField.setText("Add Field");

		setFieldTbl(new Table(this, SWT.BORDER | SWT.V_SCROLL));
		getFieldTbl().setHeaderVisible(true);
		getFieldTbl().setLinesVisible(true);

		fieldTblCol1 = new TableColumn(getFieldTbl(), SWT.NONE);
		fieldTblCol1.setText("Source Field");

		fieldTblCol2 = new TableColumn(getFieldTbl(), SWT.NONE);
		fieldTblCol2.setText("Destination Field");

		fieldTblCol3 = new TableColumn(getFieldTbl(), SWT.NONE);
		fieldTblCol3.setText("");

		fromTableSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TableItem[] items = getFieldTbl().getItems();
				DBMetadata dbMeta = dis.getGui().getD1();

				try {


					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb");

						List<String> l1 = dbMeta
								.getColumnsForTable(fromTableSelect.getItem(fromTableSelect.getSelectionIndex()));

						String[] a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						c.setItems(a1);
					}

				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}
		});

		toTableSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {

				TableItem[] items = getFieldTbl().getItems();
				DBMetadata dbMeta = dis.getGui().getD2();

				try {

					for (TableItem tItem : items) {

						Combo c = (Combo) tItem.getData("cb2");

						List<String> l1 = dbMeta
								.getColumnsForTable(toTableSelect.getItem(toTableSelect.getSelectionIndex()));

						String[] a1 = new String[l1.size()];
						a1 = l1.toArray(a1);

						c.setItems(a1);
					}

				} catch (SQLException e1) {
					e1.printStackTrace();
				}

			}
		});

		List<String> l1;
		List<String> l2;
		try {
			l1 = getGui().getD1() != null ? getGui().getD1().getTables() : new ArrayList<String>();
			String[] a1 = new String[l1.size()];
			a1 = l1.toArray(a1);

			fromTableSelect.setItems(a1);

			l2 = getGui().getD2() != null ? getGui().getD2().getTables() : new ArrayList<String>();
			String[] a2 = new String[l2.size()];
			a2 = l2.toArray(a2);

			toTableSelect.setItems(a2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		fieldsGuiList.add(this);
		fieldsGuiListIndex = fieldsGuiList.size() - 1;
		//System.out.println(fieldsGuiListIndex);
	}

	public void size(Rectangle shellSize) {

		
		int width = shellSize.width;
		int height = shellSize.height;

		// System.out.println(width);
		
		//Point comp = tableTbl.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		tableTbl.setSize(width, height);
		tableTblCol1.setWidth(width / 2);
		tableTblCol2.setWidth(width - tableTblCol1.getWidth());

		getFieldTbl().setSize(width, height);

		fieldTblCol3.setWidth(80);
		fieldTblCol1.setWidth((width - 80) / 2);
		fieldTblCol2.setWidth(width - fieldTblCol1.getWidth() - fieldTblCol3.getWidth());

	}

	public void setFromConfig(PropertiesConfiguration p, DBMetadata d1, DBMetadata d2, String tableFrom, String tableTo)
			throws SQLException {

		List<String> l1 = d1 == null ? new ArrayList<String>() : d1.getTables();

		String[] a1 = new String[l1.size()];
		a1 = l1.toArray(a1);

		fromTableSelect.setItems(a1);

		fromTableSelect.select(l1.indexOf(tableFrom));

		l1 = d2 == null ? new ArrayList<String>() : d2.getTables();

		a1 = new String[l1.size()];
		a1 = l1.toArray(a1);
		toTableSelect.setItems(a1);

		toTableSelect.select(l1.indexOf(tableTo));

		l1 = d1 == null ? new ArrayList<String>() : d1.getColumnsForTable(tableFrom);
		List<String> l2 = d2 == null ? new ArrayList<String>() : d2.getColumnsForTable(tableTo);

		int row = 0;

		for (Iterator<String> iterator = p.getKeys(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String value = p.getString(key);

			TableItem tableItem = new TableItem(getFieldTbl(), SWT.NONE);

			TableEditor editor = new TableEditor(getFieldTbl());
			Combo combo_2 = new Combo(getFieldTbl(), SWT.NONE);
			a1 = new String[l1.size()];
			a1 = l1.toArray(a1);

			combo_2.setItems(a1);

			combo_2.select(l1.indexOf(value));

			editor.grabHorizontal = true;
			editor.setEditor(combo_2, tableItem, 0);
			tableItem.setData("cb", combo_2);

			editor = new TableEditor(getFieldTbl());
			Combo combo_3 = new Combo(getFieldTbl(), SWT.NONE);

			a1 = new String[l2.size()];
			a1 = l2.toArray(a1);

			combo_3.setItems(a1);

			combo_3.select(l2.indexOf(key));

			editor.grabHorizontal = true;
			editor.setEditor(combo_3, tableItem, 1);
			tableItem.setData("cb2", combo_3);

			editor = new TableEditor(getFieldTbl());

			Button btnRmField = new Button(getFieldTbl(), SWT.NONE);
			btnRmField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			btnRmField.setText("Remove");

			btnRmField.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {

					// Identify the selected row

					//System.out.println("Row Index:"+e.widget.getData());

					int idx = (int) e.widget.getData();

					Combo cb = (Combo) getFieldTbl().getItem(idx).getData("cb");
					Combo cb2 = (Combo) getFieldTbl().getItem(idx).getData("cb2");
					Button btn = (Button) getFieldTbl().getItem(idx).getData("btn");

					cb.dispose();
					cb2.dispose();
					btn.dispose();

					getFieldTbl().remove(idx);
					
					// resync btn indexes with row indexes
					for(int i = idx; i < getFieldTbl().getItemCount(); i++) {
						TableItem item = getFieldTbl().getItem(i);
						//System.out.println("setting data of item "+ i + " to "+ i);								
						((Button)item.getData("btn")).setData(i);
					}
					
					
					
					shell.layout(true, true);
					shell.redraw();

				}
			});

			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.setEditor(btnRmField, tableItem, 2);
			btnRmField.setData(row++);
			// System.out.println(combo_2.getItemHeight());
			// btnRmField.setSize(fieldTblCol3.getWidth(), combo_2.getItemHeight());
			tableItem.setData("btn", btnRmField);


		}

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}


	public Gui getGui() {
		return gui;
	}

	public void setGui(Gui gui) {
		this.gui = gui;
	}

	public Combo getFromTableSelect() {
		return fromTableSelect;
	}

	public void setFromTableSelect(Combo fromTableSelect) {
		this.fromTableSelect = fromTableSelect;
	}

	public Combo getToTableSelect() {
		return toTableSelect;
	}

	public void setToTableSelect(Combo toTableSelect) {
		this.toTableSelect = toTableSelect;
	}

	public Table getFieldTbl() {
		return fieldTbl;
	}

	public void setFieldTbl(Table fieldTbl) {
		this.fieldTbl = fieldTbl;
	}

}