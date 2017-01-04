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

using System.Web.Script.Serialization;

using eu.skillpro.see.CS.AML;


namespace eu.skillpro.see.CS
{
    /// <summary>
    /// An enumeration to gather all the type of calls the OPC-UA server can ask, as described in deliverable 2.3
    /// </summary>
    public enum SeeSkillExecutionCallMethodsConstants { ExecuteSKill, ExecuteSkillAtTimestamp, ClearQueuedSkill, PauseCurrentSkill, ResumePausedSkill };

    /// <summary>
    /// An enumeration to gather all the type of exceptions described in deliverable 2.3
    /// </summary>
    public enum SeeCallExceptionsConstants { NoExceptions = 0, WrongCallFormat = 110, NonExistingSkill = 210, PreConditionsNotMet = 220, TimestampInThePast = 230, SkillNotPausable = 240, NoSkillExecuting = 241, SkillNotResumable = 250, NotInIdle = 260 };

    /// <summary>
    /// An enumeration to gather all the subnodes "names" that each SEE have on the Server
    /// </summary>
    public enum SeeFieldsAndMethodsOnServer { AML_Description, Call, Call_Exception, Call_Exception_Description, Condition_Configuration, Condition_Product, Mode, Next_Available, Remaining_Duration, SEE_Id, Skill, UpdateComplete, CreateSee, Productive };

    /// <summary>
    /// An enumeration to gather all the ressource modes a SEE can be in, as described in deliverable 2.3
    /// </summary>
    public enum SeeResourceMode { PreOperational = 110, ExecutingSkill = 210, ExecutingSkillPausing = 211, ExecutingSkillResumable = 212, Idle = 220, IdleQueuedSkill = 221, Error = 300 };

    /// <summary>
    /// An enumeration to gather all the modes a SEE can be in, as described in deliverable 2.3
    /// </summary>
    public enum SeeModes { Unavailable, Running, Error };

    /// <summary>
    /// An enumeration to gather all the transition between ressource modes a SEE , as described in deliverable 2.4
    /// </summary>
    public enum SeeResourceModeTransition { EXT_SHUTDOWN, EXT_CONFIGURED, INT_EXEC_SKILL, INT_SKILL_EXECUTED, INT_NO_LONGER_PAUSABLE, INT_CAN_RESUME, INT_SKILL_FINISHED, INT_ERROR, EXT_RECOVER, MES_ExecSkill, MES_ExecSkillTimestamp, MES_Clear, MES_Pause, MES_Resume };

    /// <summary>
    /// Static class for the implementation of Skillpro's "utils" methods to be consistent with naming, conventions and specifications of deliverable 2.3 and 2.4
    /// </summary>
    static public class SkillProDefinitions
    {

        /*! 
        *  \brief     Static class for gathering definitions and conventions like naming, according to specifications of deliverable 2.3 and 2.4
        *  \details   Static class for gathering definitions and conventions like naming, according to specifications of deliverable 2.3 and 2.4
        *  \author    Boris Bocquet
        *  \version   1.0.0
        *  \date      2016/10/05
        *  \copyright LGPL , SkillPro 7FP EU
        *  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
        *  \pre       .
        *  \bug       .
        *  \warning   .
        *  \todo	.
        */

        //####################################### STATIC OBJECTS #############################################################""
        //####################################################################################################################"


        public static JavaScriptSerializer JSS = new JavaScriptSerializer();

        //####################################### CONST ################################################################""
        //####################################################################################################################"

        //Related to deliverable D2.3.0 section 4.1

        //CALL from OPC-UA
        public const string WRONG_CALL_FORMAT_DESCRIPTION = "The call string cannot be parsed";
        public const string NON_EXISTING_SKILL_DESCRIPTION = "The skill-id provided in the call is unknown (on that particular resource/SEE)";
        public const string PRECONDITIONS_NOT_MET_DESCRIPTION = "The pre-conditions required for the skill are not met on the resource";
        public const string TIMESTAMP_IN_THE_PAST_DESCRIPTION = "The timestamp at which the skill execution shall be started cannot be met";
        public const string SKILL_NOT_PAUSABLE_DESCRIPTION = "The skill is not configured to be pausable";
        public const string SKILL_NOT_RESUMABLE_DESCRIPTION = "Skill cannot be resumed : The SEE is currently not in the 'Executing Skill - Resumable' mode";
        public const string NOT_IN_IDLE_DESCRIPTION = "The Skill is not executable because the SEE is not in 'IDLE' mode";
        public const string NO_EXCEPTION_DESCRIPTION = "No exception : the call was successfull (format is ok, state is ok, conditions are ok)";
        public const string NO_SKILL_EXECUTING_DESCRIPTION = "Skill not pausable because no skill is executing";

