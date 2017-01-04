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

namespace eu.skillpro.see.CS
{



    /// <summary>
    /// Mother class to manage the StateMachine of Skillpro's SEEs in C#. This machine fullfils the requirements of deliverables 2.3 and 2.4
    /// </summary>
    public class SeeStateMachine
    {


        /*! 
        *  \brief     Mother class to manage the StateMachine of Skillpro's SEEs in C#.
        *  \details   Mother class to manage the StateMachine of Skillpro's SEEs in C#. Implementation fullfils the specifications of deliverable 2.3 and 2.4
        *  \author    Boris Bocquet
        *  \version   1.0.0
        *  \date      2016/10/05
        *  \copyright LGPL , SkillPro 7FP EU
        *  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
        *  \pre       OPC-UA server respecting the specifications of deliverable 2.3  must be running
        *  \bug       .
        *  \warning   .
        *  \todo	
        */

        //####################################### ATTRIBUTES ################################################################""
        //####################################################################################################################"        

        #region Threading

        /// <summary>
        /// Lock object to avoid concurent access
        /// </summary>
        private object _LockObject = new object();

        #endregion //Threading

        #region states

        private SeeResourceMode _CurrentState = SeeResourceMode.PreOperational;

        private SeeResourceMode _PreviousState = SeeResourceMode.PreOperational;

        private Dictionary<SeeResourceMode[], SeeResourceModeTransition> _MappingOfModes;

        #endregion //states

        #region modes

        private SeeModes _CurrentMode = SeeModes.Unavailable;

        private SeeModes _PreviousMode = SeeModes.Unavailable;


        #endregion //modes

        #region events

        #region state

        /// <summary>
        /// Prototypes for delegates methods given to the StateChanged event handler
        /// </summary>
        /// <param name="sender">sender of the event</param>
        /// <param name="args">arguments of the event. Can precise the previous and new state.</param>
        public delegate void StateChangedDelegate(object sender, StateChangedEventArgs args);

        /// <summary>
        /// The event handler associated to changes in the state of the state machine
        /// </summary>
        public event StateChangedDelegate StateChanged = null;

        #endregion //state

        #region Modes

        /// <summary>
        /// Prototypes for delegates methods given to the ModeChanged event handler
        /// </summary>
        /// <param name="sender">sender of the event</param>
        /// <param name="args">arguments of the event. Can precise the previous and new mode.</param>
        public delegate void ModeChangedDelegate(object sender, ModeChangedEventArgs args);

        /// <summary>
        /// The event handler associated to changes in the mode of the state machine
        /// </summary>
        public event ModeChangedDelegate ModeChanged = null;

        #endregion //Modes

        #endregion //events

        //####################################### PROPERTIES ################################################################""
        //####################################################################################################################"

        #region states

        /// <summary>
        /// The current state of the SEE
        /// </summary>
        public SeeResourceMode CurrentState
        {
            get { return _CurrentState; }
            //set { _CurrentState = value; }
        }

        /// <summary>
        /// The previous state of the SEE
        /// </summary>
        public SeeResourceMode PreviousState
        {
            get { return _PreviousState; }
            //set { _PreviousState = value; }
        }

        #endregion //states

        #region modes

        /// <summary>
        /// The current mode of the SEE
        /// </summary>
        public SeeModes CurrentMode
        {
            get { return _CurrentMode; }
            //set { _CurrentMode = value; }
        }

        /// <summary>
        /// The previous mode of the SEE
        /// </summary>
        public SeeModes PreviousMode
        {
            get { return _PreviousMode; }
            //set { _PreviousMode = value; }
        }

        #endregion //mode

        //####################################### PUBLIC METHODS ################################################################""
        //####################################################################################################################"

        #region constructor / destructor

        /// <summary>
        /// Main constructor of the state machine
        /// </summary>
        public SeeStateMachine()
        {
            createMappingDictionary();
        }

        ~SeeStateMachine()
        {
        }

