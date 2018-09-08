package edu.clemson.openflow.sos.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TerminationMessageTemplate {

    private String transferID;
    private String overHead;
    private String avgSentBytes;
    private String stdSentBytes;
    private String avgChunks;
    private String std_chunks;
    private String type;
    private String client;


    public TerminationMessageTemplate(@JsonProperty("transfer_id") String transferID) {
        this(transferID, null, null,
                null, null,
                null, null, null);
    }
    public TerminationMessageTemplate(@JsonProperty("transfer_id") String transferID,
                                      @JsonProperty("overhead") String overHead,
                                      @JsonProperty("avg_send_bytes") String avgSentBytes,
                                      @JsonProperty("std_sent_bytes") String stdSentBytes,
                                      @JsonProperty("avg_chunk") String avgChunks,
                                      @JsonProperty("std_chunks") String std_chunks,
                                      @JsonProperty("type") String type,
                                      @JsonProperty("client") String client) {
        this.transferID = transferID;
        this.overHead = overHead;
        this.avgSentBytes = avgSentBytes;
        this.stdSentBytes = stdSentBytes;
        this.avgChunks = avgChunks;
        this.std_chunks = std_chunks;
        this.type = type;
        this.client = client;
    }

    public String getTransferID() {
        return transferID;
    }

    public String getOverHead() {
        return overHead;
    }

    public String getAvgSentBytes() {
        return avgSentBytes;
    }

    public String getStdSentBytes() {
        return stdSentBytes;
    }

    public String getAvgChunks() {
        return avgChunks;
    }

    public String getStd_chunks() {
        return std_chunks;
    }

    public String getType() {
        return type;
    }

    public String getClient() {
        return client;
    }
    /*
       snprintf(buffer, 500,
             "{ \"type\" : \"%s\", \"transfer_id\" : \"%s\", \"overhead\" : "
             "%lu, \"avg_sent_bytes\" : %lu, \"std_sent_bytes\" : %lu, "
             "\"avg_chunks\" : %lu, \"std_chunks\" : %lu }",
             client->transfer_request->type, uuid_msg, info.overhead,
             info.avg_sent_bytes, info.std_sent_bytes, info.avg_chunks,
             info.std_chunks);
     */
}
