package edu.clemson.openflow.sos.manager;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TerminationMessageTemplate {

    private String transferID;

    public TerminationMessageTemplate(@JsonProperty("transfer_id") String transferID) {
        this.transferID = transferID;
    }

    public String getTransferID() {
        return transferID;
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
