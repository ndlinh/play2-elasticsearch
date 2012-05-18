package play.modules.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import play.Logger;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * User: nboire
 * Date: 19/04/12
 */
public class IndexClient {

    /**
     * The client.
     */
    private static org.elasticsearch.client.Client client = null;

    /**
     * Client.
     *
     * @return the client
     */
    public static org.elasticsearch.client.Client client() {
        return client;
    }

    /**
     * Checks if is local mode.
     *
     * @return true, if is local mode
     */
    private boolean isLocalMode() {
        try {
            if (IndexConfig.client == null) {
                return true;
            }
            if (IndexConfig.client.equalsIgnoreCase("false") || IndexConfig.client.equalsIgnoreCase("true")) {
                return true;
            }

            return IndexConfig.local;
        } catch (Exception e) {
            Logger.error("Error! Starting in Local Model: %s", e);
            return true;
        }
    }

    public void start() throws Exception {
        // Start Node Builder
        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
        settings.put("client.transport.sniff", true);
        settings.build();

        // Check Model
        if (this.isLocalMode()) {
            Logger.info("ElasticSearch : Starting in Local Mode");
            NodeBuilder nb = nodeBuilder().settings(settings).local(true).client(false).data(true);
            Node node = nb.node();
            client = node.client();
            Logger.info("ElasticSearch : Started in Local Mode");
        }
        else
        {
            Logger.info("ElasticSearch : Starting in Client Mode");
            TransportClient c = new TransportClient(settings);
            if (IndexConfig.client == null) {
                throw new Exception("Configuration required - elasticsearch.client when local model is disabled!");
            }

            String[] hosts = IndexConfig.client.trim().split(",");
            boolean done = false;
            for (String host : hosts) {
                String[] parts = host.split(":");
                if (parts.length != 2) {
                    throw new Exception("Invalid Host: " + host);
                }
                Logger.info("ElasticSearch : Client - Host: " + parts[0] + " Port: " + parts[1]);
                c.addTransportAddress(new InetSocketTransportAddress(parts[0], Integer.valueOf(parts[1])));
                done = true;
            }
            if (!done) {
                throw new Exception("No Hosts Provided for ElasticSearch!");
            }
            client = c;
            Logger.info("ElasticSearch : Started in Client Mode");
        }

        // Check Client
        if (client == null) {
            throw new Exception("ElasticSearch Client cannot be null - please check the configuration provided and the health of your ElasticSearch instances.");
        }
    }
}