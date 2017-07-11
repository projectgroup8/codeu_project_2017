package codeu.chat.util;

public class AccessLevel {

	private byte access;
		//0b00000com
		//creators have com
		//owners have om
		//members have m

	// ======== CONSTRUCTORS ========
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