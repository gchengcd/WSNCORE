
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.3.2
 * 2012-11-13T15:21:18.315+08:00
 * Generated source version: 2.3.2
 * 
 */

@WebFault(name = "UnableToDestroyPullPointFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class UnableToDestroyPullPointFault extends Exception {
    public static final long serialVersionUID = 20121113152118L;
    
    private org.oasis_open.docs.wsn.b_2.UnableToDestroyPullPointFaultType unableToDestroyPullPointFault;

    public UnableToDestroyPullPointFault() {
        super();
    }
    
    public UnableToDestroyPullPointFault(String message) {
        super(message);
    }
    
    public UnableToDestroyPullPointFault(String message, Throwable cause) {
        super(message, cause);
    }

    public UnableToDestroyPullPointFault(String message, org.oasis_open.docs.wsn.b_2.UnableToDestroyPullPointFaultType unableToDestroyPullPointFault) {
        super(message);
        this.unableToDestroyPullPointFault = unableToDestroyPullPointFault;
    }

    public UnableToDestroyPullPointFault(String message, org.oasis_open.docs.wsn.b_2.UnableToDestroyPullPointFaultType unableToDestroyPullPointFault, Throwable cause) {
        super(message, cause);
        this.unableToDestroyPullPointFault = unableToDestroyPullPointFault;
    }

    public org.oasis_open.docs.wsn.b_2.UnableToDestroyPullPointFaultType getFaultInfo() {
        return this.unableToDestroyPullPointFault;
    }
}
