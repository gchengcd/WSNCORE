
package org.oasis_open.docs.wsn.bw_2;

import javax.xml.ws.WebFault;


/**
 * This class was generated by Apache CXF 2.3.2
 * 2012-11-13T15:21:18.353+08:00
 * Generated source version: 2.3.2
 * 
 */

@WebFault(name = "TopicExpressionDialectUnknownFault", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
public class TopicExpressionDialectUnknownFault extends Exception {
    public static final long serialVersionUID = 20121113152118L;
    
    private org.oasis_open.docs.wsn.b_2.TopicExpressionDialectUnknownFaultType topicExpressionDialectUnknownFault;

    public TopicExpressionDialectUnknownFault() {
        super();
    }
    
    public TopicExpressionDialectUnknownFault(String message) {
        super(message);
    }
    
    public TopicExpressionDialectUnknownFault(String message, Throwable cause) {
        super(message, cause);
    }

    public TopicExpressionDialectUnknownFault(String message, org.oasis_open.docs.wsn.b_2.TopicExpressionDialectUnknownFaultType topicExpressionDialectUnknownFault) {
        super(message);
        this.topicExpressionDialectUnknownFault = topicExpressionDialectUnknownFault;
    }

    public TopicExpressionDialectUnknownFault(String message, org.oasis_open.docs.wsn.b_2.TopicExpressionDialectUnknownFaultType topicExpressionDialectUnknownFault, Throwable cause) {
        super(message, cause);
        this.topicExpressionDialectUnknownFault = topicExpressionDialectUnknownFault;
    }

    public org.oasis_open.docs.wsn.b_2.TopicExpressionDialectUnknownFaultType getFaultInfo() {
        return this.topicExpressionDialectUnknownFault;
    }
}
