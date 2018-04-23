package basic;


import scala.concurrent.duration.Duration;

public class TestDurationAndDeadline {
    public static void main(String[] args) {
        Duration fivesec = Duration.create("5 seconds");
        Duration threesec = Duration.create(3, "seconds");
        System.out.println(fivesec.minus(threesec));
        Duration deadline;
    }
}
