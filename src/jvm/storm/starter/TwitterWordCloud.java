// to use this example, uncomment the twitter4j dependency information in the project.clj,
// uncomment storm.starter.spout.TwitterSampleSpout, and uncomment this class

package storm.starter;

import storm.starter.spout.TwitterSampleSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import storm.starter.bolt.PrinterBolt;
import storm.starter.bolt.RollingCountBolt;
import storm.starter.bolt.IntermediateRankingsBolt;
import storm.starter.bolt.TotalRankingsBolt;


public class TwitterWordCloud {        
    private static final int TOP_N = 40;
    public static final int WINDOW_SIZE = 300;
    public static void main(String[] args) {
        String username = args[0];
        String pwd = args[1];
        TopologyBuilder builder = new TopologyBuilder();

        String intermediateRankerId = "intermediateRanker";
        String totalRankerId = "finalRanker";
        
        builder.setSpout("spout", new TwitterSampleSpout(username, pwd));
        builder.setBolt("counter", new RollingCountBolt(WINDOW_SIZE, 10), 4).fieldsGrouping("spout", new Fields("word"));
        builder.setBolt(intermediateRankerId, new IntermediateRankingsBolt(TOP_N), 4).fieldsGrouping("counter",
            new Fields("obj"));
        builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N)).globalGrouping(intermediateRankerId);
        
        Config conf = new Config();
        
        LocalCluster cluster = new LocalCluster();
        
        cluster.submitTopology("test", conf, builder.createTopology());
        
        //Utils.sleep(6000000);
        //cluster.shutdown();
    }
}
