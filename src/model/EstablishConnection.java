package model;

import java.io.Serializable;

public class EstablishConnection implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private int clientId = 0;
    private final int seqNo = 0;
    private String projectName = "";
    private String projectPassword = "";
    private final int transmissionType = 0;
    private long retransmissionTimeout = 0;

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public void setProjectPassword(String projectPassword) {
        this.projectPassword = projectPassword;
    }

    public int getTransmissionType() {
        return transmissionType;
    }

    public long getRetransmissionTimeout() {
        return retransmissionTimeout;
    }

    public void setRetransmissionTimeout(long retransmissionTimeout) {
        this.retransmissionTimeout = retransmissionTimeout;
    }

    @Override
    public String toString() {
        return new StringBuffer("client_id = ")
                .append(getClientId())
                .append("\n")
                .append("seq_no = ")
                .append(getSeqNo())
                .append("\n")
                .append("project_name = ")
                .append(getProjectName())
                .append("\n")
                .append("project_password = ")
                .append(getProjectPassword())
                .append("\n")
                .append("transmission_type = ")
                .append(getTransmissionType())
                .append("\n")
                .append("retransmission_timeout = ")
                .append(getRetransmissionTimeout())
                .append("\n")
                .toString();
    }
}
