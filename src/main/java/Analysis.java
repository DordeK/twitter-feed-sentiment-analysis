
public class Analysis extends Thread {
    private String tweetTetx;
    NLP nlp;

    public Analysis( String tweetText, NLP nlps) {
        this.tweetTetx = tweetText;
        this.nlp = nlp;
    }

    @Override
    public void run() {
        nlp.computeSentiment(this.tweetTetx);
    }
}
