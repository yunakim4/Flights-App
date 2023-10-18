package flightapp;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;

import javax.print.attribute.standard.Chromaticity;

/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  //
  // Canned queries
  //
  private static final String FLIGHT_CAPACITY_SQL = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement flightCapacityStmt;


  private static final String CLEAR_TABLES1 = "DELETE FROM RESERVATIONS_ykimm4";
  private PreparedStatement clearTables1;

  private static final String CLEAR_TABLES2 = "DELETE FROM USERS_ykimm4";
  private PreparedStatement clearTables2;
 
  private static final String USER_EXISTS = "SELECT * FROM USERS_ykimm4 WHERE username = ?";
  private PreparedStatement userExists; 
 
  private static final String CREATE = "INSERT INTO USERS_ykimm4 VALUES(?, ?, ?)";
  private PreparedStatement create; 

  private static final String FIND_USERNAME = "SELECT * FROM USERS_ykimm4 WHERE username = ?";
  private PreparedStatement findUsername; 



  private static final String safeSearchDirectSQL = "SELECT TOP (?) fid,day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price "
  + "FROM Flights " + "WHERE origin_city = ? AND dest_city = ? AND day_of_month = ? AND canceled = 0 ORDER BY actual_time ASC";
  private PreparedStatement safeSearchDirect;

  private static final String safeSearchIndirectSQL = "SELECT TOP (?) F1.fid as fidF1,F1.day_of_month as day_of_monthF1,F1.carrier_id as carrier_idF1,F1.flight_num as flight_numF1,F1.origin_city as origin_cityF1,F1.dest_city as dest_cityF1,F1.actual_time as actual_timeF1,F1.capacity as capacityF1,F1.price as priceF1,"
  + "F2.fid as fidF2,F2.day_of_month as day_of_monthF2,F2.carrier_id as carrier_idF2,F2.flight_num as flight_numF2,F2.origin_city as origin_cityF2,F2.dest_city as dest_cityF2,F2.actual_time actual_timeF2,F2.capacity as capacityF2,F2.price as priceF2"
  + " FROM Flights as F1, Flights as F2 "
	+ "WHERE F1.origin_city = ? AND F1.dest_city = F2.origin_city AND F2.dest_city = ? AND F1.day_of_month = ? "
	+ "AND F1.day_of_month = F2.day_of_month AND F1.canceled = 0 AND F2.canceled = 0 "
  + "ORDER BY F1.actual_time + F2.actual_time ASC, F1.fid ASC, F2.fid ASC";
  private PreparedStatement safeSearchIndirect;


  private static final String GET_MONTH = "SELECT COUNT(*) AS count FROM RESERVATIONS_ykimm4 AS RY, FLIGHTS AS F WHERE RY.rusername = ? "
  +"AND RY.rFid = F.fid AND F.day_of_month = ?";
  private PreparedStatement getMonth;

  private static final String CHECK_CAPACITY = "SELECT COUNT(*) AS count FROM RESERVATIONS_ykimm4 WHERE rFid = ? "
  + "OR rFid2 = ?";
  private PreparedStatement checkCapacity;


  private static final String GET_RID = "SELECT COUNT(*) AS count FROM RESERVATIONS_ykimm4";
  private PreparedStatement getRID;

  private static final String INSERT_RES = "INSERT INTO RESERVATIONS_ykimm4 VALUES(?, ?, ?, ?, ?)";
  private PreparedStatement insertRes;

  private static final String CHECK_USER_EXISTS =  "SELECT rFid, rFid2 FROM RESERVATIONS_ykimm4 WHERE rid = ? AND is_paid = 0";
  private PreparedStatement checkUserExists;

  private static final String CHECK_USER_RES_EXISTS_INDIRECT = "SELECT COUNT(*) AS count FROM RESERVATIONS_ykimm4 WHERE rid = ? AND rusername = ? AND rFid2 != NULL AND is_paid = 0";
  private PreparedStatement checkUserResExistsIndirect;

  private static final String CHECK_USER_BALANCE = "SELECT balance FROM USERS_ykimm4 AS U"
  + " WHERE U.username = ?";
  private PreparedStatement checkUserBalance; 


  private static final String CHECK_FLIGHT_COST = "SELECT price FROM FLIGHTS WHERE fid = ?";
  private PreparedStatement checkFlightCost;

  private static final String UPDATE_PAY = "UPDATE RESERVATIONS_ykimm4 SET is_paid = 1 WHERE rid = ?";
  private PreparedStatement updatePay;

  private static final String UPDATE_USER_BALANCE = "UPDATE USERS_ykimm4 SET balance = ? WHERE username = ?";
  private PreparedStatement updateBalance;

  private static final String USER_RES_EXISTS = "SELECT * FROM RESERVATIONS_ykimm4 WHERE rusername = ?";
  private PreparedStatement userResExists;


  private static final String GET_FLIGHT_INFO = "SELECT fid, day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price "
  + "FROM FLIGHTS WHERE fid = ?";
  private PreparedStatement getFlightInfo;


  //
  // Instance variables
  //
  private boolean loggedIn;
  private ArrayList<Itinerary> itList;
  private String user; 
  protected Query() throws SQLException, IOException {
    this.loggedIn = false;
    this.itList = new ArrayList<Itinerary>();
    this.user = "";
    prepareStatements();
  }

  /**
   * Clear the data in any custom tables created.
   * 
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() {
    try {
      // TODO: YOUR CODE HERE
      clearTables1.executeUpdate();
      clearTables2.executeUpdate();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    // TODO: YOUR CODE HERE
    flightCapacityStmt = conn.prepareStatement(FLIGHT_CAPACITY_SQL);
    clearTables1 = conn.prepareStatement(CLEAR_TABLES1);
    clearTables2 = conn.prepareStatement(CLEAR_TABLES2);
    userExists = conn.prepareStatement(USER_EXISTS);
    create = conn.prepareStatement(CREATE);
    findUsername = conn.prepareStatement(FIND_USERNAME);
    
    safeSearchDirect = conn.prepareStatement(safeSearchDirectSQL);
    safeSearchIndirect = conn.prepareStatement(safeSearchIndirectSQL);
   
    //book method
    getMonth = conn.prepareStatement(GET_MONTH);
    checkCapacity = conn.prepareStatement(CHECK_CAPACITY);
    getRID = conn.prepareStatement(GET_RID);
    insertRes = conn.prepareStatement(INSERT_RES);

    //pay method
    checkUserResExistsIndirect = conn.prepareStatement(CHECK_USER_RES_EXISTS_INDIRECT);
    checkUserBalance = conn.prepareStatement(CHECK_USER_BALANCE);
    checkFlightCost = conn.prepareStatement(CHECK_FLIGHT_COST);
    updatePay = conn.prepareStatement(UPDATE_PAY);
    updateBalance = conn.prepareStatement(UPDATE_USER_BALANCE);
    checkUserExists = conn.prepareStatement(CHECK_USER_EXISTS);

    //reserve method
    userResExists = conn.prepareStatement(USER_RES_EXISTS);
    getFlightInfo = conn.prepareStatement(GET_FLIGHT_INFO);
  

  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username user's username
   * @param password user's password
   *
   * @return If someone has already logged in, then return "User already logged in\n".  For all
   *         other errors, return "Login failed\n". Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password) {
   
    // TODO: YOUR CODE HERE
    if(this.loggedIn == true) {
      return "User already logged in\n";
    }

    try {

    findUsername.setString(1, username);
    ResultSet result = findUsername.executeQuery();
    if(!result.next()) {
      result.close();
      return "Login failed\n";
    }
    byte[] correctPassword = result.getBytes("password");
    result.close();
    if(PasswordUtils.plaintextMatchesSaltedHash(password, correctPassword)) {
      String status = "Logged in as " + username + "\n";
      this.loggedIn = true;
      this.user = username;
      return status; 
    }
    
 

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return "Login failed\n";
  
  }

  /**
   * Implement the create user function.
   *
   * @param username   new user's username. User names are unique the system.
   * @param password   new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure
   *                   otherwise).
   *
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    
    // TODO: YOUR CODE HERE
    if( initAmount < 0) {
      return "Failed to create user\n";
    }

    try {
    userExists.setString(1, username);
    ResultSet existingUsers = userExists.executeQuery();
    if(existingUsers.next()) {
     return "Failed to create user\n";
    }
   existingUsers.close();
    create.setString(1, username);
    byte[] updatedPassword = PasswordUtils.saltAndHashPassword(password);
    create.setBytes(2, updatedPassword);
    create.setInt(3, initAmount);
    create.executeUpdate();
    return "Created user " + username + "\n";

    }

    catch(Exception e ) {
      e.printStackTrace();
    }

    return "Failed to create user\n";
    }
    
   
   
 

  /**
   * Implement the search function.
   *
   * Searches for flights from the given origin city to the given destination city, on the given
   * day of the month. If {@code directFlight} is true, it only searches for direct flights,
   * otherwise is searches for direct flights and flights with two "hops." Only searches for up
   * to the number of itineraries given by {@code numberOfItineraries}.
   *
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight        if true, then only search for direct flights, otherwise include
   *                            indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return, must be positive
   *
   * @return If no itineraries were found, return "No flights match your selection\n". If an error
   *         occurs, then return "Failed to search\n".
   *
   *         Otherwise, the sorted itineraries printed in the following format:
   *
   *         Itinerary [itinerary number]: [number of flights] flight(s), [total flight time]
   *         minutes\n [first flight in itinerary]\n ... [last flight in itinerary]\n
   *
   *         Each flight should be printed using the same format as in the {@code Flight} class.
   *         Itinerary numbers in each search should always start from 0 and increase by 1.
   *
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, 
                                   boolean directFlight, int dayOfMonth,
                                   int numberOfItineraries) {
    // WARNING: the below code is insecure (it's susceptible to SQL injection attacks) AND only
    // handles searches for direct flights.  We are providing it *only* as an example of how
    // to use JDBC; you are required to replace it with your own secure implementation.
    //
    // TODO: YOUR CODE HERE

   
   StringBuffer sb = new StringBuffer();

    try {
      itList.clear();
      // one hop itineraries
      safeSearchDirect.setInt(1, numberOfItineraries);
      safeSearchDirect.setString(2, originCity);
      safeSearchDirect.setString(3, destinationCity);
      safeSearchDirect.setInt(4, dayOfMonth);
      ResultSet oneHopResults = safeSearchDirect.executeQuery();
      
  
      while(oneHopResults.next() && itList.size() < numberOfItineraries) {
        int result_flightID = oneHopResults.getInt("fid");
        int result_dayOfMonth = oneHopResults.getInt("day_of_month");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        String result_originCity = oneHopResults.getString("origin_city");
        String result_destCity = oneHopResults.getString("dest_city");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");
        Flight flight = new Flight(result_flightID, result_dayOfMonth, result_carrierId, result_flightNum,result_originCity, result_destCity, result_time, result_capacity, result_price);
        itList.add(new Itinerary(flight));
      }
      oneHopResults.close();
      if(itList.size() != numberOfItineraries && !directFlight ) {

        safeSearchIndirect.setInt(1, numberOfItineraries - itList.size());
        safeSearchIndirect.setString(2, originCity);
        safeSearchIndirect.setString(3, destinationCity);
        safeSearchIndirect.setInt(4, dayOfMonth);
        ResultSet twoHopResults = safeSearchIndirect.executeQuery();


        while(twoHopResults.next() && itList.size() < numberOfItineraries) {
          int result_flightID1 = twoHopResults.getInt("fidF1");
          int result_dayOfMonth1 = twoHopResults.getInt("day_of_monthF1");
          String result_carrierId1 = twoHopResults.getString("carrier_idF1");
          String result_flightNum1 = twoHopResults.getString("flight_numF1");
          String result_originCity1 = twoHopResults.getString("origin_cityF1");
          String result_destCity1 = twoHopResults.getString("dest_cityF1");
          int result_time1 = twoHopResults.getInt("actual_timeF1");
          int result_capacity1 = twoHopResults.getInt("capacityF1");
          int result_price1 = twoHopResults.getInt("priceF1");
          Flight f1 = new Flight(result_flightID1, result_dayOfMonth1, result_carrierId1, result_flightNum1,result_originCity1,result_destCity1, result_time1,result_capacity1, result_price1);
  
          int result_flightID2 = twoHopResults.getInt("fidF2");
          int result_dayOfMonth2 = twoHopResults.getInt("day_of_monthF2");
          String result_carrierId2 = twoHopResults.getString("carrier_idF2");
          String result_flightNum2 = twoHopResults.getString("flight_numF2");
          String result_originCity2 = twoHopResults.getString("origin_cityF2");
          String result_destCity2 = twoHopResults.getString("dest_cityF2");
          int result_time2 = twoHopResults.getInt("actual_timeF2");
          int result_capacity2 = twoHopResults.getInt("capacityF2");
          int result_price2 = twoHopResults.getInt("priceF2");
          Flight f2 = new Flight(result_flightID2,result_dayOfMonth2, result_carrierId2, result_flightNum2, result_originCity2, result_destCity2, result_time2, result_capacity2,result_price2);

          itList.add(new Itinerary(f1, f2));
      
      }
      twoHopResults.close();
    }

      if(this.itList.size() == 0) {
        return "No flights match your selection\n";
     }

      Collections.sort(itList);

      for(int i = 0; i < this.itList.size(); i++) {
        if (itList.get(i).f2 == null) {
          sb.append("Itinerary " + i + ": " + 1 + " flight(s), " + itList.get(i).travelTime + " minutes\n");
          sb.append("ID: " + itList.get(i).f1.fid 
          + " Day: " + itList.get(i).f1.dayOfMonth 
          + " Carrier: " + itList.get(i).f1.carrierId 
          + " Number: " + itList.get(i).f1.flightNum 
          + " Origin: " + itList.get(i).f1.originCity 
          + " Dest: " + itList.get(i).f1.destCity 
          + " Duration: " + itList.get(i).f1.time 
          + " Capacity: " + itList.get(i).f1.capacity 
          + " Price: " + itList.get(i).f1.price + "\n");
        } else {
          sb.append("Itinerary " + i + ": " + 2 + " flight(s), " + itList.get(i).travelTime + " minutes\n");
          sb.append("ID: " + itList.get(i).f1.fid 
          + " Day: " + itList.get(i).f1.dayOfMonth 
          + " Carrier: " + itList.get(i).f1.carrierId 
          + " Number: " + itList.get(i).f1.flightNum 
          + " Origin: " + itList.get(i).f1.originCity 
          + " Dest: " + itList.get(i).f1.destCity 
          + " Duration: " + itList.get(i).f1.time 
          + " Capacity: " + itList.get(i).f1.capacity 
          + " Price: " + itList.get(i).f1.price + "\n");
          sb.append("ID: " + itList.get(i).f2.fid 
          + " Day: " + itList.get(i).f2.dayOfMonth 
          + " Carrier: " + itList.get(i).f2.carrierId 
          + " Number: " + itList.get(i).f2.flightNum 
          + " Origin: " + itList.get(i).f2.originCity 
          + " Dest: " + itList.get(i).f2.destCity 
          + " Duration: " + itList.get(i).f2.time 
          + " Capacity: " + itList.get(i).f2.capacity 
          + " Price: " + itList.get(i).f2.price + "\n");
         

        }

      }
      return sb.toString();
    }
    catch (SQLException e) {
      e.printStackTrace();
      return "Failed to search\n";
    }
  }


  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search
   *                    in the current session.
   *
   * @return If the user is not logged in, then return "Cannot book reservations, not logged
   *         in\n". If the user is trying to book an itinerary with an invalid ID or without
   *         having done a search, then return "No such itinerary {@code itineraryId}\n". If the
   *         user already has a reservation on the same day as the one that they are trying to
   *         book now, then return "You cannot book two flights in the same day\n". For all
   *         other errors, return "Booking failed\n".
   *
   *         If booking succeeds, return "Booked flight(s), reservation ID: [reservationId]\n"
   *         where reservationId is a unique number in the reservation system that starts from
   *         1 and increments by 1 each time a successful reservation is made by any user in
   *         the system.
   */
  public String transaction_book(int itineraryId) {
  if(!this.loggedIn) {
    return "Cannot book reservations, not logged in\n";
  }
  if(this.itList == null || this.itList.size() <= itineraryId) {
    return "No such itinerary " + itineraryId + "\n";
  }
  boolean trans = false;
  try {
    
    conn.setAutoCommit(false);
    trans = true;
    getMonth.setString(1, this.user);
    getMonth.setInt(2, itList.get(itineraryId).f1.dayOfMonth);
    ResultSet checkMonth = getMonth.executeQuery();
    checkMonth.next();
    int count = checkMonth.getInt("count");
    
    if(count != 0) {
      conn.rollback();
      conn.setAutoCommit(true);
      return "You cannot book two flights in the same day\n";
    }

    //check capacity direct flight 
    checkCapacity.setInt(1, itList.get(itineraryId).f1.fid);
    checkCapacity.setInt(2, itList.get(itineraryId).f1.fid);
    ResultSet capD = checkCapacity.executeQuery();
    

    capD.next();
    int capCount = capD.getInt("count");
   // System.out.println("Count was: " + capCount + " Flight Cap: " + this.itList.get(itineraryId).f1.capacity);
    if(capCount >= this.itList.get(itineraryId).f1.capacity) {
      conn.rollback();
      conn.setAutoCommit(true);
      return "Booking failed\n";
    }


    //check capacity indirect flight  
    if(itList.get(itineraryId).isDirect == false) {
      checkCapacity.clearParameters();
      checkCapacity.setInt(1, itList.get(itineraryId + 1).f2.fid);
      checkCapacity.setInt(2, itList.get(itineraryId + 1).f2.fid);
      ResultSet capI = checkCapacity.executeQuery();

      capI.next();
      int capCountIndirect = capI.getInt("count");
     // System.out.println("Count was: " + capCountIndirect+ " Flight Cap: " + this.itList.get(itineraryId + 1).f2.capacity);
      if(capCountIndirect >= itList.get(itineraryId + 1).f2.capacity) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Booking failed\n";
      }
    }

    getRID.clearParameters();
    ResultSet rids = getRID.executeQuery();
    rids.next();

    int rid = rids.getInt("count") + 1;
    insertRes.setInt(1, rid);
    insertRes.setString(2, this.user);
    insertRes.setInt(3, this.itList.get(itineraryId).f1.fid);
    if(itList.get(itineraryId).isDirect == false) {
       insertRes.setInt(4, this.itList.get(itineraryId).f2.fid);
    }
    else {
      insertRes.setObject(4, null);;
    }
    insertRes.setInt(5, 0);
    
    insertRes.executeUpdate();

    conn.commit();
    conn.setAutoCommit(true);
    return "Booked flight(s), reservation ID: " + rid + "\n";

  }
  catch(Exception e) {
    try {
    if(trans) {
    conn.rollback();
    conn.setAutoCommit(true);
    }
    }
    catch(Exception e2) {
      return "Booking failed\n";
    }
    e.printStackTrace();
    if (isDeadlock((SQLException) e))
        return transaction_book(itineraryId);
    } 
    return "Booking failed\n";
}

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   *
   * @return If no user has logged in, then return "Cannot pay, not logged in\n". If the
   *         reservation is not found / not under the logged in user's name, then return
   *         "Cannot find unpaid reservation [reservationId] under user: [username]\n".  If
   *         the user does not have enough money in their account, then return
   *         "User has only [balance] in account but itinerary costs [cost]\n".  For all other
   *         errors, return "Failed to pay for reservation [reservationId]\n"
   *
   *         If successful, return "Paid reservation: [reservationId] remaining balance:
   *         [balance]\n" where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay(int reservationId) {
    // TODO: YOUR CODE HERE
    if(this.loggedIn == false) {
      return "Cannot pay, not logged in\n";
    } 
    try {
       
      conn.setAutoCommit(false);
      int cost = 0;
 
      

      checkUserResExistsIndirect.setInt(1, reservationId);
      checkUserResExistsIndirect.setString(2, this.user);
      ResultSet userExistsI = checkUserResExistsIndirect.executeQuery();

      userExistsI.next();
    
      int countI = userExistsI.getInt("count");


      checkUserExists.setInt(1, reservationId);
      ResultSet userExists = checkUserExists.executeQuery();
      if(!userExists.next()) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "Cannot find unpaid reservation " + reservationId + " under user: " + this.user + "\n";
      }
      
      userExists.close();
      boolean isDirect = true;

      if(countI > 0) {
        isDirect = false;
      }
      
      checkUserBalance.setString(1, this.user);
      ResultSet checkBalance = checkUserBalance.executeQuery();
      checkBalance.next();
      int userBalance = checkBalance.getInt("balance");

      checkUserExists.setInt(1, reservationId);
      userExists = checkUserExists.executeQuery();
      userExists.next();

      int f1 = userExists.getInt("rFid");

      checkFlightCost.setInt(1, f1);
      ResultSet checkCost = checkFlightCost.executeQuery();
      checkCost.next();
      cost =  checkCost.getInt("price");



      //check direct flight cost 
      if(isDirect == true) {
      if(userBalance < cost) {
        conn.rollback();
        conn.setAutoCommit(true);
        return "User has only " + userBalance + " in account but itinerary costs " + cost + "\n";
      }
     }
      checkFlightCost.clearParameters();
        //check Indirect flight cost 
      if(isDirect == false) {
          int f2 = userExists.getInt("rFid2");
           checkFlightCost.setInt(1, f2);
           ResultSet checkCost2 = checkFlightCost.executeQuery();
           checkCost2.next();
           int cost2 = cost + checkCost2.getInt("price");

           if(userBalance < cost2) {
            conn.rollback();
            conn.setAutoCommit(true);
            return "User has only " + userBalance + " in account but itinerary costs " + cost2 + "\n";
           }
           cost = cost2;
      
      }

     updateBalance.setInt(1, checkBalance.getInt("balance") - cost);
     updateBalance.setString(2, this.user);
     updateBalance.executeUpdate();
     
     updatePay.setInt(1, reservationId);
     updatePay.executeUpdate();
     conn.commit();
     conn.setAutoCommit(true);
     return "Paid reservation: " + reservationId + " remaining balance: " + (userBalance - cost) + "\n";

    }
    catch(Exception e) {
      try {
      conn.rollback();
      conn.setAutoCommit(true);
      }
      catch(Exception e2) {
        return "Error";
      }
      e.printStackTrace();
      if (isDeadlock((SQLException) e))
          return transaction_pay(reservationId);
      } 

    
    return "Failed to pay for reservation " + reservationId + "\n";
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n" If
   *         the user has no reservations, then return "No reservations found\n" For all other
   *         errors, return "Failed to retrieve reservations\n"
   *
   *         Otherwise return the reservations in the following format:
   *
   *         Reservation [reservation ID] paid: [true or false]:\n [flight 1 under the
   *         reservation]\n [flight 2 under the reservation]\n Reservation [reservation ID] paid:
   *         [true or false]:\n [flight 1 under the reservation]\n [flight 2 under the
   *         reservation]\n ...
   *
   *         Each flight should be printed using the same format as in the {@code Flight} class.
   *
   * @see Flight#toString()
   */
  public String transaction_reservations() {
    if (this.loggedIn == false) {
      return "Cannot view reservations, not logged in\n";
      }
      try {
      userResExists.clearParameters();
      userResExists.setString(1, this.user);
      ResultSet userExists = userResExists.executeQuery();
  
      StringBuffer sb = new StringBuffer();
  
      int count = 0;
      while (userExists.next()) {
        count++;
  
        sb.append("Reservation " + userExists.getInt("rid") + " paid: " + (userExists.getInt("is_paid")== 1) + ":\n");
        getFlightInfo.clearParameters();
        getFlightInfo.setInt(1, userExists.getInt("rFid"));
        ResultSet flightInfo = getFlightInfo.executeQuery();
        flightInfo.next();
        sb.append(
          "ID: " + flightInfo.getInt("fid")
        + " Day: " + flightInfo.getInt("day_of_month")
        + " Carrier: " + flightInfo.getString("carrier_id")
        + " Number: " + flightInfo.getString("flight_num")
        + " Origin: " + flightInfo.getString("origin_city")
        + " Dest: " + flightInfo.getString("dest_city")
        + " Duration: " + flightInfo.getInt("actual_time")
        + " Capacity: " + flightInfo.getInt("capacity")
        + " Price: " + flightInfo.getInt("price") + "\n");
        
        flightInfo.close();
        
  
        if (userExists.getInt("rFid2") > 0) {
          getFlightInfo.clearParameters();
          getFlightInfo.setInt(1, userExists.getInt("rFid2"));
          ResultSet flightInfo2 = getFlightInfo.executeQuery();
          flightInfo2.next();
          sb.append(
          "ID: " + flightInfo2.getInt("fid")
        + " Day: " + flightInfo2.getInt("day_of_month")
        + " Carrier: " + flightInfo2.getString("carrier_id")
        + " Number: " + flightInfo2.getString("flight_num")
        + " Origin: " + flightInfo2.getString("origin_city")
        + " Dest: " + flightInfo2.getString("dest_city")
        + " Duration: " + flightInfo2.getInt("actual_time")
        + " Capacity: " + flightInfo2.getInt("capacity")
        + " Price: " + flightInfo2.getInt("price") + "\n");
        
        flightInfo2.close();
         
        }
  
      }
      //userResExists.close();
      if (count == 0) {
        return "No reservations found\n";
      }
      return  sb.toString();
  
  
  
    } catch (SQLException e) {
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }
    
  }

  /**
   * Example utility function that uses prepared statements
   */
  private int checkFlightCapacity(int fid) throws SQLException {
    flightCapacityStmt.clearParameters();
    flightCapacityStmt.setInt(1, fid);

    ResultSet results = flightCapacityStmt.executeQuery();
    results.next();
    int capacity = results.getInt("capacity");
    results.close();

    return capacity;
  }

  /**
   * Utility function to determine whether an error was caused by a deadlock
   */
  private static boolean isDeadlock(SQLException e) {
    return e.getErrorCode() == 1205;
  }


  class Itinerary implements Comparable<Itinerary>{
    int travelTime;
    Flight f1;
    Flight f2;
    boolean isDirect;

    //Constructors 
    Itinerary(Flight f1) {
      this.f1 = f1;
      this.isDirect = true;
      this.travelTime = f1.time;
    }
    Itinerary(Flight f1, Flight f2) {
      this.f1 = f1;
      this.f2 = f2;
      this.isDirect = false;
      this.travelTime = f1.time + f2.time;
    }
   
    public int compareTo(Itinerary it) {
     if(this.travelTime < it.travelTime) {
      return -2;
     }
     else if(this.travelTime > it.travelTime) {
      return 2;
     }
     else if(this.f1.fid > it.f1.fid){
       return 1;
     }
     else if(this.f1.fid < it.f1.fid) {
      return -1;
     }
     else {
      return 0;
     }
  }

  }
  /**
   * A class to store information about a single flight
   */
  class Flight {
    public int fid;
    public int dayOfMonth;
    public String carrierId;
    public String flightNum;
    public String originCity;
    public String destCity;
    public int time;
    public int capacity;
    public int price;

    Flight(int id, int day, String carrier, String fnum, String origin, String dest, int tm,
           int cap, int pri) {
      fid = id;
      dayOfMonth = day;
      carrierId = carrier;
      flightNum = fnum;
      originCity = origin;
      destCity = dest;
      time = tm;
      capacity = cap;
      price = pri;
    }
    
    @Override
    public String toString() {
      return "ID: " + fid + " Day: " + dayOfMonth + " Carrier: " + carrierId + " Number: "
          + flightNum + " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time
          + " Capacity: " + capacity + " Price: " + price;
    }
  }
  



}