        //AML
        public const string ANY_CONFIGURATIONS = "ANY";
        public const string SAME_CONFIGURATION = "SAME";

        //Time
        public const int MS_PER_DAY_INT = 24 * 3600000;
        public const double MS_PER_DAY_DBL = 24 * 3600000;

        //####################################### STATIC PUBLIC METHODS ################################################################""
        //####################################################################################################################"


        /// <summary>
        /// Static public method to get the equivalent SeeMode (high level mode) from the SeeResourceMode Constants . 
        /// </summary>
        /// <param name="aSeeResourceMode">The mode of the resource</param>
        /// <returns>The see mode (high level mode) </returns>
        static public SeeModes GetSeeModesFromSeeResourceMode(SeeResourceMode aSeeResourceMode)
        {
            switch (aSeeResourceMode)
            {
                case SeeResourceMode.PreOperational:
                    return SeeModes.Unavailable;
                case SeeResourceMode.ExecutingSkill:
                    return SeeModes.Running;
                case SeeResourceMode.ExecutingSkillPausing:
                    return SeeModes.Running;
                case SeeResourceMode.ExecutingSkillResumable:
                    return SeeModes.Running;
                case SeeResourceMode.Idle:
                    return SeeModes.Running;
                case SeeResourceMode.IdleQueuedSkill:
                    return SeeModes.Running;
                case SeeResourceMode.Error:
                    return SeeModes.Error;
                default:
                    {
                        NotImplementedException ex = new NotImplementedException();
                        throw ex;
                    }
            }

        }

        /// <summary>
        /// Public static methode to extract and parse all elements of a input CallMethodString taken on the OPC-UA server. Remark :  aSkillId and aTimestamp are dummies if they are not available in the input CallMethodString.
        /// </summary>
        /// <param name="aCallMethodString">The CallMethod string taken on the OPC-UA server in the "Call" field.</param>
        /// <param name="aSkillId">The id of the skill that needs to be called. This output is relevant only in the case "ExecuteSKill" and "ExecuteSkillAtTimestamp"</param>
        /// <param name="aTimestamp">The timestamp when to execute the string. This output is relevant only in the case "ExecuteSkillAtTimestamp" </param>
        /// <returns></returns>
        static public SeeSkillExecutionCallMethodsConstants ParseCallMethod(string aCallMethodString, out string aSkillId, out UInt64 aTimestamp)
        {

            SeeSkillExecutionCallMethodsConstants output;

            //separe the string
            string[] separedString = SepareCallMethodString(aCallMethodString);

            //read the first element
            switch (separedString[0])
            {
                case "ExecSkill":
                    {
                        //caution , it can be ExecSkill:<id> or ExecSkill:<id>:<timestamp>
                        switch (separedString.GetLength(0))
                        {
                            case 2:
                                //it was ExecSkill:<id>
                                output = SeeSkillExecutionCallMethodsConstants.ExecuteSKill;

                                aSkillId = separedString[1];

                                //aTimestamp is dummy
                                aTimestamp = GetSkillProTimestamp();
                                break;
                            case 3:
                                //it was ExecSkill:<id>:<timestamp>
                                output = SeeSkillExecutionCallMethodsConstants.ExecuteSkillAtTimestamp;

                                aSkillId = separedString[1];

                                //aTimestamp is dummy
                                aTimestamp = UInt64.Parse(separedString[2]);
                                break;
                            default:
                                {
                                    SkillProException ex = new SkillProException("Call string invalid => Can't find equivalence to :" + aCallMethodString, new ArgumentException("Call string invalid => Can't find equivalence to :" + aCallMethodString));
                                    throw ex;
                                }
                        }

                        break;
                    }
                case "Clear":
                    {

                        output = SeeSkillExecutionCallMethodsConstants.ClearQueuedSkill;

                        //skillid is dummy
                        aSkillId = "Clear";

                        //aTimestamp is dummy
                        aTimestamp = GetSkillProTimestamp();

                        break;
                    }
                case "Pause":
                    {
                        output = SeeSkillExecutionCallMethodsConstants.PauseCurrentSkill;

                        //skillid is dummy
                        aSkillId = "Pause";

                        //aTimestamp is dummy
                        aTimestamp = GetSkillProTimestamp();

                        break;
                    }
                case "Resume":
                    {
                        output = SeeSkillExecutionCallMethodsConstants.ResumePausedSkill;

                        //skillid is dummy
                        aSkillId = "Resume";

                        //aTimestamp is dummy
                        aTimestamp = GetSkillProTimestamp();

                        break;
                    }
                default:
                    {
                        SkillProException ex = new SkillProException("Call string invalid => Can't find equivalence to :" + aCallMethodString, new ArgumentException("Call string invalid => Can't find equivalence to :" + aCallMethodString));
                        throw ex;
                    }
            }

            return output;
        }

