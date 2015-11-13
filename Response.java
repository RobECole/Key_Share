import java.util.Date;

public class Response {
	private User user;
	private String type;
	private Date borrowedTime;
	private String keyName;
	
	public Response(User user, String type){
		this.user = user;
		this.type = type;
		this.borrowedTime = null;
		this.keyName = "";
	}
	
	public Response(User user, String type, Date borrowedTime){
		this.user = user;
		this.type = type;
		this.borrowedTime = borrowedTime;
		this.keyName = "";
	}
	
	public Response(User user, String type, String keyName){
		this.user = user;
		this.type = type;
		this.borrowedTime = null;
		this.keyName = keyName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getBorrowedTime() {
		return borrowedTime;
	}

	public void setBorrowedTime(Date borrowedTime) {
		this.borrowedTime = borrowedTime;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
}
