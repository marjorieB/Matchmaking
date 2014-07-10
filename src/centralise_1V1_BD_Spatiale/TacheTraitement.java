package centralise_1V1_BD_Spatiale;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class TacheTraitement extends Thread {
	private Stack<String> stack = new Stack<String>();
	private Statement st;
	private HashMap<String, String> map = new HashMap<String, String>();
	private ArrayList<ResultSet> res;
	private Connection conn;
	
	public TacheTraitement (Connection conn, ArrayList<ResultSet> res) {
		this.conn = conn;
		this.res = res;
		try {
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void run () {
		String requete;
		ResultSet result = null;
		Statement st_query;
		
		while (true) {
			synchronized(stack) {
				while (stack.isEmpty()) {
					try {
						stack.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				requete = stack.pop();
				if (map.containsKey(requete)) {
					map.remove(requete);
					try {
						st_query = conn.createStatement();
						result = st_query.executeQuery(requete);
					} catch (SQLException e) {
						e.printStackTrace();
					}
					synchronized(res) {
						res.add(result);
						res.notify();
						//System.out.println("notification + size = " + res.size());
					}
				}
				try {
					st.executeUpdate(requete);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void executeUpdate(String req) {
		synchronized(stack) {
			stack.push(req);
			stack.notify();
		}
	}
	
	public int executeQuery (String req) {
		synchronized (stack) {
			stack.push(req);
			map.put(req, req);
			stack.notify();
		}
		return res.size();
	}
}
