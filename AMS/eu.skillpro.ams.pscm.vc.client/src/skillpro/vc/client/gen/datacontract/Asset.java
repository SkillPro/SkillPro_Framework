/**
 * Asset.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package skillpro.vc.client.gen.datacontract;

import java.util.Arrays;

import skillpro.vc.csclient.VCClient;

public class Asset  implements java.io.Serializable {
    private java.lang.String VCID;

    private double[] boundingBox;

    private java.lang.String name;

    private double[] worldPositionMatrix;

    public Asset() {
    }

    public Asset(
           java.lang.String VCID,
           double[] boundingBox,
           java.lang.String name,
           double[] worldPositionMatrix) {
           this.VCID = VCID;
           this.boundingBox = boundingBox;
           this.name = name;
           this.worldPositionMatrix = worldPositionMatrix;
    }


    /**
     * Gets the VCID value for this Asset.
     * 
     * @return VCID
     */
    public java.lang.String getVCID() {
        return VCID;
    }


    /**
     * Sets the VCID value for this Asset.
     * 
     * @param VCID
     */
    public void setVCID(java.lang.String VCID) {
        this.VCID = VCID;
    }


    /**
     * Gets the boundingBox value for this Asset.
     * 
     * @return boundingBox
     */
    public double[] getBoundingBox() {
    	for (int i = 0; i < boundingBox.length; i++) {
    		boundingBox[i] /= 10;
    	}
        return boundingBox;
    }


    /**
     * Sets the boundingBox value for this Asset.
     * 
     * @param boundingBox
     */
    public void setBoundingBox(double[] boundingBox) {
        this.boundingBox = boundingBox;
    }


    /**
     * Gets the name value for this Asset.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Asset.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the worldPositionMatrix value for this Asset.
     * 
     * @return worldPositionMatrix
     */
    public double[] getWorldPositionMatrix() {
        return worldPositionMatrix;
    }


    /**
     * Sets the worldPositionMatrix value for this Asset.
     * 
     * @param worldPositionMatrix
     */
    public void setWorldPositionMatrix(double[] worldPositionMatrix) {
        this.worldPositionMatrix = worldPositionMatrix;
    }

    public void setWorldPositionMatrixSkillPro(double[] worldPositionMatrix) {
    	System.out.println("*********** " + Arrays.toString(this.worldPositionMatrix));
    	System.out.println("----------- " + Arrays.toString(worldPositionMatrix));
    	if (!Arrays.equals(this.worldPositionMatrix, worldPositionMatrix)) { 
	    	VCClient.getInstance().setAssetPosition(name, worldPositionMatrix[0], worldPositionMatrix[1], worldPositionMatrix[2], 0, 0, 0);
	        this.worldPositionMatrix = worldPositionMatrix;
    	}
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Asset)) return false;
        Asset other = (Asset) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.VCID==null && other.getVCID()==null) || 
             (this.VCID!=null &&
              this.VCID.equals(other.getVCID()))) &&
            ((this.boundingBox==null && other.getBoundingBox()==null) || 
             (this.boundingBox!=null &&
              java.util.Arrays.equals(this.boundingBox, other.getBoundingBox()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.worldPositionMatrix==null && other.getWorldPositionMatrix()==null) || 
             (this.worldPositionMatrix!=null &&
              java.util.Arrays.equals(this.worldPositionMatrix, other.getWorldPositionMatrix())));
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
        if (getVCID() != null) {
            _hashCode += getVCID().hashCode();
        }
        if (getBoundingBox() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getBoundingBox());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getBoundingBox(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getWorldPositionMatrix() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getWorldPositionMatrix());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getWorldPositionMatrix(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Asset.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/eu.skillpro.visualcomponents.server", "Asset"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("VCID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/eu.skillpro.visualcomponents.server", "VCID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("boundingBox");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/eu.skillpro.visualcomponents.server", "boundingBox"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/2003/10/Serialization/Arrays", "double"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/eu.skillpro.visualcomponents.server", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("worldPositionMatrix");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.datacontract.org/2004/07/eu.skillpro.visualcomponents.server", "worldPositionMatrix"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setItemQName(new javax.xml.namespace.QName("http://schemas.microsoft.com/2003/10/Serialization/Arrays", "double"));
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
