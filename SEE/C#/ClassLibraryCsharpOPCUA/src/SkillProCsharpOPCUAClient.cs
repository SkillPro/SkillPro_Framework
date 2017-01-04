/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: Boris Bocquet, email: b.bocquet@akeoplus.com
 *
 * Date of creation: 2016
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the SkillPro Framework. The SkillPro Framework
 * is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework.  If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

using System;
using System.Collections.Generic;
using System.Linq;
using System.ComponentModel;
using System.Text;

using UnifiedAutomation.UaBase;
using UnifiedAutomation.UaClient;

namespace eu.skillpro.opcua.client.CS
{
    /*! 
*  \brief      A C# wrapper of unified automation client design for the SkillPro project
*  \details    A C# wrapper of unified automation client design for the SkillPro project
*  \author    Boris Bocquet, AKÉO PLUS SAS, France
*  \version   1.0.0
*  \date      2016/10/05
*  \copyright LGPL , SkillPro 7FP EU
*  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
*  \pre       .
*  \bug       .
*  \warning   .
*  \todo	.
*/
    public class SkillProCsharpOPCUAClient
    {


        //####################################### CONST ################################################################""
        //####################################################################################################################"

        public const string SECURITY_POLICY_NONE_URI = "http://opcfoundation.org/UA/SecurityPolicy#None";

        public const string BOOLEAN_TRUE = "true";
        public const string BOOLEAN_FALSE = "false";

        //####################################### ATTRIBUTES ################################################################""
        //####################################################################################################################"

        private ApplicationInstance m_application;

        private Session m_session;

        private Subscription m_subscription;

        private RequestSettings _SettingsRequest = new RequestSettings();

        private bool m_IsConnected; //< A read-only boolean to know if the client is connected or not

        #region remapping of the events

        /// <summary>
        /// The event handler to subscribe your delegate method. This event is raised on every status changes
        /// </summary>
        public event ServerConnectionStatusUpdateEventHandler ConnectionStatusUpdate;

        /// <summary>
        /// The event handler to subscribe your delegate method. This event is raised on monitored node data changes
        /// </summary>
        public event DataChangedEventHandler DataChanged;

        #endregion //remapping of the events

        //####################################### Properties ################################################################""
        //####################################################################################################################"


        public ApplicationInstance Application
        //Application
        {
            get { return m_application; }
            set { m_application = value; }
        }

        //Session
        public Session Session
        {
            get { return m_session; }
            set { m_session = value; }
        }

        // Subscription
        public Subscription Subscription
        {
            get { return m_subscription; }
            set { m_subscription = value; }
        }

        /// <summary>
        /// A read-only boolean to know if the client is connected or not
        /// </summary>
        public bool IsConnected
        {
            get { return m_IsConnected; }
            //set { m_IsConnected = value; }
        }

        public RequestSettings SettingsRequest
        {
            get { return _SettingsRequest; }
            set { _SettingsRequest = value; }
        }

        //####################################### PUBLIC METHODES ###################################################################"
        //####################################################################################################################"

        #region constructor

        ///\brief The default constructor.
        ///\note client won't be availble until you call Connect() method. Certificate is created automatically.

        /// <summary>
        /// The default constructor. Remark that your client won't be availble until you call Connect() method. Certificate is created automatically.
        /// </summary>
        public SkillProCsharpOPCUAClient()
        {
            m_session = null;
            m_IsConnected = false;
            m_subscription = null;

            m_application = new ApplicationInstance();
            m_application.AutoCreateCertificate = true;
            // Create the certificate if it does not exist yet
            m_application.Start();
        }

        #endregion // constructor

        #region connection / disconnection

        ///\brief Connect to the UA server, subscribe to status changes and returns the namespace table
        ///\note We could use a connect method by passsing the Endpoint

        /// <summary>
        /// Connect to the UA server and returns the namespace table.
        /// Pass the method to subscribe to the status change event.
        /// The connection is based on the Server URL entered in input.
        /// </summary>
        /// <param name="server_URL">The URL of the Server</param>
        /// <param name="aServerConnectionStatusHandler"> The connection Status Handler you want to associate with your client (to get notification on the status)</param>
        /// <param name="allNameSpaces">The found namespaces at the server URL</param>
        public void connect(string server_URL, ServerConnectionStatusUpdateEventHandler aServerConnectionStatusHandler, out List<string> allNameSpaces)
        {
            connect(server_URL, SECURITY_POLICY_NONE_URI, aServerConnectionStatusHandler, out allNameSpaces);
        }

