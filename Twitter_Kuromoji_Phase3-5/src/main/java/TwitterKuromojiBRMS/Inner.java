package TwitterKuromojiBRMS;

public class Inner {
	private long ID;
	private String 品詞;
	private String 内容;
	private long 位置;
	private boolean 取込み済み = false;
	
	public long getID() {
		return ID;
	}
	public void setID(long iD) {
		ID = iD;
	}
	public String get品詞() {
		return 品詞;
	}
	public void set品詞(String 品詞) {
		this.品詞 = 品詞;
	}
	public String get内容() {
		return 内容;
	}
	public void set内容(String 内容) {
		this.内容 = 内容;
	}
	public long get位置() {
		return 位置;
	}
	public void set位置(long 位置) {
		this.位置 = 位置;
	}
	public boolean is取込み済み() {
		return 取込み済み;
	}
	public void set取込み済み(boolean 取込み済み) {
		this.取込み済み = 取込み済み;
	}
	
	public Inner(long iD, String 品詞, String 内容, long 位置) {
		super();
		ID = iD;
		this.品詞 = 品詞;
		this.内容 = 内容;
		this.位置 = 位置;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (ID ^ (ID >>> 32));
		result = prime * result + (int) (位置 ^ (位置 >>> 32));
		result = prime * result + ((内容 == null) ? 0 : 内容.hashCode());
		result = prime * result + (取込み済み ? 1231 : 1237);
		result = prime * result + ((品詞 == null) ? 0 : 品詞.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Inner other = (Inner) obj;
		if (ID != other.ID)
			return false;
		if (位置 != other.位置)
			return false;
		if (内容 == null) {
			if (other.内容 != null)
				return false;
		} else if (!内容.equals(other.内容))
			return false;
		if (取込み済み != other.取込み済み)
			return false;
		if (品詞 == null) {
			if (other.品詞 != null)
				return false;
		} else if (!品詞.equals(other.品詞))
			return false;
		return true;
	}


	
}