        #endregion

        #region interacting with the state machine

        /// <summary>
        /// Perform the transition. If 0 is returned, the transition was possible and state changed.
        /// </summary>
        /// <param name="StateTransition">The transition to be performed</param>
        /// <returns>0 : transition has been performed, state changed. -1 : transition was not possible and state did not change</returns>
        public int Transition(SeeResourceModeTransition StateTransition)
        {
            //If there is a changement in the state
            bool fireModechanged = false;
            bool fireStateChanged = false;
            StateChangedEventArgs argState;
            ModeChangedEventArgs argMode = null;

            lock (_LockObject)
            {
                //test if transition is possible
                SeeResourceMode NewState;
                bool TransitionOK = TestTransition(StateTransition, out NewState);

                if (!TransitionOK)
                    return -1;


                //The transition is possible => update state
                if (NewState != _CurrentState)
                {

                    //backup state and mode
                    SeeResourceMode backupPreviousSate = _CurrentState;
                    SeeModes backupPreviousMode = _CurrentMode;

                    //update state
                    _PreviousState = _CurrentState;
                    _CurrentState = NewState;



                    //check if mode changed
                    SeeModes futurMode = SkillProDefinitions.GetSeeModesFromSeeResourceMode(NewState);

                    if (futurMode != _CurrentMode)
                    {
                        //mode has changed => raise event

                        //update mode
                        _PreviousMode = _CurrentMode;
                        _CurrentMode = futurMode;

                        //raise event
                        argMode = new ModeChangedEventArgs(backupPreviousMode, futurMode);
                        fireModechanged = true;

                    }

                    argState = new StateChangedEventArgs(backupPreviousSate, NewState);
                    fireStateChanged = true;
                }
                else
                {
                    //there is no changing in the state
                    return -1;
                }

            }

            //raise event
            if (fireStateChanged)
                onChangingState(argState);

            if (fireModechanged)
                onChangingMode(argMode);

            return 0;
        }

        /// <summary>
        /// Test whether a transition is possible or not. If yes, the New state to go into is given.
        /// </summary>
        /// <param name="StateTransition">The transition you want to test feasibility</param>
        /// <param name="NewState">The new state to go into if transition is possible. Otherwise SeeResourceMode.Error is returned</param>
        /// <returns>True : Yes transition is possible. False otherwise</returns>
        public bool TestTransition(SeeResourceModeTransition StateTransition, out SeeResourceMode NewState)
        {
            //Get the states associated with this transition
            lock (_LockObject)
            {

                var AllStatePairs = from item in _MappingOfModes
                                    where item.Value == StateTransition
                                    select item.Key;

                List<SeeResourceMode[]> candidates = AllStatePairs.ToList();

                int index = candidates.FindIndex(statePair => statePair[0] == _CurrentState);

                if (index == -1)
                {
                    //The current state does not allow the asked transition
                    NewState = SeeResourceMode.Error;
                    return false;
                }

                NewState = candidates.ElementAt(index)[1];
                return true;
            }
        }

        /// <summary>
        ///  Test whether going to this state is possible or not. If yes, the transition to execute is given
        /// </summary>
        /// <param name="NewState">The new state you want to go into</param>
        /// <param name="FoundTransition">The corresponding transition</param>
        /// <returns>True : you can go into this new state and the corresponding transition is given in "FoundTransition". False : impossible and FoundTransition == SeeResourceModeTransition.INT_ERROR (do not use it)</returns>
        public bool TestChangeState(SeeResourceMode NewState, out SeeResourceModeTransition FoundTransition)
        {
            //Get the transition between current state and new state
            lock (_LockObject)
            {
                var AllTransitions = from item in _MappingOfModes
                                     where item.Key[0] == _CurrentState && item.Key[1] == NewState
                                     select item.Value;

                List<SeeResourceModeTransition> AllTransitionsList = AllTransitions.ToList();

                if (AllTransitionsList.Count == 0)
                {
                    FoundTransition = SeeResourceModeTransition.INT_ERROR;
                    return false;
                }
                else
                {
                    FoundTransition = AllTransitionsList[0];
                    return true;
                }
            }
        }

