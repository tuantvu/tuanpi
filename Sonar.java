import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;


/**
 * Write a description of class Sonar here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Sonar
{
    // instance variables - replace the example below with your own
    private GpioController gpio;
    private GpioPinDigitalOutput trigger;
    private GpioPinDigitalInput echo;
    private long startTime;
    private long endTime;
    private boolean isRangeFinding;
    private boolean isPulsing;

    /**
     * Constructor for objects of class Sonar
     */
    public Sonar()
    {
        gpio = GpioFactory.getInstance();
        
        // initialise instance variables
        trigger = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "TRIGGER", PinState.LOW);
        echo = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, PinPullResistance.PULL_DOWN);
        try {
            Thread.sleep(60);
        } catch (InterruptedException ex) {
          
        }
        
        
        // set shutdown state for this input pin
        trigger.setShutdownOptions(true);
        echo.setShutdownOptions(true);
    }
    
    public void provision()
    {
        echo.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                //System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                if (event.getState().equals(PinState.HIGH) && !isPulsing)
                {
                    isPulsing = true;
                    startTime = System.nanoTime();
                    //System.out.println("starttime: " + startTime);
                }
                else if (event.getState().equals(PinState.HIGH) && isPulsing)
                {
                    isPulsing = false;
                    startTime = 0;
                }
                else if (event.getState().equals(PinState.LOW) && isPulsing)
                {
                    long endTime = System.nanoTime();
                    //System.out.println("endTime: " + endTime);
                    double pulse_duration = (endTime - startTime) / 1000.0;
                    double distance = pulse_duration / 29 / 2;
                    //distance = Math.round(distance * 100);
                    //distance = distance/100;
                    if (distance < 100.0)
                    {
                        System.out.println("distance: " + distance + " cm");
                    }
                    isPulsing = false;
                }
            }

        });
        
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     */
    public void start()
    {
        isRangeFinding = true;
        while (isRangeFinding)
        {
            //System.out.println("pulse at " + System.currentTimeMillis());
            trigger.pulse(1, true);
            startTime = System.nanoTime();
            while (echo.isLow())
            {
                startTime = System.nanoTime();
            }
            
            while (echo.isHigh())
            {
                endTime = System.nanoTime();
            }
            
            double pulse_duration = (endTime - startTime) / 1000.0;
            double distance = pulse_duration / 29 / 2;
            System.out.println("distance: " + distance + " cm");
            
            try
            {
                Thread.sleep(250);
            }
            catch (Exception e)
            {
                System.out.println("Exception caught: " + e.getMessage());
            }
        }
    }   
    
    public void stop()
    {
        isRangeFinding = false;
    }
}
