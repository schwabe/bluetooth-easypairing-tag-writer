package de.blinkt.btepoob;

import java.util.Vector;

import android.bluetooth.BluetoothClass;
import android.os.ParcelUuid;

public class BluetoothDeviceEntry {

	private ParcelUuid[] mUuids;
	private BluetoothClass mBluetoothClass;
	private String mAddress;
	private String mName;

	public BluetoothDeviceEntry(String name, String address,
			BluetoothClass bluetoothClass, ParcelUuid[] uuids) {
		mName = name;
		mAddress = address;
		mBluetoothClass = bluetoothClass;
		mUuids = uuids;
	}
	
	public byte[] getBtAddressBytes(){
		
		Vector<Integer> values= new Vector<Integer>();
		for(String octect:mAddress.split(":"))
			values.add(Integer.parseInt(octect, 16));
		
		byte[] btadd = new byte[6];
		for(int i=0;i<values.size();i++)
			btadd[i]=(byte)((int)values.get(i));
		
		return btadd;
	}
	
	@Override
	public String toString() {
		return mName;
	}

	public String getName() {
		return mName;
	}

	public ParcelUuid[] getUUIDs() {
		return mUuids;
	}

	public byte[] getBluetoothClassBytes() {
		
		int bclass = mBluetoothClass.getDeviceClass();
		
		return new byte[] {(byte) (bclass % 256), (byte) (bclass /256 %256), (byte) (bclass / 0xffff %256)}; 
	}
}
