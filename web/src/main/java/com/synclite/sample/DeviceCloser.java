package com.synclite.sample;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.synclite.logger.*;
/**
 * Servlet implementation class DeviceCloser
 */
@WebServlet("/deviceCloser")
public class DeviceCloser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeviceCloser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doGet(request, response);
			SQLite.closeAllDevices();
			SQLiteAppender.closeAllDevices();

			DuckDB.closeAllDevices();
			DuckDBAppender.closeAllDevices();
			
			Derby.closeAllDevices();
			DerbyAppender.closeAllDevices();
			
			H2.closeAllDevices();
			H2Appender.closeAllDevices();
			
			HyperSQL.closeAllDevices();
			HyperSQLAppender.closeAllDevices();
			
			Telemetry.closeAllDevices();
			
			Streaming.closeAllDevices();
			request.getRequestDispatcher("stopDevices.jsp?closeStatus=SUCCESS&closeStatusDetails=;").forward(request, response);
		} catch (Exception e) {
			//		request.setAttribute("saveStatus", "FAIL");
			System.out.println("exception : " + e);
			String errorMsg = e.getMessage() + e.getStackTrace();
			request.getRequestDispatcher("closeDevices.jsp?closeStatus=FAIL&closeStatusDetails=" + errorMsg + ";").forward(request, response);
		}
	}

}