        public void connect(string server_URL, string SecurityPolicyUri, ServerConnectionStatusUpdateEventHandler aServerConnectionStatusHandler, out List<string> allNameSpaces)
        {
            allNameSpaces = new List<string>();

            if (!m_IsConnected)
            {
                try
                {
                    if (m_session == null)
                    {
                        // Create a session
                        m_session = new Session(m_application);
                        //m_session.UseDnsNameAndPortFromDiscoveryUrl.Equals(true);
                        m_session.UseDnsNameAndPortFromDiscoveryUrl = true;

                        #region remap (propagate) the event "ConnectionStatusUpdate"

                        //remap events
                        m_session.ConnectionStatusUpdate += session_ConnectionStatusUpdate;

                        // attach to events
                        this.ConnectionStatusUpdate += aServerConnectionStatusHandler;

                        #endregion //remap (propagate) the event "ConnectionStatusUpdate"

                    }

                    // Step 1 ----------------------------------------------------
                    // Connect to the server with no security
                    EndpointDescription DescriptionOfEndpoint = new EndpointDescription(server_URL);
                    DescriptionOfEndpoint.SecurityPolicyUri = SecurityPolicyUri;

                    //m_session.Connect(server_URL, SecuritySelection.None, _SettingsRequest);
                    m_session.Connect(DescriptionOfEndpoint, _SettingsRequest);
                }
                catch (Exception ex)
                {
                    throw ex;
                }

                //if code passes here, this mean that no exceptions were fired during connection
            }

            // Step 2 ----------------------------------------------------
            // Return all nameSpaces

            for (int i = 0; i < m_session.NamespaceUris.Count; i++)
            {
                allNameSpaces.Add(m_session.NamespaceUris[i]);
            }
            m_IsConnected = true;
        }

        ///\brief Disconnect from the UA server

        /// <summary>
        /// Disconnect from the UA server.
        /// Stops subscriptions.
        /// </summary>
        public void disconnect()
        {
            try
            {
                // Call the disconnect service of the server and stops subscription
                if (m_subscription != null)
                {
                    m_subscription.Delete(_SettingsRequest);
                    m_subscription = null;
                }

            }
            catch (Exception exception)
            {
                throw exception;
            }

            try
            {

                m_session.Disconnect(SubscriptionCleanupPolicy.Delete, _SettingsRequest);
                m_IsConnected = false;

                //m_session.BeginDisconnect(SubscriptionCleanupPolicy.Delete, _SettingsRequest, OnBeginDisconnectCompleted, new object());

            }
            catch (Exception exception)
            {

                //throw exception;

                //Try to dispose

                try
                {
                    m_session.Dispose();
                    m_session = null;
                    GC.Collect();
                }
                catch (Exception ex2)
                {
                    Exception concat = new Exception(ex2.Message, exception);
                    throw concat;
                }
            }

        }

        #endregion //connection / disconnection

        #region Endpoints

        public List<EndpointDescription> DiscoveryGetEndpoints(string serverAddress)
        {
            try
            {
                Discovery discovery = new Discovery();
                discovery.DefaultRequestSettings = _SettingsRequest;
                return discovery.GetEndpoints(serverAddress);
            }
            catch (Exception e)
            {
                throw e;
            }
        }

        #endregion //Endpoints

        #region read / write

        ///\brief Reads the values of the variables on the server thanks to the identifiers passed in arguments. In this case, the identifiers are strings.

        /// <summary>
        /// Reads the values of the variables on the server thanks to the identifiers passed in arguments.
        /// </summary>
        /// <param name="nodesIdentifiers">the identifiers of each node. For instance "1004.AML_Description"</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="allReturnedValues">the values of each node</param>
        /// <param name="allStatusCodes">all the status codes associated with the returned values. Read Unified Automation Help to get error codes</param>
        /// <returns>If FALSE : the client was not connected, outputs are not relevants. Otherwise TRUE</returns>
        public bool readNodeValue(List<string> nodesIdentifiers, ushort namespaceIndex, out List<object> allReturnedValues, out List<uint> allStatusCodes)
        {
            return ReadNodeValue<string>(nodesIdentifiers, namespaceIndex, out allReturnedValues, out allStatusCodes);
        }

