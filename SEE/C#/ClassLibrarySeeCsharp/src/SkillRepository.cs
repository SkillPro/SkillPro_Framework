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

using eu.skillpro.see.CS;

namespace eu.skillpro.see.CS
{

    /// <summary>
    /// 
    /// </summary>
    public class SkillRepository
    {

        /*! 
        *  \brief     very easy an common implementation of skills
        *  \details   very easy an common implementation of skills
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



        //####################################### CONSTANTS #####################################################################
        //#######################################################################################################################

        public const int ERROR_CODE_NOTHING_SKILL = -40;


        //####################################### ATTRIBUTES ####################################################################
        //#######################################################################################################################


        //####################################### PROPERTIES ####################################################################
        //####################################################################################################################### 


        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################


        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################


        public SkillRepository() { }


        public static OutputParams NothingSkill(InputParams inputs, ref bool SkillIsPausable)
        {
            //Skill is currently not pausable
            SkillIsPausable = false;

            try
            {
                //Lets say skill is productive (even if it is not that true)
                inputs.IsProductive = true;

                //Just propagate
                return new OutputParams(inputs.PreviousReturnCode, inputs.Values);
            }
            catch (Exception ex)
            {
                return new OutputParams(ERROR_CODE_NOTHING_SKILL, null);
            }
        }

        public static OutputParams HelloWorldSkill(InputParams inputs, ref bool SkillIsPausable)
        {
            SkillIsPausable = false;

            //First cast the inputs
            try
            {
                //Don't need the inputs
            }
            catch (System.InvalidCastException ex)
            {
                //If you can't cast the inputs, then there is a special error code for this
                return new OutputParams(Skill.ERROR_VALUES_PROCESS_INVALID, null);
            }

            //Then execute the code
            try
            {
                //Lets say skill is productive (even if it is not that true)
                inputs.IsProductive = true;

                string message;
                string val = "";
                if (inputs == null || inputs.Values == null)
                    val = "NULL";
                else
                    val = inputs.ToString();

                DateTime Now = DateTime.Now;

                message = "Hello world ! inputs.Values.ToString() == " + val;
                Console.WriteLine(message);

                return new OutputParams(0, inputs.Values);
            }
            catch (OperationCanceledException ex)
            {
                //External element asked for "emergency stop".
                //The "Closing Method" will be called (second method passed to the constructor of the skill)

                //You can do some memory freeing here.

                //And then, rethrow the exception
                throw ex;

            }
            catch (Exception ex)
            {
                //Your user code, which starts at -300
                return new OutputParams(-301, null, false, new SkillProException(ex.Message, ex));
            }

        }

        public static OutputParams WaitSkillNonPausable(InputParams inputs, ref bool SkillIsPausable)
        {
            SkillIsPausable = false;

            //First cast the inputs
            int MsToSleep = 0;
            try
            {
                MsToSleep = (int)inputs.Values;
            }
            catch (System.InvalidCastException ex)
            {
                //If you can't cast the inputs, then there is a special error code for this
                return new OutputParams(Skill.ERROR_VALUES_PROCESS_INVALID, null);
            }

            //Then execute the code
            try
            {
                //Lets say skill is productive (even if it is not that true)
                inputs.IsProductive = true;

                string message;
                string val = "";
                if (inputs == null || inputs.Values == null)
                    val = "NULL";
                else
                    val = inputs.ToString();

                DateTime Now = DateTime.Now;

                message = "WaitNonPausableSkill with inputs.Values.ToString() == " + val + " will start sleeping " + MsToSleep + " ms";
                Console.WriteLine(message);
                Thread.Sleep(MsToSleep);
                message = "WaitNonPausableSkill with inputs.Values.ToString() == " + val + " sleeping done ";
                Console.WriteLine(message);

                return new OutputParams(0, inputs.Values);
            }
            catch (OperationCanceledException ex)
            {
                //External element asked for "emergency stop".
                //The "Closing Method" will be called (second method passed to the constructor of the skill)

                //You can do some memory freeing here.

                //And then, rethrow the exception
                throw ex;

            }
            catch (Exception ex)
            {
                //Your user code, which starts at -300
                return new OutputParams(-301, null, false, new SkillProException(ex.Message, ex));
            }

        }

    }
}
