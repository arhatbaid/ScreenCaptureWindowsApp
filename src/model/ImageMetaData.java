package model;

import java.io.Serializable;

public class ImageMetaData implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    private int client_id = 0;
    private final int seq_no = 1;
    private final int transmission_type = 1;
    private String file_name = "text";
    private int file_length = 0;

    public int getClient_id() {
        return client_id;
    }

    public void setClient_id(int client_id) {
        this.client_id = client_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public int getFile_length() {
        return file_length;
    }

    public void setFile_length(int file_length) {
        this.file_length = file_length;
    }

    public int getSeq_no() {
        return seq_no;
    }

    public int getTransmission_type() {
        return transmission_type;
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
                .append("file_name = ")
                .append(file_name)
                .append("\n")
                .append("file_name = ")
                .append(file_name)
                .append("\n")
                .append("\n")
                .append("file_size = ")
                .append(file_length)
                .append("\n")
                .toString();
    }

    public byte[] toByte() {
        byte[] array = new byte[50];
        array[0] = (byte) client_id;
        array[1] = (byte) seq_no;
        array[2] = (byte) transmission_type;
        array[3] = (byte) file_length;

        byte[] arrFileName = file_name.getBytes();

        System.arraycopy(arrFileName, 0, array, 4, arrFileName.length);
        return array;
    }
}