        ///\brief Reads the values of the variables on the server thanks to the identifiers passed in arguments

        /// <summary>
        /// Reads the values of the variables on the server thanks to the identifiers passed in arguments. In this case, the identifiers are unint (numeric).
        /// </summary>
        /// <param name="nodesIdentifiers">the identifiers of each node. In this case, identifiers are numerics, such as 1002</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="allReturnedValues">the values of each node</param>
        /// <param name="allStatusCodes">all the status codes associated with the returned values. Read Unified Automation Help to get error codes</param>
        /// <returns>If FALSE : the client was not connected, outputs are not relevants. Otherwise TRUE</returns>
        public bool readNodeValue(List<uint> nodesIdentifiers, ushort namespaceIndex, out List<object> allReturnedValues, out List<uint> allStatusCodes)
        {
            return ReadNodeValue<uint>(nodesIdentifiers, namespaceIndex, out allReturnedValues, out allStatusCodes);
        }

        ///\brief Writes entered values to specified nodes.

        /// <summary>
        /// Writes entered values to specified nodes. In this case, the passed nodesIdentifiers are strings. 
        /// </summary>
        /// <param name="nodesIdentifiers">The identifiers of each node. In this case , the identifiers are strings, for instance "1004.AML_Description"</param>
        /// <param name="valuesToWrite">The list of values to write.</param>
        /// <param name="namespaceIndex"></param>
        /// <param name="OpTimeout">Timeout setting for value reading </param>
        /// <param name="allStatusCode">All the status codes associated with teh returned values. If allStatusCode[i]==1 => client was not connected. If allStatusCode[i]==2 => could not cast (parse) the value to write. Other codes : refer to Unified Automation Documentation  </param>
        /// <returns>TRUE : if nodes were correctly written. The outputs are relevant. FALSE : otherwise, the ouputs are NOT relevant</returns>
        public bool writeNodeValue(List<string> nodesIdentifiers, List<string> valuesToWrite, ushort namespaceIndex, int OpTimeout, out List<uint> allStatusCode)
        {
            return WriteNodeValue<string>(nodesIdentifiers, valuesToWrite, namespaceIndex, OpTimeout, out allStatusCode);
        }

        ///\brief Writes entered values to specified nodes.

        /// <summary>
        /// Writes entered values to specified nodes. In this case, the passed nodesIdentifiers are strings. 
        /// </summary>
        /// <param name="nodesIdentifiers">The identifiers of each node. In this case , the identifiers are uint (numerics), for instance 1002</param>
        /// <param name="valuesToWrite">The list of values to write.</param>
        /// <param name="namespaceIndex"></param>
        /// <param name="OpTimeout">Timeout setting for value reading </param>
        /// <param name="allStatusCode">All the status codes associated with teh returned values. If allStatusCode[i]==1 => client was not connected. If allStatusCode[i]==2 => could not cast (parse) the value to write. Other codes : refer to Unified Automation Documentation  </param>
        /// <returns>TRUE : if nodes were correctly written. The outputs are relevant. FALSE : otherwise, the ouputs are NOT relevant</returns>
        public bool writeNodeValue(List<uint> nodesIdentifiers, List<string> valuesToWrite, ushort namespaceIndex, int OpTimeout, out List<uint> allStatusCode)
        {
            return WriteNodeValue<uint>(nodesIdentifiers, valuesToWrite, namespaceIndex, OpTimeout, out allStatusCode);
        }

        #endregion //read / write

        #region Monitoring

        ///\brief {Managing the Monitoring design of Unified Automation}

