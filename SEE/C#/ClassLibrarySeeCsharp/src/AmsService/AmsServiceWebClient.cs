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

using System.Net;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Runtime.Serialization;

namespace eu.skillpro.see.CS.AmsService
{
    public static class AmsServiceWebClient
    {
        public static DataContractJsonSerializer SerializerAnswerRetrieveExecutableSkillArray = new DataContractJsonSerializer(typeof(AnswerRetrieveExecutableSkill[]));
        public static DataContractJsonSerializer SerializerAnswerRetrieveExecutableSkill = new DataContractJsonSerializer(typeof(AnswerRetrieveExecutableSkill));
        public static DataContractJsonSerializer SerializerAnswerStatusMessage = new DataContractJsonSerializer(typeof(AnswerStatusMessage));


        public static bool CheckAmsServiceConnection(AmsServiceConfiguration configAms)
        {
            //http://osaka05.fzi.de:8080/amsservice/checkConnection

            WebClient webClient = new WebClient();
            SetWebclientParameters(ref webClient, configAms);

            Stream streamAns = webClient.OpenRead(configAms.Uri + "checkConnection");

            StreamReader streamR = new StreamReader(streamAns);
            string stringAns = streamR.ReadToEnd();

            if (string.IsNullOrEmpty(stringAns))
            {
                return false;
            }

            return true;
        }

        public static AnswerRetrieveExecutableSkill[] RetrieveExecutableSkillBySeeID(AmsServiceConfiguration configAms, string SeeID, out AnswerStatusMessage StatusMessage)
        {
            WebClient webClient = new WebClient();
            SetWebclientParameters(ref webClient, configAms);

            webClient.QueryString.Add("seeID", SeeID);

            Stream streamAns = webClient.OpenRead(configAms.Uri + "retrieveResourceExecutableSkills");

            //Extract the string from the answer, and do not bother the webClient after
            StreamReader streamR = new StreamReader(streamAns);
            string stringAns = streamR.ReadToEnd();
            MemoryStream MemStream = new MemoryStream(Encoding.ASCII.GetBytes(stringAns));

            AnswerRetrieveExecutableSkill[] ans = (AnswerRetrieveExecutableSkill[])SerializerAnswerRetrieveExecutableSkillArray.ReadObject(MemStream);

            if (ans == null)
            {
                //Try to see if there is a error message
                MemStream.Position = 0; //Restart stream from 0
                StatusMessage = (AnswerStatusMessage)SerializerAnswerStatusMessage.ReadObject(MemStream);
            }
            else
            {
                StatusMessage = null;
            }


            MemStream.Close();
            streamR.Close();
            webClient.Dispose();

            return ans;
        }

        public static AnswerRetrieveExecutableSkill RetrieveExecutableSkillBySkillID(AmsServiceConfiguration configAms, string SkillId, out AnswerStatusMessage StatusMessage)
        {
            WebClient webClient = new WebClient();
            SetWebclientParameters(ref webClient, configAms);
            webClient.QueryString.Add("id", SkillId);

            Stream streamAns = webClient.OpenRead(configAms.Uri + "retrieveResourceExecutableSkill");

            //Extract the string from the answer, and do not bother the webClient after
            StreamReader streamR = new StreamReader(streamAns);
            string stringAns = streamR.ReadToEnd();
            MemoryStream MemStream = new MemoryStream(Encoding.ASCII.GetBytes(stringAns));

            AnswerRetrieveExecutableSkill ans = (AnswerRetrieveExecutableSkill)SerializerAnswerRetrieveExecutableSkill.ReadObject(MemStream);

            if (ans.resourceExecutableSkillID == null)
            {
                //Try to see if there is a error message
                MemStream.Position = 0; //Restart stream from 0
                StatusMessage = (AnswerStatusMessage)SerializerAnswerStatusMessage.ReadObject(MemStream);
            }
            else
            {
                StatusMessage = null;
            }

            MemStream.Close();
            streamR.Close();
            webClient.Dispose();

            return ans;
        }

        public static AnswerRetrieveExecutableSkill[] RetrieveAllExecutableSkills(AmsServiceConfiguration configAms, out AnswerStatusMessage StatusMessage)
        {
            WebClient webClient = new WebClient();
            SetWebclientParameters(ref webClient, configAms);

            Stream streamAns = webClient.OpenRead(configAms.Uri + "retrieveResourceExecutableSkills");

            StreamReader streamR = new StreamReader(streamAns);
            string stringAns = streamR.ReadToEnd();
            MemoryStream MemStream = new MemoryStream(Encoding.ASCII.GetBytes(stringAns));

            AnswerRetrieveExecutableSkill[] ans = (AnswerRetrieveExecutableSkill[])SerializerAnswerRetrieveExecutableSkillArray.ReadObject(MemStream);

            if (ans == null)
            {
                //Try to see if there is a error message
                MemStream.Position = 0; //Restart stream from 0
                StatusMessage = (AnswerStatusMessage)SerializerAnswerStatusMessage.ReadObject(MemStream);
            }
            else
            {
                StatusMessage = null;
            }

            MemStream.Close();
            streamR.Close();
            webClient.Dispose();

            return ans;
        }

        private static void SetWebclientParameters(ref WebClient AmsWebClient, AmsServiceConfiguration configAms)
        {
            AmsWebClient.Headers.Add("Content-type", configAms.ContentType);
        }
    }
}
