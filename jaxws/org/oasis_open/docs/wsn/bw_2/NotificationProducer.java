package org.oasis_open.docs.wsn.bw_2;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.3.2
 * 2012-11-13T15:21:18.414+08:00
 * Generated source version: 2.3.2
 * 
 */
 
@WebService(targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "NotificationProducer")
@XmlSeeAlso({org.oasis_open.docs.wsn.t_1.ObjectFactory.class, org.oasis_open.docs.wsn.br_2.ObjectFactory.class, org.oasis_open.docs.wsrf.r_2.ObjectFactory.class, org.oasis_open.docs.wsn.b_2.ObjectFactory.class, org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class, org.oasis_open.docs.wsrf.rp_2.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface NotificationProducer {

    @WebResult(name = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "SubscribeResponse")
    @WebMethod(operationName = "Subscribe")
    public org.oasis_open.docs.wsn.b_2.SubscribeResponse subscribe(
        @WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.Subscribe subscribeRequest
    ) throws InvalidTopicExpressionFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, InvalidProducerPropertiesExpressionFault, UnrecognizedPolicyRequestFault, TopicExpressionDialectUnknownFault, NotifyMessageNotSupportedFault, InvalidFilterFault, UnsupportedPolicyRequestFault, InvalidMessageContentExpressionFault, SubscribeCreationFailedFault, TopicNotSupportedFault, UnacceptableInitialTerminationTimeFault;

    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    public org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse getCurrentMessage(
        @WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        org.oasis_open.docs.wsn.b_2.GetCurrentMessage getCurrentMessageRequest
    ) throws InvalidTopicExpressionFault, org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault, TopicExpressionDialectUnknownFault, MultipleTopicsSpecifiedFault, NoCurrentMessageOnTopicFault, TopicNotSupportedFault;
}
