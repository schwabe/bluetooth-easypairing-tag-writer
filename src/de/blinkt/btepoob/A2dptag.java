package de.blinkt.btepoob;

import java.util.Set;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class A2dptag extends Activity implements OnClickListener, OnItemSelectedListener {

	private Spinner mBtDeviceSpinner;
	private ArrayAdapter<BluetoothDeviceEntry> mArrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mBtDeviceSpinner = (Spinner) findViewById(R.id.btdevice);
		mBtDeviceSpinner.setOnItemSelectedListener(this);


		findViewById(R.id.write_button).setOnClickListener(this);

		getBtDevList();
		addTagWriterLink();
	}

	private void getBtDevList() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
		}
		Set<BluetoothDevice> bondeddevices = mBluetoothAdapter.getBondedDevices();

		mArrayAdapter = new ArrayAdapter<BluetoothDeviceEntry>(this, android.R.layout.simple_spinner_item);
		for(BluetoothDevice btdev:bondeddevices){

			BluetoothDeviceEntry btentry = new BluetoothDeviceEntry(
					btdev.getName(), 
					btdev.getAddress(),
					btdev.getBluetoothClass(),
					btdev.getUuids()
					);

			mArrayAdapter.add(btentry);
		}

		mBtDeviceSpinner.setAdapter(mArrayAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void addTagWriterLink()
	{
		TextView installtagwriter = (TextView) findViewById(R.id.installTagWriter);
		if(!checkForTagwriter()) {

			String installtext = "This app uses <a href=\"market://details?id=com.nxp.nfc.tagwriter\">NXP Tagwriter</a> for writing the tag. You need to install Tagwriter to write tags.";
			Spanned htmltext = Html.fromHtml(installtext);
			installtagwriter.setText(htmltext);
			installtagwriter.setMovementMethod(LinkMovementMethod.getInstance());
			installtagwriter.setVisibility(View.VISIBLE);

			findViewById(R.id.write_button).setEnabled(false);
		} else {
			installtagwriter.setVisibility(View.GONE);	
			findViewById(R.id.write_button).setEnabled(true);
		}

	}

	private boolean checkForTagwriter() {
		PackageManager pm = getPackageManager();
		try {
			pm.getPackageInfo("com.nxp.nfc.tagwriter",0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}

	private void shareTag() {


		Intent shareIntent = new Intent("com.nxp.nfc.tagwriter.WRITE_NDEF");
		shareIntent.putExtra("android.nfc.extra.NDEF_MESSAGES", new NdefMessage[] {getTagData()});
		startActivity(Intent.createChooser(shareIntent, "Export Tag"));
	}

	@Override
	public void onClick(View v) {
		writeHexData();

		if(v.getId()==R.id.write_button)
			shareTag();
	}

	private void writeHexData() {
		byte[] data = getTagData().toByteArray();

		String res="";

		int i=0;
		String asc = "";
		for(byte b:data) {
			res+=String.format("%02x", b);
			if(b >= 0x20 &&  b < 0x7f )
				asc+= new String(new byte[] {b});
			else
				asc+= '.';
			
			i++;
			if(i%2==0)
				res+=" ";
			if(i%8==0) {
				res+= " " +  asc + "\n";
				asc="";
			}
		}
		if(!asc.equals("")) {
			while(i%8!=0) {
				i++;
				if(i%2 ==0)
					res+="   ";
				else
					res+="  ";
			}
			res +=" " + asc + "\n";
					
		}
			
		
		

		((TextView)findViewById(R.id.status)).setText(res);
	}

	private NdefMessage getTagData() {
		NdefRecord foo = A2NDefData.genTagData((BluetoothDeviceEntry)mBtDeviceSpinner.getSelectedItem());

		NdefMessage msg = new NdefMessage(new NdefRecord[] {foo});

		return msg;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		writeHexData();

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
}
