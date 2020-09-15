package omics.gui.test;

import java.util.concurrent.Semaphore;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 12 Sep 2020, 1:48 PM
 */
public class SemaphoreApp
{
    public static class AThread extends Thread
    {
        private Semaphore semaphore;

        public AThread(Semaphore semaphore)
        {
            this.semaphore = semaphore;
        }

        @Override
        public void run()
        {
            try {
                semaphore.acquire();
                System.out.println(System.currentTimeMillis() + " :" + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
            }
        }
    }

    public static void main(String[] args)
    {
        Semaphore semaphore = new Semaphore(0);
        for (int i = 1; i <= 10; i++) {
            new AThread(semaphore).start();
        }
        System.out.println("go");
        semaphore.release(5);
    }
}
