package de.blinkt.btepoob;

import java.util.UUID;
import java.util.Vector;

import android.nfc.NdefRecord;
import android.os.ParcelUuid;

public class A2NDefData {
	static NdefRecord genTagData(BluetoothDeviceEntry btde){
		
		byte[] btmac = btde.getBtAddressBytes();
		String name= btde.getName();
		
		assert(btmac.length == 6);
		
		ParcelUuid[] uuids = btde.getUUIDs();
		

		byte[] type = "application/vnd.bluetooth.ep.oob".getBytes();
		//byte[] devclass = btde.getBluetoothClassBytes();
		byte[] devclass =null;

		byte[] eirdata = constructEIR(name, btmac, uuids, devclass);
		
		NdefRecord rec = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, type, new byte[0] , eirdata);
		return rec;
	}

	private static byte[] constructEIR(String namestr, byte[] btmac, ParcelUuid[] uuids, byte[] bclass) {
		
		byte[] name=namestr.getBytes();
		
		Vector<Byte> eir = new Vector<Byte>();
		
		// Placeholder for length
		eir.addElement((byte) 0x0);
		eir.addElement((byte) 0x0);
		
		//byte order for the bluetooth is reversed
		for (byte b:btmac)
			eir.add(2,b);
		
		// Extended Inquiry Response (EIR) Data Length:
		// Length inkl type field
		eir.add((byte) (name.length +1));
		//EIR Data Type: Complete Local Name
		eir.add((byte)0x09);
		// Name
		for(byte b:name)
			eir.add(b);
		
		// EIR rec lenght 4 byte
		eir.add((byte) 4);
		// EIR Data Type: Class of Device
		eir.add((byte) 0x0D);

		
		// TODO: Get this from the Bluetooth Class
		// Class of Device:
		// 0x20: Service Class = Audio
		// 0x04: Major Device Class = Audio and Video 
		// 0x14: Minor Device Class = Loudspeaker
		
		for (byte b:new byte[] {0x14,0x04,0x24})
			eir.add(b);
		
		// Sony: 0x14 0x04 0x24 
		
		// Length 2 byte for each uuid + 1 for record type
		eir.add((byte)(uuids.length*2 +1));
		// EIR Data Type: 16 bit Service Class UUID list (complete)
		eir.add((byte) 0x03);
		
		//16 bit Service Class UUID list (complete): 0x110D = A2D2P
		//0x110B = A2DP (Audio Sink)
		
		for (ParcelUuid p: uuids) {
			UUID u = p.getUuid();
			String foo = u.toString();
			System.out.println(foo);
			
			// Evil, evil, evil
			long upperbits = u.getMostSignificantBits();
			
			long highbyte = (upperbits >> (5*8)) & 0xff;
			long lowbyte = (upperbits >> (4*8)) & 0xff;
			
			eir.add((byte) lowbyte);
			eir.add((byte) highbyte);
			
					
			
		}
		
		// Sony: 0x1108, 0x110B, 0x110c, 0x110d, 0x110e, 0x111e, 0x1113, 0x1200
		
		
		
		byte[] bytes = new byte[eir.size()];
		
		// Length
		bytes[0] = (byte) (eir.size() % 256);
		bytes[1] = (byte) (eir.size() / 256);
		
		for(int i=2;i<bytes.length;i++) {
			bytes[i] = eir.get(i);
		}
		return bytes;	
	}
}
