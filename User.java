import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class User implements Runnable{
	private int id;
	private List<Key> steamKeys;
	private String name;
	private List<User> friends;
	private BlockingQueue<Response> responseQueue;
	private BlockingQueue<Request> requestQueue;
	
	public User(int id, String name, BlockingQueue<Response> responseQueue, BlockingQueue<Request> requestQueue){
		this.id = id;
		this.name = name;
		this.steamKeys = new ArrayList<>();
		this.friends = new ArrayList<>();
		this.responseQueue = responseQueue;
		this.requestQueue = requestQueue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<User> getFriends() {
		return friends;
	}

	public void addFriend(User friend) {
		this.friends.add(friend);
	}

	public List<Key> getSteamKeys() {
		return steamKeys;
	}

	public void addSteamKey(Key steamKey) {
		this.steamKeys.add(steamKey);
	}

	public BlockingQueue<Response> getResponseQueue() {
		return responseQueue;
	}

	public void setResponseQueue(BlockingQueue<Response> responseQueue) {
		this.responseQueue = responseQueue;
	}
	public BlockingQueue<Request> getRequestQueue() {
		return requestQueue;
	}

	public void setRequestQueue(BlockingQueue<Request> requestQueue) {
		this.requestQueue = requestQueue;
	}
	
	public int getId(){
		return id;
	}
	public void setId(int id){
		this.id = id;
	}
	@Override
	public void run() {
		try {
			while(true){
				Thread.sleep(5000);
				//request
				Scanner reader = new Scanner(System.in);  // Reading from System.in
				System.out.println(id + "Enter action: ");
				String action = reader.next();
				String friend = "";
				String keyName = "";
				int shareTime = 0;
				if(action.equals("SHARE")){
					if(!friends.isEmpty()){
						System.out.println(id + "Enter friend: ");
						friend = reader.next();
						for(User user: friends){
							if(user.getName().equals(friend)){
								if(!steamKeys.isEmpty()){
									System.out.println(id + "Enter steam key: ");
									keyName = reader.next();
									for(Key key: steamKeys){
										if(key.getName().equals(keyName)){
											System.out.println(id + "Enter an amount of time to share: ");
											shareTime = reader.nextInt();
											Request request = new Request("SHARE", 
													this, 
													user, 
													keyName,
													shareTime
													);
											requestQueue.put(request);
											break;
										}
									}
									break;
								}
							}
						}
						
					}
				}else{
					Request request = new Request("WAIT");
					requestQueue.put(request);
				}
				reader.close();
				
				//response
				Response response = responseQueue.take();
				if(response.getUser() == null){
					responseQueue.put(response);
				}else if(response.getUser().getName().equals(this.getName())){
					if(response.getType().equals("SUCCESS")){
						for(Key key: steamKeys){
							if(key.getName().equals(keyName)){
								for(User fUser : friends){
									if(fUser.getName().equals(friend)){
										key.setUser(friend);
										key.setTimeout(response.getBorrowedTime());
										key.setBorrowed(true);
										//open socket and send key
										sendKey(key, fUser.getId());
										break;
									}
								}
								break;
							}
						}
					}else if(response.getType().equals("FAILED")){
						System.out.println("Server failed to acknowledge.");
					}else if(response.getType().equals("RECEIVE")){
						receiveKey();
					}else if(response.getType().equals("TIMEOUT")){
						String name = response.getKeyName();
						int count = 0;
						for(Key key: steamKeys){
							if(key.getName().equals(name)){
								steamKeys.remove(count);
								break;
							}
							count++;
						}
					}else if(response.getType().equals("RETURN")){
						String name = response.getKeyName();
						for(Key key: steamKeys){
							if(key.getName().equals(name)){
								key.setUser(key.getOwner());
								key.setBorrowed(false);
								key.setTimeout(null);
								break;
							}
						}
					}
				}else{
					responseQueue.put(response);
				}
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void sendKey(Key key, int friendId){
		Socket s = null;
		try{
			int serverPort = 7896 + friendId;
			//msg size in kilobytes
			String data = Util.toString(key);
			s = new Socket("localhost", serverPort);    
			DataOutputStream out = new DataOutputStream( s.getOutputStream());
			out.writeUTF(data);
		}catch (Exception e){
			//System.out.println("Sock:"+e.getMessage()); 
		}finally {
			if(s!=null){
				try {s.close();}
				catch (IOException e){}
				//System.out.println("close:"+e.getMessage());}
			}
		}
	}  
	
	public void receiveKey(){
		try{
			int serverPort = 7896 + id;
			//System.out.println(serverPort);
			ServerSocket listenSocket = new ServerSocket(serverPort);
			//Socket s = listenSocket.accept();  
			//DataInputStream in = new DataInputStream( s.getInputStream());
			while(true) {
		    	// UTF is a string encoding see Sn 4.3
				try{
					Socket s = listenSocket.accept();  
					DataInputStream in = new DataInputStream( s.getInputStream());
					String returned = in.readUTF();
					Key after = (Key)Util.fromString(returned);
					System.out.println(id + ": " + "Received: " + after.toString());
					steamKeys.add(after);
				}catch(EOFException e){
				}catch(IOException e){
					listenSocket.close();
					break;
				}
			}
		} catch(Exception e){
				e.printStackTrace();
		}
	}
	
}
