public enum CommunicationCodeEnum {
    FILE_NAME(0),
    FILE_DATA(1),
    VERIFY(3),
    REQUEST_CERT(4),
    END_COMM(10);

    private int code;

    private CommunicationCodeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }


}
