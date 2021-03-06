/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/** ***********************************************************
 * <pre>
 *
 * PURPOSE: ODMG Implementation Bootstrap Object
 *
 *
 * COMMENTS:
 *
 * </pre>
 *********************************************************** */
package rasj.odmg;

import rasj.*;
import rasj.global.*;
import rasj.clientcommhttp.*;
import org.odmg.*;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;


/**
 * This class implements the internal ODMG Bootstrap Object used by the rasj.odmg package.
 * Because it contains a lot of functionality for internal purposes (e.g. methods for
 * the RasManager), this class is not the official Implementation object the user works
 * with, it is only used by the rasj.odmg package.
 * <P>
 * The public Implementation object is the class
 * {@link rasj.RasImplementation rasj.RasImplementation}, which internally works with a
 * RasODMGImplementation object.
 *
 * @see rasj.RasImplementation
 */

public class RasODMGImplementation implements RasImplementationInterface, RasCommDefs { //implements Implementation

    private String rasServer = "";
    private String rasMgr = "";
    private int    rasMgrPort = RasGlobalDefs.RASMGRPORT_DEFAULT;
    private String userIdentification = RasGlobalDefs.GUESTIDENT_DEFAULT;
    private String databaseName = "";
    private String capability = "dummy";
    private int    maxRetry   = RasGlobalDefs.MAX_GETFREESERVER_ATTEMPTS;

    /**
    * current state of transaction
    **/
    private boolean isOpenTA = false;

    /**
    /**
     * This variable holds the current RasTransaction object.
     */
    private RasTransaction transaction = null;

    /**
     * This variable holds the current Rasdatabase object.
     */
    private RasDatabase    database    = null;

    /**
     * This variable holds the current RasOQLQuery object.
     */
    private RasOQLQuery    query       = null;

    /**
     * The standard ODMG implementation sets the access mode when the database
     * is opened, whereas the RasDaMan server expects this information when a
     * transaction is started. Therefore it is saved in this variable.
     *
     * Available modes:
     * OPEN_READ_ONLY  = 1
     * OPEN_READ_WRITE = 2
     * OPEN_EXCLUSIVE  = 3
     */
    private int accessMode = 0;

    /**
     * Since ODMG does not specify a "isDatabaseOpen" method but provides a
     * DatabaseClosedException, this variable is set to 1 if an openDB command is
     * executed (closeDB sets it back to 0).
     */
    private int dbIsOpen   = 0;

    /**
     * This value is set to 1 if a transaction has been opened. Commiting or aborting
     * a transaction sets it back to 0.
     */
    private int taIsOpen   = 0;

    private int clientID   = 0;

    /**
     * This value is used to store possible error messages of exceptions occuring
     * when opening or closing transactions.
     * The ODMG specification does not allow these operations to throw
     * any exceptions, but our implementation could produce exceptions when connecting
     * to the RasDaMan httpserver. In order to not get lost, these exception messages
     * are stored in this variable.
     */
    private String errorStatus = "";
    // later: private String strUserAndPasswd = "anonymous:anonymouspasswd";

    /**
     * Standard constructor.
     * @param server Complete URL of the RasDaMan httpserver (including port number)
     */
    public RasODMGImplementation(String server) {
        Debug.enterVerbose("RasODMGImplementation.constructor: start, server=" + server + ".");
        try {
            // server address is http://server:port, we need server and port
            StringTokenizer t = new StringTokenizer(server, "/");
            String xxx = t.nextToken();
            rasMgr = t.nextToken("/:");
            String portStr = t.nextToken(":");
            rasMgrPort = Integer.parseInt(portStr);
        } catch (NoSuchElementException e) {
            Debug.leaveVerbose("RasODMGImplementation.constructor: done. server URL format error.");
            throw new  RasConnectionFailedException(RasGlobalDefs.URL_FORMAT_ERROR, server);
        }

        isOpenTA = false;

        Debug.leaveVerbose("RasODMGImplementation.constructor: done. ok.");
    }

    /**
     * Gets the name of the actual server.
     * @return the name of the RasDaMan server used
     */
    public String getRasServer() {
        Debug.talkVerbose("RasODMGImplementation.getRasServer: server=" + rasServer + ".");
        return rasServer;
    }

    /**
     * Tells whether database is open.
     * @return open status of database
     */
    public int dbIsOpen() {
        Debug.talkVerbose("RasODMGImplementation.dbIsOpen: dbIsOpen=" + dbIsOpen + ".");
        return dbIsOpen;
    }

    /**
     * Gets the client ID
     * @return ID of this client
     */
    public int getClientID() {
        Debug.talkVerbose("RasODMGImplementation.getClientID: clientID=" + clientID + ".");
        return clientID;
    }

    /**
     * Gets the database access mode
     * @return accessMode code: OPEN_READ_ONLY  = 1; OPEN_READ_WRITE = 2; OPEN_EXCLUSIVE  = 3
     */
    public int getAccessMode() {
        Debug.talkVerbose("RasODMGImplementation.getAccessMode: accessMode=" + accessMode + ".");
        return accessMode;
    }

    /**
     * Gets the current error status
     * @return error status string
     */
    public String getErrorStatus() {
        Debug.talkVerbose("RasODMGImplementation.getErrorStatus: errorStatus=" + errorStatus + ".");
        return errorStatus;
    }


    /**
     * Create a new transaction object and associate it with the current thread.
     */
    public Transaction newTransaction() {
        Debug.enterVerbose("RasODMGImplementation.newTransaction: start.");
        transaction = new RasTransaction(this);
        Debug.leaveVerbose("RasODMGImplementation.newTransaction: done.");
        return transaction;
    }

