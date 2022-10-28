package service.sendarchives.rest;

public class RequestDate {
    String code;
    String date;
    String endDate;

    public String getCode(){ return code; }
    public void setCode(String code_){
        this.code = code_;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date_){
        this.date = date_;
    }
    public String getEndDate() { return endDate; }
    public void setEndDate(String date_){ this.endDate = date_; }
}
