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

using System.Data;

namespace eu.skillpro.see.CS
{
    public static class SkillProUtils
    {


        //####################################### CONSTANTS #####################################################################
        //#######################################################################################################################

        //####################################### PUBLIC STATIC METHODS #########################################################
        //#######################################################################################################################

        public static double EvaluateDoubleExpression(string expression)
        {
            var loDataTable = new DataTable();
            var loDataColumn = new DataColumn("Eval", typeof(double), expression);
            loDataTable.Columns.Add(loDataColumn);
            loDataTable.Rows.Add(0);
            return (double)(loDataTable.Rows[0]["Eval"]);
        }

        public static bool TryEvaluateDoubleExpression(string expression, out double result)
        {
            try
            {
                result = EvaluateDoubleExpression(expression);
                return true;
            }
            catch (Exception ex)
            {
                result = 0;
                return false;
            }
        }

        public static bool EvaluateBooleanExpression(string expression)
        {
            var loDataTable = new DataTable();
            var loDataColumn = new DataColumn("Eval", typeof(bool), expression);
            loDataTable.Columns.Add(loDataColumn);
            loDataTable.Rows.Add(0);
            return (bool)(loDataTable.Rows[0]["Eval"]);
        }

        public static bool TryEvaluateBooleanExpression(string expression, out bool result)
        {
            try
            {
                result = EvaluateBooleanExpression(expression);
                return true;
            }
            catch (Exception ex)
            {
                result = false;
                return false;
            }
        }

        public static bool CheckNumericalConstraintsOnProducts(Dictionary<string, string> AllKnownProductsAndQuantities, Dictionary<string, List<List<string>>> AllProductsAndConditions)
        {
            var empty = new Dictionary<string, string>();

            if(AllKnownProductsAndQuantities.Equals(empty))
                return false;

            var empty2 = new Dictionary<string, List<List<string>>>();

            if(AllProductsAndConditions.Equals(empty2))
                return true; //No constaints to test

            //Test all constraints one by one
            for (int i = 0; i < AllProductsAndConditions.Count; i++)
            {
                string currentProduct = AllProductsAndConditions.Keys.ElementAt(i);

                //Check if product is know and get its quantity
                string QuantityOfCurrentProduct = "";
                bool ProductKnown = AllKnownProductsAndQuantities.TryGetValue(currentProduct, out QuantityOfCurrentProduct);

                if (!ProductKnown)
                    return false; // Product is unkown => don't even have to test the conditions

                //If code passes here, this mean that product is known and you can test all the conditions
                List<List<string>> currentTests = AllProductsAndConditions[currentProduct];

                //For each test
                for (int j = 0; j < currentTests.Count; j++)
                {
                    //Create the test string
                    string TestString = "";

                    try
                    {
                        TestString  = QuantityOfCurrentProduct + currentTests[j][0] + currentTests[j][1];
                    }
                    catch (Exception ex)
                    {
                        return false; //There is a format error => conditions are not respected   
                    }

                    //Evalute the string
                    bool resultOfTest;
                    bool evalWorked = SkillProUtils.TryEvaluateBooleanExpression(TestString, out resultOfTest);

                    bool GlobalEval = resultOfTest && resultOfTest;

                    if (!GlobalEval)
                        return false; //The current evaluation failed
                }
            }

            //If code passes here, this mean that all products are known and all conditions were respected

            return true;
        }

        //####################################### PRIVATE STATIC METHODS ########################################################
        //#######################################################################################################################


    }
}
