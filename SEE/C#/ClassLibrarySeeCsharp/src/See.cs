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
using System.Text;

using System.Threading;
using System.Threading.Tasks;
using System.Collections.Concurrent;

using UnifiedAutomation.UaBase;
using UnifiedAutomation.UaClient;

using eu.skillpro.opcua.client.CS;
using eu.skillpro.see.CS.AML;
using eu.skillpro.see.CS.AmsService;

namespace eu.skillpro.see.CS
{

    /*! 
*  \brief     Mother class for the implementation of Skillpro's SEEs in C#.
*  \details   Mother class for the implementation of Skillpro's SEEs in C#. Implementation fulfils the specifications of deliverables 2.1, 2.3 and 2.4
*  \author    Boris Bocquet, AKÉO PLUS SAS, France
*  \version   1.0.0
*  \date      2016/10/05
*  \copyright LGPL , SkillPro 7FP EU
*  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
*  \pre       OPC-UA server respecting the specifications of deliverable 2.3  MUST be running. AMS connection might be running
*  \bug       Write your bugs here
*  \warning   Write your warnings here
*  \todo	MORE CODE:  1/ confOpc.AmlDescription = "" . 2/ What to do whith AML_Description on the OPC-UA server? 3/ Master / slave synchronisation. 4/Better way for changing states OPC-UA/State machine in the "same" time (whith lock object) 5/ check consistency between timeout of this class and Request settings of #OpcUaClient 6/unified return codes (not only -1, -2, -3, etc...)
*/
    /// <summary>
    /// Mother class for the implementation of Skillpro's SEEs in C#.
    /// </summary>
    public class See : IDisposable
    {

        //####################################### ATTRIBUTES ################################################################""
        //####################################################################################################################"

        #region states

        /// <summary>
        /// Private field of the State Machine
        /// </summary>
        private SeeStateMachine _StateMachine = null;

        #endregion //states

        #region OPCUA

        private SkillProCsharpOPCUAClient _OpcUaClient = null;

        private OpcUaConfiguration _OpcUaConfiguration = null;

        private List<string> _SpaceNames = null;

        #endregion //OPCUA

        #region AMS service

        private AmsServiceConfiguration _ConfigurationAms;

        private object LockObjectAms = new object();

        private bool _ConnectionAmsServiceOK = false;

        #endregion //AMS service

        #region Skills

        private SkillBasedResourceController _SBRC = null;

        private bool _CurrentSkillIsProductive = false;

        private UInt32 _CurrentRemainingDuration = 0;

        private ManualResetEventSlim _MreWaitPause = null;

        #endregion //Skills

        #region events

        /// <summary>
        /// This event is raised on every OPC-UA client status changes
        /// </summary>
        public event ServerConnectionStatusUpdateEventHandler ConnectionStatusUpdate;

        /// <summary>
        /// This event is raised when the OPC-UA server writes new value on the "call" field of your SEE object(on the server)
        /// </summary>
        public event DataChangedEventHandler CallInvokedByServer;

        /// <summary>
        /// Delegate method used for by the event NewExceptionOccured
        /// </summary>
        /// <param name="sender">sender of the exception</param>
        /// <param name="newValue">The exception raised</param>
        public delegate void NewExceptionOccuredDelegate(object sender, SkillProException newValue);


        /// <summary>
        /// This event is raised after each new enqueued exception (see ExceptionQueue also)
        /// </summary>
        /// \sa ExceptionQueue
        public event NewExceptionOccuredDelegate NewExceptionOccured = null;

        #endregion //events

        #region Error Handling

        private ConcurrentQueue<SkillProException> _ExceptionQueue = new ConcurrentQueue<SkillProException>();

        private bool _ExceptionInSetResourceMode = false;

        #endregion //Error Handling

        #region Threads

        private Thread _T_SleepingTimestamp = null;

        private int _MsSleepPerLoop = 1000;

        private bool _BreakSleeping = false;

        private bool _Disposing = false;

        #endregion //Threads

        #region IDs

        private string _DisplayToString = null;

        private bool _RefreshName = true;

        #endregion //IDs

        //####################################### PROPERTIES ################################################################""
        //####################################################################################################################"

        #region states

        /// <summary>
        /// The state machine assoicated with the SEE. This states machine is complient with specification of deliverable 2.4
        /// \todo Better way for changing states OPC-UA/State machine in the "same" time (whith lock object)
        /// </summary>
        public SeeStateMachine StateMachine
        {
            get { return _StateMachine; }
            //set { _StateMachine = value; }
        }

        #endregion //states

        #region OPCUA

        /// <summary>
        /// A OPC-UA client offering services to get/write node values on the server , subscribe to value changes and call methods
        /// \remark Have a look at SkillProCsharpOPCUAClient's API documentation
        /// </summary>
        public SkillProCsharpOPCUAClient OpcUaClient
        {
            get { return _OpcUaClient; }
            //set { _OpcUaClient = value; }
        }

        /// <summary>
        /// All parameters relative to the configuration of the OPC-UA configuration, e.g. the IP address of the server, the namespace index, the Id of the current SEE on the server ...
        /// \todo check consistency between timeout of this class and Request settings of #OpcUaClient
        /// </summary>
        public OpcUaConfiguration OpcUaConfiguration
        {
            get { return _OpcUaConfiguration; }
            //set { _OpcUaConfiguration = value; }
        }

        /// <summary>
        /// The list of space names on the server. It is available right after the connection with the OPC-UA server.
        /// </summary>
        public List<string> SpaceNames
        {
            get { return _SpaceNames; }
            //set { _OpcUaConfiguration = value; }
        }

        #endregion //OPCUA

        #region AMS service

        /// <summary>
        /// The class gathering all the configurations for the connection with the AMS
        /// </summary>
        /// \sa AmsServiceWebClient
        public AmsServiceConfiguration ConfigurationAms
        {
            get { return _ConfigurationAms; }
            set
            {
                _ConfigurationAms = value;
                CheckAmsServiceConnection();
            }
        }

        /// <summary>
        /// Property to know if the AMS service works. This returns the last value received from the last "checkConnection" web request
        /// </summary>
        ///\remarks This property the \b last value received from the last "checkConnection" web request. It does \b NOT send a request. To send a request see <see cref="CheckAmsServiceConnection"/> 
        ///\sa CheckAmsServiceConnection
        public bool ConnectionAmsServiceOK
        {
            get { return _ConnectionAmsServiceOK; }
            //set { _ConnectionAmsServiceOK = value; }
        }

        #endregion //AMS service

        #region Skills

        /// <summary>
        /// The Skill Based Resource Controller the SEE owns
        /// </summary>
        /// \sa SkillBasedResourceController
        public SkillBasedResourceController SBRC
        {
            get { return _SBRC; }
        }

        private bool CurrentSkillIsProductive
        {
            get { return _CurrentSkillIsProductive; }
            set
            {
                if (_OpcUaClient != null && _OpcUaClient.IsConnected)
                {
                    try
                    {
                        OpcUaSetProductive(value);
                    }
                    catch (Exception ex)
                    {
                        LogStringTODO("OpcUaSetProductive return an exception : " + ex.Message, "CurrentSkillIsProductive property");
                        LogStringException(new SkillProException("OpcUaSetProductive return an exception in CurrentSkillIsProductive property", ex));
                    }
                }

                _CurrentSkillIsProductive = value;
            }
        }

        private UInt32 CurrentRemainingDuration
        {
            get { return _CurrentRemainingDuration; }
            set
            {
                if (_OpcUaClient != null && _OpcUaClient.IsConnected)
                {
                    try
                    {
                        OpcUaSetRemainingDuration(value);
                    }
                    catch (Exception ex)
                    {
                        LogStringTODO("OpcUaSetRemainingDuration return an exception : " + ex.Message, "CurrentRemainingDuration property");
                        LogStringException(new SkillProException("OpcUaSetRemainingDuration return an exception in CurrentRemainingDuration property", ex));
                    }
                }

                _CurrentRemainingDuration = value;
            }
        }

        #endregion //Skills

        #region Error Handling

        /// <summary>
        /// The queue gathering all the exceptions until you fully recovered with the method <see cref="EXT_RECOVER"/>
        /// </summary>
        /// \sa EXT_RECOVER
        public ConcurrentQueue<SkillProException> ExceptionQueue
        {
            get { return _ExceptionQueue; }
        }

        #endregion //Error Handling

        #region Transitions, State changes

        /// <summary>
        /// Property to know if from your current state, you can call the this command (must be preoparationnal)
        /// </summary>
        /// \sa SeeStateMachine
        public bool EXT_CONFIGURED_FEASIBLE
        {
            get
            {
                SeeResourceMode NewState;
                return _StateMachine.TestTransition(SeeResourceModeTransition.EXT_CONFIGURED, out NewState);
            }
        }

        /// <summary>
        /// Property to know if from your current state, you can call the this command (must be Idle)
        /// </summary>
        /// \sa SeeStateMachine
        public bool EXT_SHUTDOWN_FEASIBLE
        {
            get
            {
                SeeResourceMode NewState;
                return _StateMachine.TestTransition(SeeResourceModeTransition.EXT_SHUTDOWN, out NewState);
            }
        }

        /// <summary>
        /// Property to know if from your current state, you can call the this command (must be Error)
        /// </summary>
        /// \sa SeeStateMachine
        public bool EXT_RECOVER_FEASIBLE
        {
            get
            {
                SeeResourceMode NewState;
                return _StateMachine.TestTransition(SeeResourceModeTransition.EXT_RECOVER, out NewState);
            }
        }

        #endregion //Transitions


        //####################################### PUBLIC METHODS ################################################################""
        //####################################################################################################################"

        #region constructor / destructor

        /// <summary>
        /// Possible constructor for the SEE.
        /// </summary>
        /// <param name="aOpcUaConfiguration">All parameters concerning OPCUA communication</param>
        /// <param name="aSkillBasedResourceController">The %Skill Based Resource Controller describing the Executable %Skill of the SEE</param>
        /// \remarks We recommand you to use the other constructors, since all the SEE data shall come from AML files. So be carefull with your inputs
        public See(OpcUaConfiguration aOpcUaConfiguration, AmsServiceConfiguration aAmsServiceConfiguration, SkillBasedResourceController aSkillBasedResourceController)
        {
            MainConstructorSee(aOpcUaConfiguration, aAmsServiceConfiguration, aSkillBasedResourceController);
        }

