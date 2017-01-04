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

namespace eu.skillpro.see.CS.AML
{
    public class AMLDocument
    {
        #region Fields

        private List<AMLExecutableSkill> _executableSkills = new List<AMLExecutableSkill>();
        private List<AMLSkillExecutionEngine> _SEEs = new List<AMLSkillExecutionEngine>();

        #endregion

        #region Properties

        public List<AMLExecutableSkill> ExecutableSkills
        {
            get { return _executableSkills; }
            set { _executableSkills = value; }
        }

        public List<AMLSkillExecutionEngine> SkillExecutionEngine
        {
            get { return _SEEs; }
            set { _SEEs = value; }
        }

        #endregion

        #region Public methods


        public static void AmlFileHeaderFooter(string FileName, string InstanceHierarchyName, out string Header, out string Footer)
        {

            string HeaderXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + Environment.NewLine + "<CAEXFile xsi:noNamespaceSchemaLocation=\"CAEX_ClassModel_V2.15.xsd\" FileName=\"" + FileName + "\" SchemaVersion=\"2.15\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
            string InstanceHierarchy = "<InstanceHierarchy Name=\"" + InstanceHierarchyName + "\">";

            Header = HeaderXml + Environment.NewLine + InstanceHierarchy;

            string FooterHierar = "</InstanceHierarchy>";
            string FooterCaex = "</CAEXFile>";

            Footer = FooterHierar + Environment.NewLine + FooterCaex;

        }


        #endregion
    }
}