    /**
     * Get current transaction for thread, or NULL if none.
     */
    public Transaction currentTransaction() {
        Debug.talkVerbose("RasODMGImplementation.currentTransaction.");
        return transaction;
    }

    /**
     * Create a new database object.
     */
    public Database newDatabase() {
        Debug.enterVerbose("RasODMGImplementation.newDatabase: start.");
        database = new RasDatabase(this);
        Debug.leaveVerbose("RasODMGImplementation.newDatabase: done.");
        return database;
    }

    /**
     * Create a new query object.
     */
    public OQLQuery newOQLQuery() {
        Debug.enterVerbose("RasODMGImplementation.newOQLQuery: start.");
        query = new RasOQLQuery(this);
        Debug.leaveVerbose("RasODMGImplementation.newOQLQuery: done.");
        return query;
    }

    /**
     * Create a new DList object.
     */
    public DList newDList() {
        Debug.talkVerbose("RasODMGImplementation.newDList.");
        return new RasList();
    }

    /**
     * Create a new DBag object.
     */
    public DBag newDBag() {
        return new RasBag();
    }

    /**
     * Create a new DSet object.
     */
    public DSet newDSet() {
        Debug.talkVerbose("RasODMGImplementation.newDSet.");
        return new RasSet();
    }

    /**
     * Not implemented yet.
     */
    public DArray newDArray() {
        Debug.talkWarning("RasODMGImplementation.newDArray: not yet implemented.");
        throw new NotImplementedException();
    }

    /**
     * Not implemented yet.
     */
    public DMap newDMap() {
        Debug.talkWarning("RasODMGImplementation.newDMap: not yet implemented.");
        throw new NotImplementedException();
    }

    /**
     * Get a String representation of the object's identifier.
     * @returns: OID string on success, null otherwise
     */
    public String getObjectId(Object obj) {
        Debug.enterVerbose("RasODMGImplementation.getObjectId: start.");

        if (!(obj instanceof RasObject)) {          // currently all must be derived from RasObject
            Debug.leaveWarning("RasODMGImplementation.getObjectId: not yet implemented.");
            throw new NotImplementedException();
        }

        // if we come here: yes, we are derived from RasObject, let's proceed
        RasOID roid = ((RasObject)obj).getOID();
        String oid = roid.toString();
        DBag resultBag = null;
        if (!((RasObject)obj).getOID().isValid()) {     // OID of our object is not valid -> get one
            Debug.talkWarning("RasODMGImplementation.getObjectId: OID not Valid: " + roid + ".");
            String params = "ClientID=" + clientID + "&Command=10";
            RasHttpRequest request = new RasHttpRequest();
            if (((RasTransaction)this.currentTransaction()).isOpenLocally()) // TA is open, we can proceed
                // (decide w/o asking server -- PB 2003-jun-25)
            {
                // get new oid
                try {
                    request.execute(rasServer, params);     // get it from server
                } catch (RasQueryExecutionFailedException e) {
                    // this cannot occur (theoretically)
                    Debug.talkCritical("RasODMGImplementation.getObjectId: query execution failed: " + e.getMessage());
                }
            } else {                    // TA is not open, so we do an open here
                Debug.talkSparse("RasODMGImplementation.getObjectId: db not open, opening: " + databaseName + ".");
                boolean openedDbHere = false;           // did we open a db locally?
                boolean openedTaHere = false;           // did we open a db locally?
                Database d = null;
                Transaction t = null;
                try {
                    if (this.dbIsOpen == Database.NOT_OPEN) { // we even open the db if not done already
                        Debug.talkSparse("RasODMGImplementation.getObjectId: db not open, opening: " + databaseName + ".");
                        d = this.newDatabase();
                        d.open(databaseName, Database.OPEN_READ_WRITE);
                        // fix: was: "RASBASE"; now: take name of last opened db. not good, but better maybe -- PB 2003-jun-13
                        // FIXME: r/w open not good, do we have info at this point? Maybe getOid needs r/w
                        openedDbHere = true;
                    }
                    t = this.newTransaction();
                    t.begin();
                    openedTaHere = true;            // we know now we have an open TA
                    // get new oid
                    request.execute(rasServer, params);     // get it from server
                    t.commit();
                    if (openedDbHere) {
                        Debug.talkSparse("RasODMGImplementation.getObjectId: closing locally opened DB. ");
                        d.close();
                        openedDbHere = false;           // no more locally opened DB to close
                    }
                } catch (ODMGException e) {
                    Debug.talkCritical("RasODMGImplementation.getObjectId: failed: " + e.getMessage());
                    try {
                        if (openedTaHere) {
                            t.abort();
                        }
                        if (openedDbHere) {
                            d.close();
                        }
                    } catch (ODMGException e3) {
                        Debug.talkSparse("RasODMGImplementation.getObjectId: error closing locally opened DB (ignored): " + e3.getMessage());
                    }
                }
            } // if (TA open)

            resultBag = (DBag)request.getResult();        // if all went fine we now have OID in result
            if (resultBag != null) {
                Iterator iter = resultBag.iterator();
                if (iter.hasNext()) {
                    roid = (RasOID)iter.next();
                }
                oid = roid.toString();
                ((RasObject)obj).setOID(roid);
            } else {
                Debug.talkCritical("RasODMGImplementation.getObjectId: empty query result, cannot fetch OID.");
                oid = null;
            }
        } // valid OID

        Debug.leaveVerbose("RasODMGImplementation.getObjectId: done. oid=" + oid + ".");
        return oid;
    } // getObjectId()

