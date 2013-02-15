//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.14 at 10:56:40 AM EST 
//


package edu.indiana.d2i.sloan.schema.user;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for JobTypeEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="JobTypeEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="pig"/>
 *     &lt;enumeration value="mapreduce"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "JobTypeEnumeration")
@XmlEnum
public enum JobTypeEnumeration {

    @XmlEnumValue("pig")
    PIG("pig"),
    @XmlEnumValue("mapreduce")
    MAPREDUCE("mapreduce");
    private final String value;

    JobTypeEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static JobTypeEnumeration fromValue(String v) {
        for (JobTypeEnumeration c: JobTypeEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}