import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NLP {
    static ExecutorService executor2;
    StanfordCoreNLP pipeline;
    AtomicReference<Double> totalSentiment;
    static final int numberOfThreads = 10;

    public NLP(AtomicReference<Double> totalSentiment) {
        executor2 = Executors.newFixedThreadPool(numberOfThreads);
        this.totalSentiment = totalSentiment;
        pipeline = new StanfordCoreNLP("Configure.properties");
    }

    public double computeSentiment(String text) {
        AtomicReference<Double> totalScore = new AtomicReference<>((double) 0); // Default as Neutral. 1 = Negative, 2 = Neutral, 3 = Positive
        Annotation annotation = pipeline.process(text);
        for(CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            executor2.execute(() -> {
                System.out.println("--------------------");
                System.out.println(Thread.activeCount());
                Tree tree = sentence.get(SentimentAnnotatedTree.class);
                totalScore.updateAndGet(v -> new Double((double) (v + RNNCoreAnnotations.getPredictedClass(tree))));
            });
        }
        executor2.shutdown();
        try {
            executor2.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        totalScore.updateAndGet(v -> new Double((double) (v / annotation.get(CoreAnnotations.SentencesAnnotation.class).size())));
        this.totalSentiment.set(this.totalSentiment.get()+ totalScore.get());
        return(totalScore.get());
    }
}