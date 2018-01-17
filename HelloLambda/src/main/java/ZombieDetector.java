import greenfoot.Actor;

public abstract class ZombieDetector extends Actor
{
    private boolean isOn;
    
    /**
     * Create an invisible ZombieDetector
     */
    public ZombieDetector()
    {
        isOn = false;
    }
    
    /**
     * Check if a Zombie has stepped on the detector.  Increment the count only when the zombie
     * moves onto the detector.
     */
    @Override
    public void act() 
    {
        if (isTouching(Zombie.class)) {
            if (isOn == false){     
                detected();
                isOn = true;
            }
        }
        else {
            isOn = false;
        }
    }
    
    /**
     * A method that is called whenever a zombie steps onto the detector
     */
    public abstract void detected();
}
