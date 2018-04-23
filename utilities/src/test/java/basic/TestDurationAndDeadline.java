package basic;


import scala.concurrent.duration.Duration;

public class TestDurationAndDeadline {
    public static void main(String[] args) {
        Duration fivesec = Duration.create("5 seconds");
        Duration threesec = Duration.create(3, "seconds");
        System.out.println(fivesec.minus(threesec));
<<<<<<< HEAD
        Duration deadline;
=======
        Duration deadline;ß
>>>>>>> 4354cd3ceac8d3b692b3193889cbe6d23546a6b0
    }
}
