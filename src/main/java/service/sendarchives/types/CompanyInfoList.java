package service.sendarchives.types;

import java.util.ArrayList;

public class CompanyInfoList {
    private ArrayList<CompanyInfo> companyInfoList;

    public CompanyInfoList(){
        companyInfoList = new ArrayList<CompanyInfo>();
    }

    public ArrayList<CompanyInfo> getCompanyInfoList() {
        return companyInfoList;
    }

    public void setCompanyInfoList(ArrayList<CompanyInfo> values){
        companyInfoList = new ArrayList<CompanyInfo>();
        companyInfoList.addAll(values);
    }

    public void add(CompanyInfo item){
        companyInfoList.add(item);
    }
}