        /// <summary>
        /// Method to manage the monitoring of a node in the OPC-UA server. In this case, the Node identifiers are strings
        /// </summary>
        /// <param name="NodesIdentifiers">The identifiers of each node. In this case , the identifiers are strings, for instance "1004.AML_Description"</param>
        /// <param name="textboxes">The textboxes were the updated value of the node will be displayed (monitored)</param>
        /// <param name="aDataChangedEventHandler">The delegate method that subscribes to the data changed event</param>
        /// <param name="NamespaceIndex"></param>
        /// <param name="samplingInterval"></param>
        /// <param name="allStatusCode"></param>
        /// <returns>TRUE : if nodes were correctly monitored. The outputs are relevant. FALSE : you already subscribed to nodes before => unsubscribe first! Output codes are not relevant</returns>
        public bool StartMonitoring(List<string> NodesIdentifiers, List<object> textboxes, DataChangedEventHandler aDataChangedEventHandler, ushort NamespaceIndex, double samplingInterval, out List<uint> allStatusCode)
        {
            return StartMonitoring<string>(NodesIdentifiers, textboxes, aDataChangedEventHandler, NamespaceIndex, samplingInterval, out allStatusCode);
        }

        ///\brief {Managing the Monitoring design of Unified Automation}

        /// <summary>
        /// Method to manage the monitoring of a node in the OPC-UA server. In this case, the Node identifiers are strings
        /// </summary>
        /// <param name="NodesIdentifiers">The identifiers of each node. In this case , the identifiers are unint (numeric), for instance 1002</param>
        /// <param name="textboxes">The textboxes were the updated value of the node will be displayed (monitored)</param>
        /// <param name="aDataChangedEventHandler">The delegate method that subscribes to the data changed event</param>
        /// <param name="NamespaceIndex"></param>
        /// <param name="samplingInterval"></param>
        /// <param name="allStatusCode"></param>
        /// <returns>TRUE : if nodes were correctly monitored. The outputs are relevant. FALSE : you already subscribed to nodes before => unsubscribe first! Output codes are not relevant</returns>
        public bool StartMonitoring(List<uint> NodesIdentifiers, List<object> textboxes, DataChangedEventHandler aDataChangedEventHandler, ushort NamespaceIndex, double samplingInterval, out List<uint> allStatusCode)
        {
            return StartMonitoring<uint>(NodesIdentifiers, textboxes, aDataChangedEventHandler, NamespaceIndex, samplingInterval, out allStatusCode);
        }


        /// <summary>
        /// Stop monitoring by deleting the subscription.
        /// Monitored Items are also deleted.
        /// </summary>
        public void StopMonitoring()
        {
            if (m_subscription != null)
            {
                m_subscription.Delete(_SettingsRequest);
                m_subscription = null;
            }
        }

        #endregion //Monitoring

        #region Subscription

        ///\brief Update Subscription

        /// <summary>
        /// Update Subscription.
        /// </summary>
        /// <param name="publishingEnabled"> Boolean parameter indicating whether publishing is enabled</param>
        /// <param name="publishingInterval"> The publishing interval in milliseconds</param>
        public void UpdateSubscription(bool publishingEnabled, uint publishingInterval)
        {
            if (m_subscription != null)
            {
                m_subscription.PublishingEnabled = publishingEnabled;
                m_subscription.PublishingInterval = publishingInterval;
                m_subscription.Modify(_SettingsRequest);
            }
        }


        #endregion //Subscription

        #region Call


        ///brief Synchronously call a method on the server.

