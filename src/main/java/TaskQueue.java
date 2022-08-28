import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

public class TaskQueue {
    static ExecutorService executor;
    static NLP nlp;
    static final int numberOfThreads = Runtime.getRuntime().availableProcessors()+4;

    public TaskQueue(AtomicReference<Double> totalSentiment) {
        executor = Executors.newFixedThreadPool(numberOfThreads);
        nlp = new NLP(totalSentiment);
        System.out.println(numberOfThreads);
    }

    public boolean addTweetToQueue(String tweet){
        executor.submit(() -> {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            int activeCount = threadPoolExecutor.getActiveCount();
            long taskCount = threadPoolExecutor.getTaskCount();
            long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
            long tasksToDo = taskCount - completedTaskCount - activeCount;
            System.out.println(completedTaskCount + " completed out of "+ taskCount);
            nlp.computeSentiment(tweet);
        });
        return true;
    }
}