/**
 * AMLProject.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema;

public class AMLProject  implements java.io.Serializable {
    private eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLFile;

    private eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLLinkedFile;

    private java.lang.String ID;

    private java.lang.String name;

    public AMLProject() {
    }

    public AMLProject(
           eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLFile,
           eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLLinkedFile,
           java.lang.String ID,
           java.lang.String name) {
           this.AMLFile = AMLFile;
           this.AMLLinkedFile = AMLLinkedFile;
           this.ID = ID;
           this.name = name;
    }


    /**
     * Gets the AMLFile value for this AMLProject.
     * 
     * @return AMLFile
     */
    public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] getAMLFile() {
        return AMLFile;
    }


    /**
     * Sets the AMLFile value for this AMLProject.
     * 
     * @param AMLFile
     */
    public void setAMLFile(eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLFile) {
        this.AMLFile = AMLFile;
    }

    public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription getAMLFile(int i) {
        return this.AMLFile[i];
    }

    public void setAMLFile(int i, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription _value) {
        this.AMLFile[i] = _value;
    }


    /**
     * Gets the AMLLinkedFile value for this AMLProject.
     * 
     * @return AMLLinkedFile
     */
    public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] getAMLLinkedFile() {
        return AMLLinkedFile;
    }


    /**
     * Sets the AMLLinkedFile value for this AMLProject.
     * 
     * @param AMLLinkedFile
     */
    public void setAMLLinkedFile(eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription[] AMLLinkedFile) {
        this.AMLLinkedFile = AMLLinkedFile;
    }

    public eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription getAMLLinkedFile(int i) {
        return this.AMLLinkedFile[i];
    }

    public void setAMLLinkedFile(int i, eu.skillpro.ams.pscm.connector.amlservice.gen.v2.schema.FileDescription _value) {
        this.AMLLinkedFile[i] = _value;
    }


    /**
     * Gets the ID value for this AMLProject.
     * 
     * @return ID
     */
    public java.lang.String getID() {
        return ID;
    }


    /**
     * Sets the ID value for this AMLProject.
     * 
     * @param ID
     */
    public void setID(java.lang.String ID) {
        this.ID = ID;
    }


    /**
     * Gets the name value for this AMLProject.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this AMLProject.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AMLProject)) return false;
        AMLProject other = (AMLProject) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.AMLFile==null && other.getAMLFile()==null) || 
             (this.AMLFile!=null &&
              java.util.Arrays.equals(this.AMLFile, other.getAMLFile()))) &&
            ((this.AMLLinkedFile==null && other.getAMLLinkedFile()==null) || 
             (this.AMLLinkedFile!=null &&
              java.util.Arrays.equals(this.AMLLinkedFile, other.getAMLLinkedFile()))) &&
            ((this.ID==null && other.getID()==null) || 
             (this.ID!=null &&
              this.ID.equals(other.getID()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getAMLFile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAMLFile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAMLFile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getAMLLinkedFile() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getAMLLinkedFile());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getAMLLinkedFile(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getID() != null) {
            _hashCode += getID().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(AMLProject.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "AMLProject"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("AMLFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "AMLFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "FileDescription"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("AMLLinkedFile");
        elemField.setXmlName(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "AMLLinkedFile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "FileDescription"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "ID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://xml.skillpro.org/schema/AMLFileDescription", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