    /**
     * Not implemented yet.
     */
    public Database getDatabase(Object obj) {
        Debug.talkCritical("RasODMGImplementation.getDatabase: not yet implemented.");
        throw new NotImplementedException();
    }

    /**
    * Open database
    */
    public void openDB(String name, int accessMode) throws ODMGException, ODMGRuntimeException {
        Debug.enterVerbose("RasODMGImplementation.openDB: start, db=" + name + ", accessMode=" + accessMode);

        databaseName = name;
        this.accessMode = accessMode;

        try {
            getFreeServer();            // sets rasServer
            executeOpenDB(databaseName, accessMode);
            // executeCloseDB();        // does nothing, so clean away -- PB 2003-jun-25
            dbIsOpen = 1;
        } catch (ODMGException e) {     // catch just for logging, then rethrow immediately
            Debug.leaveCritical("RasODMGImplementation.openDB: done. Exception: " + e.getMessage());
            throw new ODMGException(e.getMessage());
        } catch (ODMGRuntimeException x) {  // catch just for logging, then rethrow immediately
            Debug.leaveCritical("RasODMGImplementation.openDB: done. ODMGRuntimeException: " + x.getMessage());
            throw new ODMGException(x.getMessage());
        }

        Debug.leaveVerbose("RasODMGImplementation.openDB: done. OK.");
    }

    private void executeOpenDB(String name, int accessMode) throws ODMGException {
        Debug.enterVerbose("RasODMGImplementation.executeOpenDB: start, name=" + name + ", accessMode=" + accessMode);
        String params = "Command=" + RasODMGGlobal.commOpenDB + "&Database=" + name + "&Capability=" + capability;
        RasHttpRequest request = new RasHttpRequest();
        request.execute(rasServer, params);
        // Later, the client ID is determined here
        clientID = 1; // not used anymore
        Debug.leaveVerbose("RasODMGImplementation.executeOpenDB: done.");
    }

    /**
     * Closes an open database. At the moment, only one database can be open at
     * a given time and thus no parameter "database" is necessary here.
     */
    public void closeDB() throws ODMGException {
        Debug.enterVerbose("RasODMGImplementation.closeDB start.");

        // not necessary, others do close already
        // PB: this is due to an old bug in O2 which needed a closeDB in order to free objects, hence we do this in commitTA/abortTA

        dbIsOpen = 0;
        Debug.leaveVerbose("RasODMGImplementation.closeDB done.");
    }

    private void executeCloseDB() throws ODMGException {
        String params = "ClientID=" + clientID + "&Command=" + RasODMGGlobal.commCloseDB;
        RasHttpRequest request = new RasHttpRequest();
        request.execute(rasServer, params);
    }

    /**
     * Begin a transaction.
     */
    public void beginTA() {
        Debug.enterVerbose("RasODMGImplementation.beginTA start.");

        // exception handling deactivated, as not thrown any longer -- PB 2004-jul-03
        // try
        //   {
        // this hurts in several ways, so deactivated it: -- PB 2004-jul-03
        // - "getFreeServer();executeOpenDB();" done in openDB(). ODMG says: to begin a TA DB must be open
        // - this sucks up an additional server process for clean clients doing "openDB();beginTA()"
        // getFreeServer();
        // executeOpenDB(databaseName,accessMode);
        executeBeginTA();
        //   }
        // catch(ODMGException e)
        //   {
        //     Debug.talkWarning( "RasODMGImplementation.beginTA: " + e.getMessage() );
        //     errorStatus = e.getMessage();
        //   }
        Debug.leaveVerbose("RasODMGImplementation.beginTA done.");
    }

    private void executeBeginTA() {
        String errorMsg = "Could not open transaction: ";
        if (dbIsOpen == 0) {
            throw new DatabaseClosedException(errorMsg + "database not open");
        }

        String params = "ClientID=" + clientID + "&Command=";
        // Is the database opened READ_ONLY or READ_WRITE ?
        if (accessMode == Database.OPEN_READ_ONLY) {
            params = params + RasODMGGlobal.commBTreadOnly;
        } else {
            params = params + RasODMGGlobal.commBTreadWrite;
        }
        params = params + "&Capability=" + capability;
        RasHttpRequest request = new RasHttpRequest();
        try {
            request.execute(rasServer, params);
        } catch (RasQueryExecutionFailedException e) {
            // this cannot occur (theoretically)
            Debug.talkWarning("RasODMGImplementation.executeBeginTA: " + e.getMessage());
            errorStatus = e.getMessage();
        }
    }

    /**
     * Returns TRUE if a transaction is currently open.
     * This method MUST be sincere in that it asks the server about its state! (some apps use it to override timeout)
     */
    public boolean isOpenTA() {
        Debug.enterVerbose("RasODMGImplementation.isOpenTA start.");
        boolean result = false;

        String params = "ClientID=" + clientID + "&Command=" + RasODMGGlobal.commIsOpenTA;
        RasHttpRequest request = new RasHttpRequest();
        try {
            request.execute(rasServer, params);
            result = (request.getResultType() == 99) ? true : false;
        } catch (RasQueryExecutionFailedException e) {
            // this cannot occur (theoretically)
            Debug.talkWarning("RasODMGImplementation.isOpenTA: " + e.getMessage());
            errorStatus = e.getMessage();
            result = false;
        }

        Debug.leaveVerbose("RasODMGImplementation.isOpenTA done. result=" + result);
        return result;
    }

