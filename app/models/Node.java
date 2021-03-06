/**
 * Copyright 2013 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package models;

import com.google.common.collect.Lists;
import lib.APIException;
import lib.ApiClient;
import lib.ServerNodes;
import models.api.responses.NodeSummaryResponse;
import models.api.responses.system.InputSummaryResponse;
import models.api.responses.system.InputsResponse;
import org.joda.time.DateTime;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class Node {

    private static final Random randomGenerator = new Random();

    private final String transportAddress;
    private final DateTime lastSeen;
    private final String nodeId;
    private final String shortNodeId;
    private final String hostname;
    private final boolean isMaster;

    // populate this lazily to avoid blowing tests...(the Configuration class isn't loaded at that point)
    private static Node INITIAL_NODE;

    public Node(NodeSummaryResponse r) {
        transportAddress = r.transportAddress;
        lastSeen = new DateTime(r.lastSeen);
        nodeId = r.nodeId;
        shortNodeId = r.shortNodeId;
        hostname = r.hostname;
        isMaster = r.isMaster;
    }

    public static Node fromId(String id) {
        NodeSummaryResponse response;
        try {
            response = ApiClient.get(NodeSummaryResponse.class)
                    .node(ServerNodes.any())
                    .path("/cluster/nodes/{0}", id)
                    .execute();
        } catch (IOException e) {
            return null;
        } catch (APIException e) {
            return null;
        }

        return new Node(response);
    }

    @Deprecated
    public synchronized static List<Node> all() throws IOException, APIException {
        return ServerNodes.all();
    }

    @Deprecated
    public static Map<String, Node> asMap() throws IOException, APIException {
        return ServerNodes.asMap();
    }

    @Deprecated
    public static Node random() throws IOException, APIException {
        return ServerNodes.any();
    }

    public String getThreadDump() throws IOException, APIException {
        return ApiClient.get(String.class).node(this).path("/system/threaddump").execute();
    }

    public List<Input> getInputs() {
        List<Input> inputs = Lists.newArrayList();

        for (InputSummaryResponse input : inputs().inputs) {
            inputs.add(new Input(input));
        }

        return inputs;
    }

    public Input getInput(String inputId) throws IOException, APIException {
        final InputSummaryResponse inputSummaryResponse = ApiClient.get(InputSummaryResponse.class).node(this).path("/system/inputs/{0}", inputId).execute();
        return new Input(inputSummaryResponse);
    }

    public int numberOfInputs() {
        return inputs().total;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public DateTime getLastSeen() {
        return lastSeen;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getShortNodeId() {
        return shortNodeId;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isMaster() {
        return isMaster;
    }

    /**
     * This swallows all exceptions to allow easy lazy-loading in views without exception handling.
     *
     * @return List of running inputs o this node.
     */
    private InputsResponse inputs()  {
        try {
            return ApiClient.get(InputsResponse.class).node(this).path("/system/inputs").execute();
        } catch (Exception e) {
            Logger.error("Could not get inputs.", e);
            throw new RuntimeException("Could not get inputs.", e);
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "nodeId='" + nodeId + '\'' +
                ", transportAddress='" + transportAddress + '\'' +
                ", isMaster=" + isMaster +
                '}';
    }
}
