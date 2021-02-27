package com.joaovrmaia;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.CommunityTarget;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.Collections;
import java.util.Enumeration;

public class Main {
    public static void main(String []args) throws Exception {
        Address addressTarget = GenericAddress.parse("udp:0.0.0.0/1610");
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(addressTarget);
        target.setRetries(3);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID("1 ")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1.0")));
        pdu.add(new VariableBinding(new OID("1.3.6.1.4.1.8072.3.2.10")));

        pdu.setType(PDU.GETNEXT);

        ResponseEvent response = snmp.send(pdu, target, transport);
        if (response != null) {
            PDU responsePDU = response.getResponse();

            if (responsePDU != null) {
                int errorStatus = responsePDU.getErrorStatus();
                int errorIndex = responsePDU.getErrorIndex();
                String errorStatusText = responsePDU.getErrorStatusText();

                if (errorStatus == PDU.noError)  {
                    Enumeration<? extends VariableBinding> elements = responsePDU.getVariableBindings().elements();
                    for (VariableBinding binding : Collections.list(elements)) {
                        System.out.println(String.format("%s: %s", binding.getOid(), binding.getVariable()));
                    }
                } else {
                    System.out.println(String.format("status:%s index:%s text:%s", errorStatus, errorIndex, errorStatusText));
                }
            } else {
                System.out.println("PDU is null");
            }
        } else {
            System.out.println("Agent Timeout");
        }
        snmp.close();
    }
}