        #endregion

        //####################################### PRIVATE METHODS ################################################################""
        //####################################################################################################################"

        private void createMappingDictionary()
        {
            _MappingOfModes = new Dictionary<SeeResourceMode[], SeeResourceModeTransition>();

            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.Idle, SeeResourceMode.PreOperational }, SeeResourceModeTransition.EXT_SHUTDOWN);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.Error, SeeResourceMode.PreOperational }, SeeResourceModeTransition.EXT_RECOVER);

            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.PreOperational, SeeResourceMode.Idle }, SeeResourceModeTransition.EXT_CONFIGURED);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.IdleQueuedSkill, SeeResourceMode.ExecutingSkill }, SeeResourceModeTransition.INT_EXEC_SKILL);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkill, SeeResourceMode.Idle }, SeeResourceModeTransition.INT_SKILL_EXECUTED);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillPausing, SeeResourceMode.ExecutingSkillResumable }, SeeResourceModeTransition.INT_CAN_RESUME);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillPausing, SeeResourceMode.ExecutingSkill }, SeeResourceModeTransition.INT_NO_LONGER_PAUSABLE);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillPausing, SeeResourceMode.Idle }, SeeResourceModeTransition.INT_SKILL_FINISHED);

            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.Idle, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.IdleQueuedSkill, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkill, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillPausing, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillResumable, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.PreOperational, SeeResourceMode.Error }, SeeResourceModeTransition.INT_ERROR);

            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.Idle, SeeResourceMode.ExecutingSkill }, SeeResourceModeTransition.MES_ExecSkill);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.Idle, SeeResourceMode.IdleQueuedSkill }, SeeResourceModeTransition.MES_ExecSkillTimestamp);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.IdleQueuedSkill, SeeResourceMode.Idle }, SeeResourceModeTransition.MES_Clear);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillResumable, SeeResourceMode.Idle }, SeeResourceModeTransition.MES_Clear);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkill, SeeResourceMode.ExecutingSkillPausing }, SeeResourceModeTransition.MES_Pause);
            _MappingOfModes.Add(new SeeResourceMode[] { SeeResourceMode.ExecutingSkillResumable, SeeResourceMode.ExecutingSkill }, SeeResourceModeTransition.MES_Resume);

        }

        #region events

        private void onChangingState(StateChangedEventArgs arguments)
        {
            if (StateChanged != null)
            {
                StateChanged(this, arguments);
            }
        }

        private void onChangingMode(ModeChangedEventArgs arguments)
        {
            if (ModeChanged != null)
            {
                ModeChanged(this, arguments);
            }
        }

        #endregion //events
    }


    public class StateChangedEventArgs : EventArgs
    {
        public UInt64 TimeStampOfEvent;

        public SeeResourceMode NewState = SeeResourceMode.PreOperational;

        public SeeResourceMode PreviousState = SeeResourceMode.PreOperational;

        public StateChangedEventArgs(SeeResourceMode aPreviousState, SeeResourceMode aNewState)
        {
            TimeStampOfEvent = SkillProDefinitions.GetSkillProTimestamp();

            NewState = aNewState;

            PreviousState = aPreviousState;
        }
    }


    public class ModeChangedEventArgs : EventArgs
    {
        public UInt64 TimeStampOfEvent;

        public SeeModes NewMode = SeeModes.Unavailable;

        public SeeModes PreviousMode = SeeModes.Unavailable;

        public ModeChangedEventArgs(SeeModes aPreviousMode, SeeModes aNewMode)
        {
            TimeStampOfEvent = SkillProDefinitions.GetSkillProTimestamp();

            NewMode = aNewMode;

            PreviousMode = aPreviousMode;
        }
    }
}
