
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.io.IOException;

import com.pi4j.component.switches.SwitchListener;
import com.pi4j.component.switches.SwitchState;
import com.pi4j.component.switches.SwitchStateChangeEvent;
import com.pi4j.component.switches.impl.GpioMomentarySwitchComponent;

/**
 * @author Tuan Vu
 *
 */
public class ShutDownPiExample{
	
	//Switch Operations
	private static boolean isButtonPressed = false;
	private static long buttonPressTimeStamp = 0;
	private static boolean isShuttingDown = false;
	private final static int SHUTDOWN_TIME = 5;
	private final static long SHUTDOWN_MILLIS = 4000;
	private static long lastInstructionTimeStamp = 0;
	private final static long WAIT_MILLIS = 2000;
	
	//Robot
	private static boolean isRobotOn = false;
	
	public static void main(String[] args) throws InterruptedException {
		// create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
		setUpSwitch(gpio, setUpBluetooth());
        run(gpio);
        	
	}   
	

	private static void setUpSwitch(GpioController gpio, final BluetoothListener bluetooth) {
		//Using Pi's internal pull up resistor instead of an external one. 
        //Dangerous if this pin is ever set as an output pin, but saves circuitry
        final GpioPinDigitalInput switchInputPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "InputPin", PinPullResistance.PULL_UP);
        switchInputPin.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        final GpioMomentarySwitchComponent shutDownSwitch = new GpioMomentarySwitchComponent(switchInputPin);
        
        shutDownSwitch.addListener(new SwitchListener() {
			
			@Override
			public void onStateChange(SwitchStateChangeEvent event) {
//				System.out.println("Old: " + event.getOldState() + ", New: " + event.getNewState());
				
				//User pressed button and let go. Remove shutdown countdown and start or stop the robot
				//Pulling the pin up sets the unpressed state as ON, while pressing the button is OFF. Backwards I know
				if (SwitchState.OFF == event.getOldState() && SwitchState.ON == event.getNewState()) {
					handleButtonUnpress(bluetooth);
				}
				
				//User pressed button. Start countdown to shut down
				else if (SwitchState.ON == event.getOldState() && SwitchState.OFF == event.getNewState()) {
					handleButtonPress();
				}
				
				
			}
		});
	}
	

	private static BluetoothListener setUpBluetooth() {
		BluetoothListener bluetooth = new BluetoothListener();
		bluetooth.setCallback(new BluetoothListener.Callback() {
			
			@Override
			public void onData(String data) {
				System.out.println("BT: " + data);
			}
		});
		return bluetooth;
	}
	
	private static void run(GpioController gpio) {
		while (true) {
        	//If buttonPressedTimeStamp is more than 4 seconds, start the shutdown timer
        	if (isButtonPressed)
			{
        		long current = System.currentTimeMillis();
        		//Checking isButtonPressed again due to quick button presses
        		if (isButtonPressed && 
        				(current - buttonPressTimeStamp) > SHUTDOWN_MILLIS) {
//        			System.out.println("Difference: " + (current - buttonPressTimeStamp));
//            		System.out.println("current: " + current);
//            		System.out.println("button: " + buttonPressTimeStamp);
            		startShutDown(gpio);
        		}
        	}
        }
	}
	
	/**
	 * When button is let go, issue instructions to the robot or abort a shutdown sequence
	 * @param bluetooth 
	 */
	private static void handleButtonUnpress(BluetoothListener bluetooth) {
		//Only turn on/off robot if the shut down sequence has not been started
		//Also can not issue instructions WAIT_MILLIS from each other
		if (!isShuttingDown &&
				System.currentTimeMillis() - lastInstructionTimeStamp > WAIT_MILLIS) {
		
			if (isRobotOn){
				//Turn robot off
				System.out.println("Turning Robot Off");
				bluetooth.stop();
				isRobotOn = false;
			}
			else {
				//Turn robot on
				System.out.println("Turning Robot On");
				boolean isBluetoothSuccessful = bluetooth.start();
				isRobotOn = isBluetoothSuccessful;
			}
			lastInstructionTimeStamp = System.currentTimeMillis();
		}
		
		//This will abort the shutdown sequence if it's started
		resetShutDownTimeStamp();
	}
	
	/**
	 * Set buttonPressed to true and capture the timestamp
	 */
	private static void handleButtonPress(){
		buttonPressTimeStamp = System.currentTimeMillis();
		isButtonPressed = true;
	}
	
	/**
	 * Aborts the shut down sequence
	 */
	private static void resetShutDownTimeStamp(){
		isButtonPressed = false;
		isShuttingDown = false;
	}
	
	/**
	 * Starts the shutdown sequence. After SHUTDOWN_TIME seconds, shuts down the
	 * gpio to disable all pins and shuts down the Pi. After green light stops blinking
	 * it will be safe to unplug the Pi. Shut down sequence can be overriden by letting
	 * go of the button
	 * @param gpio
	 */
	private static void startShutDown(GpioController gpio){
		isShuttingDown = true;
		ProcessBuilder process = new ProcessBuilder("halt");
		try {
			for (int i = SHUTDOWN_TIME; i > 0 && isShuttingDown; i--) {
				System.out.println("Shutting Down in " + i);
				Thread.sleep(1000);
			}
			
			//Check if shutdown override has been done
			if (isShuttingDown){
				gpio.shutdown();
				process.start();
			}
			else {
				System.out.println("Shut Down Aborted");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}		

