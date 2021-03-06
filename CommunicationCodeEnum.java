public enum CommunicationCodeEnum {
    FILE_NAME(0),
    FILE_DATA(1),
    VERIFY(3),
    REQUEST_CERT(4),
    SHARE_SECRET_KEY(5),
    END_COMM(10),
    FILE_DONE(11);

    private int code;

    private CommunicationCodeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }


}
