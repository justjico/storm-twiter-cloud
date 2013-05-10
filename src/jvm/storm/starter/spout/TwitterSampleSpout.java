package storm.starter.spout;

import backtype.storm.Config;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TwitterSampleSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    LinkedBlockingQueue<String> queue = null;
    TwitterStream _twitterStream;
    String _username;
    String _pwd;
    
    
    public TwitterSampleSpout(String username, String pwd) {
        _username = username;
        _pwd = pwd;
    }
    
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        queue = new LinkedBlockingQueue<String>(10000);
        _collector = collector;
        StatusListener listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                String[] words = status.getText().split(" ");
                String[] stopWrds={"i", "a", "about","an","are","as","at","be","by","com","for","from","how","in", "is","it",
                "of","on","or","that","the","this","to","was","what","when","where","who","will","with"};
                List<String> l = Arrays.asList(stopWrds);
                for( int i = 0; i < words.length ; i++)
                  {
                    if( words[i].length() > 4 && !l.contains(words[i]))
                      queue.offer(words[i]);
                  }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
            }

            @Override
            public void onTrackLimitationNotice(int i) {
            }

            @Override
            public void onScrubGeo(long l, long l1) {
            }

            @Override
            public void onException(Exception e) {
            }
            
        };
        TwitterStreamFactory fact = new TwitterStreamFactory(new ConfigurationBuilder().setUser(_username).setPassword(_pwd).build());
        _twitterStream = fact.getInstance();
        _twitterStream.addListener(listener);
        _twitterStream.sample();
    }

    @Override
    public void nextTuple() {
        String ret = queue.poll();
        if(ret==null) {
            Utils.sleep(50);
        } else {
            _collector.emit(new Values(ret));
        }
    }

    @Override
    public void close() {
        _twitterStream.shutdown();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }    

    @Override
    public void ack(Object id) {
    }

    @Override
    public void fail(Object id) {
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }
    
}
