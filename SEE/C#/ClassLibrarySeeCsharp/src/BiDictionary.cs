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
    /// A bidictionary => the key is (of course) unique but also the value. Usefull to create 1 for 1 mapping
    /// </summary>
    public class BiDictionary<TFirst, TSecond>
    {
        /*! 
        *  \brief      A bidictionary => the key is (of course) unique but also the value. Usefull to create 1 for 1 mapping
        *  \details    A bidictionary => the key is (of course) unique but also the value. Usefull to create 1 for 1 mapping
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

        IDictionary<TFirst, TSecond> firstToSecond = new Dictionary<TFirst, TSecond>();
        IDictionary<TSecond, TFirst> secondToFirst = new Dictionary<TSecond, TFirst>();

        public BiDictionary()
        {
        }

        public BiDictionary(Dictionary<TFirst, TSecond> d)
        {
            firstToSecond = d;
            foreach (KeyValuePair<TFirst, TSecond> item in d)
            {
                secondToFirst.Add(item.Value, item.Key);
            }
        }

        public void Add(TFirst first, TSecond second)
        {
            if (firstToSecond.ContainsKey(first) ||
                secondToFirst.ContainsKey(second))
            {
                throw new SkillProException("Duplicate first or second", new ArgumentException("Duplicate first or second"));
            }
            firstToSecond.Add(first, second);
            secondToFirst.Add(second, first);
        }

        public void AddIfPossible(TFirst first, TSecond second)
        {
            int test1 = 0;
            int test2 = 0;

            if (firstToSecond.ContainsKey(first)) { test1 = 1; };
            if (secondToFirst.ContainsKey(second)) { test2 = 10; };

            int synthesis = test1 + test2;

            switch (synthesis)
            {
                case 0:
                    //Pair can be safely added
                    firstToSecond.Add(first, second);
                    secondToFirst.Add(second, first);
                    break;
                case 1:
                    //This is no good : the first dictionnary contains 'first' but 'second' is not consistant with 'first'
                    throw new SkillProException("the first dictionnary contains 'first' but 'second' is not consistant with 'first'", new ArgumentException("the first dictionnary contains 'first' but 'second' is not consistant with 'first'"));
                case 10:
                    //This is no good : the second dictionnary contains 'second' but 'first' is not consistant with 'second'
                    throw new SkillProException("the second dictionnary contains 'second' but 'first' is not consistant with 'second'", new ArgumentException("the first dictionnary contains 'first' but 'second' is not consistant with 'first'"));
                case 11:
                    //the pair already exists
                    break;
                default:
                    throw new NotImplementedException();

            }
        }

        public bool TryGetByFirst(TFirst first, out TSecond second)
        {
            return firstToSecond.TryGetValue(first, out second);
        }

        public bool TryGetBySecond(TSecond second, out TFirst first)
        {
            return secondToFirst.TryGetValue(second, out first);
        }

        public bool RemoveElement(TFirst first, TSecond second)
        {
            if (firstToSecond.ContainsKey(first) &&
                secondToFirst.ContainsKey(second))
            {
                firstToSecond.Remove(first);
                secondToFirst.Remove(second);
                return true;
            }
            else
            {
                return false;
            }

        }
    }

}