    /**
     * Commit a transaction.
     */
    public void commitTA() {
        Debug.enterVerbose("RasODMGImplementation.commitTA start.");

        try {
            executeCommitTA();
            executeCloseDB();           // FIXME: why close here??? -- PB 2003-jun-13
        } catch (ODMGException e) {
            Debug.talkWarning("RasODMGImplementation.commitTA: " + e.getMessage());
            errorStatus = e.getMessage();
        }

        Debug.leaveVerbose("RasODMGImplementation.commitTA done.");
    }

    private void executeCommitTA() {
        String errorMsg = "Could not commit transaction: ";
        if (dbIsOpen == 0) {
            throw new DatabaseClosedException(errorMsg + "database not open");
        }
        String params = "ClientID=" + clientID + "&Command=" + RasODMGGlobal.commCT;
        RasHttpRequest request = new RasHttpRequest();
        try {
            request.execute(rasServer, params); //RasODMGGlobal.getRasServer(),params);
        } catch (RasQueryExecutionFailedException e) {
            // this cannot occur (theoretically)
            Debug.talkWarning("RasODMGImplementation.executeCommitTA: " + e.getMessage());
            errorStatus = e.getMessage();
        }
    }

    /**
     * Abort a transaction.
     */
    public void abortTA() {
        Debug.enterVerbose("RasODMGImplementation.abortTA start.");

        try {
            executeAbortTA();
            executeCloseDB();
        } catch (ODMGException e) {
            Debug.talkWarning("RasODMGImplementation.abortTA: " + e.getMessage());
            errorStatus = e.getMessage();
        }

        Debug.leaveVerbose("RasODMGImplementation.abortTA done.");
    }

    private void executeAbortTA() {
        String errorMsg = "Cannot abort transaction: ";
        if (dbIsOpen == 0) {
            throw new DatabaseClosedException(errorMsg + "database not open");
        }
        String params = "ClientID=" + clientID + "&Command=" + RasODMGGlobal.commAT;
        RasHttpRequest request = new RasHttpRequest();
        try {
            request.execute(rasServer, params);
        } catch (RasQueryExecutionFailedException e) {
            // this cannot occur (theoretically)
            Debug.talkWarning("RasODMGImplementation.executeAbortTA: " + e.getMessage());
            errorStatus = e.getMessage();
        }
    }

    /**
     * Set the maximum retry parameter
     */
    public void setMaxRetry(int newRetry) {
        Debug.talkVerbose("RasODMGImplementation.setMaxRetry to " + newRetry + ".");
        maxRetry = newRetry;
    }

    /**
     * Get the maximum retry parameter
     */
    public int getMaxRetry() {
        Debug.talkVerbose("RasODMGImplementation.getMaxRetry: maxRetry=" + maxRetry + ".");
        return maxRetry;
    }

    /**
    * Requests a free server and retry's
    */
    //private void getFreeServer( )
    public void getFreeServer()
    throws  RasQueryExecutionFailedException, RasConnectionFailedException {
        Debug.enterVerbose("RasODMGImplementation.getFreeServer: start.");

        String uniqueID = uniqueRequestID();

        int millisec = RasGlobalDefs.GETFREESERVER_WAIT_INITIAL;
        for (int retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {
                executeGetFreeServer(uniqueID);
                // if no error, we have the server, so break
                break;
            } catch (RasConnectionFailedException e) {
                Debug.talkCritical("RasODMGImplementation.getFreeServer: cannot obtain a free server:" + e.getMessage());
                // the following errors justify that we try again, maybe we get a free server a little later
                int errno = e.getErrorNo();
                if (errno == RasGlobalDefs.MANAGER_BUSY
                        /*     || errno==RasGlobalDefs.NO_ACTIVE_SERVERS */  // if no such server is started, waiting won't help
                        || errno == RasGlobalDefs.WRITE_TRANS_IN_PROGRESS) {
                    // retry, but with increasing wait period
                    millisec = millisec * RasGlobalDefs.GETFREESERVER_WAIT_INCREMENT;

                    Debug.talkCritical("RasODMGImplementation.getFreeServer: no free server available, errno=" + errno + ", retry #"  + retryCount + " of " + maxRetry + " after " + millisec + " msecs.");
                    try {
                        Thread.sleep(millisec);
                    } catch (InterruptedException intex) {
                        // wake up
                    }
                } else {
                    Debug.talkCritical("RasODMGImplementation.getFreeServer: giving up, cannot obtain free server. marking connection as closed.");
                    Debug.leaveVerbose("RasODMGImplementation.getFreeServer: done.");

                    // reset ta & db
                    isOpenTA = false;
                    dbIsOpen = 0;
                    throw(e);   // we give up, or we shouldn't retry with this kind of error
                }
            }
        } // for

        Debug.leaveVerbose("RasODMGImplementation.getFreeServer: done.");
    }

    /**
    * Requests a free server from rasmgr
    */

