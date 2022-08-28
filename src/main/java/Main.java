import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TweetsApi;
import com.twitter.clientlib.api.TwitterApi;

import com.twitter.clientlib.model.Get2TweetsSearchRecentResponse;
import twitter4j.*;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
     static Set<String> tweetFields = new HashSet<>();
     static Set<String> expansions = new HashSet<>();
     static int tweetsPerPage = 15;
     static int retries = 4;
     static AtomicReference<Double> totalSentiment = new AtomicReference<>((double) 0);
     static OffsetDateTime  startTime = OffsetDateTime.now().minusMinutes(0).minusSeconds(30);
     static OffsetDateTime endTime = OffsetDateTime.now().minusSeconds(10);
     static String queryTopic = "eth";
     static int totalReadTweetsSoFar = 0;

    public static String parseTetx(String text){
        String oneLiner = text.replaceAll("\n", "");
        String characterFilter = "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";
        String emotionless = oneLiner.replaceAll(characterFilter,"");
        return emotionless.replaceAll("[\\u205C\\u2066\\u2E1C\\u200D\\u202F\\uD83D\\uFFFD\\uFE0F\\u203C\\u3010\\u3011\\u300A\\u166D\\u200C\\u202A\\u202C\\u2049\\u20E3\\u300B\\u300C\\u3030\\u065F\\u0099\\u0F3A\\u0F3B\\uF610\\uFFFC]", "");
    }

    public static void main(String[] args) throws TwitterException, ApiException, IOException {
        final long startTimeOfProgram = System.currentTimeMillis();
        TaskQueue taskQueue = new TaskQueue(totalSentiment);

        TwitterApi apiInstance = new TwitterApi(new TwitterCredentialsBearer());
        TweetsApi result = apiInstance.tweets();

        int numberOfTweets = result.tweetCountsRecentSearch(queryTopic)
                .startTime(startTime)
                .endTime(endTime)
                .execute(retries)
                .getMeta()
                .getTotalTweetCount();
        System.out.println("Number of tweets to analize ->"+numberOfTweets);


        Get2TweetsSearchRecentResponse page = result.tweetsRecentSearch(queryTopic)
                .startTime(startTime)
                .endTime(endTime)
                .maxResults(tweetsPerPage)
                .execute(retries);
        AtomicReference<String> textToAnalize = new AtomicReference<>("");
        page.getData().stream().forEach(tweet -> textToAnalize.set(textToAnalize.get()+tweet.getText()));
        System.out.println(parseTetx(textToAnalize.get()));
        taskQueue.addTweetToQueue(parseTetx(textToAnalize.get()));
        totalReadTweetsSoFar+=tweetsPerPage;


        String nextPage = page.getMeta().getNextToken();
        while(nextPage != null){
            textToAnalize.set("");
            page = result.tweetsRecentSearch(queryTopic)
                    .startTime(startTime)
                    .endTime(endTime)
                    .paginationToken(nextPage)
                    .maxResults(tweetsPerPage)
                    .execute(retries);
            totalReadTweetsSoFar+=tweetsPerPage;
            nextPage = page.getMeta().getNextToken();
            page.getData().stream().forEach(tweet -> textToAnalize.set(textToAnalize.get()+tweet.getText()));
            taskQueue.addTweetToQueue(parseTetx(textToAnalize.get()));
        }
        System.out.println("------ GOT ALL TWEETS ------");
        taskQueue.executor.shutdown();
        try {
            taskQueue.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        double averageSentiment = totalSentiment.get() / numberOfTweets;
        System.out.println("OVERALL SENTIMENT OF topic:"+ queryTopic +" is ->" + averageSentiment);
        System.out.println("Number of tweets ->" + numberOfTweets);
        final long endTimeOfTheProgram = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTimeOfTheProgram - startTimeOfProgram));

    }
}