        /// <summary>
        /// Prefered constructor for the SEE
        /// </summary>
        /// <param name="AmlSee">AML description of the SEE, receive from AMS service</param>
        /// <param name="aSkillBasedResourceController">The %Skill Based Resource Controller describing the Executable %Skill of the SEE</param>
        /// \remarks We recommand you to use this constructor
        public See(AML.AMLSkillExecutionEngine AmlSee, SkillBasedResourceController aSkillBasedResourceController)
        {
            ConstructorSeeFromAmlSee(AmlSee, aSkillBasedResourceController);
        }

        /// <summary>
        /// Possible constructor for the SEE
        /// </summary>
        /// <param name="AmlString">string containing the AML description of the SEE, received from AMS service</param>
        /// <param name="aSkillBasedResourceController">The %Skill Based Resource Controller describing the Executable %Skill of the SEE</param>
        /// \warning do not misunderstand : the parameter AmlString is NOT the path of a file : this is the AML string of the see description
        public See(string AmlString, SkillBasedResourceController aSkillBasedResourceController)
        {
            AMLDocument doc = AMLSerializer.DeserializeFromAmlString(AmlString);

            if (doc.SkillExecutionEngine == null)
            {
                throw new Exception("No Skill Execution Engine in the given AML string");
            }

            if (doc.SkillExecutionEngine.Count != 1)
            {
                throw new Exception("There are " + doc.SkillExecutionEngine.Count + " SEE in this AML. Need one and only one SEE inside the file");
            }

            ConstructorSeeFromAmlSee(doc.SkillExecutionEngine[0], aSkillBasedResourceController);
        }

        /// <summary>
        /// Possible constructor for the SEE
        /// </summary>
        /// <param name="aSkillBasedResourceController">The %Skill Based Resource Controller describing the Executable %Skill of the SEE</param>
        /// <param name="AmlFilePath">The path of the file ontaining the AML description of the SEE</param>
        /// \warning do not misunderstand : the parameter AmlFilePath is NOT the AML string of the see description : it is the path of the AML file 
        public See(SkillBasedResourceController aSkillBasedResourceController, string AmlFilePath)
        {
            AMLDocument doc = AMLSerializer.DeserializeFromFile(AmlFilePath);

            if (doc.SkillExecutionEngine == null)
            {
                throw new Exception("No Skill Execution Engine in the given AML string");
            }


            if (doc.SkillExecutionEngine.Count != 1)
            {
                throw new Exception("There are " + doc.SkillExecutionEngine.Count + " SEE in this AML. Need one and only one SEE inside the file");
            }


            ConstructorSeeFromAmlSee(doc.SkillExecutionEngine[0], aSkillBasedResourceController);
        }

        /// <summary>
        /// The destructor of the SEE. Note that Dispose method is called inside.
        /// </summary>
        /// \sa Dispose()
        ~See()
        {
            Dispose();
        }

        /// <summary>
        /// The dispose method. 
        /// Takes care of stopping the threads, check current state and try to make final transitions, disconnect the OPC-UA client and finnaly call Dispose(true)
        /// </summary>
        /// \remarks At the end of the method, Dispose(true) is called and then the garbage collector is invoked. Refer to http://msdn.microsoft.com/en-us/library/System.IDisposable.aspx
        /// \sa ~See() , Dispose(bool)
        public void Dispose()
        {
            LogString("Dispose (public) called");

            _Disposing = true;
            _BreakSleeping = true;

            //Wait end of queuing
            if (_T_SleepingTimestamp != null && _T_SleepingTimestamp.IsAlive)
            {
                _T_SleepingTimestamp.Join();
            }


            switch (_StateMachine.CurrentState)
            {
                case SeeResourceMode.PreOperational:
                    //Nothing to do
                    break;
                case SeeResourceMode.ExecutingSkill:
                    //Go to error state
                    _StateMachine.Transition(SeeResourceModeTransition.INT_ERROR);
                    EXT_RECOVER();
                    break;
                case SeeResourceMode.ExecutingSkillPausing:
                    //Go to error state
                    _StateMachine.Transition(SeeResourceModeTransition.INT_ERROR);
                    EXT_RECOVER();
                    break;
                case SeeResourceMode.ExecutingSkillResumable:
                    //Go to error state
                    _StateMachine.Transition(SeeResourceModeTransition.INT_ERROR);
                    EXT_RECOVER();
                    break;
                case SeeResourceMode.Idle:
                    EXT_SHUTDOWN();
                    break;
                case SeeResourceMode.IdleQueuedSkill:
                    //Go to error state
                    _StateMachine.Transition(SeeResourceModeTransition.INT_ERROR);
                    EXT_RECOVER();
                    break;
                case SeeResourceMode.Error:
                    EXT_RECOVER();
                    break;
                default:
                    throw new NotImplementedException();
            }


            //close OPC UA
            if (_OpcUaClient != null)
            {
                try
                {
                    _OpcUaClient.StopMonitoring();
                    _OpcUaClient.disconnect();
                }
                catch (UnifiedAutomation.UaBase.StatusException ex)
                {
                    SetLastException("EXCEPTION UnifiedAutomation.UaBase.StatusException caught while executing  Dispose", ex);
                }
                catch (Exception ex)
                {
                    SetLastException("EXCEPTION caught while executing  Dispose", ex);
                }
            }


            Dispose(true);

            // this tells the garbage collector not to execute the finalizer
            GC.SuppressFinalize(this);
        }

        #endregion //constructor / destructor

        #region Override of Object methods

        /// <summary>
        /// Override of the ToString() object method
        /// </summary>
        /// <returns>A JSON dictionnary with {"Id": , "Name": , "OpcUaNodeId": } data that all come from the AML description of the SEE</returns>
        /// \remarks this is the method called by the logger
        public override string ToString()
        {

            if (!_RefreshName)
                return _DisplayToString;

            if (_DisplayToString != null)
                return _DisplayToString;

            Dictionary<string, string> DicoToDisplay = new Dictionary<string, string>();

            if (_SBRC != null && _SBRC.SeeAmlDescription != null && _SBRC.SeeAmlDescription.ID != null)
                DicoToDisplay.Add("Id", _SBRC.SeeAmlDescription.ID);
            else
                DicoToDisplay.Add("Id", "");

            if (_SBRC != null && _SBRC.SeeAmlDescription != null && _SBRC.SeeAmlDescription.Name != null)
                DicoToDisplay.Add("Name", _SBRC.SeeAmlDescription.Name);
            else
                DicoToDisplay.Add("Name", "");

            if (_OpcUaConfiguration != null && _OpcUaConfiguration.SeeNodeId != null)
                DicoToDisplay.Add("OpcUaNodeId", _OpcUaConfiguration.SeeNodeId.ToString());
            else
                DicoToDisplay.Add("OpcUaNodeId", "");

            _DisplayToString = SkillProDefinitions.JSS.Serialize(DicoToDisplay);

            _RefreshName = false;

            return _DisplayToString;

        }

        #endregion

        #region Transitions, State changes

        /// <summary>
        /// External call for transition from Preoperationnal to Idle. Check deliverable 2.4
        /// </summary>
        /// <returns>True : transition has been performed False : Transition has NOT been performed</returns>
        /// \remarks this is recommanded to check before using #EXT_CONFIGURED_FEASIBLE
        /// \sa EXT_CONFIGURED_FEASIBLE
        public bool EXT_CONFIGURED()
        {
            if (!EXT_CONFIGURED_FEASIBLE)
                return false;

            int TransitionOk = _StateMachine.Transition(SeeResourceModeTransition.EXT_CONFIGURED);

            if (TransitionOk != 0)
                return false;

            int SettingOK = OpcUaSetResourceMode(SeeResourceMode.Idle, "", true);

            return SettingOK == 0;
        }

        /// <summary>
        /// External call for transition from Idle to Preoperationnal. Check deliverable 2.4
        /// </summary>
        /// <returns>True : transition has been performed False : Transition has NOT been performed</returns>
        /// \remarks this is recommanded to check before using #EXT_SHUTDOWN_FEASIBLE
        /// \sa EXT_SHUTDOWN
        public bool EXT_SHUTDOWN()
        {
            if (!EXT_SHUTDOWN_FEASIBLE)
                return false;

            int TransitionOk = _StateMachine.Transition(SeeResourceModeTransition.EXT_SHUTDOWN);

            if (TransitionOk != 0)
                return false;

            int SettingOK = OpcUaSetResourceMode(SeeResourceMode.PreOperational, "", true);

            return SettingOK == 0;
        }

        /// <summary>
        /// External call for transition from error to Preoperationnal. Check deliverable 2.4
        /// </summary>
        /// <returns>True : transition has been performed False : Transition has NOT been performed</returns>
        /// \remarks this is recommanded to check before using #EXT_RECOVER_FEASIBLE
        /// \sa EXT_RECOVER_FEASIBLE
        public bool EXT_RECOVER()
        {
            if (!EXT_RECOVER_FEASIBLE)
                return false;

            int TransitionOk = _StateMachine.Transition(SeeResourceModeTransition.EXT_RECOVER);

            if (TransitionOk != 0)
                return false;

            _ExceptionInSetResourceMode = false;

            int SettingOK = OpcUaSetResourceMode(SeeResourceMode.PreOperational, "", true);

            if (SettingOK == 0)
            {
                _ExceptionQueue = new ConcurrentQueue<SkillProException>();
                return true;
            }
            else
            {
                return false;
            }
        }

        #endregion // Transitions, State changes

        #region logging

        /// <summary>
        /// Main function to log a "simple" string, which is not a TODO, an Event or an exception
        /// </summary>
        /// <param name="stringToWrite">The string to be logged</param>
        /// \remarks this function uses the ToString() method
        /// \sa LogStringTODO, LogStringEvent, LogStringException
        public void LogString(string stringToWrite)
        {
            if (!string.IsNullOrEmpty(stringToWrite))
            {
                DateTime now = DateTime.Now;
                System.Diagnostics.Debug.WriteLine(now.ToString("yyyy_MM_dd_hh_mm_ss_") + now.Millisecond.ToString() + "; " + this.ToString() + " ;" + stringToWrite);
                return;
            }
        }

        /// <summary>
        /// Main function to log a "TODO" string, (thing that are not implemented yet, opened questions, etc...)
        /// </summary>
        /// <param name="theTest">Here you can place the test (like returnCode != 0) that provoked the case</param>
        /// <param name="methodName">The method where you are</param>
        /// \remarks this function uses the ToString() method
        /// \sa LogString, LogStringEvent, LogStringException 
        public void LogStringTODO(string theTest, string methodName)
        {
            LogString("TODO; " + theTest + " occured in method " + methodName);
        }

