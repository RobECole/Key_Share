import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable{
	private Map<User, Shared> timeouts;
	private BlockingQueue<Request> requestQueue;
	private BlockingQueue<Response> responseQueue;
	private List<User> users;
	
	public Server(BlockingQueue<Request> requestQueue, BlockingQueue<Response> responseQueue, List<User> users){
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
		timeouts = new HashMap<>();
		this.users = users;
	}

	@Override
	public void run() {
		try {
			while(true){
				Request r = requestQueue.poll(10, TimeUnit.SECONDS);
				Response response;
				String rType = "";
				if(r == null){
					rType = "";
				}else{
					rType = r.getType();
				}
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
					    cal.add(Calendar.MINUTE, shareTime); // adds one hour
					    Date borrowTime = cal.getTime(); // returns new date object, one hour in the future

				    	response = new Response(userOne, "SUCCESS", borrowTime);
				    	System.out.println("Server: SUCCESS");
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
						System.out.println("Server: FAILED");
						responseQueue.put(response);
					}
				}else{
					//check timeouts
					System.out.println("Server: Checking Timeouts");
					for (Map.Entry<User, Shared> entry : timeouts.entrySet())
					{
						Shared shared = entry.getValue();
						String key = shared.checkForTimeout();
						System.out.println("Key: " + key);
						if(!key.equals("")){
							String split[] = key.split(":");
							System.out.println("SPLIT 0: " + split[0]);
							System.out.println("SPLIT 1: " + split[1]);
							System.out.println("Server: TIMEOUT KEY " + entry.getKey().getName());
							Response responseT = new Response(entry.getKey(), "TIMEOUT", split[0]);
							responseQueue.put(responseT);
							
							for(User userOne : users){
								if(key.contains(userOne.getName())){
									System.out.println("Server: RETURNING KEY " + userOne.getName());
									Response responseR = new Response(userOne, "RETURN", split[0]);
									responseQueue.put(responseR);
									break;
								}
							}
							timeouts.remove(entry.getKey());
						}
					}
					//send nothing back
					//for(int i = 0; i < users.size(); i++){
						//response = new Response(null, "WAIT");
						//responseQueue.put(response);
					//}
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
