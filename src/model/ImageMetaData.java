package model;

import java.io.Serializable;
import java.util.Arrays;

public class ImageMetaData implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private int clientId = 0;
    private final int seqNo = 1;
    private final int transmissionType = 1;
    private ImageChunksMetaData[] arrImageChunks = null;

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public int getTransmissionType() {
        return transmissionType;
    }

    public ImageChunksMetaData[] getArrImageChunks() {
        return arrImageChunks;
    }

    public void setArrImageChunks(ImageChunksMetaData[] arrImageChunks) {
        this.arrImageChunks = arrImageChunks;
    }

    @Override
    public String toString() {
        return new StringBuffer("client_id = ")
                .append(getClientId())
                .append("\n")
                .append("seq_no = ")
                .append(getSeqNo())
                .append("\n")
                .append("transmission_type = ")
                .append(getTransmissionType())
                .append("\n")
                .append("file_name = ")
                .append(Arrays.toString(getArrImageChunks()))
                .toString();
    }
}
