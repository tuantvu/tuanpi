
/**
 * Controls the robot's wheels by translating instructions into motor operations
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MotorController
{
    // instance variables - replace the example below with your own
    private Motor left;
    private Motor right;
    private static final int SPEED_DIFF = Motor.FASTEST - Motor.SLOWEST;

    /**
     * Constructor for objects of class MotorController
     */
    public MotorController()
    {
        left = new Motor(Motor.MotorType.LEFT);
        right = new Motor(Motor.MotorType.RIGHT);
    }
    
    /**
     * Moves the motors. Positive speedPct will move the robot forward, negative will move backwards
     */
    public void move(int speedPct, int turn)
    {
        if (speedPct > 0)
        {
            forward(speedPct, turn);
        }
        else if (speedPct < 0)
        {
            backward(-speedPct, turn);
        }
        else if (turn > 0)
        {
            turnRight(turn);
        }
        else if (turn < 0)
        {
            turnLeft(-turn);
        }
        else
        {
            brake();
        }
    }
    
    public void moveWheels(int leftPct, int rightPct)
    {
        //Left wheels
        int leftSpeed = getMotorSpeed(Math.abs(leftPct));
        if (leftPct > 0)
        {
            left.forward(leftSpeed);
        }
        else if (leftPct < 0)
        {
            left.backward(leftSpeed);
        }
        else
        {
            left.brake();
        }
        
        //Right wheels
        int rightSpeed = getMotorSpeed(Math.abs(rightPct));
        if (rightPct > 0)
        {
            right.forward(rightSpeed);
        }
        else if (rightPct < 0)
        {
            right.backward(rightSpeed);
        }
        else
        {
            right.brake();
        }
    }

    /**
     * Moves the robot forward with speed percentage and direction
     */
    public void forward(int speedPct, int turn)
    {
        left.forward(getLeftMotorSpeed(speedPct, turn));
        right.forward(getRightMotorSpeed(speedPct, turn));
    }
    
    public void backward(int speedPct, int turn)
    {
       left.backward(getLeftMotorSpeed(speedPct, -turn));
       right.backward(getRightMotorSpeed(speedPct, -turn));
    }
    
    public void turnRight(int speedPct)
    {
       int motorSpeed = getMotorSpeed(speedPct);
       left.forward(motorSpeed);
       right.backward(motorSpeed);
    }
    
    public void turnLeft(int speedPct)
    {
       int motorSpeed = getMotorSpeed(speedPct);
       left.backward(motorSpeed);
       right.forward(motorSpeed);
    }
    
    public void brake()
    {
       left.brake();
       right.brake();
    }
    
    private int getMotorSpeed(int speedPct)
    {
        int adjustedSpeed = speedPct;
        if (speedPct > 100)
        {
            adjustedSpeed = 100;
        }
        else if (speedPct < 0)
        {
            adjustedSpeed = 0;
        }
        
        double speed = adjustedSpeed / 100.0;
        return (int)(speed * SPEED_DIFF + Motor.SLOWEST);
    }
    
    /**
     * Returns the right motor speed depending on the speedPct subtracted by
     * the turn ratio. If turn is greater than 0, that means the right motor
     * needs to slow down by that ratio
     */
    private int getRightMotorSpeed(int speedPct, int turn)
    {
        if (turn > 0)
        {
            if (turn > 100)
            {
                turn = 100;
            }
            double turnPct = turn / 100.0;
            int speedReduction = (int)(speedPct * turnPct);
            speedPct = speedPct - speedReduction;
        }
        return getMotorSpeed(speedPct);
    }
    
    /**
     * Returns the left motor speed depending on the speedPct subtracted by
     * the turn ratio. If turn is less than 0, that means the left motor
     * needs to slow down by the absolute number of that ratio
     */
    private int getLeftMotorSpeed(int speedPct, int turn)
    {
        if (turn < 0)
        {
            if (turn < -100)
            {
                turn = -100;
            }
            double turnPct = -turn / 100.0;
            int speedReduction = (int)(speedPct * turnPct);
            speedPct = speedPct - speedReduction;
        }
        return getMotorSpeed(speedPct);
    }
    
}
