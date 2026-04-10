package model;

import engine.BPSimulator;
import engine.Clock;
import engine.exception.ProcessValidationException;
import engine.exception.BPSimulatorException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;



public class TimeTable extends model.xsd.TimeTable {
    private Clock clock;
    private BPSimulator simulator;
    private double to = 0.0D;
    private List<TimeTableRule> rules = new ArrayList();
    private TimeTable.Range firstRange;
    private TimeTable.Range lastRange;


    public TimeTable() {
    }


    public void loadFrom(model.xsd.TimeTable sourceTimeTable) throws ProcessValidationException {
        if (sourceTimeTable == null) {
            throw new ProcessValidationException("Specified timetable not found");
        } else {
            this.rules = new ArrayList(sourceTimeTable.getRules().getRule().size());
            WeekDay[] days = WeekDay.values();


            Iterator var4 = sourceTimeTable.getRules().getRule().iterator();

            while(true) {
                while(var4.hasNext()) {
                    model.xsd.TimeTableRule tt = (model.xsd.TimeTableRule)var4.next();
                    TimeTableRule rule;
                    if (tt.getToWeekDay() != null && tt.getToWeekDay() != tt.getFromWeekDay()) {
                        int i = tt.getFromWeekDay().ordinal();

                        while(true) {
                            rule = new  TimeTableRule();
                            rule.init(days[i], tt.getFromTime(), tt.getToTime());
                            this.rules.add(rule);
                            if (i == tt.getToWeekDay().ordinal()) {
                                break;
                            }

                            i = (i + 1) % days.length;
                        }
                    } else {
                        rule = new TimeTableRule();
                        rule.init(tt.getFromWeekDay(), tt.getFromTime(), tt.getToTime());
                        this.rules.add(rule);
                    }
                }

                return;
            }
        }
    }

    public void setCompletionTime(ProcessActivity activity) throws InterruptedException, BPSimulatorException {
        this.setGetCalculatedTimes(this.clock.getTime(), activity.getDuration(), activity);
    }

    private double buildTimeTableUntilDay(double date) throws InterruptedException {
        double availTime = 0.0D;
        Calendar c = Calendar.getInstance();
        long dateinMillis = Math.round(date) * 1000L;
        if (this.to == 0.0D) {
            c.setTimeInMillis(dateinMillis);
        } else {
            c.setTimeInMillis(Math.round(this.to) * 1000L);
            c.add(5, 1);
        }

        c.set(12, 0);
        c.set(11, 0);
        c.set(13, 0);
        c.set(14, 0);

        while(c.getTimeInMillis() <= dateinMillis) {
            this.checkInterrupted();
            int day = c.get(7);
            Iterator var9 = this.rules.iterator();

            while(var9.hasNext()) {
                TimeTableRule r = (TimeTableRule)var9.next();
                if (day == r.getDay()) {
                    this.addRange(c.getTimeInMillis(), r);
                    availTime += (double)(r.getTimeTo() - r.getTimeFrom());
                }
            }

            this.to = (double)(c.getTimeInMillis() / 1000L);
            c.add(5, 1);
        }

        return availTime;
    }

    private double buildTimeTableForNextDay() throws InterruptedException {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(Math.round(this.to) * 1000L);
        c.add(5, 1);
        return this.buildTimeTableUntilDay((double)(c.getTimeInMillis() / 1000L));
    }

    private void setFirstRangeByCurrentDate(double date) {
        while(this.firstRange != null && this.firstRange.To <= date) {
            this.firstRange = this.firstRange.Next;
        }
    }



