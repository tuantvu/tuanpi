
/**
 * Write a description of class RobotController here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class RobotController
{
    // instance variables - replace the example below with your own
    private MotorController motors;
    private BluetoothListener bluetooth;

    /**
     * Constructor for objects of class RobotController
     */
    public RobotController()
    {
        motors = new MotorController();
        bluetooth = new BluetoothListener();
        bluetooth.setCallback(new BluetoothListener.Callback(){
            @Override
            public void onData(String data)
            {
               handleBluetoothData(data);
                
            }
        });
    }
    
    private void handleBluetoothData(String data)
    {
         //Split the data
        String[] dataArray = data.split(",");
        //Get the last two data elements
        if (dataArray.length >= 2)
        {
            String data1 = dataArray[dataArray.length - 2];
            String data2 = dataArray[dataArray.length - 1];
            //System.out.println("Split speed: " + speed + ", turn: " + turn);
            
            boolean automaticMode = true;
            
            //Data with s & t is speed and turn, else left and right wheel
            if (data1.contains("s") && data2.contains("t"))
            {
                data1 = data1.replace("s","");
                data2 = data2.replace("t","");
                automaticMode = true;
            }
            else if ( data1.contains("l") && data2.contains("r"))
            {
                data1 = data1.replace("l","");
                data2 = data2.replace("r","");
                automaticMode = false;
            }
            
            try
            {
                int data1Value = Integer.parseInt(data1);
                int data2Value = Integer.parseInt(data2);
                
                if (automaticMode)
                {
                    System.out.println("Move motors: " + data1Value + ", " + data2Value);
                    motors.move(data1Value, data2Value);
                }
                else
                {
                    System.out.println("Move wheels: " + data1Value + ", " + data2Value);
                    motors.moveWheels(data1Value, data2Value);
                }
            }
            catch (NumberFormatException e)
            {
                //Do nothing
                System.err.println("NumberFormatEx parsing: " + data1 + " or " + data2);
            }
            
            
            
        }
    }

    /**
     * An example of a method - replace this comment with your own
     * 
     * @param  y   a sample parameter for a method
     * @return     the sum of x and y 
     */
    public void start()
    {
        bluetooth.tryConnecting();
    }
    
    public void hold()
    {
        bluetooth.heldConnect();
    }
    
    public void talk()
    {
        System.out.println("Hi I can do other stuff");
    }
}