        /// <summary>
        /// Main function to log an event string
        /// </summary>
        /// <param name="eventMethodName">The string to be logged</param>
        /// \remarks this function uses the ToString() method
        /// \sa LogStringTODO, LogString, LogStringException
        public void LogStringEvent(string eventMethodName)
        {
            LogString("EVENT; received method called : " + eventMethodName);
        }

        /// <summary>
        /// Main function to log an exception 
        /// </summary>
        /// <param name="ex">The exception to be logged</param>
        /// \remarks this function uses the ToString() method
        /// \sa LogStringTODO, LogString, LogStringException
        public void LogStringException(SkillProException ex)
        {
            LogString("EXCEPTION;" + ex.Message + "; InnerException ;" + ex.InnerException.Message);
        }

        #endregion //logging

        #region related to skill execution, pausing, unpausing

        /// <summary>
        /// Call this method if you want to ask for pausing the current skill execution
        /// </summary>
        /// <returns>0 : the skill is pausable and the skill is paused (or finished). Else see TestIsPausable() and PauseCurrentSkill() return code</returns>
        /// \remarks the MES also calls this method by writing "Pause" in the Call field of the See object on the OPC-UA server
        /// \sa TestIsPausable, PauseCurrentSkill
        /// \todo better return codes here
        public int PauseSkillExecution()
        {
            //Then check that you can pause the Skill
            int IsPausable = TestIsPausable(false); //Do NOT write on OPC-UA server

            if (IsPausable != 0)
                return IsPausable;

            //Skill is Pausable, then pause it
            return PauseCurrentSkill();
        }

        /// <summary>
        /// Call this method if you want to resume the current skill execution
        /// </summary>
        /// <returns>0 : the skill is resumable and the skill is resumed Else see TestIsResumable() and UnPauseCurrentSkill() return codes</returns>
        /// \remarks the MES also calls this method by writing "Resume" in the Call field of the See object on the OPC-UA server
        /// \sa TestIsResumable, UnPauseCurrentSkill
        /// \todo better return codes here
        public int ResumeSkillExecution()
        {
            //Then check that you can resume the Skill
            int IsResumable = TestIsResumable(false); //Do NOT write on OPC-UA server

            if (IsResumable != 0)
                return IsResumable;

            //Skill is Resumable, then resume it
            return UnPauseCurrentSkill();
        }

        /// <summary>
        /// Call this method if you want to stop the "waiting" of the SEE (Idle Queued skill) and go back to Idle
        /// </summary>
        /// \remarks the MES also calls this method by writing "Clear" in the Call field of the See object on the OPC-UA server
        /// \warninig This is a non blocking method. At the end, of this function, you might still be in Idle-Queue (for a while)
        public void ClearQueuedSkill()
        {

            lock (_StateMachine)
            {
                if (_StateMachine.CurrentState == SeeResourceMode.IdleQueuedSkill)
                {
                    //A Skill was queue => break the sleep
                    _BreakSleeping = true;
                }
            }
        }

        /// <summary>
        ///  Call this method if you want to stop the current %Skill execution (emergency stop)
        /// </summary>
        /// <returns>An eventually raised exception</returns>
        /// \warninig This is a non blocking method. At the end, of this function, you might still have the skill running. This is also not garantied that the skill could be stopped
        public SkillProException TryStopSkillExecution()
        {
            try
            {
                if (_SBRC == null)
                    return new SkillProException("The Skill Based Resource Controller is NULL");

                if (_SBRC.CurrentExecutingSkill == null || _SBRC.CurrentExecutingSkill.SkillToExecute == null)
                    return new SkillProException("There is no current Executing Skill");

                _SBRC.CurrentExecutingSkill.SkillToExecute.StopExecution();
            }
            catch (Exception ex)
            {
                return new SkillProException(ex.Message, ex);
            }

            return null;
        }

        #endregion //related to skill execution, pausing, unpausing

        #region related to OPC-UA communication

        /// <summary>
        /// Main method to set the Call field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="stringForCallVariable">The string to write in the call variable</param>
        /// <param name="notifyServer">If true, the Server will be notified : UpdateComplete will be called</param>
        /// <returns>0 : no errors</returns>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaSetCallVariable(string stringForCallVariable, bool notifyServer)
        {

            LogString("OpcUaSetCallVariable called : " + stringForCallVariable + ";" + notifyServer.ToString());

            List<string> callMethodIdentifier = new List<string>();
            string theNodeIdOfCall = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Call);


            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(theNodeIdOfCall);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(stringForCallVariable);

            //write that the current mode is Idle
            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the Call variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the Call variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            if (notifyServer)
            {
                OpcUaUpdateComplete();
            }

            return 0;
        }

        /// <summary>
        /// Main method to set a product condition field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="stringIDOfProduct">The product condition you want to write on the server.</param>
        /// <returns> 0 : product configuration has been correctly setted. </returns>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaSetProductCondition(string stringIDOfProduct, bool notifyServer)
        {
            LogString("OpcUaSetProductCondition called : " + stringIDOfProduct + ";" + notifyServer.ToString());

            string nodeOfCondition_Product = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Condition_Product);


            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(nodeOfCondition_Product);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(stringIDOfProduct);

            //write that the current mode is Idle
            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the Condition_Product variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the Condition_Product variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            if (notifyServer)
            {
                OpcUaUpdateComplete();
            }

