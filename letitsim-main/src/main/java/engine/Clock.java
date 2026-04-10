package engine;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Clock {
    private double time = 1.0D;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Calendar calendar = Calendar.getInstance();


    public Clock() {
    }

    public double getTime() {
        return this.time;
    }

    public void setTime(double newTime) {
        if (this.time != newTime) {
            this.time = newTime;
        }

    }

    public double getEventTime(double duration) {
        return this.time + duration;
    }

    public String timeToString(double time) {
        this.calendar.setTimeInMillis((long)(time * 1000.0D));
        return this.dateFormat.format(this.calendar.getTime());
    }

    public String getFormattedTime()
    {
        return this.timeToString(this.time);
    }
}
