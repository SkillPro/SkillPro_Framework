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
    public class Execution
    {
        #region Fields

        private List<Data> _datas = new List<Data>();

        #endregion

        #region Properties

        public string Type
        {
            get;
            set;
        }

        public List<Data> Data
        {
            get { return _datas; }
            set { _datas = value; }
        }

        #endregion

        public Execution()
        {
        }

        public Execution(Execution other)
        {
            if (other == null)
            {
                return;
            }

            this.Type = other.Type;
            this.Data = new List<Data>(other.Data);
        }

    }

    public class Data
    {
        #region Fields


        #endregion

        #region Properties

        public object Value
        {
            get;
            set;
        }

        public string Name
        {
            get;
            set;
        }

        public string Description
        {
            get;
            set;
        }

        #endregion

        public Data()
        {
        }

        public Data(Data other)
        {
            this.Description = other.Description;
            this.Name = other.Name;
            this.Value = other.Value; //TODO : clone! BB 150413
        }
    }

}
