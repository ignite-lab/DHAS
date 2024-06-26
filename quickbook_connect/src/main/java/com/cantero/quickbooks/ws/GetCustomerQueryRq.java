package com.cantero.quickbooks.ws;

import java.util.ArrayList;

import javax.jws.WebService;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import com.opencsv.CSVWriter;

/*
 * http://developer.intuit.com/qbsdk-current/doc/pdf/qbwc_proguide.pdf
 */
@SuppressWarnings("unused")
@WebService(endpointInterface = "com.cantero.quickbooks.ws.QBWebConnectorSvcSoap")
public class GetCustomerQueryRq implements QBWebConnectorSvcSoap {


    @Override
    public ArrayOfString authenticate(String strUserName, String strPassword) {
        ArrayOfString arr = new ArrayOfString();
        arr.string = new ArrayList<String>();
        arr.string.add("The first element is a token for the web connector�s session");
        arr.string.add(""); //To use the currently open company, specify an empty string
        return arr;
    }

    @Override
    public String closeConnection(String ticket) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String connectionError(String ticket, String hresult, String message) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLastError(String ticket) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return A positive integer less than 100 represents the percentage of work completed. A value of 1 means one percent complete, a value of 100 means 100 percent complete--there is no more work. A negative value means an error has occurred and the Web Connector responds to this with a getLastError call. The negative value could be used as a custom error code.
     */
    @Override
    public int receiveResponseXML(String ticket, String response, String hresult, String message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(response));
            Document document = builder.parse(is);

            NodeList customerList = document.getElementsByTagName("CustomerRet");
            try (CSVWriter writer = new CSVWriter(new FileWriter("CSV/READ/accounts.csv"))) {
                writer.writeNext(new String[]{"CustomerCode", "CreditLimit", "CreditTerm"});
                for (int i = 0; i < customerList.getLength(); i++) {
                    Element item = (Element) customerList.item(i);
                    String listID = item.getElementsByTagName("ListID").item(0).getTextContent();
                    // String[] parts = listID.split("-");
                    // String customerCode = parts[1];
                    String creditLimit = "0";
                    NodeList creditLimitTag = item.getElementsByTagName("CreditLimit");
                    if (creditLimitTag.getLength() > 0) {
                        creditLimit = item.getElementsByTagName("CreditLimit").item(0).getTextContent();
                    }
                    String termsFullName = "";
                    NodeList termsRefList = item.getElementsByTagName("TermsRef");
                    if (termsRefList.getLength() > 0) {
                        Element termsRef = (Element) termsRefList.item(0);
                        termsFullName = termsRef.getElementsByTagName("FullName").item(0).getTextContent();
                    }
                    writer.writeNext(new String[]{listID, creditLimit, termsFullName});
                }
                System.out.println("accounts.csv file created successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return 100;
    }


    @Override
    public String sendRequestXML(String ticket, String strHCPResponse,
                                 String strCompanyFileName, String qbXMLCountry, int qbXMLMajorVers,
                                 int qbXMLMinorVers) {
        //Example qbXML to Query for an Item
        //http://www.consolibyte.com/wiki/doku.php/quickbooks_qbxml_itemquery
        String query = "<?xml version=\"1.0\" encoding=\"utf-8\"?><?qbxml version=\"7.0\"?><QBXML><QBXMLMsgsRq onError=\"stopOnError\"><CustomerQueryRq requestID=\"1\"></CustomerQueryRq ></QBXMLMsgsRq></QBXML>";
        return query;
    }

}
