
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.3.2
 * 2012-11-13T15:21:18.350+08:00
 * Generated source version: 2.3.2
 * 
 */

@WebFault(name = "MultipleTopicsSpecifiedFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class MultipleTopicsSpecifiedFault extends Exception {
    public static final long serialVersionUID = 20121113152118L;
    
    private org.oasis_open.docs.wsn.b_2.MultipleTopicsSpecifiedFaultType multipleTopicsSpecifiedFault;

    public MultipleTopicsSpecifiedFault() {
        super();
    }
    
    public MultipleTopicsSpecifiedFault(String message) {
        super(message);
    }
    
    public MultipleTopicsSpecifiedFault(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleTopicsSpecifiedFault(String message, org.oasis_open.docs.wsn.b_2.MultipleTopicsSpecifiedFaultType multipleTopicsSpecifiedFault) {
        super(message);
        this.multipleTopicsSpecifiedFault = multipleTopicsSpecifiedFault;
    }

    public MultipleTopicsSpecifiedFault(String message, org.oasis_open.docs.wsn.b_2.MultipleTopicsSpecifiedFaultType multipleTopicsSpecifiedFault, Throwable cause) {
        super(message, cause);
        this.multipleTopicsSpecifiedFault = multipleTopicsSpecifiedFault;
    }

    public org.oasis_open.docs.wsn.b_2.MultipleTopicsSpecifiedFaultType getFaultInfo() {
        return this.multipleTopicsSpecifiedFault;
    }
}
