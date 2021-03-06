﻿/*****************************************************************************
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

using eu.skillpro.see.CS.AML;

namespace eu.skillpro.see.CS
{

    public class ExecutableSkill
    {
        public AMLExecutableSkill AmlSkillDescription = null;
        public Skill SkillToExecute = null;
        public object Container = null;

        public ExecutableSkill()
        {
        }

        public ExecutableSkill(ExecutableSkill other)
        {
            this.AmlSkillDescription = new AMLExecutableSkill(other.AmlSkillDescription);
            this.SkillToExecute = new Skill(other.SkillToExecute);
            this.Container = other.Container; //TODO : Clone. BB 150413
        }

        public string ToCallableString()
        {
            return AmlSkillDescription.ToCallableString();
        }
    }
}
