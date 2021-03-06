/*
#
# Copyright 2007 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: HTRC Sloan job submission web interface
# File:  TransferRequestType.java
# Description:  
#
# -----------------------------------------------------------------
# 
 */


// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.14 at 10:57:43 AM EST 
//


package edu.indiana.d2i.sloan.schema.internal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TransferRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransferRequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="srcPath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="srcType" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}TransferType"/>
 *         &lt;element name="destPath" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="destType" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}TransferType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransferRequestType", propOrder = {
    "srcPath",
    "srcType",
    "destPath",
    "destType"
})
public class TransferRequestType {

    @XmlElement(required = true)
    protected String srcPath;
    @XmlElement(required = true)
    protected TransferType srcType;
    @XmlElement(required = true)
    protected String destPath;
    @XmlElement(required = true)
    protected TransferType destType;

    /**
     * Gets the value of the srcPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSrcPath() {
        return srcPath;
    }

    /**
     * Sets the value of the srcPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSrcPath(String value) {
        this.srcPath = value;
    }

    /**
     * Gets the value of the srcType property.
     * 
     * @return
     *     possible object is
     *     {@link TransferType }
     *     
     */
    public TransferType getSrcType() {
        return srcType;
    }

    /**
     * Sets the value of the srcType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransferType }
     *     
     */
    public void setSrcType(TransferType value) {
        this.srcType = value;
    }

    /**
     * Gets the value of the destPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestPath() {
        return destPath;
    }

    /**
     * Sets the value of the destPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestPath(String value) {
        this.destPath = value;
    }

    /**
     * Gets the value of the destType property.
     * 
     * @return
     *     possible object is
     *     {@link TransferType }
     *     
     */
    public TransferType getDestType() {
        return destType;
    }

    /**
     * Sets the value of the destType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransferType }
     *     
     */
    public void setDestType(TransferType value) {
        this.destType = value;
    }

}
