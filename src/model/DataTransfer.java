package model;

import java.io.Serializable;
import java.util.Arrays;

public class DataTransfer implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private int client_id = 0;
    private int seq_no = 0;
    private final int transmission_type = 3;
    private boolean is_last_packet = false;
    private byte[] arrImage = new byte[65000];

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public int getSeq_no() {
        return seq_no;
    }

    public void setSeq_no(int seq_no) {
        this.seq_no = seq_no;
    }

    public boolean getIs_last_packet() {
        return is_last_packet;
    }

    public void setIs_last_packet(boolean is_last_packet) {
        this.is_last_packet = is_last_packet;
    }

    public int getTransmission_type() {
        return transmission_type;
    }

    public byte[] getArrImage() {
        return arrImage;
    }

    public void setArrImage(byte[] arrImage) {
        this.arrImage = arrImage;
    }

    @Override
    public String toString() {
        return new StringBuffer("client_id = ")
                .append(client_id)
                .append("\n")
                .append("seq_no = ")
                .append(seq_no)
                .append("\n")
                .append("transmission_type = ")
                .append(transmission_type)
                .append("\n")
                .append("is_last_packet = ")
                .append(is_last_packet)
                .append("\n")
                .append(Arrays.toString(arrImage))
                .append("\n")
                .toString();
    }
}
