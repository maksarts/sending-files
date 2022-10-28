package service.sendarchives.types;

import java.util.ArrayList;

public class CompanyInfo {
    protected String name;
    protected String code;
    protected String password;
    protected ArrayList<String> emailList;
    protected ArrayList<String> emailListCopy;
    protected ArrayList<String> emailListError;

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String value) {
        this.code = value;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String value) {
        this.password = value;
    }

    public ArrayList<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(ArrayList<String> values){
        emailList = new ArrayList<String>();
        emailList.addAll(values);
    }

    public ArrayList<String> getEmailListCopy() {
        return emailListCopy;
    }

    public void setEmailListCopy(ArrayList<String> values){
        emailListCopy = new ArrayList<String>();
        emailListCopy.addAll(values);
    }

    public ArrayList<String> getEmailListError() {
        return emailListError;
    }

    public void setEmailListError(ArrayList<String> values){
        emailListError = new ArrayList<String>();
        emailListError.addAll(values);
    }

}
