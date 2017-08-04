package codeu.chat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AccessLevel {

	public static final Serializer<AccessLevel> SERIALIZER = new Serializer<AccessLevel>() {

	    @Override
	    public void write(OutputStream out, AccessLevel value) throws IOException {
	    	byte[] statuses = new byte[1];
	    	statuses[0] = value.getStatus();
	    	Serializers.BYTES.write(out, statuses);
	    }

	    @Override
	    public AccessLevel read(InputStream in) throws IOException {
	    	byte[] statuses = Serializers.BYTES.read(in);
	    	return new AccessLevel(statuses[0]);
	    }
  };

	private byte access;
		//0b00000com
		//creators have com
		//owners have om
		//members have m

	// ======== CONSTRUCTORS ========

	public String toString() {
		String byteStr = "00000000";
		if (hasCreatorAccess()) {
			byteStr = byteStr.substring(0,5) + "1" + byteStr.substring(6,8);
		}
		if (hasOwnerAccess()) {
			byteStr = byteStr.substring(0,6) + "1" + byteStr.substring(7,8);
		}
		if (hasMemberAccess()) {
			byteStr = byteStr.substring(0,7) + "1" + byteStr.substring(8,8);
		}
		return byteStr;
	}

	public AccessLevel() {
		access = (byte)0b00000000;
	}

	public AccessLevel(byte status) {
		setStatus(status);
	}

	// ======== SETTERS ========
	public void setStatus(byte status) {
		access = status;
	}

	public void setCreatorStatus() {
		setStatus((byte)0b00000111);
	}

	// sets owner access
	public void setOwnerStatus() {
		setStatus((byte)0b00000011);
	}

	public void setMemberStatus() {
		setStatus((byte)0b00000001);
	}

	// ======== GETTERS ========
	public byte getStatus() {
		return access;
	}

	public boolean hasCreatorAccess() {
		return ((access&(byte)0b00000100) > 0);
	}

	public boolean hasOwnerAccess() {
		return ((access&(byte)0b00000010) > 0);
	}

	public boolean hasMemberAccess() {
		return ((access&(byte)0b00000001) > 0);
	}
}