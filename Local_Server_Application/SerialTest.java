import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialTest implements SerialPortEventListener
{
	private SerialPort serialPort;
	private static final String PORT_NAMES[] = {
		"/dev/tty.usbserial-A9007UX1", // Mac OS X
		"/dev/ttyUSB0", // Linux
		"COM5", // Windows
	};
	private BufferedReader input;
	private OutputStream output;
	
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
				System.out.println(inputLine);
			}
			catch (Exception e){
				System.err.println(e.toString());
			}
		
		}
	}
	
	public static void main(String[] args) throws Exception {
		SerialTest main = new SerialTest();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
			}
		};
		t.start();
		System.out.println("Local Application started");
	}

}