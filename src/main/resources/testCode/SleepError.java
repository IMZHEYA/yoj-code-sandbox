
/**
 * 无限睡眠（阻塞程序执行）
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000l;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡醒了");
    }
}
