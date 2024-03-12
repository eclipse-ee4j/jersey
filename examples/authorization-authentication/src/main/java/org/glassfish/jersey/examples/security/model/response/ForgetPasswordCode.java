package org.glassfish.jersey.examples.security.model.response;

public class ForgetPasswordCode {

    private String code;


    public ForgetPasswordCode() {
    }

    public ForgetPasswordCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    @Override
    public String toString() {
        return "ForgetPasswordCode{" +
                "code='" + code + '\'' +
                '}';
    }
}