    private void executeGetFreeServer(String uniqueID) throws  RasQueryExecutionFailedException, RasConnectionFailedException {
        Debug.enterVerbose("RasODMGImplementation.executeGetFreeServer: enter, uniqueID=" + uniqueID);

        try {
            Socket soclu = new Socket(rasMgr, rasMgrPort); // FIXME: where is this socket closed ??? PB
            Debug.talkVerbose("RasODMGImplementation.executeGetFreeServer: socket=" + soclu);
            PrintStream ps = new PrintStream(soclu.getOutputStream());
            String body = databaseName + " HTTP " + (accessMode == Database.OPEN_READ_ONLY ? "ro" : "rw") + " " + uniqueID + " \0";
            ps.print("POST getfreeserver HTTP/1.1\r\nAccept: text/plain\r\nContent-type: text/plain\r\n"
                     + "User-Agent: RasDaMan Java Client1.0\r\nAuthorization: ras " + userIdentification
                     + "\r\nContent length: " + body.length() + "\r\n\r\n" + body);
            ps.flush();
            Debug.talkVerbose("RasODMGImplementation.executeGetFreeServer: sent body=" + body);

            BufferedReader ds = new BufferedReader(new InputStreamReader(soclu.getInputStream()));
            int resultCode = getResultCode(ds);
            String bodyLine = getBodyLine(ds);
            Debug.talkVerbose("RasODMGImplementation.executeGetFreeServer: received result code=" + resultCode + ", bodyLine=" + bodyLine);

            ps.close();
            ds.close();
            soclu.close();                 // this one was missing all the time -- PB
            Debug.talkVerbose("RasODMGImplementation.executeGetFreeServer: socket closed: " + soclu);

            if (resultCode == 200) {
                StringTokenizer t = new StringTokenizer(bodyLine, " ");
                String host = t.nextToken();
                String port = t.nextToken(" ");
                capability = t.nextToken(" \t\r\n\0");
                rasServer = "http://" + host + ":" + port;
            } else {
                // if error =>bodyLine: errorCode someText
                Debug.talkSparse("RasODMGImplementation.executeGetFreeServer: bodyLine=" + bodyLine);
                StringTokenizer t = new StringTokenizer(bodyLine, " ");
                String errorStr = t.nextToken();
                int errorCode = Integer.parseInt(errorStr);
                if (resultCode < 1000) {
                    Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer: done. connection failed, code=" + errorCode);
                    throw new RasConnectionFailedException(errorCode, null);
                } else {
                    Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer: done. request format error, code=" + errorCode);
                    throw new RasConnectionFailedException(RasGlobalDefs.REQUEST_FORMAT_ERROR, " code=" + errorCode);
                }
            }
        } catch (MalformedURLException e) {
            Debug.talkCritical("RasODMGImplementation.executeGetFreeServer: malformed URL exception: " + e.getMessage());
            Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer: done with exception: " + e.getMessage());
            throw new RasConnectionFailedException(RasGlobalDefs.MANAGER_CONN_FAILED, rasMgr);
        } catch (IOException e) {
            Debug.talkCritical("RasODMGImplementation.executeGetFreeServer: IO exception: " + e.getMessage());
            Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer: done with exception: " + e.getMessage());
            throw new RasClientInternalException("RasODMGImplementation", "executeGetFreeServer()", e.getMessage());
        }

        Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer: done.");
    } // executeGetFreeServer()

    public Object queryRequest(String parameters) throws RasQueryExecutionFailedException {
        Debug.enterVerbose("RasODMGImplementation.queryRequest start. parameters=" + parameters + ".");

        BenchmarkTimer qTimer = new BenchmarkTimer("queryRequest");
        qTimer.startTimer();

        RasHttpRequest request = new RasHttpRequest();
        request.execute(rasServer, parameters);

        qTimer.stopTimer();
        qTimer.print();

        Debug.leaveVerbose("RasODMGImplementation.executeGetFreeServer done.");
        return request.getResult();
    }

    public String getTypeStructure(String typename, int typetype) {
        Debug.talkCritical("RasODMGImplementation.getTypeStructure: not yet implemented.");
        throw new NotImplementedException();
    }

    //private int getResultCode(BufferedReader ds) throws IOException
    public int getResultCode(BufferedReader ds) throws IOException {
        Debug.enterVerbose("RasODMGImplementation.getResultCode start.");

        String s = ds.readLine();
        StringTokenizer t = new StringTokenizer(s, " ");
        String http = t.nextToken();
        String resultString = t.nextToken(" ");
        int result = Integer.parseInt(resultString);

        Debug.leaveVerbose("RasODMGImplementation.getResultCode done. result=" + result + ".");
        return result;
    }

    //private String getBodyLine(BufferedReader ds) throws IOException
    public String getBodyLine(BufferedReader ds) throws IOException {
        Debug.enterVerbose("RasODMGImplementation.getBodyLine start.");

        String s;
        do {                // was a "for(;;)" loop, cleaned it -- PB 2003-jun-13
            s = ds.readLine();
            if (s == null) {
                Debug.talkCritical("RasODMGImplementation.getBodyLine: Unexpected EOF in rasmgr answer.");
                throw new IOException("Unexpected EOF in rasmgr answer.");
            }
        } while (s.length() != 0);

        String result = ds.readLine();
        Debug.leaveVerbose("RasODMGImplementation.getBodyLine done. result=" + result + ".");
        return result;
    }

    public void setUserIdentification(String userName, String plainPass) {
        Debug.enterVerbose("RasODMGImplementation.setUserIdentification start.");

        MD5 md5 = new MD5();
        String  hex;
        md5.Init();
        md5.Update(plainPass);
        hex = md5.asHex();
        userIdentification = userName + ":" + hex;

        Debug.leaveVerbose("RasODMGImplementation.setUserIdentification done.");
    }

    public void connectClient(String userName, String passwordHash) {}

    public void disconnectClient() {}

    private String strHostID = null;
    static private int idcounter = 0;

