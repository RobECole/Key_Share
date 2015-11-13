
public class Request {
	private String type;
	private User userOne;
	private User userTwo;
	private String keyName;
	private int shareTime;
	
	public Request(String type, User userOne, User userTwo, String keyName, int shareTime){
		this.type = type;
		this.userOne = userOne;
		this.userTwo = userTwo;
		this.keyName = keyName;
		this.shareTime = shareTime;
	}
	public Request(String type){
		this.type = type;
		this.userOne = null;
		this.userTwo = null;
		this.keyName = "";
		this.shareTime = 0;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public User getUserOne() {
		return userOne;
	}

	public void setUserOne(User userOne) {
		this.userOne = userOne;
	}

	public User getUserTwo() {
		return userTwo;
	}

	public void setUserTwo(User userTwo) {
		this.userTwo = userTwo;
	}

	public int getShareTime() {
		return shareTime;
	}

	public void setShareTime(int shareTime) {
		this.shareTime = shareTime;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
}