        public static string StringIdentifierOfSeeFieldOrMethod(string nodeId, SeeFieldsAndMethodsOnServer fieldOrMethod)
        {
            string separator = ".";

            string outputString = "";

            switch (fieldOrMethod)
            {
                case SeeFieldsAndMethodsOnServer.AML_Description:
                    {
                        outputString = nodeId.ToString() + separator + "AML_Description";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Call:
                    {
                        outputString = nodeId.ToString() + separator + "Call";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Call_Exception:
                    {
                        outputString = nodeId.ToString() + separator + "Call_Exception";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Call_Exception_Description:
                    {
                        outputString = nodeId.ToString() + separator + "Call_Exception_Description";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Condition_Configuration:
                    {
                        outputString = nodeId.ToString() + separator + "Condition_Configuration";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Condition_Product:
                    {
                        outputString = nodeId.ToString() + separator + "Condition_Product";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.CreateSee:
                    {
                        outputString = nodeId.ToString() + separator + "CreateSee";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Mode:
                    {
                        outputString = nodeId.ToString() + separator + "Mode";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Next_Available:
                    {
                        outputString = nodeId.ToString() + separator + "Next_Available";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Remaining_Duration:
                    {
                        outputString = nodeId.ToString() + separator + "Remaining_Duration";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.SEE_Id:
                    {
                        outputString = nodeId.ToString() + separator + "SEE_Id";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Skill:
                    {
                        outputString = nodeId.ToString() + separator + "Skill";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.UpdateComplete:
                    {
                        outputString = nodeId.ToString() + separator + "UpdateComplete";
                        break;
                    }
                case SeeFieldsAndMethodsOnServer.Productive:
                    {
                        outputString = nodeId.ToString() + separator + "Productive";
                        break;
                    }
                default:
                    {
                        NotImplementedException ex = new NotImplementedException();
                        throw ex;
                    }
            }

            return outputString;

        }

        static public UInt64 GetSkillProTimestamp()
        {
            DateTime now;
            return GetSkillProTimestamp(out now);
        }

        static public UInt64 GetSkillProTimestamp(out DateTime UtcNow)
        {
            UtcNow = DateTime.UtcNow;
            return (UInt64)(UtcNow - new DateTime(1970, 1, 1)).TotalMilliseconds;
        }

        static public UInt64 GetSkillProTimestampFuture(int MsInTheFuture)
        {
            TimeSpan TimeDuration = new TimeSpan(0, 0, 0, 0, MsInTheFuture);
            return GetSkillProTimestampFuture(TimeDuration);
        }

        static public UInt64 GetSkillProTimestampFuture(TimeSpan TimeDuration)
        {
            DateTime Future = DateTime.UtcNow + TimeDuration;
            return (UInt64)(Future - new DateTime(1970, 1, 1)).TotalMilliseconds;
        }

        public static DateTime TimestampSkillProToDateTime(UInt64 SkillProTimeStamp)
        {
            DateTime BaseDate = new DateTime(1970, 1, 1);

            if (SkillProTimeStamp > int.MaxValue)
            {
                //Now do some calculations
                double SkillProTimeStamp_dbl = (double)SkillProTimeStamp;

                //Devide per days
                int Quotient_days = (int)Math.Floor(SkillProTimeStamp_dbl / MS_PER_DAY_DBL);

                UInt64 remainerDays = SkillProTimeStamp - (UInt64)(Quotient_days * MS_PER_DAY_INT);

                //Remainer days is less than 1 day, so less than MS_PER_DAY_INT (less than MAX_INT)

                TimeSpan DaysToSpend = new TimeSpan(Quotient_days, 0, 0, 0, 0);
                TimeSpan MsToSpend = new TimeSpan(0, 0, 0, 0, (int)remainerDays);

                return BaseDate + DaysToSpend + MsToSpend;

            }
            else
            {
                TimeSpan Ts = new TimeSpan(0, 0, 0, 0, (int)SkillProTimeStamp);
                return BaseDate + Ts;
            }

        }

        static public string CreateCallMethodString(SeeSkillExecutionCallMethodsConstants TypeOfCall, string SkillId = "", UInt64 aTimestamp = 0)
        {
            switch (TypeOfCall)
            {
                case SeeSkillExecutionCallMethodsConstants.ExecuteSKill:
                    return "ExecSkill:" + SkillId;
                case SeeSkillExecutionCallMethodsConstants.ExecuteSkillAtTimestamp:
                    return "ExecSkill:" + SkillId + ':' + aTimestamp.ToString();
                case SeeSkillExecutionCallMethodsConstants.ClearQueuedSkill:
                    return "Clear";
                case SeeSkillExecutionCallMethodsConstants.PauseCurrentSkill:
                    return "Pause";
                case SeeSkillExecutionCallMethodsConstants.ResumePausedSkill:
                    return "Resume";
                default:
                    return "";
            }
        }

        /// <summary>
        /// For format of configuration string, for pre-condition only, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"
        /// </summary>
        /// <param name="input"> For format of configuration string, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"</param>
        /// <returns>Array of strings [id1,id2,id3]</returns>
        static public string[] ParseConfigurationString_Precondition(string input)
        {
            return ParseConfigurationStringPrecondition_private(input);
        }

        /// <summary>
        /// For format of configuration string, for post-condition and alternative post-condition only, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"
        /// </summary>
        /// <param name="input"> For format of configuration string, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"</param>
        /// <returns>string of the post condition</returns>
        static public string ParseConfigurationString_Postcondition(string input)
        {
            return input.Trim();
        }

        public static bool CheckStringIsAnyConfiguration(string input)
        {
            return CheckTwoStrings(input, ANY_CONFIGURATIONS);
        }

        public static bool CheckTwoStrings(string string1, string string2)
        {
            if (string1 == null && string2 == null)
                return true;

            if (string1 == null)
                return false;

            if (string2 == null)
                return false;

            //Here you are sure that both strings are not null

            string processedString1 = string1.Trim();
            processedString1 = processedString1.ToLower();

            string processedString2 = string2;
            processedString2 = processedString2.Trim();
            processedString2 = processedString2.ToLower();

            return processedString1 == processedString2;
        }

        public static Dictionary<string, List<List<string>>> ParseProductStringPreCondition(string input)
        {
            try
            {
                return JSS.Deserialize<Dictionary<string, List<List<string>>>>(input);
            }
            catch (Exception ex)
            {
                throw new SkillProException("Invalid given Product string for PreCondition : " + input, ex);
            }
        }

        public static Dictionary<string, string> ParseProductStringPostCondition(string input)
        {

            try
            {
                return JSS.Deserialize<Dictionary<string, string>>(input);
            }
            catch (Exception ex)
            {
                throw new SkillProException("Invalid given Product string for PostCondition : " + input, ex);
            }
        }

        public static List<KeyValuePair<string, string>> ParseProductStringInitialCondition(string input)
        {
            Dictionary<string, string> dico;

            try
            {
                dico = JSS.Deserialize<Dictionary<string, string>>(input);
                if (dico == null)
                    throw new Exception("deserialization is null");
            }
            catch (Exception ex)
            {
                throw new SkillProException("Invalid given Product string for Initial Condition : " + input, ex);
            }

            return dico.ToList();
        }

        //####################################### SATIC PRIVATE METHODS ################################################################""
        //####################################################################################################################"

        static private string[] SepareCallMethodString(string aCallMethodString)
        {
            return aCallMethodString.Split(':');
        }

        /// <summary>
        /// For format of configuration string, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"
        /// </summary>
        /// <param name="input"> For format of configuration string, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"</param>
        /// <returns>Array of strings [id1,id2,id3]</returns>
        static private string[] ParseConfigurationStringPrecondition_private(string input)
        {
            try
            {
                string InputTrimmed = input.Trim();

                if (InputTrimmed[0] != '[')
                {
                    if (InputTrimmed[0] == '\"')
                        return new string[] { JSS.Deserialize<string>(input) }; //you simply received the ID of the configuration, not in a array with []. And it is in JSON format (with "")
                    else
                        return new string[] { InputTrimmed }; // You just received a string, which is NOT in Json format (there is no "")
                }
                else
                    return JSS.Deserialize<string[]>(input); //You received something like "["<id1>" , "<id2>" ]"
            }
            catch (Exception ex)
            {
                throw new SkillProException("Impossible to deserialize given Configuration string : " + input, ex);
            }
        }

    }
}