        /// <summary>
        /// Synchronously call a method on the server.
        /// </summary>
        /// <param name="ObjectIdentifier">Identifier of the object's node</param>
        /// <param name="ObIdIsString">Selected Namespace URI</param>
        /// <param name="selectedMethodId">Identifier of the method's node</param>
        /// <param name="slctdMthdIsString">TRUE : the method Identifier is String. FALSE : the method Identifier is unint (numeric)</param>
        /// <param name="namespaceIndex">Selected Namespace URI</param>
        /// <param name="inputArguments">The input arguments of for the method that will be called</param>
        /// <param name="timeOut">The timeout for the operation in ms</param>
        /// <param name="inputArgumentErrors">The errors associated with the input arguments</param>
        /// <param name="outputArguments">The outputs of the called method</param>
        /// <param name="errorCode">The error code of the code</param>
        /// <returns>TRUE : the call was sucessfull, outputs are relevants. FALSE : the call was UNsucessfull and outputs might NOT be relevant</returns>
        public bool CallSynchronously(object ObjectIdentifier, bool ObIdIsString, object selectedMethodId, bool slctdMthdIsString, ushort namespaceIndex,
            List<Variant> inputArguments, int timeOut, out List<uint> inputArgumentErrors, out List<Variant> outputArguments, out uint errorCode)
        {
            try
            {
                // nothing to do if no session.
                if (m_session == null)
                {
                    inputArgumentErrors = null;
                    outputArguments = null;
                    errorCode = 1;
                    return false;
                }

                // get the object id according to the IdType.
                NodeId objectId = null;
                if (!ObIdIsString)
                {
                    objectId = new NodeId(IdType.Numeric, ObjectIdentifier, namespaceIndex);
                }
                else
                {
                    objectId = new NodeId(IdType.String, ObjectIdentifier, namespaceIndex);
                }

                // get the selected method id according to the IdType.
                NodeId methodId = null;
                if (!slctdMthdIsString)
                {
                    methodId = new NodeId(IdType.Numeric, selectedMethodId, namespaceIndex);
                }
                else
                {
                    methodId = new NodeId(IdType.String, selectedMethodId, namespaceIndex);
                }

                // call the method on the server.
                List<StatusCode> UAinputErrors;
                StatusCode error = m_session.Call(
                    objectId,
                    methodId,
                    inputArguments,
                   _SettingsRequest,
                    out UAinputErrors,
                    out outputArguments);

                errorCode = error.Code;

                //return more conventionnal error types
                inputArgumentErrors = new List<uint>();
                foreach (StatusCode sc in UAinputErrors)
                {
                    inputArgumentErrors.Add(sc.Code);
                }

                // check for error.
                // A method always returns a status code indicating success or failure
                if (StatusCode.IsBad(error))
                {
                    return false;
                }

                return true;
            }
            catch (Exception exception)
            {
                inputArgumentErrors = null;
                outputArguments = null;
                errorCode = 2;
                throw exception;
            }
        }

        #endregion //Call

        #region utils

        /// <summary>
        /// Method to get the string representation of your boolean (to write on the OPC-UA)
        /// </summary>
        /// <param name="booleanValue">true or false</param>
        /// <returns>"true" or "false"</returns>
        public string BooleanToString(bool booleanValue)
        {
            if (booleanValue)
                return BOOLEAN_TRUE;
            else
                return BOOLEAN_FALSE;
        }

        #endregion //utils

        //####################################### PRIVATE METHODES ###################################################################"
        //####################################################################################################################"

        //private void OnBeginDisconnectCompleted(IAsyncResult result)
        //{
        //    m_session.EndDisconnect(result);
        //    m_IsConnected = false;
        //}

        #region read / write

        ///\brief Writes entered values to specified nodes.