    private String uniqueRequestID() {
        Debug.enterVerbose("RasODMGImplementation.uniqueRequestID start.");

        if (strHostID == null) {
            long hostid = 0;
            try {
                InetAddress addr = InetAddress.getLocalHost();
                // Get IP Address
                byte[] ipAddr = addr.getAddress();

                for (int i = 0; i < ipAddr.length; i++) {
                    int ss = (int)ipAddr[i];
                    if (ss < 0) {
                        ss = 256 + ss;
                    }
                    hostid = hostid * 256 + ss;
                }
            } catch (UnknownHostException e) {
                Random random = new Random();
                hostid = random.nextInt();
            }
            idcounter = (idcounter + 1) & 0xF;
            // it's unique enough, we don't need such a huge number
            strHostID = "" + hostid + ':' + (System.currentTimeMillis() & 0xFFFFFFF0) + idcounter;
        }

        Debug.leaveVerbose("RasODMGImplementation.uniqueRequestID done. strHostID=" + strHostID + ".");
        return strHostID;
    }

}

//##################################################################################
/**
 * Contains internal state of the MD5 class
 */

class MD5State {
    /**
     * 128-byte state
     */
    int state[];

    /**
     * 64-bit character count (could be true Java long?)
     */
    int count[];

    /**
     * 64-byte buffer (512 bits) for storing to-be-hashed characters
     */
    byte    buffer[];

    public MD5State() {
        Debug.enterVerbose("MD5State.constructor start.");

        buffer = new byte[64];
        count = new int[2];
        state = new int[4];

        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;

        count[0] = count[1] = 0;

        Debug.leaveVerbose("MD5State.constructor done.");
    }

    /**
    Create this State as a copy of another state
    **/
    public MD5State(MD5State from) {
        this();

        Debug.enterVerbose("MD5State.cloner start.");

        int i;

        for (i = 0; i < buffer.length; i++) {
            this.buffer[i] = from.buffer[i];
        }

        for (i = 0; i < state.length; i++) {
            this.state[i] = from.state[i];
        }

        for (i = 0; i < count.length; i++) {
            this.count[i] = from.count[i];
        }

        Debug.leaveVerbose("MD5State.cloner done.");
    }

}; // MD5State

/**
 * Implementation of RSA's MD5 hash generator
 *
 * @version $Revision: 1.25 $
 * @author  Santeri Paavolainen <sjpaavol@cc.helsinki.fi>
 */

class MD5 {
    /**
     * MD5 state
     */
    MD5State  state;

    /**
     * If Final() has been called, finals is set to the current finals
     * state. Any Update() causes this to be set to null.
     */
    MD5State  finals;

