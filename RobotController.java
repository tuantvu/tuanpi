
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
            String speed = dataArray[dataArray.length - 2];
            String turn = dataArray[dataArray.length - 1];
            System.out.println("Split speed: " + speed + ", turn: " + turn);
            
            //Make sure speed has an "s" and turn has a "t"
            if (speed.contains("s") && turn.contains("t"))
            {
                speed = speed.replace("s","");
                turn = turn.replace("t","");
                
                //Try to parse numbers
                try
                {
                    int speedValue = Integer.parseInt(speed);
                    int turnValue = Integer.parseInt(turn);
                    
                    System.out.println("Move motors: " + speedValue + ", " + turnValue);
                    motors.move(speedValue, turnValue);
                }
                catch (NumberFormatException e)
                {
                    //Do nothing
                    System.err.println("NumberFormatEx parsing: " + speed + " or " + turn);
                }
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
        bluetooth.start();
    }
}
