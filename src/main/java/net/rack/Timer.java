package net.rack;

public class Timer {
  public static final String ZERO_TIME = "0:00.000";

  private long    startTime = 0;
  private long    runTime   = 0;
  private boolean isRunning = false;

  public void starter() {
    if (isRunning) {
      return;
    }
    startTime = System.nanoTime();
    isRunning = true;
  }

  public long getStartTime() {
    return startTime;
  }

  public boolean getIsRunning() {
    return isRunning;
  }

  public void stopper() {
    if (!isRunning) {
      return;
    }
    long stopTime = System.nanoTime();
    isRunning = false;
    runTime = (stopTime - startTime);
  }

  public long getRunTime() {
    return runTime;
  }

  public void reset() {
    startTime = 0;
    runTime = 0;
    isRunning = false;
  }

}
