package Server;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class Main {
	public static void main(String[] args) throws Exception {
		
		initFirebase();
		
		SerialTest main = new SerialTest();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
					
				}
			}
		};
		t.start();
		System.out.println("Local Application started");
	}
	
	private static void initFirebase() throws IOException{
		
		FileInputStream serviceAccount = new FileInputStream("test_credentials.json");

		FirebaseOptions options = new FirebaseOptions.Builder()
				  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
				  .setDatabaseUrl("https://test-749b2-default-rtdb.firebaseio.com/")
				  .build();

		FirebaseApp.initializeApp(options);
		
	}
}
