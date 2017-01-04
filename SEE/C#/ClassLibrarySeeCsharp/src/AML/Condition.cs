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
    public class Condition
    {
        #region Properties

        public ConditionElement Configuration
        {
            get;
            set;
        }

        public ConditionElement Product
        {
            get;
            set;
        }

        #endregion

        public Condition()
        {
        }

        public Condition(Condition other)
        {
            if (other == null)
            {
                return;
            }

            this.Configuration = new ConditionElement(other.Configuration);
            this.Product = new ConditionElement(other.Product);
        }

        #region Methods

        public override bool Equals(object obj)
        {
            Condition cast;
            try
            {
                cast = (Condition)obj;
            }
            catch (Exception ex)
            {
                return false;
            }


            return ((cast.Configuration.Equals(this.Configuration)) && (cast.Product.Equals(this.Product)));
        }

        #endregion //Methods
    }

    public class ConditionElement
    {
        #region Properties

        public string Description { get; set; }
        public string Value { get; set; }

        #endregion

        public ConditionElement()
        {
        }

        public ConditionElement(ConditionElement other)
        {
            if (other == null)
            {
                return;
            }
            this.Description = other.Description;
            this.Value = other.Value;
        }

        #region Methods

        public override bool Equals(object obj)
        {
            ConditionElement cast;
            try
            {
                cast = (ConditionElement)obj;
            }
            catch (Exception ex)
            {
                return false;
            }


            return ((cast.Value == this.Value) && (cast.Description == this.Description));
        }

        public static void SplitPreConditionConfigurations(ConditionElement ConditionFromSkill, out string[] ConditionValues, out string[] ConditionDescriptions)
        {
            ConditionValues = SkillProDefinitions.ParseConfigurationString_Precondition(ConditionFromSkill.Value);

            if (ConditionFromSkill.Description != null)
            {
                try
                {
                    ConditionDescriptions = SkillProDefinitions.ParseConfigurationString_Precondition(ConditionFromSkill.Description);

                    if (ConditionDescriptions.Length != ConditionValues.Length)
                    {
                        ConditionDescriptions = ConditionValues;
                    }

                }
                catch
                {
                    ConditionDescriptions = ConditionValues;
                }
            }
            else
            {
                ConditionDescriptions = ConditionValues;
            }
        }

        public void SplitPreConditionConfigurations(out string[] ConditionValues, out string[] ConditionDescriptions)
        {
            ConditionElement.SplitPreConditionConfigurations(this, out ConditionValues, out ConditionDescriptions);
        }

        #endregion //Methods
    }
}