    /**
     * Padding for Final()
     */
    static byte   padding[] = {
        (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * Initialize MD5 internal state (object can be reused just by
     * calling Init() after every Final()
     */
    public synchronized void Init() {
        state = new MD5State();
        finals = null;
    }

    /**
     * Class constructor
     */
    public MD5() {
        this.Init();
    }

    /**
     * Initialize class, and update hash with ob.toString()
     *
     * @param ob  Object, ob.toString() is used to update hash
     *            after initialization
     */
    public MD5(Object ob) {
        this();
        Update(ob.toString());
    }

    private int rotate_left(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

    /* I wonder how many loops and hoops you'll have to go through to
       get unsigned add for longs in java */

    private int uadd(int a, int b) {
        long aa, bb;
        aa = ((long) a) & 0xffffffffL;
        bb = ((long) b) & 0xffffffffL;

        aa += bb;

        return (int)(aa & 0xffffffffL);
    }

    private int uadd(int a, int b, int c) {
        return uadd(uadd(a, b), c);
    }

    private int uadd(int a, int b, int c, int d) {
        return uadd(uadd(a, b, c), d);
    }

    private int FF(int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, ((b & c) | (~b & d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int GG(int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, ((b & d) | (c & ~d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int HH(int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, (b ^ c ^ d), x, ac);
        return uadd(rotate_left(a, s) , b);
    }

    private int II(int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, (c ^ (b | ~d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int[] Decode(byte buffer[], int len, int shift) {
        int     out[];
        int     i, j;

        out = new int[16];

        for (i = j = 0; j < len; i++, j += 4) {
            out[i] = ((int)(buffer[j + shift] & 0xff)) |
                     (((int)(buffer[j + 1 + shift] & 0xff)) << 8) |
                     (((int)(buffer[j + 2 + shift] & 0xff)) << 16) |
                     (((int)(buffer[j + 3 + shift] & 0xff)) << 24);

            /*      System.out.println("out[" + i + "] = \t" +
                         ((int) buffer[j + 0 + shift] & 0xff) + "\t|\t" +
                         ((int) buffer[j + 1 + shift] & 0xff) + "\t|\t" +
                         ((int) buffer[j + 2 + shift] & 0xff) + "\t|\t" +
                         ((int) buffer[j + 3 + shift] & 0xff));*/
        }

        return out;
    }

    private void Transform(MD5State state, byte buffer[], int shift) {
        int
        a = state.state[0],
        b = state.state[1],
        c = state.state[2],
        d = state.state[3],
        x[];

        x = Decode(buffer, 64, shift);

        /* Round 1 */
        a = FF(a, b, c, d, x[ 0],   7, 0xd76aa478);  /* 1 */
        d = FF(d, a, b, c, x[ 1],  12, 0xe8c7b756);  /* 2 */
        c = FF(c, d, a, b, x[ 2],  17, 0x242070db);  /* 3 */
        b = FF(b, c, d, a, x[ 3],  22, 0xc1bdceee);  /* 4 */
        a = FF(a, b, c, d, x[ 4],   7, 0xf57c0faf);  /* 5 */
        d = FF(d, a, b, c, x[ 5],  12, 0x4787c62a);  /* 6 */
        c = FF(c, d, a, b, x[ 6],  17, 0xa8304613);  /* 7 */
        b = FF(b, c, d, a, x[ 7],  22, 0xfd469501);  /* 8 */
        a = FF(a, b, c, d, x[ 8],   7, 0x698098d8);  /* 9 */
        d = FF(d, a, b, c, x[ 9],  12, 0x8b44f7af);  /* 10 */
        c = FF(c, d, a, b, x[10],  17, 0xffff5bb1);  /* 11 */
        b = FF(b, c, d, a, x[11],  22, 0x895cd7be);  /* 12 */
        a = FF(a, b, c, d, x[12],   7, 0x6b901122);  /* 13 */
        d = FF(d, a, b, c, x[13],  12, 0xfd987193);  /* 14 */
        c = FF(c, d, a, b, x[14],  17, 0xa679438e);  /* 15 */
        b = FF(b, c, d, a, x[15],  22, 0x49b40821);  /* 16 */

        /* Round 2 */
        a = GG(a, b, c, d, x[ 1],   5, 0xf61e2562);  /* 17 */
        d = GG(d, a, b, c, x[ 6],   9, 0xc040b340);  /* 18 */
        c = GG(c, d, a, b, x[11],  14, 0x265e5a51);  /* 19 */
        b = GG(b, c, d, a, x[ 0],  20, 0xe9b6c7aa);  /* 20 */
        a = GG(a, b, c, d, x[ 5],   5, 0xd62f105d);  /* 21 */
        d = GG(d, a, b, c, x[10],   9,  0x2441453);  /* 22 */
        c = GG(c, d, a, b, x[15],  14, 0xd8a1e681);  /* 23 */
        b = GG(b, c, d, a, x[ 4],  20, 0xe7d3fbc8);  /* 24 */
        a = GG(a, b, c, d, x[ 9],   5, 0x21e1cde6);  /* 25 */
        d = GG(d, a, b, c, x[14],   9, 0xc33707d6);  /* 26 */
        c = GG(c, d, a, b, x[ 3],  14, 0xf4d50d87);  /* 27 */
        b = GG(b, c, d, a, x[ 8],  20, 0x455a14ed);  /* 28 */
        a = GG(a, b, c, d, x[13],   5, 0xa9e3e905);  /* 29 */
        d = GG(d, a, b, c, x[ 2],   9, 0xfcefa3f8);  /* 30 */
        c = GG(c, d, a, b, x[ 7],  14, 0x676f02d9);  /* 31 */
        b = GG(b, c, d, a, x[12],  20, 0x8d2a4c8a);  /* 32 */

        /* Round 3 */
        a = HH(a, b, c, d, x[ 5],   4, 0xfffa3942);  /* 33 */
        d = HH(d, a, b, c, x[ 8],  11, 0x8771f681);  /* 34 */
        c = HH(c, d, a, b, x[11],  16, 0x6d9d6122);  /* 35 */
        b = HH(b, c, d, a, x[14],  23, 0xfde5380c);  /* 36 */
        a = HH(a, b, c, d, x[ 1],   4, 0xa4beea44);  /* 37 */
        d = HH(d, a, b, c, x[ 4],  11, 0x4bdecfa9);  /* 38 */
        c = HH(c, d, a, b, x[ 7],  16, 0xf6bb4b60);  /* 39 */
        b = HH(b, c, d, a, x[10],  23, 0xbebfbc70);  /* 40 */
        a = HH(a, b, c, d, x[13],   4, 0x289b7ec6);  /* 41 */
        d = HH(d, a, b, c, x[ 0],  11, 0xeaa127fa);  /* 42 */
        c = HH(c, d, a, b, x[ 3],  16, 0xd4ef3085);  /* 43 */
        b = HH(b, c, d, a, x[ 6],  23,  0x4881d05);  /* 44 */
        a = HH(a, b, c, d, x[ 9],   4, 0xd9d4d039);  /* 45 */
        d = HH(d, a, b, c, x[12],  11, 0xe6db99e5);  /* 46 */
        c = HH(c, d, a, b, x[15],  16, 0x1fa27cf8);  /* 47 */
        b = HH(b, c, d, a, x[ 2],  23, 0xc4ac5665);  /* 48 */

        /* Round 4 */
        a = II(a, b, c, d, x[ 0],   6, 0xf4292244);  /* 49 */
        d = II(d, a, b, c, x[ 7],  10, 0x432aff97);  /* 50 */
        c = II(c, d, a, b, x[14],  15, 0xab9423a7);  /* 51 */
        b = II(b, c, d, a, x[ 5],  21, 0xfc93a039);  /* 52 */
        a = II(a, b, c, d, x[12],   6, 0x655b59c3);  /* 53 */
        d = II(d, a, b, c, x[ 3],  10, 0x8f0ccc92);  /* 54 */
        c = II(c, d, a, b, x[10],  15, 0xffeff47d);  /* 55 */
        b = II(b, c, d, a, x[ 1],  21, 0x85845dd1);  /* 56 */
        a = II(a, b, c, d, x[ 8],   6, 0x6fa87e4f);  /* 57 */
        d = II(d, a, b, c, x[15],  10, 0xfe2ce6e0);  /* 58 */
        c = II(c, d, a, b, x[ 6],  15, 0xa3014314);  /* 59 */
        b = II(b, c, d, a, x[13],  21, 0x4e0811a1);  /* 60 */
        a = II(a, b, c, d, x[ 4],   6, 0xf7537e82);  /* 61 */
        d = II(d, a, b, c, x[11],  10, 0xbd3af235);  /* 62 */
        c = II(c, d, a, b, x[ 2],  15, 0x2ad7d2bb);  /* 63 */
        b = II(b, c, d, a, x[ 9],  21, 0xeb86d391);  /* 64 */

        state.state[0] += a;
        state.state[1] += b;
        state.state[2] += c;
        state.state[3] += d;
    }

    /**
     * Updates hash with the bytebuffer given (using at maximum length bytes from
     * that buffer)
     *
     * @param state   Which state is updated
     * @param buffer  Array of bytes to be hashed
     * @param offset  Offset to buffer array
     * @param length  Use at maximum `length' bytes (absolute
     *            maximum is buffer.length)
     */
    public void Update(MD5State stat, byte buffer[], int offset, int length) {
        int index, partlen, i, start;

        /*    System.out.print("Offset = " + offset + "\tLength = " + length + "\t");
            System.out.print("Buffer = ");
            for (i = 0; i < buffer.length; i++)
            System.out.print((int) (buffer[i] & 0xff) + " ");
            System.out.print("\n");*/

        finals = null;

        /* Length can be told to be shorter, but not inter */
        if ((length - offset) > buffer.length) {
            length = buffer.length - offset;
        }

        /* compute number of bytes mod 64 */
        index = (int)(stat.count[0] >>> 3) & 0x3f;

        if ((stat.count[0] += (length << 3)) <
                (length << 3)) {
            stat.count[1]++;
        }

        stat.count[1] += length >>> 29;

        partlen = 64 - index;

        if (length >= partlen) {
            for (i = 0; i < partlen; i++) {
                stat.buffer[i + index] = buffer[i + offset];
            }

            Transform(stat, stat.buffer, 0);

            for (i = partlen; (i + 63) < length; i += 64) {
                Transform(stat, buffer, i);
            }

            index = 0;
        } else {
            i = 0;
        }

        /* buffer remaining input */
        if (i < length) {
            start = i;
            for (; i < length; i++) {
                stat.buffer[index + i - start] = buffer[i + offset];
            }
        }
    }

    /*
     * Update()s for other datatypes than byte[] also. Update(byte[], int)
     * is only the main driver.
     */

    /**
     * Plain update, updates this object
     */

    public void Update(byte buffer[], int offset, int length) {
        Update(this.state, buffer, offset, length);
    }

    public void Update(byte buffer[], int length) {
        Update(this.state, buffer, 0, length);
    }

    /**
     * Updates hash with given array of bytes
     *
     * @param buffer  Array of bytes to use for updating the hash
     */
    public void Update(byte buffer[]) {
        Update(buffer, 0, buffer.length);
    }

    /**
     * Updates hash with a single byte
     *
     * @param b       Single byte to update the hash
     */
    public void Update(byte b) {
        byte buffer[] = new byte[1];
        buffer[0] = b;

        Update(buffer, 1);
    }

    /**
     * Update buffer with given string.
     *
     * @param s       String to be update to hash (is used as
     *                s.getBytes())
     */
    public void Update(String s) {
        byte    chars[];

        /* deprecated chars = new byte[s.length()];
           s.getBytes(0, s.length(), chars, 0);
         */
        chars = s.getBytes();

        Update(chars, chars.length);
    }

    /**
     * Update buffer with a single integer (only & 0xff part is used,
     * as a byte)
     *
     * @param i       Integer value, which is then converted to
     *            byte as i & 0xff
     */

    public void Update(int i) {
        Update((byte)(i & 0xff));
    }

    private byte[] Encode(int input[], int len) {
        int     i, j;
        byte    out[];

        out = new byte[len];

        for (i = j = 0; j  < len; i++, j += 4) {
            out[j] = (byte)(input[i] & 0xff);
            out[j + 1] = (byte)((input[i] >>> 8) & 0xff);
            out[j + 2] = (byte)((input[i] >>> 16) & 0xff);
            out[j + 3] = (byte)((input[i] >>> 24) & 0xff);
        }

        return out;
    }

    /**
     * Returns array of bytes (16 bytes) representing hash as of the
     * current state of this object. Note: getting a hash does not
     * invalidate the hash object, it only creates a copy of the real
     * state which is finalized.
     *
     * @return    Array of 16 bytes, the hash of all updated bytes
     */
    public synchronized byte[] Final() {
        byte    bits[];
        int     index, padlen;
        MD5State    fin;

        if (finals == null) {
            fin = new MD5State(state);

            bits = Encode(fin.count, 8);

            index = (int)((fin.count[0] >>> 3) & 0x3f);
            padlen = (index < 56) ? (56 - index) : (120 - index);

            Update(fin, padding, 0, padlen);
            /**/
            Update(fin, bits, 0, 8);

            /* Update() sets finalds to null */
            finals = fin;
        }

        return Encode(finals.state, 16);
    }

    /**
     * Turns array of bytes into string representing each byte as
     * unsigned hex number.
     *
     * @param hash    Array of bytes to convert to hex-string
     * @return    Generated hex string
     */
    public static String asHex(byte hash[]) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if (((int) hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString((int) hash[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Returns 32-character hex representation of this objects hash
     *
     * @return String of this object's hash
     */
    public String asHex() {
        return asHex(this.Final());
    }

} // MD5

