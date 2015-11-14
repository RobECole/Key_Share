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
	private Scanner reader;
	private int serverPort;
	//System.out.println(serverPort);
	private ServerSocket listenSocket;
	
	public User(int id, String name, BlockingQueue<Response> responseQueue, BlockingQueue<Request> requestQueue, Scanner reader) throws IOException{
		this.id = id;
		this.name = name;
		this.steamKeys = new ArrayList<>();
		this.friends = new ArrayList<>();
		this.responseQueue = responseQueue;
		this.requestQueue = requestQueue;
		this.reader = reader;
		this.serverPort = 7896 + id;
		this.listenSocket = new ServerSocket(serverPort);
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
			String action = "";
			String friend = "";
			String keyName = "";
			int shareTime = 0;
			while(true){
				//request
				if(id == 0){
					System.out.println(id + " Enter action: ");
					action = reader.next();
					friend = "";
					keyName = "";
					shareTime = 0;
					if(action.contains("SHARE")){
						System.out.println(id + " SHARE");
						if(!friends.isEmpty()){
							System.out.println(id + " Enter friend: ");
							friend = reader.next();
							System.out.println(id + " Looking for " + friend);
							for(User user: friends){
								if(user.getName().contains(friend)){
									System.out.println(id + " Found Friend");
									if(!steamKeys.isEmpty()){
										System.out.println(id + " Enter steam key: ");
										keyName = reader.next();
										System.out.println(id + " Looking for Steam Key " + keyName);
										for(Key key: steamKeys){
											if(key.getName().equals(keyName)){
												System.out.println(id + " Found Key");
												System.out.println(id + " Enter an amount of time to share (Minutes): ");
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
					}else if(action.contains("SHOW")){
						for(Key key : steamKeys){
							System.out.println(key.toString());
						}
						Request request = new Request("WAIT");
						requestQueue.put(request);
					}else{
						Request request = new Request("WAIT");
						requestQueue.put(request);
					}
				}
				//response
				Response response = responseQueue.take();
				//System.out.println(response.getType() + " : " + response.getUser() + " : " + response.getKeyName() + " : " + response.getBorrowedTime());
				if(response.getUser() == null){
					responseQueue.put(response);
				}else if(response.getUser().getName().equals(this.getName())){
					System.out.println(response.getType() + " : " + response.getUser() + " : " + response.getKeyName() + " : " + response.getBorrowedTime());
					if(response.getType().equals("SUCCESS")){
						System.out.println(id + "SUCCESS");
						for(Key key: steamKeys){
							if(key.getName().equals(keyName)){
								for(User fUser : friends){
									if(fUser.getName().equals(friend)){
										key.setUser(friend);
										key.setTimeout(response.getBorrowedTime());
										key.setBorrowed(true);
										System.out.println(key.toString());
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
						System.out.println(id + " RECEIVE");
						receiveKey();
					}else if(response.getType().equals("TIMEOUT")){
						System.out.println(id + " TIMEOUT");
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
						System.out.println(id + " RETURN");
						String name = response.getKeyName();
						int count = 0;
						for(Key key: steamKeys){
							if(key.getName().equals(name)){
								System.out.println(key);
								key.setUser(key.getOwner());
								key.setBorrowed(false);
								key.setTimeout(null);
								steamKeys.remove(count);
								steamKeys.add(key);
								System.out.println(steamKeys.get(count));
								break;
							}
							count++;
						}
					}
				}else{
					responseQueue.put(response);
				}
				
				Thread.sleep(1000);
				if(id!=0){
					Thread.sleep(1000);
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
			System.out.println("Sending Key: " + data);
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
			//int serverPort = 7896 + id;
			//System.out.println(serverPort);
			//ServerSocket listenSocket = new ServerSocket(serverPort);
			//Socket s = listenSocket.accept();  
			//DataInputStream in = new DataInputStream( s.getInputStream());
			while(true) {
		    	// UTF is a string encoding see Sn 4.3
				try{
					Socket s = listenSocket.accept();  
					DataInputStream in = new DataInputStream( s.getInputStream());
					String returned = in.readUTF();
					System.out.println("Receiving Key: " + returned);
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
	
	public String toString(){
		return id + name;
	}
}
