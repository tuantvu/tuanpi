import com.pi4j.io.gpio.*;


/**
 * Controls one motor to move forward, backward, or brake. Forwards and backwards can take in a speed.
 * The operating pwn speed for this motor is between 310 (slowest) to 500 (fastest) 
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Motor
{
    // instance variables - replace the example below with your own
    private GpioController gpio;
    private GpioPinPwmOutput speedPin;
    private GpioPinDigitalOutput forwardPin;
    private GpioPinDigitalOutput backwardPin;
    
    public static final int SLOWEST = 300;
    public static final int FASTEST = 500;
    private static final int MIN_SPEED = 0;
    private static final int MAX_SPEED = 1024;
       
    /**
     * Constructor for objects of class Sonar
     */
    public Motor(MotorType type)
    {
        gpio = GpioFactory.getInstance();
        
        // initialise instance variables
        if (type == MotorType.RIGHT)
        {
            speedPin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_23, "SPEED", 0);
            forwardPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "FORWARD", PinState.LOW);
            backwardPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "BACKWARD", PinState.LOW);
        }
        else if (type == MotorType.LEFT)
        {
            speedPin = gpio.provisionPwmOutputPin(RaspiPin.GPIO_26, "SPEED", 0);
            forwardPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "FORWARD", PinState.LOW);
            backwardPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "BACKWARD", PinState.LOW);
        }
        // set shutdown state for this input pin
        speedPin.setShutdownOptions(true);
        forwardPin.setShutdownOptions(true);
        backwardPin.setShutdownOptions(true);
    }

    /**
     * Moves motor forward
     * 
     */
    public void forward(int speed)
    {
       speed = adjustSpeed(speed);
       if (speed == MIN_SPEED)
       {
           brake();
       }
       else
       {
           forwardPin.high();
           backwardPin.low();
           speedPin.setPwm(speed);
       }
       
    }   
    
    /**
     * Moves motor backward
     */
    public void backward(int speed)
    {
       speed = adjustSpeed(speed);
       if (speed == MIN_SPEED)
       {
           brake();
       }
       else
       {
           forwardPin.low();
           backwardPin.high();
           speedPin.setPwm(speed);
       }
    }
    
    /**
     * Moves motor backward
     */
    public void brake()
    {
       forwardPin.low();
       backwardPin.low();
       speedPin.setPwm(MIN_SPEED);
    }
    
    /**
     * If the speed is equal or lower than the SLOWEST or equal to or higher than the FASTEST,
     * then set the min and max speed of the motor. 
     */
    private int adjustSpeed(int speed)
    {
        int result = speed;
        if (speed >= FASTEST)
        {
            result = MAX_SPEED;
        }
        else if (speed <= SLOWEST)
        {
            result = MIN_SPEED;
        }
        return result;
    }
    
    public static enum MotorType 
    {
        LEFT, RIGHT
    };
}