    private double setGetCalculatedTimes(double currTime, double duration, ProcessActivity activity) throws InterruptedException, BPSimulatorException {
        TimeTable.Range prevR = null;
        double completionTime = 0.0D;
        double idleTime = 0.0D;
        this.setFirstRangeByCurrentDate(currTime);
        if (this.to == 0.0D) {
            this.buildTimeTableUntilDay(currTime);
        }


        while(this.firstRange == null) {
            this.buildTimeTableForNextDay();
        }

        TimeTable.Range r = this.firstRange;

        while(true) {
            if (r.From > currTime) {
                if (prevR != null) {
                    idleTime += r.From - Math.max(prevR.To, currTime);
                } else {
                    idleTime += r.From - currTime;
                }
            }

            double available = r.getAvailableTime(currTime);
            double enabledTime;
            if (duration <= available) {
                completionTime = Math.max(currTime, r.From) + duration;
                if (activity != null) {
                    activity.addWorkInterval(Math.max(currTime, r.From), completionTime);
                }

                if (activity != null) {
                    activity.setCompletionTime(completionTime);
                    activity.setWorkingIdleTime(idleTime);
                    if (this.simulator != null && simulator.getMaxSimulationCycleTimeInSeconds() > 0 && activity.getCompletionTime() > 0.0D && activity.getCompletionTime() - this.simulator.getSimulationStartTime() > (double)this.simulator.getMaxSimulationCycleTimeInSeconds()) {

                        throw new BPSimulatorException("Maximum allowed cycle time exceeded, maximum is " + this.simulator.getMaxSimulationCycleTimeInSeconds() / 86400 + " days");
                    }

                    enabledTime = activity.getTimeStamp((byte)0);
                    if (enabledTime < currTime) {
                        double enabledIdle = 0.0D;

                        for(r = this.firstRange; r != null && r.To < currTime; r = r.Next) {
                            ;
                        }

                        for(; r != null; r = r.Previous) {
                            activity.addEnabledInterval(Math.max(r.From, enabledTime), Math.min(r.To, currTime));
                            if (r.Previous == null) {
                                if (enabledTime < r.From) {
                                    enabledIdle += r.From - enabledTime;
                                }
                                break;
                            }

                            if (enabledTime >= r.From) {
                                break;
                            }

                            if (currTime >= r.From && enabledTime <= r.Previous.To) {
                                enabledIdle += r.From - r.Previous.To;
                            } else if (currTime >= r.From && enabledTime > r.Previous.To) {
                                enabledIdle += r.From - enabledTime;
                            } else {
                                if (currTime >= r.From || enabledTime > r.Previous.To) {
                                    break;
                                }

                                enabledIdle += currTime - r.Previous.To;
                            }
                        }

                        activity.setEnabledIdleTime(enabledIdle);
                    }
                }

                return completionTime;
            }

            duration -= available;
            if (activity != null && available > 0.0D) {
                activity.addWorkInterval(Math.max(currTime, r.From), completionTime != 0.0D ? completionTime : r.To);
            }

            if (r.Next == null) {
                enabledTime = 0.0D;

                do {
                    enabledTime += this.buildTimeTableForNextDay();
                } while(duration > enabledTime);
            }

            prevR = r;
            r = r.Next;
        }
    }

    private void addRange(long dateInMillis, TimeTableRule rule) {
        double from = (double)(dateInMillis / 1000L + (long)rule.getTimeFrom());
        double to = (double)(dateInMillis / 1000L + (long)rule.getTimeTo());
        TimeTable.Range r = new TimeTable.Range(from, to);
        if (this.lastRange != null) {
            this.lastRange.Next = r;
            r.Previous = this.lastRange;
        }

        if (this.firstRange == null) {
            this.firstRange = r;
        }

        this.lastRange = r;
    }

    public double getCompletionTime(double currentTime, double duration) throws InterruptedException, BPSimulatorException {
        return this.setGetCalculatedTimes(currentTime, duration, (ProcessActivity)null);
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return this.clock;
    }

    public List<TimeTableRule> getLocalRules() {
        return this.rules;
    }

    public double getTotalIdleTime(double simulationStartTime, double simulationEndTime) {
        double idle = 0.0D;

        TimeTable.Range r;
        for(r = this.firstRange; r != null && r.Previous != null && r.To > simulationStartTime; r = r.Previous) {
            ;
        }

        if (r != null && simulationStartTime < r.From) {
            idle += r.From - simulationStartTime;
        }

        while(r != null && r.Next != null) {
            TimeTable.Range r2 = r.Next;
            double next = Math.min(r2.From, simulationEndTime);
            double since = Math.max(r.To, simulationStartTime);
            if (next > since) {
                idle += next - since;
            }

            if (r2.From >= simulationEndTime) {
                break;
            }

            r = r2;
        }

        return idle;
    }

    public BPSimulator getSimulator() {
        return this.simulator;
    }

    public void setSimulator(BPSimulator simulator) {
        this.simulator = simulator;
    }

    private void checkInterrupted() throws InterruptedException {
        if (this.getSimulator() != null) {
            this.getSimulator().checkInterrupted();
        }

    }

    class Range {
        public double From;
        public double To;
        public TimeTable.Range Next;
        public TimeTable.Range Previous;

        public Range(double from, double to) {
            this.From = from;
            this.To = to;
        }

        public boolean intersects(double stamp) {
            return this.From >= stamp && stamp < this.To;
        }

        public int compareTo(double stamp) {
            if (stamp < this.From) {
                return -1;
            } else {
                return stamp >= this.To ? 1 : 0;
            }
        }

        public double getAvailableTime(double forTime) {
            if (forTime < this.From) {
                return this.To - this.From;
            } else {
                return forTime < this.To && forTime >= this.From ? this.To - forTime : 0.0D;
            }
        }

        public String toString() {
            return this.From + "-" + this.To;
        }
    }
}
