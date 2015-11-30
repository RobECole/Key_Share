import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class KeyShareTester {

	public static void main (String args[])throws InterruptedException, IOException {
		//arg 1: number of nodes
		BlockingQueue<Response> responseQueue = new ArrayBlockingQueue<Response>(100);
		BlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(100);
		List<User> users = new ArrayList<>();
		List<Thread> threads = new ArrayList<Thread>();
		int numUsers = Integer.parseInt(args[0]);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));  // Reading from System.in
		for(int i = 0 ;  i < numUsers; i++){
			//Add Users
			System.out.println("Username " + i + ": ");
			String name = reader.readLine();
			User user = new User(i, name, responseQueue, requestQueue, reader);
			Key key = new Key(i, name + i + i, name, new Date());
			user.addSteamKey(key);
			users.add(user);
			threads.add(new Thread(user));
		}
		for(int i = 0; i < numUsers; i++){
			User user = users.get(i);
			for(int j = 0; j < numUsers; j++){
				if(i != j){
					User friend = users.get(j);
					user.addFriend(friend);
				}
			}
		}
		
		
		
		Server server = new Server(requestQueue, responseQueue, users);
		threads.add(new Thread(server));
		
		
		
		//start threads
		System.out.println("Started");
		for(Thread t : threads){
			t.start();
		}
		
		//wait for the threads to complete
		for(Thread t: threads){
			t.join();
		}
		reader.close();
		System.out.println("Done");
		
	}
}
