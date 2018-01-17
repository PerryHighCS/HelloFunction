/**
 * A Zombie's ultimate destination
 */
public class ZombieGoal extends ZombieDetector
{  
    /**
     * Act - do whatever the ZombieGoal wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    @Override
    public void act() 
    {
        super.act();
    }    
    
    /**
     * When a zombie reaches this cell, it has reached its goal.
     */
    @Override
    public void detected()
    {
        getIntersectingObjects(Zombie.class).forEach((z) -> z.win());
    }
}
