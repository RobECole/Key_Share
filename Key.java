import java.io.Serializable;
import java.util.Date;

public class Key implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String owner;
	private String user;
	private Date purchased;
	private Date timeout;
	private boolean borrowed;
	
	public Key(int id, String name, String owner, Date purchased){
		this.id = id;
		this.name = name;
		this.owner = owner;
		this.user = owner;
		this.purchased = purchased;
		this.timeout = null;
		this.borrowed = false;
	}
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Date getPurchased() {
		return purchased;
	}
	public void setPurchased(Date purchased) {
		this.purchased = purchased;
	}
	public Date getTimeout() {
		return timeout;
	}
	public void setTimeout(Date timeout) {
		this.timeout = timeout;
	}
	public boolean isBorrowed() {
		return borrowed;
	}
	public void setBorrowed(boolean borrowed) {
		this.borrowed = borrowed;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return id + " : " + 
				name + " : " + 
				owner + " : " + 
				user + " : " + 
				purchased + " : " + 
				timeout + " : " + 
				borrowed;
		
	}

}
