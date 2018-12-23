package com.flossoftware.dbmigrator;

import java.sql.SQLException;
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
	public Text textDB;
	public Text textIP;
	public Text textPort;
	public Text textUser;
	public Text textPw;
	public Text textConn;
	public Text textConn2;
	public Combo comboDriver;
	public Button btnNuID;
	public boolean isToConfig;
	public AbstractFileConfiguration configProp;
	private Gui gui;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 * @throws ConfigurationException
	 */
	public ConfigGui(Composite parent, int style, String pPath, boolean aIsToConfig, Gui gui)
			throws ConfigurationException {
		super(parent, style);

		this.gui = gui;

//		Monitor mon = Display.getDefault().getMonitors()[0];

		isToConfig = aIsToConfig;

		configProp = new PropertiesConfiguration(pPath);

		setLayout(new GridLayout(3, false));

		Label lblFile = new Label(this, SWT.NONE);
		GridData g = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		lblFile.setLayoutData(g);
		lblFile.setText("File");

		Label lblPath = new Label(this, SWT.NONE);
		GridData g2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);

		//g2.widthHint = mon.getBounds().width / 2 - 200;
		lblPath.setLayoutData(g2);
		lblPath.setText(pPath);

		Label lblDriver = new Label(this, SWT.NONE);
		lblDriver.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDriver.setText("Driver");

		String[] s = new String[] { "com.mysql.jdbc.Driver", "net.sourceforge.jtds.jdbc.Driver",
				"oracle.jdbc.driver.OracleDriver" };
		ArrayList<String> s1 = new ArrayList<String>(Arrays.asList(s));

		comboDriver = new Combo(this, SWT.READ_ONLY);
		comboDriver.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setConnString();
			}
		});
		comboDriver.setItems(s);
		comboDriver.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblDB = new Label(this, SWT.NONE);
		lblDB.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDB.setText("Database");

		textDB = new Text(this, SWT.BORDER);
		textDB.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});
		textDB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblIp = new Label(this, SWT.NONE);
		lblIp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIp.setText("IP");

		textIP = new Text(this, SWT.BORDER);
		textIP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textIP.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});

		Label lblPort = new Label(this, SWT.NONE);
		lblPort.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPort.setText("Port");

		textPort = new Text(this, SWT.BORDER);
		textPort.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		textPort.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setConnString();
			}
		});

		Label lblUser = new Label(this, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUser.setText("User");

		textUser = new Text(this, SWT.BORDER);
		textUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label lblPw = new Label(this, SWT.NONE);
		lblPw.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPw.setText("Pw");

		textPw = new Text(this, SWT.PASSWORD | SWT.BORDER);
		textPw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Label lblConn = new Label(this, SWT.NONE);
		lblConn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConn.setText("Connection");

		textConn2 = new Text(this, SWT.BORDER);
		textConn2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textConn2.setEnabled(false);

		textConn = new Text(this, SWT.BORDER);
		textConn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblNuID = new Label(this, SWT.NONE);
		lblNuID.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNuID.setText("New ID");
		btnNuID = new Button(this, SWT.CHECK);
		if (!isToConfig) {
			lblNuID.setVisible(false);
			btnNuID.setVisible(false);
		}

		comboDriver.select(s1.indexOf(configProp.getString("driver")));

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
			h.put("driver", comboDriver.getItem(comboDriver.getSelectionIndex()));
		h.put("database", textDB.getText());
		h.put("ip", textIP.getText());
		h.put("password", textPw.getText());
		h.put("port", textPort.getText());
		h.put("user", textUser.getText());
		h.put("connection", textConn2.getText() + textConn.getText());
		if (isToConfig)
			h.put("newId", "" + btnNuID.getSelection());

		return h;
	}

	public DBMetadata dbConnect(boolean refreshOnlyLastCompo) throws ClassNotFoundException, SQLException {
		DBMetadata dbMeta = null;
	
		dbMeta = new DBMetadata(getPropHashMap());

		List<String> l1 = dbMeta.getTables();

		String[] a1 = new String[l1.size()];
		a1 = l1.toArray(a1);

		if (refreshOnlyLastCompo) {
			FieldsGui campiGui = gui.fieldsGuiList.get(gui.fieldsGuiList.size() - 1);
			if (!isToConfig)
				campiGui.fromTableSelect.setItems(a1);
			else
				campiGui.toTableSelect.setItems(a1);
		} else

			for (FieldsGui campiGui : gui.fieldsGuiList) {
				if (!isToConfig)
					campiGui.fromTableSelect.setItems(a1);
				else
					campiGui.toTableSelect.setItems(a1);
			}

		
		return dbMeta;
	}

	public void saveFile() {

		if (comboDriver.getSelectionIndex() > -1)
			configProp.setProperty("driver", comboDriver.getItem(comboDriver.getSelectionIndex()));
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
			textConn2.setText("jdbc:mysql://" + textIP.getText() + ":" + textPort.getText() + "/" + textDB.getText());
		else if (s.equals("net.sourceforge.jtds.jdbc.Driver"))
			textConn2.setText(
					"jdbc:jtds:sqlserver://" + textIP.getText() + ":" + textPort.getText() + "/" + textDB.getText()
			// + ";instance=SQLEXPRESS"
			);
		else
			textConn2.setText("jdbc:oracle:thin:@//" + textIP.getText() + ":" + textPort.getText() + "/XE");
	}

}
