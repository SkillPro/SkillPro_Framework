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
using System.Xml;

using eu.skillpro.see.CS.AML;

namespace eu.skillpro.see.CS.AmsService
{
    [DataContract]
    public class AnswerRetrieveExecutableSkill
    {
        [DataMember]
        public string resourceExecutableSkillID;

        [DataMember]
        public string amlDescription;

        public override string ToString()
        {
            return "resourceExecutableSkillID:" + resourceExecutableSkillID + ";amlDescription:" + amlDescription;
        }

        public string ToAmlFile(string FileName, string InstanceHierarchyName)
        {
            string Header, Footer;
            AMLDocument.AmlFileHeaderFooter(FileName, InstanceHierarchyName, out Header, out Footer);

            return Header + Environment.NewLine + amlDescription + Environment.NewLine + Footer;
        }

        public static string ToAmlFile(AnswerRetrieveExecutableSkill[] Answers, string FileName, string InstanceHierarchyName)
        {
            //string Header, Footer;
            //AMLDocument.AmlFileHeaderFooter(FileName, InstanceHierarchyName, out Header, out Footer);

            //string corpse = Environment.NewLine;

            //for (int i = 0; i < Answers.Length; i++)
            //{
            //    corpse += Answers[i].amlDescription + Environment.NewLine;
            //}

            //return Header + corpse + Footer;

            return AnswerRetrieveExecutableSkill.ToAmlFile(Answers, FileName, InstanceHierarchyName, "");
        }

        public static string ToAmlFile(AnswerRetrieveExecutableSkill[] Answers, string FileName, string InstanceHierarchyName, string SeeId = "")
        {
            string Header, Footer;
            AMLDocument.AmlFileHeaderFooter(FileName, InstanceHierarchyName, out Header, out Footer);

            string corpse = Environment.NewLine;

            for (int i = 0; i < Answers.Length; i++)
            {
                if (string.IsNullOrEmpty(SeeId))
                {
                    //Don't have to check for See ID
                    corpse += Answers[i].amlDescription + Environment.NewLine;
                }
                else
                {
                    AMLDocument AmlD = AMLSerializer.DeserializeFromAmlString(Answers[i].amlDescription);

                    //normally you should have one and only one
                    for (int j = 0; j < AmlD.ExecutableSkills.Count; j++)
                    {
                        if (AmlD.ExecutableSkills[j].ResponsibleSEE == SeeId)
                            corpse += Answers[i].amlDescription + Environment.NewLine;
                    }
                }
            }

            return Header + corpse + Footer;
        }
    }
}
