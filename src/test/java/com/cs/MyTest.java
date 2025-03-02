package com.cs;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MyTest {
    String configFilePath = "src/test/resources/TestDataConfig.json";
    ExcelUtils excelUtils = ExcelUtils.loadConfig(configFilePath);

    @DataProvider(name = "CloneData")
    public Object[][] getTestData() {
        return excelUtils.getTable();
    }

    @Test(dataProvider = "CloneData")
    public void myTest(String testNo, String clientSearchName, String clientSearchIndexNo, String engYearSearchText,
                       String engNameSearchText, String engNameSearchIndexNo, String cloneEngYearText) {
        System.out.println("Inside data provider");
    }

    @AfterMethod
    public void updateResult(ITestResult result) {
        String status = result.isSuccess() ? "PASS" : "FAIL";
        int rowNum = Integer.parseInt(result.getParameters()[0].toString());
        int colNum = excelUtils.getColumnPosition("STATUS");
        excelUtils.writeExecutionStatus(excelUtils.getExcelSheetName(), rowNum, colNum, status);
    }
}