        /// <summary>
        /// Writes entered values to specified nodes. In this case, the passed nodesIdentifiers are strings. 
        /// </summary>
        /// <param name="nodesIdentifiers">The identifiers of each node. In this case , the identifiers are either uint (numerics) or string, for instance 1002</param>
        /// <param name="valuesToWrite">The list of values to write.</param>
        /// <param name="namespaceIndex"></param>
        /// <param name="OpTimeout">Timeout setting for value reading </param>
        /// <param name="allStatusCode">All the status codes associated with teh returned values. If allStatusCode[i]==1 => client was not connected. If allStatusCode[i]==2 => could not cast (parse) the value to write. Other codes : refer to Unified Automation Documentation  </param>
        /// <returns>TRUE : if nodes were correctly written. The outputs are relevant. FALSE : otherwise, the ouputs are NOT relevant</returns>
        private bool WriteNodeValue<T>(List<T> nodesIdentifiers, List<string> valuesToWrite, ushort namespaceIndex, int OpTimeout, out List<uint> allStatusCode)
        {
            try
            {
                // The parameter 'allStatusCode' must be asigned before control leaves the current method
                allStatusCode = new List<uint>();

                // nothing to do if not connected.
                if (!m_IsConnected)
                {
                    // TODO : or not because because it is a Unified Automation Status Code
                    allStatusCode.Add(1);
                    return false;
                }

                int i = -1;
                List<WriteValue> nodesToWrite = new List<WriteValue>();

                foreach (object nodeId in nodesIdentifiers)
                {
                    i++;
                    // Get value from the specified index of the 'valuesToWrite' list
                    string crValue = valuesToWrite.ElementAt(i);

                    //If value is empty, nothing happens.
                    if (!string.IsNullOrEmpty(crValue))
                    {
                        //create the node
                        NodeId NodeId;

                        //Get datatype of the node and convert the written value to the specified target type
                        DataValue theResult;
                        BuiltInType theType;

                        bool test;

                        if (nodeId.GetType() == typeof(string))
                        {
                            test = ReadDataTypes((string)nodeId, namespaceIndex, out theResult, out theType);
                            NodeId = new NodeId(IdType.String, nodeId, namespaceIndex);
                        }
                        else if (nodeId.GetType() == typeof(uint))
                        {
                            test = ReadDataTypes((uint)nodeId, namespaceIndex, out theResult, out theType);
                            NodeId = new NodeId(IdType.Numeric, nodeId, namespaceIndex);
                        }
                        else
                        {
                            throw new NotImplementedException("The given type in writeNodeValue (" + nodeId.GetType().ToString() + ") is not implemented");
                        }


                        //Check if the node exist, if yes, write the value

                        if (!test)
                        {
                            allStatusCode.Add(2);
                            return false;
                        }


                        //if code passes here, this mean that node has been read and we can write its value

                        DataValue val = new DataValue();
                        val.Value = TypeUtils.Cast(crValue, theType);

                        nodesToWrite.Add(new WriteValue()
                        {
                            NodeId = NodeId,
                            AttributeId = Attributes.Value,
                            Value = val
                        });
                    }
                }


                // read the value (setting a timeout in milliseconds).
                List<StatusCode> allUnifiedAutomationStatusCode;

                allUnifiedAutomationStatusCode = m_session.Write(
                    nodesToWrite,
                    new RequestSettings() { OperationTimeout = OpTimeout });


                //Wrap the status code into more conventionnal types
                foreach (StatusCode sc in allUnifiedAutomationStatusCode)
                {
                    allStatusCode.Add(sc.Code);
                }


                return true;

            }
            catch (Exception exception)
            {
                throw exception;
            }
        }

        ///\brief Reads the values of the variables on the server thanks to the identifiers passed in arguments

        /// <summary>
        /// Reads the values of the variables on the server thanks to the identifiers passed in arguments. In this case, the identifiers are either uint (numeric) or string.
        /// </summary>
        /// <param name="nodesIdentifiers">the identifiers of each node. In this case, identifiers are numerics, such as 1002</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="allReturnedValues">the values of each node</param>
        /// <param name="allStatusCodes">all the status codes associated with the returned values. Read Unified Automation Help to get error codes</param>
        /// <returns>If FALSE : the client was not connected, outputs are not relevants. Otherwise TRUE</returns>
        private bool ReadNodeValue<T>(List<T> nodesIdentifiers, ushort namespaceIndex, out List<object> allReturnedValues, out List<uint> allStatusCodes)
        {
            allReturnedValues = new List<object>();
            allStatusCodes = new List<uint>();

            if (m_IsConnected)
            {
                // Step 1 --------------------------------------------------
                // Prepare nodes to read
                ReadValueIdCollection nodesToRead = new ReadValueIdCollection();

                foreach (object id in nodesIdentifiers)
                {

                    //create the node
                    NodeId curNodeId;

                    if (id.GetType() == typeof(string))
                    {
                        curNodeId = new NodeId(IdType.String, (string)id, namespaceIndex);
                    }
                    else if (id.GetType() == typeof(uint))
                    {
                        curNodeId = new NodeId(IdType.Numeric, (uint)id, namespaceIndex);
                    }
                    else
                    {
                        throw new NotImplementedException("The given type in ReadNodeValue (" + id.GetType().ToString() + ") is not implemented");
                    }

                    //create the read value
                    ReadValueId rvId = new ReadValueId();

                    //set all the required fields of the ReadValue
                    rvId.AttributeId = Attributes.Value;
                    rvId.NodeId = curNodeId;

                    //Append to list
                    nodesToRead.Add(rvId);

                }


                // Step 2 --------------------------------------------------
                // Read the values from the server
                List<DataValue> results = m_session.Read(nodesToRead);


                // Step 3  --------------------------------------------------
                //Wrap the values into more conventionnal types
                foreach (DataValue dv in results)
                {
                    allReturnedValues.Add(dv.Value);
                    allStatusCodes.Add(dv.StatusCode.Code);
                }

                return true;
            }
            else
                return false;
        }

