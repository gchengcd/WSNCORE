
/*
 * 
 */

package org.apache.servicemix.wsn.jaxws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;

/**
 * This class was generated by Apache CXF 2.3.2
 * 2012-11-13T15:21:18.444+08:00
 * Generated source version: 2.3.2
 * 
 */


@WebServiceClient(name = "NotificationProducerService", 
                  wsdlLocation = "file:/E:/wenpeng/compile/compile/servicemix-wsn2005-2011.01-MutiThread-publish/src/main/resources/org/apache/servicemix/wsn/wsn.wsdl",
                  targetNamespace = "http://servicemix.apache.org/wsn/jaxws") 
public class NotificationProducerService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://servicemix.apache.org/wsn/jaxws", "NotificationProducerService");
    public final static QName JBI = new QName("http://servicemix.apache.org/wsn/jaxws", "JBI");
    static {
        URL url = null;
        try {
            url = new URL("file:/E:/wenpeng/compile/compile/servicemix-wsn2005-2011.01-MutiThread-publish/src/main/resources/org/apache/servicemix/wsn/wsn.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:/E:/wenpeng/compile/compile/servicemix-wsn2005-2011.01-MutiThread-publish/src/main/resources/org/apache/servicemix/wsn/wsn.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public NotificationProducerService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public NotificationProducerService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public NotificationProducerService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     * 
     * @return
     *     returns NotificationProducer
     */
    @WebEndpoint(name = "JBI")
    public NotificationProducer getJBI() {
        return super.getPort(JBI, NotificationProducer.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns NotificationProducer
     */
    @WebEndpoint(name = "JBI")
    public NotificationProducer getJBI(WebServiceFeature... features) {
        return super.getPort(JBI, NotificationProducer.class, features);
    }

}
