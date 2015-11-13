import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable{
	private Map<User, Shared> timeouts;
	private BlockingQueue<Request> requestQueue;
	private BlockingQueue<Response> responseQueue;
	private List<User> users;
	
	public Server(BlockingQueue<Request> requestQueue, BlockingQueue<Response> responseQueue, ArrayList<User> users){
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
		timeouts = new HashMap<>();
		this.users = users;
	}

	@Override
	public void run() {
		try {
			while(true){
				Request r = requestQueue.take();
				Response response;
				String rType = r.getType();
				boolean sharable = false;
				if(rType.equals("SHARE")){
					User userOne = r.getUserOne();
					User userTwo = r.getUserTwo();
					String keyName = r.getKeyName();
					for(Key key1 : userOne.getSteamKeys()){
						if(key1.getName().equals(keyName)){
							if(key1.getOwner().equals(key1.getUser())){
								sharable = true;
								break;
							}
						}
					}
					for(Key key2 : userTwo.getSteamKeys()){
						if(key2.getName().equals(keyName)){
							if(key2.getOwner().equals(key2.getUser())){
								sharable = false;
								break;
							}
						}
					}
					if(sharable == true){
						int shareTime = r.getShareTime();
						
						Calendar cal = Calendar.getInstance(); // creates calendar
					    cal.setTime(new Date()); // sets calendar time/date
					    cal.add(Calendar.HOUR_OF_DAY, shareTime); // adds one hour
					    Date borrowTime = cal.getTime(); // returns new date object, one hour in the future

				    	response = new Response(userOne, "SUCCESS", borrowTime);
				    	System.out.println("SUCCESS");
				    	responseQueue.put(response);
				    	
				    	if(timeouts.containsKey(userTwo.getName())){
				    		Shared shared = timeouts.get(userTwo.getName());
				    		shared.addTimeout(keyName+":"+userOne.getName(), borrowTime);
				    		timeouts.replace(userTwo, shared);
				    	}else{
				    		Shared shared = new Shared();
				    		shared.addTimeout(keyName+":"+userOne.getName(), borrowTime);
				    		timeouts.put(userTwo, shared);
				    	}
				    	response = new Response(userTwo, "RECEIVE");
				    	responseQueue.put(response);
				    	
					}else{
						response = new Response(userOne, "FAILED");
						System.out.println("FAILED");
						responseQueue.put(response);
					}
				}else{
					//check timeouts
					for (Map.Entry<User, Shared> entry : timeouts.entrySet())
					{
						Shared shared = entry.getValue();
						String key = shared.checkForTimeout();
						String[] split = key.split(":");
						response = new Response(entry.getKey(), "TIMEOUT", key);
						responseQueue.put(response);
						
						for(User userOne : users){
							if(userOne.getName().equals(split[0])){
								response = new Response(userOne, "RETURN:" + entry.getKey().getName(), split[1]);
								responseQueue.put(response);
							}
						}
					}
					//send nothing back
					response = new Response(null, "WAIT");
					responseQueue.put(response);
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
