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
    public class AMLExecutableSkill
    {
        #region Fields

        public string Name
        {
            get;
            set;
        }

        public string ID
        {
            get;
            set;
        }

        public string ResourceId
        {
            get;
            set;
        }

        public string ResponsibleSEE
        {
            get;
            set;
        }

        public string Duration
        {
            get;
            set;
        }

        public string Slack
        {
            get;
            set;
        }

        public Execution Execution
        {
            get;
            set;
        }

        public Condition PreCondition
        {
            get;
            set;
        }

        public Condition PostCondition
        {
            get;
            set;
        }

        public Condition AltPostCondition
        {
            get;
            set;
        }

        #endregion

        public AMLExecutableSkill()
        {

        }

        public AMLExecutableSkill(AMLExecutableSkill other)
        {
            this.ID = other.ID;
            this.Name = other.Name;
            this.Duration = other.Duration;
            this.ResourceId = other.ResourceId;
            this.ResponsibleSEE = other.ResponsibleSEE;
            this.Slack = other.Slack;
            this.Execution = other.Execution;
            this.AltPostCondition = new Condition(other.AltPostCondition);
            this.PostCondition = new Condition(other.PostCondition);
            this.PreCondition = new Condition(other.PreCondition);
        }

        public static ExecutableSkill ToExecutableSkill(AMLExecutableSkill AmlExeSkill)
        {
            return new ExecutableSkill() { AmlSkillDescription = AmlExeSkill, Container = null, SkillToExecute = null };
        }

        public string ToCallableString()
        {
            return SkillProDefinitions.CreateCallMethodString(SeeSkillExecutionCallMethodsConstants.ExecuteSKill, ID);
        }
    }
}
