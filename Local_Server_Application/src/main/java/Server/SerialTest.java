package Server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialTest implements SerialPortEventListener{
	private SerialPort serialPort;
	private static final String PORT_NAMES[] = {
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyUSB0", // Linux
		"COM5", // Windows
	};
	private BufferedReader input;
	private OutputStream output;
	
	private int countSensors = 0;
	
	public BufferedReader getInput(){	
		return input;
	}
	
	public void setInput(BufferedReader input){
		this.input = input;
	}
	
	public OutputStream getOutput(){
		return output;
	}
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	public void initialize(){
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		//Finding of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()){
			CommPortIdentifier currPortId = (CommPortIdentifier)
			portEnum.nextElement();
			for (String portName : PORT_NAMES){
				if (currPortId.getName().equals(portName)){
					portId = currPortId;
					break;
				}
			}
		}
		
		if (portId == null){
			System.out.println("Could not find COM port.");
			return;
		}
		
		try{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
			TIME_OUT);
			
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
			// open the streams
			input = new BufferedReader(new
			InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch (Exception e){
			System.err.println(e.toString());
		}
	}
	/**
	* This should be called when you stop using the port.
	* This will prevent port locking on platforms like Linux.
	*/
	public synchronized void close(){
		if (serialPort != null){
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	/**
	* Handle an event on the serial port. Read the data and print it.
	*/
	public synchronized void serialEvent(SerialPortEvent oEvent){
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE){
			try{
				String inputLine=input.readLine();
				
//				for(String line : inputArray) {
//					System.out.println(line.substring(line.length()-5).trim() + "---");
//				}
				
				countSensors += 1;
				
				if(countSensors > 3) {
					countSensors = 1;
				}
				
				String sensorType = "";
				
				if(countSensors == 1) {
					// System.out.println("UVIndex: " + inputLine.substring(inputLine.length() - 5).trim());
					sensorType = "UVIndexSensor";
				} else if(countSensors == 2) {
					// System.out.println("Temperatura: " + inputLine.substring(inputLine.length() - 5).trim());
					sensorType = "TemperatureSensor";
				} else if(countSensors == 3) {
					//System.out.println("Umiditate sol: " + inputLine.substring(inputLine.length() - 5).trim());
					sensorType = "HumiditySensor";
				}
				
				addDataToFirebase(sensorType, Double.valueOf(inputLine.substring(inputLine.length() - 5).trim()));
				
				// System.out.println(inputLine);
			}
			catch (Exception e){
				System.err.println(e.toString());
			}
		
		}
	}
	
	private static void setValue(String timeStamp, String sensorType, double value) throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        
        FirebaseDatabase.getInstance().getReference().child(sensorType).child(timeStamp).setValue(value, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError de, DatabaseReference dr) {
                done.countDown();
            }
        });
          
        done.await();
		
	}
	
	private static void addDataToFirebase(String sensorType, double value) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {
		  @Override
		  public void run() {
			  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			  
			  String timeStampToBeAdded = sdf.format(timestamp);
			  
				try {
					setValue(timeStampToBeAdded, sensorType, value);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		  }
		}, 0, 1, TimeUnit.MINUTES);
	}

}