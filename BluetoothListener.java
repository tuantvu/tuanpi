import com.pi4j.io.serial.*;
import java.io.IOException;

/**
 * Write a description of class BluetoothListener here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class BluetoothListener
{
    // instance variables - replace the example below with your own
    private Serial serial;
    private Callback callback;

    /**
     * Constructor for objects of class BluetoothListener
     */
    public BluetoothListener()
    {
        serial = SerialFactory.createInstance();
        serial.addListener(new SerialDataEventListener()
        {
           @Override
           public void dataReceived(SerialDataEvent event)
           {
               try
               {
                   System.out.println(event.getAsciiString());
                   if (callback != null)
                   {
                       callback.onData(event.getAsciiString());
                   }
               }
               catch (IOException e)
               {
                   e.printStackTrace();
               }
                 
           }
        });
    }
    
    public void setCallback(Callback callback){
        this.callback = callback;
    }

    /**
     * 
     */
    public void start()
    {
        try
        {
            SerialConfig config = new SerialConfig();
            config.device("/dev/rfcomm0")
                .baud(Baud._38400)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE);
                
            serial.open(config);
        }
        catch (IOException e)
        {
            System.err.println("====> SERIAL SETUP FAILED: " + e.getMessage());
            return;
        }
    }
    
    public static interface Callback{
        void onData(String data);
    }
}
