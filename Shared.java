import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Shared {
	private Map<String, Date> timeouts;
	
	public Shared(){
		this.timeouts = new HashMap<>();
	}

	public Map<String, Date> getTimeouts() {
		return timeouts;
	}

	public void setTimeouts(Map<String, Date> timeouts) {
		this.timeouts = timeouts;
	}
	
	public void addTimeout(String keyName, Date borrowedTime) {
		this.timeouts.put(keyName, borrowedTime);
	}
	public String checkForTimeout(){
		for (Map.Entry<String, Date> entry : timeouts.entrySet())
		{
		    Date date = entry.getValue();
		    Date currDate = new Date();
		    if(date.compareTo(currDate) <= 0){
		    	return entry.getKey() + "";
		    }
		}
		return "";
	}
}