        #endregion //read / write

        #region data type

        //\brief Reads dataType of a node.

        /// <summary>
        /// Reads dataType of a node. In this case, the passed nodeIdentifier are strings
        /// </summary>
        /// <param name="nodesIdentifier">the identifiers of each node. In this case, the identifiers are strings, for instance "1004.AML_Description"</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="result">The values of each node in input</param>
        /// <param name="builtInType">The type of each node in input</param>
        /// <returns>TRUE : if node exist and types were correctly read. FALSE otherwise (outputs are not relevants)</returns>
        private bool ReadDataTypes(string nodesIdentifier, ushort namespaceIndex, out DataValue result, out BuiltInType builtInType)
        {
            return ReadDataTypes<string>(nodesIdentifier, namespaceIndex, out result, out builtInType);
        }

        //\brief Reads dataType of a node.

        /// <summary>
        /// Reads dataType of a node. In this case, the passed nodeIdentifier are unint (numeric)
        /// </summary>
        /// <param name="nodesIdentifier">the identifiers of each node. In this case, the identifiers are unint (numeric), for instance 1002</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="result">The values of each node in input</param>
        /// <param name="builtInType">The type of each node in input</param>
        /// <returns>TRUE : if node exist and types were correctly read. FALSE otherwise (outputs are not relevants)</returns>
        private bool ReadDataTypes(uint nodesIdentifier, ushort namespaceIndex, out DataValue result, out BuiltInType builtInType)
        {
            return ReadDataTypes<uint>(nodesIdentifier, namespaceIndex, out result, out builtInType);
        }


        /// <summary>
        /// Reads dataType of a node. In this case, the passed nodeIdentifier are either uint (numeric) or string
        /// </summary>
        /// <param name="nodesIdentifier">the identifiers of each node. In this case, the identifiers are unint (numeric), for instance 1002</param>
        /// <param name="namespaceIndex">the index of the desired namespace</param>
        /// <param name="result">The values of each node in input</param>
        /// <param name="builtInType">The type of each node in input</param>
        /// <returns>TRUE : if node exist and types were correctly read. FALSE otherwise (outputs are not relevants)</returns>
        private bool ReadDataTypes<T>(T nodesIdentifier, ushort namespaceIndex, out DataValue result, out BuiltInType builtInType)
        {
            // Add the two variable NodeIds to the list of nodes to read
            // NodeId is constructed from
            // - the identifier text in the text box
            // - the namespace index collected during the server connect
            ReadValueIdCollection nodesToRead = new ReadValueIdCollection();

            if (nodesIdentifier.GetType() == typeof(string))
            {
                string nodeItStr = nodesIdentifier.ToString();
                nodesToRead.Add(new ReadValueId()
                {
                    NodeId = new NodeId(nodeItStr, namespaceIndex),
                    AttributeId = Attributes.DataType
                });
            }
            else if (nodesIdentifier.GetType() == typeof(uint))
            {
                uint nodesIdentifier_uint = (uint)Convert.ChangeType(nodesIdentifier, typeof(uint));

                nodesToRead.Add(new ReadValueId()
                {
                    NodeId = new NodeId(nodesIdentifier_uint, namespaceIndex),
                    AttributeId = Attributes.DataType
                });
            }
            else
            {
                throw new NotImplementedException("The given type in readDataTypes (" + nodesIdentifier.GetType().ToString() + ") is not implemented");
            }


            // Read the datatypes
            List<DataValue> results = new List<DataValue>();

            try
            {
                results = m_session.Read(nodesToRead, 0, TimestampsToReturn.Neither, null);

                // check the result code
                if (StatusCode.IsGood(results[0].StatusCode))
                {
                    // The node succeeded - save buildInType for later use
                    builtInType = TypeUtils.GetBuiltInType((NodeId)results[0].Value);
                    result = results[0];

                    //end of method
                    //results are valid
                    return true;
                }
                else
                {
                    builtInType = new BuiltInType();
                    result = new DataValue();

                    //end of method
                    //results are not valid
                    return false;
                }



            }
            catch (Exception exception)
            {
                //end of method
                //results are not valid

                builtInType = new BuiltInType();
                result = new DataValue();

                throw exception;
            }
        }

        #endregion //data type

        #region Monitoring

