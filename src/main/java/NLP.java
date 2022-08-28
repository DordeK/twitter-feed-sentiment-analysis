import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class NLP {
    StanfordCoreNLP pipeline;
    AtomicReference<Double> totalSentiment;
    static final int numberOfThreads = 10;

    public NLP(AtomicReference<Double> totalSentiment) {
        this.totalSentiment = totalSentiment;
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse, sentiment");
        props.setProperty("parse.maxlen", "70");
        pipeline = new StanfordCoreNLP(props);
    }

    public double computeSentiment(String text) {
        double totalScore = 0; // Default as Neutral. 1 = Negative, 2 = Neutral, 3 = Positive
        Annotation annotation = pipeline.process(text);
        for(CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence.get(SentimentAnnotatedTree.class);
                totalScore += RNNCoreAnnotations.getPredictedClass(tree);
        }
        totalScore /= annotation.get(CoreAnnotations.SentencesAnnotation.class).size();
        totalSentiment.set(totalSentiment.get()+totalScore);
        return(totalScore);
    }
}