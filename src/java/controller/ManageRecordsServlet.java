/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import model.BookingRecord;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ManageRecordsServlet extends HttpServlet {

    Connection con;
    static StringBuffer url;
    static String userDB, passDB;                                               // Username and Password from web.xml
    static String userArg, passArg, query;        
    
    private ArrayList<BookingRecord> brList;
    private HttpSession session;
    private String path;

    private int currentPage;
    private int MAX_RECORDS_PER_PAGE = 10;
    
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);
        try                                                                     
        {
            // Getting the Parameters for the Connection
            Class.forName(config.getInitParameter("jdbcClassName"));
            userDB = config.getInitParameter("dbUserName");
            passDB = config.getInitParameter("dbPassword");

            //StringBuffer is used to make the string changeable
            url = new StringBuffer(config.getInitParameter("jdbcDriverURL"))
                    .append("://")
                    .append(config.getInitParameter("dbHostName"))
                    .append(":")
                    .append(config.getInitParameter("dbPort"))
                    .append("/")
                    .append(config.getInitParameter("databaseName"));
            con = DriverManager.getConnection(url.toString(),userDB,passDB);  
        } 
        catch (SQLException sqle){ } 
        catch (ClassNotFoundException nfe){ }
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            session = request.getSession();
        
            if(con != null){
                try{
                    AccessRecords record = new AccessRecords(con);
                    session = request.getSession();
                    
                    String statusRecords = request.getParameter("status");
                    System.out.println("the status of the records you're trying to get is: " + statusRecords);
                    
                    ResultSet rs = record.showRecords(statusRecords);
                    brList = new ArrayList<>();
                    String room_type = "";
                    String status_type = "";
                    int numberOfRecords = 0;
                    while(rs.next()){
                        switch(rs.getInt("room_id"))
                        {
                            case 1: room_type = "deluxe";
                                    break;                         
                            case 2: room_type = "family";
                                    break;    
                        }
                        switch(rs.getInt("status_id")) 
                        {
                            case 0: status_type = "unconfirmed";
                                    break;                         
                            case 1: status_type = "confirmed";
                                    break;    
                            case 2: status_type = "cancelled";
                                    break;  
                        }
                        brList.add(
                            new BookingRecord(
                            rs.getInt("booking_id"),         
                            rs.getTimestamp("date_booked"),                   
                            rs.getString("name"),             
                            rs.getString("email"),              
                            rs.getString("phone_number"),              
                            rs.getString("country"),                  
                            room_type,
                            rs.getDate("start_booking"),
                            rs.getDate("end_booking"),
                            rs.getInt("number_of_days"),
                            rs.getDouble("cost"),
                            rs.getString("booking_code"),
                            status_type)
                        );
                        numberOfRecords++;
                        System.out.println("the name of this record is " + rs.getString("name"));
                    }

                    session.setAttribute("brList", brList);

                    System.out.println(numberOfRecords);

                    int maxPage = (int) Math.ceil(numberOfRecords / Double.valueOf(MAX_RECORDS_PER_PAGE));
                    session.setAttribute("brList", brList);

                    // Overwrites the search, filter, and sort parameters.
                    /*session.setAttribute("searchView", search);
                    session.setAttribute("filterView", filter);
                    session.setAttribute("sortView", sort);*/
                    session.setAttribute("viewCurrentPageNumber", currentPage + "");
                    session.setAttribute("viewMaxPageNumber", maxPage + "");

                    //redirect based on status records
                    if(((String)request.getParameter("status")).equalsIgnoreCase("unconfirmed")){
                        path = request.getContextPath() + "/HBMS/unconfirmed.jsp";
                    } else{
                        path = request.getContextPath() + "/HBMS/confirmed.jsp";
                    }

                    //allRecordsFromDB.close();
                    //record.close();  

                    response.sendRedirect(path);

                } catch(SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }

    private boolean hasValue(String s) {
        if (s == null || s.trim().equals("") || s.isEmpty()) {
            return false;
        } 
        return true;
    }

    private String getValue(String reqParam, String sessParam, HttpServletRequest request, HttpSession session) {
        if (request.getParameter(reqParam) != null) {
            session.removeAttribute("viewCurrentPageNumber");
            return request.getParameter(reqParam);
        }
        else if (session.getAttribute(sessParam) != null) {
            return (String) session.getAttribute(sessParam);
        } else {
            return null;
        }        
    }

    private int getPageNumber(String changePage, String currentPage, HttpServletRequest request, HttpSession session) {
        if (session.getAttribute(currentPage) == null) {
            return 1;
        } else if (session.getAttribute(currentPage) != null && request.getParameter(changePage) != null) {
            int change = Integer.parseInt(request.getParameter(changePage));
            int currentPageNumber = Integer.parseInt((String)session.getAttribute(currentPage));
            return currentPageNumber + change;
        } else {
            return Integer.parseInt((String)session.getAttribute(currentPage));
        }       
    }

 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