        ///\brief {Managing the Monitoring design of Unified Automation}

        /// <summary>
        /// Method to manage the monitoring of a node in the OPC-UA server. In this case, the Node identifiers are strings
        /// </summary>
        /// <param name="NodesIdentifiers">The identifiers of each node. In this case , the identifiers are either unint (numeric) or strings, for instance 1002</param>
        /// <param name="textboxes">The textboxes were the updated value of the node will be displayed (monitored)</param>
        /// <param name="aDataChangedEventHandler">The delegate method that subscribes to the data changed event</param>
        /// <param name="NamespaceIndex"></param>
        /// <param name="samplingInterval"></param>
        /// <param name="allStatusCode"></param>
        /// <returns>TRUE : if nodes were correctly monitored. The outputs are relevant. FALSE : you already subscribed to nodes before => unsubscribe first! Output codes are not relevant</returns>
        private bool StartMonitoring<T>(List<T> NodesIdentifiers, List<object> textboxes, DataChangedEventHandler aDataChangedEventHandler, ushort NamespaceIndex, double samplingInterval, out List<uint> allStatusCode)
        {
            if (m_subscription != null)
            {
                allStatusCode = new List<uint>();
                return false;
            }
            // Step 1 --------------------------------------------------
            // Create and initialize subscription
            m_subscription = new Subscription(m_session);
            m_subscription.PublishingEnabled = true;
            m_subscription.PublishingInterval = samplingInterval;

            #region remap (propagate) the event "DataChanged"
            // Data change events will be received through Subscription_DataChanged
            m_subscription.DataChanged += subscription_DataChanged;
            //Attach to events.
            this.DataChanged += aDataChangedEventHandler;
            #endregion

            // Create subscription on server
            m_subscription.Create(_SettingsRequest);

            // Step 2 --------------------------------------------------
            // Prepare variables to monitor as data monitored item
            List<MonitoredItem> monitoredItems = new List<MonitoredItem>();
            // Default is monitoring Value attributes.
            int i = -1;
            foreach (object elem in NodesIdentifiers)
            {
                i++;
                if (elem.GetType() == typeof(string))
                {
                    monitoredItems.Add(new DataMonitoredItem(new NodeId((string)elem, NamespaceIndex)) { UserData = textboxes[i] });
                }
                else if (elem.GetType() == typeof(uint))
                {
                    monitoredItems.Add(new DataMonitoredItem(new NodeId((uint)elem, NamespaceIndex)) { UserData = textboxes[i] });
                }
                else
                {
                    throw new NotImplementedException("The given type " + elem.GetType().ToString() + " is not implemented for  StartMonitoring<T>");
                }
            }

            // Step 3 --------------------------------------------------
            // Create monitored items on server
            List<StatusCode> results;
            results = m_subscription.CreateMonitoredItems(monitoredItems, _SettingsRequest);
            allStatusCode = new List<uint>();
            foreach (StatusCode status in results)
            {
                allStatusCode.Add(status.Code);
            }

            return true;
        }

        #endregion //Monitoring

        #region events management

        ///\brief remaping (forwarding) of status changes event

        /// <summary>
        /// To remap (forward) the event based on status changes. It also update the IsConnected attribute.
        /// </summary>
        /// <param name="sender">Sender of the event</param>
        /// <param name="e">Arguments passed</param>
        private void session_ConnectionStatusUpdate(Session sender, ServerConnectionStatusUpdateEventArgs e)
        {

            if (e.Status == ServerConnectionStatus.Connected)
                m_IsConnected = true;

            if (e.Status == ServerConnectionStatus.Disconnected)
                m_IsConnected = false;

            ServerConnectionStatusUpdateEventHandler handler = ConnectionStatusUpdate;
            if (handler != null)
            {
                handler(sender, e);
            }
        }

        ///\brief To remap (forward) the event based on datachange notifications.

        /// <summary>
        /// To remap (forward) the event based on datachange notifications.
        /// </summary>
        /// <param name="subscription"></param>
        /// <param name="e"></param>
        private void subscription_DataChanged(Subscription subscription, DataChangedEventArgs e)
        {
            DataChangedEventHandler handler = DataChanged;
            if (handler != null)
            {
                handler(subscription, e);
            }
        }

        #endregion

    }
}
