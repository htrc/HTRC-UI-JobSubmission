//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.14 at 10:57:43 AM EST 
//


package edu.indiana.d2i.sloan.schema.internal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for JobDescriptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="JobDescriptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="localUserId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="commandLine" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jobType" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}JobTypeEnumeration"/>
 *         &lt;element name="fileStageOut" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}TransferRequestType" maxOccurs="unbounded"/>
 *         &lt;element name="VMCount" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" minOccurs="0"/>
 *         &lt;element name="executionTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *         &lt;element name="tokenPath" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}TransferRequestType"/>
 *         &lt;element name="archive" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}TransferRequestType"/>
 *         &lt;element name="worksets" type="{http://www.globus.org/namespaces/2004/10/gram/job/description}WorkSets" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "JobDescriptionType", propOrder = {
    "localUserId",
    "commandLine",
    "jobType",
    "fileStageOut",
    "vmCount",
    "executionTime",
    "tokenPath",
    "archive",
    "worksets"
})
public class JobDescriptionType {

    @XmlElement(required = true)
    protected String localUserId;
    @XmlElement(required = true)
    protected String commandLine;
    @XmlElement(required = true)
    protected JobTypeEnumeration jobType;
    @XmlElement(required = true)
    protected List<TransferRequestType> fileStageOut;
    @XmlElement(name = "VMCount", defaultValue = "1")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger vmCount;
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar executionTime;
    @XmlElement(required = true)
    protected TransferRequestType tokenPath;
    @XmlElement(required = true)
    protected TransferRequestType archive;
    protected WorkSets worksets;

    /**
     * Gets the value of the localUserId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalUserId() {
        return localUserId;
    }

    /**
     * Sets the value of the localUserId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalUserId(String value) {
        this.localUserId = value;
    }

    /**
     * Gets the value of the commandLine property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * Sets the value of the commandLine property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCommandLine(String value) {
        this.commandLine = value;
    }

    /**
     * Gets the value of the jobType property.
     * 
     * @return
     *     possible object is
     *     {@link JobTypeEnumeration }
     *     
     */
    public JobTypeEnumeration getJobType() {
        return jobType;
    }

    /**
     * Sets the value of the jobType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JobTypeEnumeration }
     *     
     */
    public void setJobType(JobTypeEnumeration value) {
        this.jobType = value;
    }

    /**
     * Gets the value of the fileStageOut property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fileStageOut property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFileStageOut().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TransferRequestType }
     * 
     * 
     */
    public List<TransferRequestType> getFileStageOut() {
        if (fileStageOut == null) {
            fileStageOut = new ArrayList<TransferRequestType>();
        }
        return this.fileStageOut;
    }

    /**
     * Gets the value of the vmCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getVMCount() {
        return vmCount;
    }

    /**
     * Sets the value of the vmCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setVMCount(BigInteger value) {
        this.vmCount = value;
    }

    /**
     * Gets the value of the executionTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the value of the executionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExecutionTime(XMLGregorianCalendar value) {
        this.executionTime = value;
    }

    /**
     * Gets the value of the tokenPath property.
     * 
     * @return
     *     possible object is
     *     {@link TransferRequestType }
     *     
     */
    public TransferRequestType getTokenPath() {
        return tokenPath;
    }

    /**
     * Sets the value of the tokenPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransferRequestType }
     *     
     */
    public void setTokenPath(TransferRequestType value) {
        this.tokenPath = value;
    }

    /**
     * Gets the value of the archive property.
     * 
     * @return
     *     possible object is
     *     {@link TransferRequestType }
     *     
     */
    public TransferRequestType getArchive() {
        return archive;
    }

    /**
     * Sets the value of the archive property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransferRequestType }
     *     
     */
    public void setArchive(TransferRequestType value) {
        this.archive = value;
    }

    /**
     * Gets the value of the worksets property.
     * 
     * @return
     *     possible object is
     *     {@link WorkSets }
     *     
     */
    public WorkSets getWorksets() {
        return worksets;
    }

    /**
     * Sets the value of the worksets property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkSets }
     *     
     */
    public void setWorksets(WorkSets value) {
        this.worksets = value;
    }

}
