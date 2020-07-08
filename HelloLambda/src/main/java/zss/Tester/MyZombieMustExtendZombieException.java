package zss.Tester;

/**
 *
 * @author bdahl
 */
public class MyZombieMustExtendZombieException extends RuntimeException {
    @Override
    public String getMessage() {
        return "For this scenario, MyZombie must extend the Zombie class.";
    }

    @Override
    public String getLocalizedMessage() {
        return "For this scenario, MyZombie must extend the Zombie class.";
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        super.setStackTrace(new StackTraceElement[0]);
    }
}
