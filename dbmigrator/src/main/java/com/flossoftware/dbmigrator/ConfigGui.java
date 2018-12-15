package com.flossoftware.dbmigrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

public class ConfigGui extends Composite {
	final public Text textDB;
	final public Text textIP;
	final public Text textPort;
	final public Text textUser;
	final public Text textPw;
	final public Text textConn;
	final public Text textConn2;
	final public Combo comboDriver;
	final public Combo comboOverride;
	final public Button btnNuID;
	final public boolean isToConfig;
	final public AbstractFileConfiguration configProp;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @throws ConfigurationException
	 */
	public ConfigGui(Composite parent, int style, String pPath,
			final boolean aIsToConfig) throws ConfigurationException {
		super(parent, style);
		
		Monitor mon = Display.getDefault().getMonitors()[0];

		isToConfig = aIsToConfig;

		configProp = new PropertiesConfiguration(pPath);

		setLayout(new GridLayout(3, false));

		Label lblFile = new Label(this, SWT.NONE);
		GridData g = new GridData(SWT.RIGHT, SWT.CENTER, true, false,
				1, 1);
		lblFile.setLayoutData(g);
		lblFile.setText("File");

		Label lblPath = new Label(this, SWT.NONE);
		GridData g2 = new GridData(SWT.RIGHT, SWT.CENTER, true, false,
				2, 1);
		
		g2.widthHint = mon.getBounds().width / 2 - 200;
		lblPath.setLayoutData(g2);
		lblPath.setText(pPath);

		Label lblDriver = new Label(this, SWT.NONE);
		lblDriver.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblDriver.setText("Driver");

		String[] s = new String[] { "com.mysql.jdbc.Driver",
				"net.sourceforge.jtds.jdbc.Driver",
				"oracle.jdbc.driver.OracleDriver" };
		ArrayList<String> s1 = new ArrayList<String>(Arrays.asList(s));

		comboDriver = new Combo(this, SWT.NONE);
		comboDriver.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setConnString();
			}
		});
		comboDriver.setItems(s);
		comboDriver.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));

		Label lblDB = new Label(this, SWT.NONE);
		lblDB.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblDB.setText("Database");

		textDB = new Text(this, SWT.BORDER);
		textDB.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});
		textDB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));

		Label lblIp = new Label(this, SWT.NONE);
		lblIp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblIp.setText("IP");

		textIP = new Text(this, SWT.BORDER);
		textIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));
		textIP.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});

		Label lblPort = new Label(this, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblPort.setText("Port");

		textPort = new Text(this, SWT.BORDER);
		textPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		
		textPort.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});

		Label lblUser = new Label(this, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblUser.setText("User");

		textUser = new Text(this, SWT.BORDER);
		textUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));

		Label lblPw = new Label(this, SWT.NONE);
		lblPw.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblPw.setText("Pw");

		textPw = new Text(this, SWT.BORDER);
		textPw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2,
				1));
		Label lblConn = new Label(this, SWT.NONE);
		lblConn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblConn.setText("Connection");

		textConn2 = new Text(this, SWT.BORDER);
		textConn2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		textConn2.setEnabled(false);

		textConn = new Text(this, SWT.BORDER);
		textConn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Label lblOverride = new Label(this, SWT.NONE);
		lblOverride.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		lblOverride.setText("Destination Platform");

		comboOverride = new Combo(this, SWT.NONE);

		String[] s2 = new String[] { "", "sugar" };
		ArrayList<String> s3 = new ArrayList<String>(Arrays.asList(s2));
		comboOverride.setItems(s2);
		comboOverride.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		if (!isToConfig) {
			lblOverride.setVisible(false);
			comboOverride.setVisible(false);
		}
			

		Label lblNuID = new Label(this, SWT.NONE);
		lblNuID.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		lblNuID.setText("New ID");
		btnNuID = new Button(this, SWT.CHECK);
		if (!isToConfig) {
			lblNuID.setVisible(false);
			btnNuID.setVisible(false);
		}
			

		comboDriver.select(s1.indexOf(configProp.getString("driver")));
		if (isToConfig)
			comboOverride.select(s3.indexOf(configProp
					.getString("server_type_override")));
		textDB.setText(configProp.getString("database"));
		textIP.setText(configProp.getString("ip"));
		textPw.setText(configProp.getString("password"));
		textPort.setText(configProp.getString("port"));
		textUser.setText(configProp.getString("user"));
		textConn2.setText(configProp.getString("connection-pre"));
		textConn.setText(configProp.getString("connection-post"));
		new Label(this, SWT.NONE);
		if (isToConfig)
			btnNuID.setSelection(configProp.getBoolean("newId"));

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public HashMap<String, String> getPropHashMap() {
		HashMap<String, String> h = new HashMap<String, String>();

		if (comboDriver.getSelectionIndex() > -1)
			h.put("driver",
					comboDriver.getItem(comboDriver.getSelectionIndex()));
		if (isToConfig && comboOverride.getSelectionIndex() > -1)
			h.put("server_type_override",
					comboOverride.getItem(comboOverride.getSelectionIndex()));
		h.put("database", textDB.getText());
		h.put("ip", textIP.getText());
		h.put("password", textPw.getText());
		h.put("port", textPort.getText());
		h.put("user", textUser.getText());
		h.put("connection", textConn2.getText()+textConn.getText());
		if (isToConfig)
			h.put("newId", "" + btnNuID.getSelection());

		return h;
	}

	public void dbConnect(boolean refreshOnlyLastCompo) {

		try {
			DBMetadata dbMeta = new DBMetadata(getPropHashMap());

			List<String> l1 = dbMeta.getTables();

			String[] a1 = new String[l1.size()];
			a1 = l1.toArray(a1);
			
			if(refreshOnlyLastCompo) {
				FieldsGui campiGui = Gui.fieldsGuiList.get(Gui.fieldsGuiList.size()-1);
				if (!isToConfig)
					campiGui.fromTableSelect.setItems(a1);
				else
					campiGui.toTableSelect.setItems(a1);
			} else 

				for (FieldsGui campiGui : Gui.fieldsGuiList) {
					if (!isToConfig)
						campiGui.fromTableSelect.setItems(a1);
					else
						campiGui.toTableSelect.setItems(a1);
				}

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	public void saveFile() {

		if (comboDriver.getSelectionIndex() > -1)
			configProp.setProperty("driver",
					comboDriver.getItem(comboDriver.getSelectionIndex()));
		if (isToConfig && comboOverride.getSelectionIndex() > -1)
			configProp.setProperty("server_type_override",
					comboOverride.getItem(comboOverride.getSelectionIndex()));
		configProp.setProperty("database", textDB.getText());
		configProp.setProperty("ip", textIP.getText());
		configProp.setProperty("password", textPw.getText());
		configProp.setProperty("port", textPort.getText());
		configProp.setProperty("user", textUser.getText());
		configProp.setProperty("connection-pre", textConn2.getText());
		configProp.setProperty("connection-post", textConn.getText());
		if (isToConfig)
			configProp.setProperty("newId", btnNuID.getSelection());
		try {
			configProp.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	public void setConnString() {
		
		String s = comboDriver.getItem(comboDriver.getSelectionIndex());
		if (s.equals("com.mysql.jdbc.Driver"))
			textConn2.setText("jdbc:mysql://" + textIP.getText() + ":"
					+ textPort.getText() + "/" + textDB.getText());
		else if (s.equals("net.sourceforge.jtds.jdbc.Driver"))
			textConn2.setText("jdbc:jtds:sqlserver://"
					+ textIP.getText() + ":" + textPort.getText() + "/"
					+ textDB.getText() + ";instance=SQLEXPRESS");
		else
			textConn2.setText("jdbc:oracle:thin:@//" + textIP.getText()
					+ ":" + textPort.getText() + "/XE");
	}

}
