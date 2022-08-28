import DTO.Tweet;
import edu.stanford.nlp.pipeline.*;

import java.util.*;


public class BasicPipelineExample {

//    public static String text = "Joe Smith was born in California. " +
//            "In 2017, he went to Paris, France in the summer. " +
//            "His flight left at 3:00pm on July 10th, 2017. " +
//            "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
//            "He sent a postcard to his sister Jane Smith. " +
//            "After hearing about Joe's trip, Jane decided she might go to France one day.";
    public static String text = "";

    public static void main(String[] args) {
        Tweet[] tweets = new Tweet[]{
                new Tweet("1561362640261226499","I have very good opinion. "),
                new Tweet("1561361064163516417","I love you. "),
                new Tweet("1561332565960806400","You always come in on time, follow your schedule and adhere to your designated lunch break time. "),
                new Tweet("1561157556369768448","You often come late to the office, causing scheduled meetings to start late. It also affects others schedules. You need to keep up with your schedule so your coworkers can keep up with theirs too. "),
                new Tweet("1561157326291222536","You have a unique imagination and have come up with some of the most creative ideas we’ve ever seen. "),
                new Tweet("1561046217840332808","He likes to take a traditional and risk-averse approach to things over a creative one. "),
                new Tweet("1561032727230648322","He maintains a culture of transparency and knowledge-sharing across all levels in your department. "),
                new Tweet("1560853365206171648","She often creates a communication gap and withholds information from her subordinates. "),
                new Tweet("1560806937008250880","This movie was actually neither that funny, nor super witty. "),
        };
        for (int i = 0; i < tweets.length; i++) {
            text+=tweets[i].text;
        }
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,sentiment");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument document = new CoreDocument(text);
//        // annnotate the document
        pipeline.annotate(document);

        List<CoreSentence> sentences = document.sentences();
        sentences.stream().forEach(sentence -> {
            System.out.println(sentence.sentiment() + " | " + sentence.text());

        });
    }
}