            return 0;
        }

        /// <summary>
        /// Main method to set a configuration condition field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="stringIDofConfiguration">The configuration condition you want to write on the server.</param>
        /// <returns> 0 : configuration condition has been correctly setted. </returns>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaSetConfigurationCondition(string stringIDofConfiguration, bool notifyServer)
        {
            LogString("OpcUaSetConfigurationCondition called : " + stringIDofConfiguration + ";" + notifyServer.ToString());

            string nodeOfCondition_Configuration = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Condition_Configuration);


            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(nodeOfCondition_Configuration);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(stringIDofConfiguration);

            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the Condition_Configuration variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the Condition_Configuration variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            if (notifyServer)
            {
                OpcUaUpdateComplete();
            }

            return 0;
        }

        /// <summary>
        /// Main function to update the resource mode  field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="theNewResourceMode">The new state you want to go into</param>
        /// <param name="stringSkillId">The string id of the %Skill like 3e8cace2-567d-4635-8a0e-54b94a55a275</param>
        /// <param name="notifyServer">If true, the Server will be notified : UpdateComplete will be called</param>
        /// <returns> 0 : no errors</returns>
        /// \remarks stringSkillId is used in the case you are going to ExecutingSkill mode
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaSetResourceMode(SeeResourceMode theNewResourceMode, string stringSkillId, bool notifyServer)
        {
            LogString("OpcUaSetResourceMode called : " + theNewResourceMode.ToString() + ";" + stringSkillId + ";" + notifyServer.ToString());

            try
            {
                string nodeOfResourceMode = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Mode);

                int ModeId = (int)theNewResourceMode;

                List<string> NodeToWrite = new List<string>();
                NodeToWrite.Add(nodeOfResourceMode);
                List<string> ValueToWrite = new List<string>();
                ValueToWrite.Add(ModeId.ToString());

                //if the mode is Executing skill then you must also update Skill variable on the Server 
                if (theNewResourceMode == SeeResourceMode.ExecutingSkill)
                {
                    string nodeIdOfSkill = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Skill);
                    NodeToWrite.Add(nodeIdOfSkill);
                    ValueToWrite.Add(stringSkillId);
                }

                //write that the current mode is Idle
                List<uint> allStatusCode;
                _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

                if (allStatusCode == null)
                {
                    SkillProException ex = new SkillProException("Writing the Mode variable on the node failed.");
                    throw ex;
                }

                if (allStatusCode[0] != 0)
                {
                    SkillProException ex = new SkillProException("Writing the Mode variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                    throw ex;
                }

                if (allStatusCode.Count == 2)
                {
                    if (allStatusCode[1] != 0)
                    {
                        SkillProException ex = new SkillProException("Writing the Skill variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                        throw ex;
                    }
                }

                //if code passes here, this mean that everything passed alrigth. So Notify the server and Update state
                if (notifyServer)
                {
                    OpcUaUpdateComplete();
                }

                return 0;
            }
            catch (Exception ex)
            {

                if (!_ExceptionInSetResourceMode)
                {
                    _ExceptionInSetResourceMode = true;
                    SetLastException("EXCEPTION IN SetResourceMode", ex);
                }

                return -1;
            }
        }

        /// <summary>
        /// Main method to set a the SkillId field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="stringSkillId">The string to write</param>
        /// <param name="notifyServer">If true, the Server will be notified : UpdateComplete will be called</param>
        /// <returns>0 : no errors</returns>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaSetSkillId(string stringSkillId, bool notifyServer)
        {
            LogString("OpcUaSetSkillId called");

            List<string> NodeToWrite = new List<string>();
            List<string> ValueToWrite = new List<string>();

            string nodeIdOfSkill = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Skill);
            NodeToWrite.Add(nodeIdOfSkill);
            ValueToWrite.Add(stringSkillId);

            //write that the current mode is Idle
            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the SkillId variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the SkillId variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            //if code passes here, this mean that everything passed alrigth. So Notify the server and Update state
            if (notifyServer)
            {
                OpcUaUpdateComplete();
            }

            return 0;
        }

        /// <summary>
        /// Main method to notify the OPC-UA server that an exception occured. It simply writes the correct error identifier on the correct Node Id on the server.
        /// </summary>
        /// <param name="anException">The type of exception that occured.</param>
        /// <param name="notifyServer">A boolean to notify or not the server.</param>
        /// <returns>0 : no errors</returns>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        /// \todo better return codes (unified)
        public int OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants anException, string ExceptionDescription, bool notifyServer)
        {
            LogString("OpcUaWriteCallExceptionOnTheServer called");

            string nodeOfCallException = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Call_Exception);
            string nodeOfCallExceptionDescription = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Call_Exception_Description);

            int identifierOfException = (int)anException;

            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(nodeOfCallException);
            NodeToWrite.Add(nodeOfCallExceptionDescription);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(identifierOfException.ToString());
            ValueToWrite.Add(ExceptionDescription);

            //write that the current mode is Idle
            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the Call_Exception variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the Call_Exception variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            if (notifyServer)
            {
                OpcUaUpdateComplete();
            }

            return 0;

        }

        /// <summary>
        /// Method to notify the OPC-UA server that a change on the SEE node has been performed, by calling UpdateComplete method on the See object on the OPC-UA server
        /// </summary>
        /// \warning you are not supposed to use it from outside the class : this method should be private (but can be currently usefull for development phasis)
        public void OpcUaUpdateComplete()
        {
            LogString("OpcUaUpdateComplete called");

            List<Variant> inputs = new List<Variant>();
            List<Variant> outputs;
            List<uint> inputArgumentErrors;
            uint errorCode;

            //there are no inputs

            //method Id of the "UpdateComplete"
            string IdOfUpdateComplete = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.UpdateComplete);

            //call the method
            bool callSucceed = _OpcUaClient.CallSynchronously((object)_OpcUaConfiguration.SeeNodeId, false,
                 (object)IdOfUpdateComplete, true,
                 _OpcUaConfiguration.SpaceNameIndex,
                 inputs, _OpcUaConfiguration.TimeOut,
                 out inputArgumentErrors, out outputs, out errorCode);

            //check the potentiall errors
            uint LastError = 0;
            foreach (uint scInput in inputArgumentErrors)
            {
                if (scInput != 0)
                {
                    LastError = scInput;
                }
            }

            if ((!callSucceed) || (LastError != 0) || (errorCode != 0))
            {
                SkillProException ex = new SkillProException("The call of UpdateComplete method on the OPCUA server failed." + Environment.NewLine + " The call returned the code " + errorCode.ToString() + Environment.NewLine + "and the code on the input parameters is " + LastError.ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

        }

        /// <summary>
        /// Main method to set a the RemainingDuration field on the See object on the OPC-UA server
        /// </summary>
        /// <param name="RemainingDuration">The remaining duration in ms</param>
        /// <returns>0 : no error</returns>
        /// \todo better return codes (unified)
        public int OpcUaSetRemainingDuration(UInt32 RemainingDuration)
        {
            LogString("OpcUaSetRemainingDuration called : " + RemainingDuration.ToString());

            string nodeOfRemaining_Duration = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Remaining_Duration);


            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(nodeOfRemaining_Duration);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(RemainingDuration.ToString());

            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the RemainingDuration variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the RemainingDuration variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            OpcUaUpdateComplete();

            return 0;
        }

        #endregion

        #region related to AMS service

        /// <summary>
        /// Method to send the "checkConnection" webrequest to the AMS service. Also update the internal value of the property ConnectionAmsServiceOK
        /// </summary>
        /// <returns>True : connection is OK. False : connection failed</returns>
        /// \sa ConnectionAmsServiceOK
        public bool CheckAmsServiceConnection()
        {
            lock (LockObjectAms)
            {
                try
                {
                    _ConnectionAmsServiceOK = AmsServiceWebClient.CheckAmsServiceConnection(_ConfigurationAms);
                }
                catch (Exception ex)
                {
                    SetLastException("EXCEPTION caught in CheckAmsServiceConnection", ex);
                    _ConnectionAmsServiceOK = false;
                }

                return _ConnectionAmsServiceOK;
            }
        }

        #endregion //related to AMS service

        //####################################### PROTECTED METHODS TO OVERRIDE ################################################################""
        //####################################################################################################################"



        //\brief The "override" of the Dispose method from IDisposable

        /// <summary>
        /// The "override" of the Dispose method from IDisposable .\n
        /// Taken from MSDN example. \n
        /// Refer to http://msdn.microsoft.com/en-us/library/System.IDisposable.aspx
        /// </summary>
        /// <param name="disposing">If true, then managed are cleaned (currently no managed resources). False otherwise.</param>
        protected virtual void Dispose(bool disposing)
        {
            LogString("Dispose (virtual) called");
            if (disposing)
            {
                // clean up managed resources:
                // dispose child objects that implement IDisposable
            }

            // clean up unmanaged resources


        }

        /// <summary>
        /// The method to override with your child class. Switch to the SkillToExecute.AmlSkillDescription.Execution.Type and give the appropriate parameters
        /// </summary>
        /// <param name="SkillToExecute">The ExecutableSkill that will be executed</param>
        /// <param name="ParametersForTheSkill">The input parameters you want to pass to the skill</param>
        /// <returns>0: no error. If you return something else, then the See will go in error state</returns>
        /// \remarks see the C# coding guidelines for more information. You can also have a look at some %See examples like HelloWorldSee, HelloWorldAndQuestionsSee, DedicatedPopUpSee
        /// \sa HelloWorldSee, HelloWorldAndQuestionsSee, DedicatedPopUpSee
        protected virtual int ProvideInputDataForSkillExecution(ExecutableSkill SkillToExecute, out InputParams ParametersForTheSkill)
        {
            ParametersForTheSkill = new InputParams(Skill.ERROR_INPUT_PROCESS_NULL, null);
            return -1;
        }

        /// <summary>
        /// The method to override with your child class. Switch to the SkillExecuted.AmlSkillDescription.Execution.Type and get the returned outputs of the executed skill
        /// </summary>
        /// <param name="SkillExecuted">The %Skill that has been executed</param>
        /// <param name="OutputOfExecution">The outputs of the executed skill</param>
        protected virtual void GetOutputsOfExecutedSkill(ExecutableSkill SkillExecuted, OutputParams OutputOfExecution)
        {

        }

        /// <summary>
        /// The method to override with your child class. Switch to the skillWithNoMethod.AmlSkillDescription.Execution.Type and instanciate skillWithNoMethod.SkillToExecute with the 2 appropriate MethodToExecute
        /// </summary>
        /// <param name="skillWithNoMethod"></param>
        /// \sa Skill, <seealso cref="Skill.MethodToExecute"/>
        protected virtual void ProvideSkillToExecute(ref ExecutableSkill skillWithNoMethod)
        {
            skillWithNoMethod.SkillToExecute = new Skill(SkillRepository.NothingSkill, SkillRepository.NothingSkill);
        }

        //####################################### PRIVATE METHODS ################################################################""
        //####################################################################################################################"

        #region constructor / destructor
        /// <summary>
        /// private constructor for the SEE
        /// </summary>
        /// <param name="aOpcUaConfiguration">All parameters concerning OPCUA communication</param>
        /// <param name="aSkillBasedResourceController">The Skill Based Resource Controller describing the Executable Skill of the SEE</param>
        private void MainConstructorSee(OpcUaConfiguration aOpcUaConfiguration, AmsServiceConfiguration aAmsServiceConfiguration, SkillBasedResourceController aSkillBasedResourceController)
        {
            try
            {
                //load configuration OPCUA
                _OpcUaConfiguration = aOpcUaConfiguration;

                //load conditions and skills
                _SBRC = aSkillBasedResourceController;

                _RefreshName = true;

                LogString("See Constructor called");

                //take care of the state machine
                _StateMachine = new SeeStateMachine();

                //Configuraion with AMS
                ConfigurationAms = aAmsServiceConfiguration; //note that here is tested the communication

                //Check Ams connection

                //start OPCUA client
                _OpcUaClient = new SkillProCsharpOPCUAClient();
                LogString("Connecting to OPC-UA server");
                _OpcUaClient.connect(OpcUaConfiguration.ServerUrl, OnConnectionStatusUpdate, out _SpaceNames); //note that the list of spacenames are now available

                //create the SEE on the server if needed TODO : better is checking if your ID already exists
                if (_OpcUaConfiguration.SeeNodeId == 0)
                {
                    CreateSeeOnServer();
                }

                //Set the first mandatory (for standard) fields on the server (i.e put mode into PreOperationnal)
                SetTheFirstFieldsOnSeeServer();

                //subscribe to call node notifications
                SubscribeToCallNodeNotifications();

                //If code arrives here, init is done => the code will now go to the Child class. 

                //CAUTION : the state is not IDLE => this is the user (programmer) duty to call method INT_CONFIGURED

                _RefreshName = true;

            }
            catch (Exception ex)
            {
                _RefreshName = true;
                SetLastException("EXCEPTION caught while executing  See constructor", ex);
            }

        }

        /// <summary>
        /// private constructor for the SEE
        /// </summary>
        /// <param name="AmlSee">AML description of the SEE, receive from AMS service</param>
        /// <param name="aSkillBasedResourceController">The Skill Based Resource Controller describing the Executable Skill of the SEE</param>
        private void ConstructorSeeFromAmlSee(AML.AMLSkillExecutionEngine AmlSee, SkillBasedResourceController aSkillBasedResourceController)
        {
            OpcUaConfiguration confOpc;

            try
            {
                ushort NameSpaceIndex;
                uint SeeNodeId;

                AmlSee.MESCommType.NodeIdToSpaceNameIndexAndNodeId(out NameSpaceIndex, out SeeNodeId);

                confOpc = new OpcUaConfiguration();
                confOpc.SeeName = AmlSee.Name;
                confOpc.SeeNodeId = SeeNodeId;
                confOpc.SpaceNameIndex = NameSpaceIndex;
                confOpc.ServerUrl = AmlSee.MESCommType.URI;
                confOpc.SeeId = AmlSee.ID;
                confOpc.AmlDescription = ""; //TODO BB 150323

            }
            catch (Exception ex)
            {
                SetLastException("EXCEPTION caught while executing ConstructorSeeFromAmlSee", ex);
                throw ex;
            }

            AmsServiceConfiguration AmsConf = new AmsServiceConfiguration() { Uri = AmlSee.AMSCommType.URI };

            MainConstructorSee(confOpc, AmsConf, aSkillBasedResourceController);

        }

        #endregion //constructor / destructor

        #region related to OPC-UA communication

        /// <summary>
        /// A "utils" method to get the entire string identifier to access the desired field of the SEE on the OPC-UA server. For instance "3008.Call_Exception"
        /// </summary>
        /// <param name="fieldOrMethod">The field you want to acces</param>
        /// <returns>The string of the node Id (on the server) of the field</returns>
        /// \sa <seealso cref="SkillProDefinitions.StringIdentifierOfSeeFieldOrMethod"/>
        private string StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer fieldOrMethod)
        {
            return SkillProDefinitions.StringIdentifierOfSeeFieldOrMethod(_OpcUaConfiguration.SeeNodeId.ToString(), fieldOrMethod);
        }

        /// <summary>
        /// Method to create the SEE on the server. It fullfils the requirement from specifications.
        /// </summary>
        /// \warning normally you don't have to do it yourself => the SEE object should already be on the OPC-UA server
        private void CreateSeeOnServer()
        {
            LogString("CreateSeeOnServer called");
            List<Variant> inputs = new List<Variant>();
            List<Variant> outputs;
            List<uint> inputArgumentErrors;
            uint errorCode;

            //fill the 3 inputs of the createSee method : the See name (string), the Aml file (string) and the SEEid
            inputs.Add((Variant)_OpcUaConfiguration.SeeName);
            inputs.Add((Variant)_OpcUaConfiguration.AmlDescription);
            inputs.Add((Variant)_OpcUaConfiguration.SeeId);

            //call the method
            bool callSucceed = _OpcUaClient.CallSynchronously((object)_OpcUaConfiguration.CreateSeeNodeId, true,
                 (object)_OpcUaConfiguration.CreateSeeMethodId, false,
                 _OpcUaConfiguration.SpaceNameIndex,
                 inputs, _OpcUaConfiguration.TimeOut,
                 out inputArgumentErrors, out outputs, out errorCode);

            //check the potentiall errors
            uint LastError = 0;
            foreach (uint scInput in inputArgumentErrors)
            {
                if (scInput != 0)
                {
                    LastError = scInput;
                }
            }

            if ((!callSucceed) || (LastError != 0) || (errorCode != 0))
            {
                SkillProException ex = new SkillProException("The call of createSee method on the OPCUA server failed." + Environment.NewLine + " The call returned the code " + errorCode.ToString() + Environment.NewLine + "and the code on the input parameters is " + LastError.ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            //if code passes here, this mean that they were no errors at all
            NodeId NodeIdOfSee = (NodeId)outputs[0].Value;
            _OpcUaConfiguration.SeeNodeId = (uint)NodeIdOfSee.Identifier;

            //Write this index on the SEE variable on the OPCUA server

            string nodeOfSeeId = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.SEE_Id);

            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(nodeOfSeeId);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(_OpcUaConfiguration.SeeNodeId.ToString());

            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the SEE_Id variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the SEE_Id variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

        }

        /// <summary>
        /// Method to subscribe to the datachanged of the value of the Call node on the server.
        /// </summary>
        private void SubscribeToCallNodeNotifications()
        {
            LogString("SubscribeToCallNodeNotifications called");

            List<string> callMethodIdentifier = new List<string>();
            string theNodeIdOfCall = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Call);
            callMethodIdentifier.Add(theNodeIdOfCall);

            List<object> dummyList = new List<object>();
            //System.Windows.Controls.TextBox dummyTextBox = new System.Windows.Controls.TextBox();
            //dummyList.Add(dummyTextBox);
            dummyList.Add(new object());


            List<uint> allStatusCode;
            _OpcUaClient.StartMonitoring(callMethodIdentifier, dummyList, OnCallInvokedByServer, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.SamplingIntervals, out allStatusCode);

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("The subscription to the Call node on SEE failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }
        }

        /// <summary>
        /// Method to first set the required fields of the SEE node on the server. It fullfils the requirement from specifications.
        /// </summary>
        private void SetTheFirstFieldsOnSeeServer()
        {
            LogString("setTheFirstFieldsOnSeeServer called");

            OpcUaSetProductCondition(_SBRC.CurrentProductsToOpcUaString(), false);
            OpcUaSetConfigurationCondition(_SBRC.CurrentConfiguration.Key, false);
            OpcUaSetCallVariable("0", false);
            OpcUaSetSkillId("0", false);
            OpcUaSetResourceMode(SeeResourceMode.PreOperational, "", true);
        }

        private int OpcUaSetProductive(bool Productive)
        {
            LogString("OpcUaSetProductive called : " + Productive.ToString());

            List<string> callMethodIdentifier = new List<string>();
            string theNodeIdOfCall = StringIdentifierOfSeeFieldOrMethod(SeeFieldsAndMethodsOnServer.Productive);


            List<string> NodeToWrite = new List<string>();
            NodeToWrite.Add(theNodeIdOfCall);
            List<string> ValueToWrite = new List<string>();
            ValueToWrite.Add(_OpcUaClient.BooleanToString(Productive));

            //write that the current mode is Idle
            List<uint> allStatusCode;
            _OpcUaClient.writeNodeValue(NodeToWrite, ValueToWrite, _OpcUaConfiguration.SpaceNameIndex, _OpcUaConfiguration.TimeOut, out allStatusCode);

            if (allStatusCode == null)
            {
                SkillProException ex = new SkillProException("Writing the Call variable on the node failed.");
                throw ex;
            }

            if (allStatusCode[0] != 0)
            {
                SkillProException ex = new SkillProException("Writing the Call variable on the node failed." + Environment.NewLine + " The call returned the code " + allStatusCode[0].ToString() + Environment.NewLine + "See Unified Automation documentation for details.");
                throw ex;
            }

            return 0;
        }

        #endregion //related to OPC-UA communication

        #region related to skill execution, pausing, unpausing

        private int ProvideInputDataForSkillExecution_private(ExecutableSkill SkillToExecute, out InputParams ParametersForTheSkill)
        {
            try
            {
                try
                {
                    LogString("ProvideInputDataForSkillExecution_private called on skill : " + SkillToExecute.AmlSkillDescription.Name + " ID : " + SkillToExecute.AmlSkillDescription.ID);
                }
                catch (Exception ex)
                {
                    LogStringException(new SkillProException("EXCEPTION while logging ProvideInputDataForSkillExecution_private called on skill SkillToExecute", ex));
                }

                if (SkillToExecute == null)
                {
                    ParametersForTheSkill = null;
                    string mess = "Exception while executing ProvideInputDataForSkillExecution_private : SkillToExecute is null";
                    SetLastException(mess, new Exception(mess));
                    return Skill.ERROR_INVALID_GIVEN_SKILL;
                }

                if (SkillToExecute.AmlSkillDescription.Execution == null || SkillToExecute.AmlSkillDescription.Execution.Type == null)
                {
                    ParametersForTheSkill = null;
                    string mess = "Exception while executing ProvideInputDataForSkillExecution_private : SkillToExecute.AmlSkillDescription.Execution.Type is null";
                    SetLastException(mess, new Exception(mess));
                    return Skill.ERROR_INVALID_GIVEN_SKILL;
                }

                int returnedCode = ProvideInputDataForSkillExecution(SkillToExecute, out ParametersForTheSkill);

                if (returnedCode != 0)
                {
                    SetLastException("EXCEPTION the method ProvideInputDataForSkillExecution, (which is overriden) return !=0", new Exception("EXCEPTION the method ProvideInputDataForSkillExecution, (which is overriden) return !=0"));
                }

                //By default, a skill is productive, during all its execution
                ParametersForTheSkill.IsProductive = true;
                CurrentSkillIsProductive = true;
                ParametersForTheSkill.ProductiveValueChanged -= ParametersForTheSkill_ProductiveValueChanged;
                ParametersForTheSkill.ProductiveValueChanged += ParametersForTheSkill_ProductiveValueChanged;

                return returnedCode;
            }
            catch (Exception ex)
            {
                SetLastException("EXCEPTION caught while executing ProvideInputDataForSkillExecution (which is overriden)", ex);

                ParametersForTheSkill = new InputParams(Skill.ERROR_INPUT_PROCESS_NULL, null);
                return -1;
            }

        }

        private bool ProvideSkillToExecute_private(ref ExecutableSkill skillWithNoMethod)
        {
            try
            {
                LogString("ProvideSkillToExecute_private called on skill : " + skillWithNoMethod.AmlSkillDescription.Name + " ID : " + skillWithNoMethod.AmlSkillDescription.ID);
            }
            catch (Exception ex)
            {
                LogStringException(new SkillProException("EXCEPTION caught while logging ProvideSkillToExecute_private called on skill SkillToExecute", ex));
            }

            if (skillWithNoMethod == null)
            {
                string mess = "Exception while executing ProvideSkillToExecute_private : skillWithNoMethod is null";
                SetLastException(mess, new Exception(mess));
                return false;
            }

            if (skillWithNoMethod.AmlSkillDescription.Execution == null || skillWithNoMethod.AmlSkillDescription.Execution.Type == null)
            {
                string mess = "Exception while executing ProvideSkillToExecute_private : skillWithNoMethod.AmlSkillDescription.Execution.Type is null";
                SetLastException(mess, new Exception(mess));
                return false;
            }

            try
            {
                ProvideSkillToExecute(ref skillWithNoMethod);
            }
            catch (Exception ex)
            {
                SetLastException("Exception while executing ProvideSkillToExecute", ex);
                return false;
            }

            //IF code passes here, this mean that ProvideSkillToExecute, worked
            return true;
        }

        private void GetOutputsOfExecutedSkill_private(ExecutableSkill SkillExecuted, OutputParams OutputOfExecution)
        {
            try
            {
                try
                {
                    LogString("GetOutputsOfExecutedSkill_private called on skill : " + SkillExecuted.AmlSkillDescription.Name + " ID : " + SkillExecuted.AmlSkillDescription.ID);
                }
                catch (Exception ex)
                {
                    LogStringException(new SkillProException("EXCEPTION caught while logging GetOutputsOfExecutedSkill_private called on skill SkillExecuted", ex));
                }

                if (SkillExecuted == null)
                {
                    throw new SkillProException("The SkillExecuted given to GetOutputsOfExecutedSkill is null");
                }

                if (OutputOfExecution == null)
                {
                    throw new SkillProException("The OutputOfExecution given to GetOutputsOfExecutedSkill are null");
                }

                GetOutputsOfExecutedSkill(SkillExecuted, OutputOfExecution);

            }
            catch (Exception ex)
            {
                SetLastException("EXCEPTION caught while executing  GetOutputsOfExecutedSkill", ex);
            }

            return;

        }

        private void ExecuteSkill(ExecutableSkill SkillToExecute, bool TimeStamped, UInt64 TimestampOfExecution)
        {
            try
            {
                LogString("ExecuteSkill called on skill : " + SkillToExecute.AmlSkillDescription.Name + " ID : " + SkillToExecute.AmlSkillDescription.ID);
            }
            catch (Exception ex)
            {
                LogStringException(new SkillProException("EXCEPTION caught while logging ExecuteSkill called on skill", ex));
            }

            try
            {
                int PotentiallyExecutable = CheckGivenSkillIsPotentiallyExecutable(SkillToExecute, true);

                if (PotentiallyExecutable != 0)
                    return;
            }
            catch (Exception ex)
            {
                SetLastException("EXCEPTION checking SEE conditions and writing Call Exceptions codes and description on OPC-UA server", ex);
                return;
            }

            if (!TimeStamped)
            {
                //Execute Right now!
                lock (_StateMachine)
                {
                    //Change the state of the state machine
                    _StateMachine.Transition(SeeResourceModeTransition.MES_ExecSkill);

                    //Change the state of on OPC-UA server
                    int SettingOk = OpcUaSetResourceMode(SeeResourceMode.ExecutingSkill, SkillToExecute.AmlSkillDescription.ID, true); //Notify so that the previous changement are taken into account

                    if (SettingOk != 0)
                        return; //Exception is allready handled in SetResourceMode

                    //Set the active Skill
                    _SBRC.CurrentExecutingSkill = SkillToExecute;

                }

                //Execute the skill async => otherwise you will not be able to Pause/Resume/Stop
                ExecuteSbrcCurSkillAsync();
                //The execution of the skill will end with the event "AsyncExecutionFinished". See "SkillToExecute_AsyncExecutionFinished" method
            }
            else
            {
                //The execution is timestamped
                //Check timestamp
                DateTime UtcNow;
                UInt64 CurrentTimestamp = SkillProDefinitions.GetSkillProTimestamp(out UtcNow);

                if (TimestampOfExecution < CurrentTimestamp)
                {
                    //Timestamp is in the past => do not execute
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.TimestampInThePast, SkillProDefinitions.TIMESTAMP_IN_THE_PAST_DESCRIPTION, true);
                    return;
                }


                lock (_StateMachine)
                {
                    //Change the state of the state machine
                    _StateMachine.Transition(SeeResourceModeTransition.MES_ExecSkillTimestamp);

                    //Change the state of on OPC-UA server
                    int SettingOk = OpcUaSetResourceMode(SeeResourceMode.IdleQueuedSkill, SkillToExecute.AmlSkillDescription.ID, true); //Notify so that the previous changement are taken into account

                    if (SettingOk != 0)
                        return; //Exception is allready handled in SetResourceMode

                    //Set the active Skill
                    _SBRC.CurrentExecutingSkill = SkillToExecute;
                }


                //Get timespan to sleep
                DateTime DateTimeOfExecution = SkillProDefinitions.TimestampSkillProToDateTime(TimestampOfExecution);
                TimeSpan TimeSpanToWait = DateTimeOfExecution - UtcNow;

                //Then Sleep and start execution (in another thread, in order not to disturb OPC-UA communication)
                SleepWhileQueueing(TimeSpanToWait.TotalMilliseconds);
            }

        }

        private void SleepWhileQueueing(double MsToSleep)
        {
            //Done in another thread, in order not to disturb OPC-UA communication

            ThreadStart Ts = new ThreadStart(() =>
                {

                    SleepingInLoops(MsToSleep);

                    //Sleeping is over, check no-one broke the sleep
                    if (_BreakSleeping)
                    {
                        //The sleep was broken
                        _BreakSleeping = false;

                        //Null current executing skill
                        _SBRC.CurrentExecutingSkill = null;

                        //Check if The MES cleared or if the SEE is disposing

                        if (_Disposing)
                        {
                            //The SEE is disposing
                            LogStringTODO("The SEE is disposing, but it was in Idle Queue => What to do?", "ExecuteSkill");
                            return;
                        }
                        else
                        {
                            //The MES called a "clear"
                            //Go back to IDLE

                            lock (_StateMachine)
                            {
                                if (_StateMachine.CurrentState != SeeResourceMode.IdleQueuedSkill)
                                {
                                    //This is not normal
                                    string Message = "Implementation error => the sleep was broken and you should be IdleQueuedSkill, but not";
                                    LogStringTODO(Message, "ExecuteSkill");
                                    SetLastException(Message, new Exception(Message));
                                    return;
                                }

                                //Change the state of on OPC-UA server
                                int SettingOk = OpcUaSetResourceMode(SeeResourceMode.Idle, "", true);

                                if (SettingOk != 0)
                                    return; //Exception is allready handled in SetResourceMode

                                _StateMachine.Transition(SeeResourceModeTransition.MES_Clear);
                            }

                        }

                    }
                    else
                    {
                        //Sleeping is over, and no-one called to break the sleep
                        //So normally you are still in QueueSkill state
                        //check you are still in QueueSkill state
                        if (_StateMachine.CurrentState != SeeResourceMode.IdleQueuedSkill)
                        {
                            //The queued skill was cancelled (error state probably?)
                            //No need to execute
                            LogString("The queued skill was canceled, no need to execute");
                            return;
                        }
                        else
                        {
                            //Go to executing skill
                            _StateMachine.Transition(SeeResourceModeTransition.INT_EXEC_SKILL);

                            //Execute the skill async => otherwise you will not be able to Pause/Resume/Stop
                            ExecuteSbrcCurSkillAsync();
                            //The execution of the skill will end with the event "AsyncExecutionFinished". See "SkillToExecute_AsyncExecutionFinished" method
                        }
                    }

                });

            _T_SleepingTimestamp = new Thread(Ts);
            _T_SleepingTimestamp.Start();
        }

        private void SleepingInLoops(double TotalMsToSleep)
        {

            //Perform sleeping loops, with subdivision
            UInt64 subdivision = (UInt64)(Math.Floor(TotalMsToSleep / (double)_MsSleepPerLoop));
            double remain = TotalMsToSleep - subdivision * (UInt64)_MsSleepPerLoop;

            if (subdivision >= 1)
            {
                //there are more than 1 second of execution time
                for (UInt64 i = 0; i < subdivision; i++)
                {
                    if (_BreakSleeping)
                        return;
                    System.Threading.Thread.Sleep((int)_MsSleepPerLoop);
                }

                if (_BreakSleeping)
                    return;

                //Sleep the remainer
                Thread.Sleep((int)remain);
            }
            else
            {
                //there are Less than _MsSleepPerLoop second of execution time

                if (_BreakSleeping)
                    return;

                System.Threading.Thread.Sleep((int)TotalMsToSleep);
            }

        }

        private void ExecuteSbrcCurSkillAsync()
        {
            try
            {
                if (_SBRC.CurrentExecutingSkill.SkillToExecute == null)
                {
                    //No method to execute has been provided => need to call ProvideSkillToExecute(ref skillWithNoMethod);
                    ExecutableSkill CurrentExecutingSkill = _SBRC.CurrentExecutingSkill;
                    bool SkillProvided = ProvideSkillToExecute_private(ref CurrentExecutingSkill);

                    if (!SkillProvided)
                        return; // Error has already been handled in ProvideSkillToExecute_private (see inside method). No need to continue the execution
                }

                //Get the up to date input data for the SKill

                InputParams inputForSkill;
                int code = ProvideInputDataForSkillExecution_private(_SBRC.CurrentExecutingSkill, out inputForSkill);

                if (code == 0)
                {
                    //Update remaining duration
                    UInt32 remainingDuration = 0;

                    if (_SBRC.CurrentExecutingSkill.AmlSkillDescription != null && !string.IsNullOrEmpty(_SBRC.CurrentExecutingSkill.AmlSkillDescription.Duration))
                    {
                        double remainingDuration_dbl = 0;

                        try
                        {
                            remainingDuration_dbl = double.Parse(_SBRC.CurrentExecutingSkill.AmlSkillDescription.Duration);
                        }
                        catch (Exception eparse)
                        {
                            LogStringException(new SkillProException("impossible to parse  _SBRC.CurrentExecutingSkill.AmlSkillDescription.Duration into double", eparse));
                        }

                        //Duration given in AmlSkillDescription.Duration is in seconds
                        //So multiply by 1000 to get ms

                        try
                        {
                            remainingDuration_dbl *= 1000;
                            remainingDuration_dbl = Math.Round(remainingDuration_dbl);

                            //Cast into Uint32
                            remainingDuration = (UInt32)remainingDuration_dbl;
                        }
                        catch (Exception emult)
                        {
                            LogStringException(new SkillProException("impossible multiply the duration by 1000, round and then cast into Uint32", emult));
                        }

                    }

                    //Set the current remaining time (inform the OPCUA)
                    CurrentRemainingDuration = remainingDuration;

                    //Set the remaining time on the inputs
                    inputForSkill.RemainingDuration = remainingDuration;

                    //And subscribe to value change, in order to notify the OPCUA server
                    inputForSkill.RemainingDurationValueChanged -= inputForSkill_RemainingDurationValueChanged;
                    inputForSkill.RemainingDurationValueChanged += inputForSkill_RemainingDurationValueChanged;

                    //Execute the Skill
                    _SBRC.CurrentExecutingSkill.SkillToExecute.AsyncExecutionFinished -= SkillToExecute_AsyncExecutionFinished;
                    _SBRC.CurrentExecutingSkill.SkillToExecute.AsyncExecutionFinished += SkillToExecute_AsyncExecutionFinished;
                    _SBRC.CurrentExecutingSkill.SkillToExecute.ExecuteAsync(inputForSkill, 0);
                }
                else
                {
                    //Error has already been handled in ProvideInputDataForSkillExecution_private (see inside method). No need to continue the execution
                    return;
                }

            }
            catch (Exception ex)
            {
                SetLastException("EXCEPTION caught while executing ExecuteSkill", ex);
                return;
            }

            //The execution of the skill will end with the event "AsyncExecutionFinished". See "SkillToExecute_AsyncExecutionFinished" method

        }

        private int CheckGivenSkillIsPotentiallyExecutable(ExecutableSkill SkillToExecute, bool WriteOnOpcUaServer)
        {
            int configOk = _SBRC.CheckCurrentConfiguration(SkillToExecute.AmlSkillDescription.PreCondition.Configuration.Value);


            if (configOk != 0)
            {
                //Configuration is not OK => notify MES via OPC-UA (see D2.3.0 section 4.1)
                if (WriteOnOpcUaServer)
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.PreConditionsNotMet, SkillProDefinitions.PRECONDITIONS_NOT_MET_DESCRIPTION, true);
                return -1;
            }

            int ProductOk = _SBRC.CheckCurrentProduct(SkillToExecute.AmlSkillDescription.PreCondition.Product.Value);

            if (ProductOk != 0)
            {
                //product is not OK => notify MES via OPC-UA (see D2.3.0 section 4.1)
                if (WriteOnOpcUaServer)
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.PreConditionsNotMet, SkillProDefinitions.PRECONDITIONS_NOT_MET_DESCRIPTION, true);
                return -2;
            }

            if (_StateMachine.CurrentState != SeeResourceMode.Idle)
            {
                //state is not OK => notify MES via OPC-UA (see D2.3.0 section 4.1)
                if (WriteOnOpcUaServer)
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NotInIdle, SkillProDefinitions.NOT_IN_IDLE_DESCRIPTION, true);
                return -3;
            }

            //Ok for executing the skill
            if (WriteOnOpcUaServer)
                OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NoExceptions, SkillProDefinitions.NO_EXCEPTION_DESCRIPTION, true);

            return 0;
        }

        private int PauseCurrentSkill()
        {
            try
            {
                LogString("PauseCurrentSkill called on skill : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.Name + " ID : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID);
            }
            catch (Exception ex)
            {
                LogStringException(new SkillProException("logging error in PauseCurrentSkill", ex));
            }

            //Wait the execution to pause in order to be resumable
            _SBRC.CurrentExecutingSkill.SkillToExecute.SkillPaused -= SkillToExecute_SkillPaused;
            _SBRC.CurrentExecutingSkill.SkillToExecute.SkillPaused += SkillToExecute_SkillPaused;

            _SBRC.CurrentExecutingSkill.SkillToExecute.PauseExecution();

            //Wait the Pausing
            if (!_MreWaitPause.IsSet)
            {
                _MreWaitPause.Wait();
            }
            _MreWaitPause.Dispose(); //Dispose the _Mre
            _MreWaitPause = null;

            lock (_StateMachine)
            {
                //Check whether you are in Executing Skill - Pausing or Idle state
                if (_StateMachine.CurrentState == SeeResourceMode.Idle)
                {
                    //The previous execution finishised before you could stop it.
                }
                else if (_StateMachine.CurrentState == SeeResourceMode.ExecutingSkillPausing)
                {
                    //Now you can safely go to  Resumable state
                    int codeCom2 = OpcUaSetResourceMode(SeeResourceMode.ExecutingSkillResumable, _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID, true);

                    if (codeCom2 != 0)
                    {
                        return -6; //Exception is allready handled in SetResourceMode
                    }

                    int codeState2 = _StateMachine.Transition(SeeResourceModeTransition.INT_CAN_RESUME);

                    if (codeState2 != 0)
                    {
                        LogStringTODO("codeState2 != 0", "PauseCurrentSkill");
                        return -7;
                    }
                }
                else
                {
                    LogStringTODO("else line 1488", "PauseCurrentSkill");
                    return -8;
                }

            }

            return 0;
        }

        private int TestIsPausable(bool WriteOnOpcUaServer)
        {
            try
            {
                LogString("TestIsPausable called on skill : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.Name + " ID : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID);
            }
            catch (Exception ex)
            {
                LogStringException(new SkillProException("logging error in PauseCurrentSkill", ex));
            }

            if (_SBRC.CurrentExecutingSkill == null)
            {
                if (WriteOnOpcUaServer)
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NoSkillExecuting, SkillProDefinitions.NO_SKILL_EXECUTING_DESCRIPTION, true);

                return -1;
            }


            if (_SBRC.CurrentExecutingSkill.SkillToExecute.IsSkillPausable == false)
            {
                if (WriteOnOpcUaServer)
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.SkillNotPausable, SkillProDefinitions.SKILL_NOT_PAUSABLE_DESCRIPTION, true);
                return -2;
            }

            lock (_StateMachine)
            {
                _MreWaitPause = new ManualResetEventSlim(false);

                if (_StateMachine.CurrentState != SeeResourceMode.ExecutingSkill)
                {
                    if (WriteOnOpcUaServer)
                        OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NoSkillExecuting, SkillProDefinitions.NO_SKILL_EXECUTING_DESCRIPTION, true);
                    return -3;
                }

                int codeState = _StateMachine.Transition(SeeResourceModeTransition.MES_Pause);

                if (codeState != 0)
                {
                    //I think this cannot happend, since you test above that you are in ExecutingSkill and you locked the statemachine
                    if (WriteOnOpcUaServer)
                        OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NoSkillExecuting, SkillProDefinitions.NO_SKILL_EXECUTING_DESCRIPTION, true);
                    return -4;
                }

                //Here you are sure that you are in Executing Skill Pausing state
            }

            //If code passes here, this mean that a Skill is executing and that you can pause it
            if (WriteOnOpcUaServer)
            {
                int codeCom = OpcUaSetResourceMode(SeeResourceMode.ExecutingSkillPausing, _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID, true);

                if (codeCom != 0)
                {
                    return -5; //Exception is allready handled in SetResourceMode
                }
            }

            return 0;
        }

        private int TestIsResumable(bool WriteOnOpcUaServer)
        {

            lock (_StateMachine)
            {
                if (_StateMachine.CurrentState != SeeResourceMode.ExecutingSkillResumable)
                {

                    if (WriteOnOpcUaServer)
                        OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.SkillNotResumable, SkillProDefinitions.SKILL_NOT_RESUMABLE_DESCRIPTION, true);

                    return -1;
                }

                if (_SBRC.CurrentExecutingSkill == null)
                {
                    LogStringTODO("_SBRC.CurrentExecutingSkill == null", "TestIsResumable");

                    if (WriteOnOpcUaServer)
                        OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.SkillNotResumable, SkillProDefinitions.SKILL_NOT_RESUMABLE_DESCRIPTION, true); //I don't know what to write here! That should not appear anyway. BB 15/09/17
                    return -2;
                }

                return 0;
            }


        }

        private int UnPauseCurrentSkill()
        {
            lock (_StateMachine)
            {

                try
                {
                    LogString("UnPauseCurrentSkill called on skill : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.Name + " ID : " + _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID);
                }
                catch (Exception ex)
                {
                    LogStringException(new SkillProException("EXCEPTION caught while logging UnPauseCurrentSkill called on skill _SBRC.CurrentExecutingSkill", ex));
                }

                //Now you can safely go to Executing

                int codeCom = OpcUaSetResourceMode(SeeResourceMode.ExecutingSkill, _SBRC.CurrentExecutingSkill.AmlSkillDescription.ID, true);

                if (codeCom != 0)
                {
                    return codeCom - 100;
                }

                int codeState = _StateMachine.Transition(SeeResourceModeTransition.MES_Resume);

                if (codeState != 0)
                {
                    //What to do? That should not append
                    LogStringTODO("codeState != 0", "UnPauseCurrentSkill");
                    string mess = "Error for state machine transition in UnPauseCurrentSkill";
                    SetLastException(mess, new Exception(mess));
                    return -120;
                }
            }

            _SBRC.CurrentExecutingSkill.SkillToExecute.UnPauseExecution();

            return 0;

        }

        private int CheckSkillIsKnown(string stringSkillId, out ExecutableSkill SkillToExecute)
        {
            try
            {
                LogString("See.CheckSkillIsKnown called on skill : " + stringSkillId);
            }
            catch (Exception ex)
            {
                LogStringException(new SkillProException("EXCEPTION caught while logging See.CheckSkillIsKnown called on skill " + stringSkillId, ex));
            }

            //Check skill is known
            string ID;
            int code = _SBRC.CheckSkillIsKnown(stringSkillId, out ID);

            if (code == 0)
            {
                SkillToExecute = _SBRC.AllExecutableSkills.Find(skill => skill.AmlSkillDescription.ID == ID);
                return 0;
            }
            else
            {

                //The skill seems currently unknown

                //Check if AMS service has data concerning this Skill
                if (ConnectionAmsServiceOK)
                {
                    LogString("Skill " + stringSkillId + " is unknown. Asking the AMS service");

                    AnswerStatusMessage SatusMessage;
                    AnswerRetrieveExecutableSkill AnsAms = AmsServiceWebClient.RetrieveExecutableSkillBySkillID(this.ConfigurationAms, stringSkillId, out SatusMessage);

                    if (SatusMessage != null)
                    {
                        //The AMS service returned an error
                        LogStringTODO("SatusMessage != null", "OnCallInvokedByServer");
                        LogString("AMS service returned an erreur : " + SatusMessage.message);

                        SkillToExecute = null;
                        return -2;

                    }
                    else
                    {
                        //Add this skill into the Skill Based Ressource Controller
                        AMLDocument AmlSkill = AMLSerializer.DeserializeFromAmlString(AnsAms.amlDescription);

                        if (AmlSkill.ExecutableSkills.Count == 1)
                        {
                            LogString("The AMS service knew about Skill = " + stringSkillId);

                            //The AMS service knew about this Skill. => Add it to the Skill Based Ressource Controller
                            ExecutableSkill NewSkillFromAms = new ExecutableSkill() { AmlSkillDescription = AmlSkill.ExecutableSkills[0], SkillToExecute = null };
                            ProvideSkillToExecute(ref NewSkillFromAms);

                            _SBRC.AddExecutableSkills(NewSkillFromAms);

                            SkillToExecute = NewSkillFromAms;

                            return 0;
                        }
                        else
                        {
                            LogStringTODO("AmlSkill.ExecutableSkills.Count != 1", "OnCallInvokedByServer");
                            SkillToExecute = null;
                            return -3;
                        }

                    }
                }
                else
                {
                    //Skill is unknown and you have no connection whith the AMS service
                    SkillToExecute = null;
                    return -1;
                }
            }
        }

        #endregion //related to skill execution, pausing, unpausing

        #region events

        /// <summary>
        /// The method used to remap the event linked to status of the connection of the OPC-UA client.  
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnConnectionStatusUpdate(Session sender, ServerConnectionStatusUpdateEventArgs e)
        {
            LogStringEvent("OnConnectionStatusUpdate");

            ServerConnectionStatusUpdateEventHandler handler = this.ConnectionStatusUpdate;
            if (handler != null)
            {
                handler(sender, e);
            }
        }

        /// <summary>
        /// The method used to remap the event linked to datachanged of the value of the "Call" node on the OPC-UA server
        /// </summary>
        /// <param name="subscription"></param>
        /// <param name="e"></param>
        private void OnCallInvokedByServer(Subscription subscription, DataChangedEventArgs e)
        {
            LogStringEvent("OnCallInvokedByServer");

            //Remap the event, so that other object can subscribe
            DataChangedEventHandler handler = CallInvokedByServer;
            if (handler != null)
            {
                Thread Propagate = new Thread(() =>
                    {
                        handler(subscription, e);
                    });
                Propagate.Start();
            }


            //Process received Call
            DataChange change = e.DataChanges[0];



            if (StatusCode.IsGood(change.Value.StatusCode))
            {

                string calledString = change.Value.WrappedValue.ToString();

                string stringSkillId;
                UInt64 theTimeStamp;
                SeeSkillExecutionCallMethodsConstants commandCalled;

                try
                {
                    commandCalled = SkillProDefinitions.ParseCallMethod(calledString, out stringSkillId, out theTimeStamp);

                }
                catch (Exception ex)
                {
                    //According to D2.3.0 section 4.1
                    OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.WrongCallFormat, SkillProDefinitions.WRONG_CALL_FORMAT_DESCRIPTION, true);
                    return;
                }


                switch (commandCalled)
                {
                    case SeeSkillExecutionCallMethodsConstants.ExecuteSKill:

                        //First check the skill is known
                        ExecutableSkill SkillToExecute;
                        int TheSkillIsKnown = CheckSkillIsKnown(stringSkillId, out SkillToExecute);

                        if (TheSkillIsKnown == 0)
                        {
                            //Skill is known => Execute that skill (at least try)
                            ExecuteSkill(SkillToExecute, false, 0);
                        }
                        else
                        {
                            //The skill is unknown => notify the server
                            OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NonExistingSkill, SkillProDefinitions.NON_EXISTING_SKILL_DESCRIPTION, true);
                        }
                        break;
                    case SeeSkillExecutionCallMethodsConstants.ExecuteSkillAtTimestamp:

                        //First check the skill is known
                        ExecutableSkill SkillToExecuteAtTimeStamp;
                        int TheSkillIsKnownAtTimeStamp = CheckSkillIsKnown(stringSkillId, out SkillToExecuteAtTimeStamp);

                        if (TheSkillIsKnownAtTimeStamp == 0)
                        {
                            //Skill is known => Execute that skill (at least try)
                            ExecuteSkill(SkillToExecuteAtTimeStamp, true, theTimeStamp);
                        }
                        else
                        {
                            //The skill is unknown => notify the server
                            OpcUaWriteCallExceptionOnTheServer(SeeCallExceptionsConstants.NonExistingSkill, SkillProDefinitions.NON_EXISTING_SKILL_DESCRIPTION, true);
                        }


                        break;
                    case SeeSkillExecutionCallMethodsConstants.ClearQueuedSkill:

                        ClearQueuedSkill();

                        break;
                    case SeeSkillExecutionCallMethodsConstants.PauseCurrentSkill:

                        //Test if Skill is pausable
                        int CodeIsPausable = TestIsPausable(true); //The call was made by the OPC-UA server => write elements on the OPC-UA server

                        if (CodeIsPausable != 0)
                            return;  //No it was not pausable. Errors and exceptions are already Handled in TestIsPausable

                        PauseCurrentSkill();

                        break;
                    case SeeSkillExecutionCallMethodsConstants.ResumePausedSkill:

                        int CodeIsResumable = TestIsResumable(true);//The call was made by the OPC-UA server => write elements on the OPC-UA server

                        if (CodeIsResumable != 0)
                            return;  //No it was not resumable. Errors and exceptions are already Handled in TestIsResumable

                        UnPauseCurrentSkill();

                        break;
                }

            }
            else
            {
                // WHAT TO DO?
                LogStringTODO("StatusCode.IsGood(change.Value.StatusCode)", "OnCallInvokedByServer");
            }

            return;

        }

        private void SkillToExecute_SkillPaused(object sender, EventArgs e)
        {
            LogStringEvent("SkillToExecute_SkillPaused");

            _SBRC.CurrentExecutingSkill.SkillToExecute.SkillPaused -= SkillToExecute_SkillPaused;

            //continue execution of the OnCallInvokedByServer() method
            _MreWaitPause.Set();

        }

        private void SkillToExecute_AsyncExecutionFinished(object sender, OutputParams outputs)
        {
            LogStringEvent("SkillToExecute_AsyncExecutionFinished");

            CurrentSkillIsProductive = false;
            CurrentRemainingDuration = 0;

            if (outputs == null)
            {
                //Free the current active Skill
                _SBRC.CurrentExecutingSkill = null;
                string message = "EXCEPTION while executing SkillToExecute_AsyncExecutionFinished => the provided outputs are NULL";
                SetLastException(message, new Exception(message));
                return;
            }

            //If execution was sucessfull
            if (outputs.ReturnCode == 0)
            {
                try
                {
                    //change the current configuration on SBRC

                    string ConditionToSetOnOpcUAServer;

                    if (!outputs.GoToAlternativePostCondition)
                        _SBRC.ExecutePostConditionConfigurationString(_SBRC.CurrentExecutingSkill.AmlSkillDescription.PostCondition.Configuration, out ConditionToSetOnOpcUAServer);
                    else
                        _SBRC.ExecutePostConditionConfigurationString(_SBRC.CurrentExecutingSkill.AmlSkillDescription.AltPostCondition.Configuration, out ConditionToSetOnOpcUAServer);


                    //change the current product on SBRC

                    string ProductToSetOnOpcUAServer;

                    if (!outputs.GoToAlternativePostCondition)
                        _SBRC.ExecutePostConditionProductString(_SBRC.CurrentExecutingSkill.AmlSkillDescription.PostCondition.Product, out ProductToSetOnOpcUAServer);
                    else
                        _SBRC.ExecutePostConditionProductString(_SBRC.CurrentExecutingSkill.AmlSkillDescription.AltPostCondition.Product, out ProductToSetOnOpcUAServer);


                    //change the current configuration on OPC-UA server
                    OpcUaSetConfigurationCondition(ConditionToSetOnOpcUAServer, false);

                    //change the current product on OPC-UA server
                    OpcUaSetProductCondition(ProductToSetOnOpcUAServer, false);

                    //Check if you were in "Executing Skill - pausing" or "Executing Skill"
                    lock (_StateMachine)
                    {

                        if (_StateMachine.CurrentState != SeeResourceMode.ExecutingSkill && _StateMachine.CurrentState != SeeResourceMode.ExecutingSkillPausing)
                        {
                            //Free the current active Skill
                            _SBRC.CurrentExecutingSkill = null;
                            string message = "line 2110 else case in SkillToExecute_AsyncExecutionFinished)";
                            SetLastException(message, new Exception(message));
                            return;
                        }

                        //Unlock the pause (just in case)
                        if (_MreWaitPause != null)
                        {
                            _MreWaitPause.Set();
                        }

                        try
                        {
                            //return the outputs of execution
                            GetOutputsOfExecutedSkill_private(_SBRC.CurrentExecutingSkill, outputs);

                            //Free the current active Skill
                            _SBRC.CurrentExecutingSkill = null;
                        }
                        catch (Exception ex)
                        {
                            //Free the current active Skill
                            _SBRC.CurrentExecutingSkill = null;
                            SetLastException("EXCEPTION caught while executing  SkillToExecute_AsyncExecutionFinished line 2152", ex);
                            return;
                        }


                        if (_StateMachine.CurrentState == SeeResourceMode.ExecutingSkill)
                        {
                            //You were executing a skill => the skill excution is over now
                            int code = _StateMachine.Transition(SeeResourceModeTransition.INT_SKILL_EXECUTED);

                            if (code != 0)
                            {
                                //Free the current active Skill
                                _SBRC.CurrentExecutingSkill = null;
                                string message = "line 2147 int code = _StateMachine.Transition(SeeResourceModeTransition.INT_SKILL_EXECUTED)";
                                SetLastException(message, new Exception(message));
                                return;
                            }
                        }
                        else if (_StateMachine.CurrentState == SeeResourceMode.ExecutingSkillPausing)
                        {
                            //You were pausing a skill, but the execution finished
                            int code = _StateMachine.Transition(SeeResourceModeTransition.INT_SKILL_FINISHED);

                            if (code != 0)
                            {
                                //Free the current active Skill
                                _SBRC.CurrentExecutingSkill = null;
                                string message = "line 2161 int code = _StateMachine.Transition(SeeResourceModeTransition.INT_SKILL_FINISHED)";
                                SetLastException(message, new Exception(message));
                                return;
                            }
                        }

                        //If code passes here, this mean that it is safe to go to idle
                        //change the Status to Idle
                        int SettingOk = OpcUaSetResourceMode(SeeResourceMode.Idle, "", true); //Notify so that the previous changement (SetConfigurationCondition, SetProductCondition) are taken into account

                        if (SettingOk != 0)
                        {
                            //Exceptions are handled in SetResourceMode
                            return;
                        }
                    }

                }
                catch (Exception ex)
                {
                    //Free the current active Skill
                    _SBRC.CurrentExecutingSkill = null;
                    SetLastException("EXCEPTION caught while executing  SkillToExecute_AsyncExecutionFinished line 1824", ex);
                    return;
                }

            }
            else
            {

                try
                {
                    //return the outputs of execution
                    //ExecutableSkill PropagationSkill = new ExecutableSkill(_SBRC.CurrentExecutingSkill);
                    //GetOutputsOfExecutedSkill_private(PropagationSkill, outputs);
                    GetOutputsOfExecutedSkill_private(_SBRC.CurrentExecutingSkill, outputs);

                    //Free the current active Skill
                    _SBRC.CurrentExecutingSkill = null;
                }
                catch (Exception ex)
                {
                    //Free the current active Skill
                    _SBRC.CurrentExecutingSkill = null;

                    SetLastException("EXCEPTION caught while executing  SkillToExecute_AsyncExecutionFinished line 2197", ex);
                    return;
                }

                string message = "Execution of Skill returned code " + outputs.ReturnCode + " see inner exception ";
                SetLastException(message, new Exception("message", outputs.ExceptionToPropagate));
            }
        }

        private void OnLastExceptionValueChanged(SkillProException ex)
        {
            if (NewExceptionOccured != null)
            {
                NewExceptionOccured(this, ex);
            }
        }

        private void ParametersForTheSkill_ProductiveValueChanged(object sender, bool NewValue)
        {
            CurrentSkillIsProductive = NewValue;
        }

        private void inputForSkill_RemainingDurationValueChanged(object sender, uint NewValue)
        {
            CurrentRemainingDuration = NewValue;
        }

        #endregion //events

        #region Error Handling

        private void SetLastException(string message, Exception ex)
        {
            SkillProException newSkillProEx = new SkillProException(message, ex);
            _ExceptionQueue.Enqueue(newSkillProEx);
            _StateMachine.Transition(SeeResourceModeTransition.INT_ERROR);
            OpcUaSetResourceMode(SeeResourceMode.Error, "", true);
            OnLastExceptionValueChanged(newSkillProEx);
        }

        #endregion //Error Handling

    }
}
