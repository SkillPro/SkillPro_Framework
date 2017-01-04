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
using System.Threading.Tasks;

using System.Threading;
using System.Runtime.Serialization.Json;
using System.Runtime.Serialization;

// codes : 
// -1 : inputs for process were null
// -2 : inputs.Values for process were null
// -3 : inputs.Values for process were invalid
// -11 : inputs for transition were null
// -12 : inputs.Values for transition were null
// -13 : inputs.Values for transition were invalid
// -21 : inputs for closing were null
// -22 : inputs.Values for closing were null
// -23 : inputs.Values for closing were invalid

namespace eu.skillpro.see.CS
{
    /// <summary>
    /// Mother class for Skills, which fullfil the requirements of SkillPro architecture
    /// </summary>
    public class Skill
    {

        /*! 
*  \brief     Mother class for Skills, which fullfil the requirements of SkillPro architecture
*  \details   Mother class for Skills, which fullfil the requirements of SkillPro architecture. The class is flexible : only the code that is executed change from one skill to another. If you follow the design pattern, you are able to stop, pause, resume a skill.
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

        //####################################### CONSTS ####################################################################
        //#######################################################################################################################

        public const int ERROR_INPUT_PROCESS_NULL = -1;
        public const int ERROR_VALUES_PROCESS_NULL = -2;
        public const int ERROR_VALUES_PROCESS_INVALID = -3;

        public const int ERROR_INPUT_CLOSING_NULL = -21;
        public const int ERROR_VALUES_CLOSING_NULL = -22;
        public const int ERROR_VALUES_CLOSING_INVALID = -23;

        public const int ERROR_INVALID_GIVEN_SKILL = -30;


        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        private CancellationTokenSource _StopToken;
        private PauseTokenSource _PauseToken;

        private bool _IsSkillPausable;

        public delegate OutputParams MethodToExecute(InputParams parameters, ref bool SkillIsPausable);

        public delegate void AsyncExecutionFinishedDelegate(object sender, OutputParams outputs);
        public event AsyncExecutionFinishedDelegate AsyncExecutionFinished = null;

        public event EventHandler SkillPaused = null;
        public event EventHandler SkillUnPaused = null;

        private MethodToExecute _Process;
        private MethodToExecute _Closing;

        private InputParams _ParamsForClosing = null;

        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################


        public bool IsSkillPausable
        {
            get { return _IsSkillPausable; }
        }

        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################


        public Skill(MethodToExecute process, MethodToExecute closing)
        {
            _Process = process;
            _Closing = closing;
            _IsSkillPausable = false;
        }

        public Skill(Skill other)
        {
            if (other == null)
            {
                return;
            }

            if (other._Process != null)
                _Process = other._Process;

            if (other._Closing != null)
                _Closing = other._Closing;

            _IsSkillPausable = other.IsSkillPausable;
        }

        public void Execute(InputParams parametersOfProcess, int MsToWaitBeforeExe, out OutputParams output)
        {

            #region Checking the validity of the inputs

            if (parametersOfProcess == null)
            {
                output = new OutputParams(ERROR_INPUT_PROCESS_NULL, null);
                output.Source = SourceOfOutput.Process;
                return;
            }

            if (parametersOfProcess.PreviousReturnCode != 0)
            {
                output = new OutputParams(parametersOfProcess.PreviousReturnCode, null);
                output.Source = SourceOfOutput.Process;
                return;

            }

            if (parametersOfProcess.Values == null)
            {
                output = new OutputParams(ERROR_VALUES_PROCESS_NULL, null);
                output.Source = SourceOfOutput.Process;
                return;
            }

            #endregion //Checking the validity of the inputs

            //take care of the tokens
            _StopToken = new CancellationTokenSource();
            parametersOfProcess.StopToken = _StopToken.Token;

            _PauseToken = new PauseTokenSource();
            parametersOfProcess.PauseToken = _PauseToken.Token;

            //Start Process
            try
            {
                Thread.Sleep(MsToWaitBeforeExe);
                output = _Process(parametersOfProcess, ref _IsSkillPausable);
                output.Source = SourceOfOutput.Process;
            }
            catch (System.OperationCanceledException ex)
            {
                if (_ParamsForClosing == null)
                {
                    //No new parameters were passed for closing => use the same one has for 
                    output = _Closing(parametersOfProcess, ref _IsSkillPausable);
                    output.Source = SourceOfOutput.Closing;
                }
                else
                {
                    //New parameters were passed for closing => check its validity

                    #region Checking the validity of the inputs

                    if (_ParamsForClosing == null)
                    {
                        output = new OutputParams(ERROR_INPUT_CLOSING_NULL, null);
                        output.Source = SourceOfOutput.Closing;
                        return;
                    }

                    if (_ParamsForClosing.PreviousReturnCode != 0)
                    {
                        output = new OutputParams(_ParamsForClosing.PreviousReturnCode, null);
                        output.Source = SourceOfOutput.Closing;
                        return;

                    }

                    if (_ParamsForClosing.Values == null)
                    {
                        output = new OutputParams(ERROR_VALUES_CLOSING_NULL, null);
                        output.Source = SourceOfOutput.Closing;
                        return;
                    }

                    #endregion //Checking the validity of the inputs


                    output = _Closing(_ParamsForClosing, ref _IsSkillPausable);
                    output.Source = SourceOfOutput.Closing;
                }
            }


        }

        public void ExecuteAsync(InputParams parametersOfProcess, int MsToWaitBeforeExe)
        {
            Thread execution = new Thread(() =>
            {
                OutputParams output;

                #region Checking the validity of the inputs

                if (parametersOfProcess == null)
                {
                    output = new OutputParams(ERROR_INPUT_PROCESS_NULL, null);
                    output.Source = SourceOfOutput.Process;
                    OnAsyncExeFinished(output);
                    return;
                }

                if (parametersOfProcess.PreviousReturnCode != 0)
                {
                    output = new OutputParams(parametersOfProcess.PreviousReturnCode, null);
                    output.Source = SourceOfOutput.Process;
                    OnAsyncExeFinished(output);
                    return;

                }

                if (parametersOfProcess.Values == null)
                {
                    output = new OutputParams(ERROR_VALUES_PROCESS_NULL, null);
                    output.Source = SourceOfOutput.Process;
                    OnAsyncExeFinished(output);
                    return;
                }

                #endregion //Checking the validity of the inputs

                //take care of the tokens
                _StopToken = new CancellationTokenSource();
                parametersOfProcess.StopToken = _StopToken.Token;

                _PauseToken = new PauseTokenSource();
                parametersOfProcess.PauseToken = _PauseToken.Token;

                //Start Process
                try
                {
                    Thread.Sleep(MsToWaitBeforeExe);
                    output = _Process(parametersOfProcess, ref _IsSkillPausable);
                    output.Source = SourceOfOutput.Process;
                    OnAsyncExeFinished(output);
                }
                catch (System.OperationCanceledException ex)
                {
                    if (_ParamsForClosing == null)
                    {
                        //No new parameters were passed for closing => use the same one has for 
                        output = _Closing(parametersOfProcess, ref _IsSkillPausable);
                        output.Source = SourceOfOutput.Closing;
                        OnAsyncExeFinished(output);
                    }
                    else
                    {
                        //New parameters were passed for closing => check its validity

                        #region Checking the validity of the inputs

                        if (_ParamsForClosing == null)
                        {
                            output = new OutputParams(ERROR_INPUT_CLOSING_NULL, null);
                            output.Source = SourceOfOutput.Closing;
                            OnAsyncExeFinished(output);
                            return;
                        }

                        if (_ParamsForClosing.PreviousReturnCode != 0)
                        {
                            output = new OutputParams(_ParamsForClosing.PreviousReturnCode, null);
                            output.Source = SourceOfOutput.Closing;
                            OnAsyncExeFinished(output);
                            return;

                        }

                        if (_ParamsForClosing.Values == null)
                        {
                            output = new OutputParams(ERROR_VALUES_CLOSING_NULL, null);
                            output.Source = SourceOfOutput.Closing;
                            OnAsyncExeFinished(output);
                            return;
                        }

                        #endregion //Checking the validity of the inputs


                        output = _Closing(_ParamsForClosing, ref _IsSkillPausable);
                        output.Source = SourceOfOutput.Closing;
                        OnAsyncExeFinished(output);
                    }
                }
            });

            execution.Start();


        }

        public void StopExecution()
        {
            _StopToken.Cancel();

            //Check the System is not paused
            if (_PauseToken.IsPauseRequested)
            {
                //Unpause the System
                _PauseToken.UnPause();
            }

        }

        public void StopExecution(InputParams parametersForClosing)
        {
            _ParamsForClosing = parametersForClosing;
            _StopToken.Cancel();

            //Check the System is not paused
            if (_PauseToken.IsPauseRequested)
            {
                //Unpause the System
                _PauseToken.UnPause();
            }

        }

        public void PauseExecution()
        {
            _PauseToken.SkillPaused -= _PauseToken_SkillPaused;
            _PauseToken.SkillPaused += _PauseToken_SkillPaused;//To propagate the event from the PauseToken
            _PauseToken.Pause();
        }

        void _PauseToken_SkillPaused(object sender, EventArgs e)
        {
            OnSkillPaused();//To propagate the event from the PauseToken
        }

        public void UnPauseExecution()
        {
            _PauseToken.SkillUnPaused -= _PauseToken_SkillUnPaused;
            _PauseToken.SkillUnPaused += _PauseToken_SkillUnPaused; //To propagate the event from the PauseToken
            _PauseToken.UnPause();
        }

        void _PauseToken_SkillUnPaused(object sender, EventArgs e)
        {
            OnSkillUnPaused(); //To propagate the event from the PauseToken
        }


        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################

        #region event

        private void OnAsyncExeFinished(OutputParams outputs)
        {
            if (AsyncExecutionFinished != null)
            {
                AsyncExecutionFinished(this, outputs);
            }
        }

        private void OnSkillPaused()
        {
            //To propagate the event from the PauseToken
            if (SkillPaused != null)
            {
                EventArgs args = new EventArgs();
                SkillPaused(this, args);
            }
        }

        private void OnSkillUnPaused()
        {
            //To propagate the event from the PauseToken
            if (SkillUnPaused != null)
            {
                EventArgs args = new EventArgs();
                SkillUnPaused(this, args);
            }
        }


        #endregion //event

    }

    public class PauseTokenSource
    {

        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        protected ManualResetEvent mre = new ManualResetEvent(true);

        object syncRoot = new object();

        public event EventHandler SkillPaused = null;
        public event EventHandler SkillUnPaused = null;

        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################

        public PauseToken Token { get { return new PauseToken(this); } }

        public bool IsPauseRequested { get { return !mre.WaitOne(0); } }

        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################


        public void Pause()
        {
            lock (syncRoot) { mre.Reset(); }
        }

        public void UnPause()
        {
            lock (syncRoot) { mre.Set(); }
        }

        public void WaitUntilUnPaused()
        {
            OnSkillPaused();
            mre.WaitOne();
            OnSkillUnPaused();
        }

        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################

        #region event


        private void OnSkillPaused()
        {
            if (SkillPaused != null)
            {
                EventArgs args = new EventArgs();
                SkillPaused(this, args);
            }
        }

        private void OnSkillUnPaused()
        {
            if (SkillUnPaused != null)
            {
                EventArgs args = new EventArgs();
                SkillUnPaused(this, args);
            }
        }

        #endregion //event




    }

    public class PauseToken
    {

        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        private PauseTokenSource source;

        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################

        public PauseToken(PauseTokenSource source)
        {
            this.source = source;
        }

        public bool IsPauseRequested
        {
            get { return source != null && source.IsPauseRequested; }
        }

        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################

        public void WaitUntilUnPaused()
        {
            if (source != null)
                source.WaitUntilUnPaused();
        }

        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################

    }

    public class InputParams
    {

        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        private DateTime _CreationDate;

        private int _PreviousReturnCode;

        public object Values;

        public CancellationToken StopToken;

        public PauseToken PauseToken;

        public event EventHandler Pausing = null;

        public event EventHandler Resuming = null;

        public event EventHandler Cancelling = null;

        private bool _IsProductive = false;

        public delegate void ProductiveValueChangedDelegate(object sender, bool NewValue);

        public event ProductiveValueChangedDelegate ProductiveValueChanged = null;

        private UInt32 _RemainingDuration = 0;

        public delegate void RemainingDurationValueChangedDelegate(object sender, UInt32 NewValue);

        public event RemainingDurationValueChangedDelegate RemainingDurationValueChanged = null;

        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################

        public DateTime CreationDate
        {
            get { return _CreationDate; }
        }

        public int PreviousReturnCode
        {
            get { return _PreviousReturnCode; }
        }

        public bool IsProductive
        {
            get { return _IsProductive; }
            set
            {
                bool ThrowEvent = _IsProductive != value;

                _IsProductive = value;

                if (ThrowEvent)
                    OnProductiveValueChanged(value);
            }
        }

        public UInt32 RemainingDuration
        {
            get { return _RemainingDuration; }
            set
            {
                bool ThrowEvent = _RemainingDuration != value;

                _RemainingDuration = value;

                if (ThrowEvent)
                    OnRemainingDurationValueChanged(value);
            }
        }

        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################

        public InputParams(int aPreviousReturnCode, object theValues)
        {
            _CreationDate = DateTime.Now;
            _PreviousReturnCode = aPreviousReturnCode;
            Values = theValues;
        }

        public void StopIfRequired()
        {
            if (StopToken.IsCancellationRequested)
            {
                OnCancelling();
                IsProductive = false;
                StopToken.ThrowIfCancellationRequested();
            }

        }

        public void PauseIfRequired()
        {
            if (PauseToken.IsPauseRequested)
            {
                OnPausing();
                bool BackupProductive = IsProductive;
                IsProductive = false;
                PauseToken.WaitUntilUnPaused();
                OnResuming();
                IsProductive = BackupProductive;
            }
        }

        public void StopOrPauseIfRequired()
        {
            if (StopToken.IsCancellationRequested)
            {
                OnCancelling();
                IsProductive = false;
                StopToken.ThrowIfCancellationRequested();
            }

            if (PauseToken.IsPauseRequested)
            {
                OnPausing();
                bool BackupProductive = IsProductive;
                IsProductive = false;
                PauseToken.WaitUntilUnPaused();
                OnResuming();

                //The execution might come from a pause and a stop is required right away
                if (StopToken.IsCancellationRequested)
                {
                    OnCancelling();
                    IsProductive = false;
                    StopToken.ThrowIfCancellationRequested();
                }

                IsProductive = BackupProductive;
            }
        }

        public InputParamsJson SerializeToJson()
        {
            InputParamsJson returned = new InputParamsJson()
            {
                CreationDate = this.CreationDate,
                PreviousReturnCode = this.PreviousReturnCode,
                TypeOfValues = this.Values.GetType().ToString(),
                ValuesToString = this.Values.ToString()
            };

            return returned;
        }

        public override string ToString()
        {
            InputParamsJson ObjectJson = this.SerializeToJson();
            DataContractJsonSerializer jser = new DataContractJsonSerializer(typeof(InputParamsJson));

            using (System.IO.MemoryStream ms = new System.IO.MemoryStream())
            {
                jser.WriteObject(ms, ObjectJson);
                return Encoding.Default.GetString(ms.ToArray());
            }
        }

        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################

        private void OnPausing()
        {
            if (Pausing != null)
            {
                Pausing(this, null);
            }
        }

        private void OnResuming()
        {
            if (Resuming != null)
            {
                Resuming(this, null);
            }
        }

        private void OnCancelling()
        {
            if (Cancelling != null)
            {
                Cancelling(this, null);
            }
        }

        private void OnProductiveValueChanged(bool newValue)
        {
            if (ProductiveValueChanged != null)
            {
                ProductiveValueChanged(this, newValue);
            }
        }

        private void OnRemainingDurationValueChanged(UInt32 newValue)
        {
            if (RemainingDurationValueChanged != null)
            {
                RemainingDurationValueChanged(this, newValue);
            }
        }

    }

    [DataContract]
    public class InputParamsJson
    {
        [DataMember]
        public DateTime CreationDate;

        [DataMember]
        public int PreviousReturnCode;

        [DataMember]
        public string TypeOfValues;

        [DataMember]
        public string ValuesToString;
    }

    public enum SourceOfOutput { Process, Closing }

    public class OutputParams
    {
        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        private DateTime _CreationDate;

        private int _ReturnCode;

        public object Values;

        public SourceOfOutput Source;

        private bool _GoToAlternativePostCondition;

        public SkillProException ExceptionToPropagate;


        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################

        public DateTime CreationDate
        {
            get { return _CreationDate; }
        }

        public int ReturnCode
        {
            get { return _ReturnCode; }
        }

        public bool GoToAlternativePostCondition
        {
            get { return _GoToAlternativePostCondition; }
            set { _GoToAlternativePostCondition = value; }
        }

        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################

        public OutputParams(int aReturnCode, object theValues, bool goToAlternativePostCondition = false, SkillProException exceptionToPropagate = null)
        {
            _CreationDate = DateTime.Now;
            _ReturnCode = aReturnCode;
            Values = theValues;
            _GoToAlternativePostCondition = goToAlternativePostCondition;
            ExceptionToPropagate = exceptionToPropagate;
        }

        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################


    }

}
