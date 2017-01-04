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

using eu.skillpro.see.CS.AML;

namespace eu.skillpro.see.CS
{
    public class SkillBasedResourceController
    {


        /*! 
        *  \brief     Mother class to handle the correct execution of a Skill : e.g. checking the current Product, configuration. 
        *  \details   Mother class to handle the correct execution of a Skill : e.g. checking the current Product, configuration. 
        *  \author    Boris Bocquet, AKÉO PLUS SAS, France
        *  \version   1.0.0
        *  \date      2016/10/05
        *  \copyright LGPL , SkillPro 7FP EU
        *  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
        *  \pre       .
        *  \bug       .
        *  \warning   .
        *  \todo
        */


        //####################################### ATTRIBUTES ####################################################################
        //####################################################################################################################### 

        //private enum WhichBiDictionnary { MappingProductIds, MappingConfigurationIds, MappingSkillNames };

        private AMLSkillExecutionEngine _SeeAmlDescription;

        #region Conditions

        private Dictionary<string, string> _AllKnownConfigurations = new Dictionary<string, string>();

        private Dictionary<string, string> _AllKnownProductsAndQuantities = new Dictionary<string, string>();

        private List<string> _CurrentProductKeys = null;

        private object _LockProducts = new object();

        private string _CurrentConfigurationKey = null;

        private object _LockConfigs = new object();

        #endregion //Conditions

        #region Skills

        private List<ExecutableSkill> _AllExecutableSkills = new List<ExecutableSkill>();

        private ExecutableSkill _CurrentExecutingSkill = null;

        private object _LockObjCurrentExeSkill = new object();

        #endregion //Skills


        //####################################### PROPERTIES ####################################################################
        //#######################################################################################################################

        public AMLSkillExecutionEngine SeeAmlDescription
        {
            get { return _SeeAmlDescription; }
        }

        #region Conditions

        /// <summary>
        /// All the conditions relative to the configurations the SEE can have.
        /// </summary>
        public Dictionary<string, string> AllKnownConfigurations
        {
            get { return _AllKnownConfigurations; }
        }

        /// <summary>
        /// All the conditions relative to the products the SEE can have.
        /// </summary>
        public Dictionary<string, string> AllKnownProductsAndQuantities
        {
            get { return _AllKnownProductsAndQuantities; }
        }

        public List<KeyValuePair<string, string>> CurrentProductsAndQuantities
        {
            get
            {

                List<KeyValuePair<string, string>> returned = new List<KeyValuePair<string, string>>();

                lock (_LockProducts)
                {
                    for (int i = 0; i < _CurrentProductKeys.Count; i++)
                    {
                        returned.Add(new KeyValuePair<string, string>(_CurrentProductKeys[i], _AllKnownProductsAndQuantities[_CurrentProductKeys[i]]));
                    }
                }

                return returned;
            }
            set
            {
                List<string> newCur = new List<string>();

                lock (_LockProducts)
                {
                    for (int i = 0; i < value.Count; i++)
                    {
                        bool KeyExist = _AllKnownProductsAndQuantities.ContainsKey(value[i].Key);

                        if (!KeyExist)
                        {
                            //The current product did not exists => add it to the dictionnary
                            _AllKnownProductsAndQuantities.Add(value[i].Key, value[i].Value);
                        }

                        //Update quantity
                        _AllKnownProductsAndQuantities[value[i].Key] = value[i].Value;

                        newCur.Add(value[i].Key);
                    }

                    //Set current product keys
                    _CurrentProductKeys = newCur;
                }
            }
        }

        public KeyValuePair<string, string> CurrentConfiguration
        {
            get
            {
                lock (_LockConfigs)
                {
                    return new KeyValuePair<string, string>(_CurrentConfigurationKey, _AllKnownConfigurations[_CurrentConfigurationKey]);
                }
            }
            set
            {
                lock (_LockConfigs)
                {

                    bool KeyExist = _AllKnownConfigurations.ContainsKey(value.Key);

                    if (!KeyExist)
                    {
                        //The current product did not exists => add it to the dictionnary
                        if (value.Value != null)
                            _AllKnownConfigurations.Add(value.Key, value.Value);
                        else
                            _AllKnownConfigurations.Add(value.Key, value.Key);//Their is no "description"
                    }

                    //Set current key
                    _CurrentConfigurationKey = value.Key;
                }
            }
        }

        #endregion //Conditions

        #region Skills

        public List<ExecutableSkill> AllExecutableSkills
        {
            get { return _AllExecutableSkills; }
        }

        public ExecutableSkill CurrentExecutingSkill
        {
            get
            {

                lock (_LockObjCurrentExeSkill)
                {
                    return _CurrentExecutingSkill;
                }
            }
            set
            {
                lock (_LockObjCurrentExeSkill)
                {
                    if (value == null)
                    {
                        //The user wants to null the variable =>no problem
                        _CurrentExecutingSkill = null;
                    }
                    else
                    {

                        //The user wants to set the variable to a skill value (not null)=> Set this variable if and only if it was null

                        if (_CurrentExecutingSkill != null)
                        {
                            throw new SkillProException("Current Executing skill is not null, so you cannot set this property");
                        }
                        else
                        {
                            _CurrentExecutingSkill = value;
                        }
                    }
                }
            }
        }

        #endregion //Skills


        //####################################### PUBLIC METHODS ################################################################
        //#######################################################################################################################

        #region Constructor / Destructor

        public SkillBasedResourceController(AMLSkillExecutionEngine AmlSee, List<AMLExecutableSkill> ExecutableSkills)
        {
            ConstructorAmlSkills(AmlSee, ExecutableSkills);
        }

        public SkillBasedResourceController(string AmlFile, string AmlSkillFile)
        {
            //AMLSerializer.

            AMLDocument docSee = AMLSerializer.DeserializeFromFile(AmlFile);

            if (docSee.SkillExecutionEngine == null)
            {
                throw new Exception("No Skill Execution Engine in the given AML string");
            }

            if (docSee.SkillExecutionEngine.Count != 1)
            {
                throw new Exception("There are " + docSee.SkillExecutionEngine.Count + " SEE in this AML. Need one and only one SEE inside the file");
            }

            AMLDocument docSkills = AMLSerializer.DeserializeFromFile(AmlFile);

            if (docSkills.ExecutableSkills == null)
            {
                throw new Exception("No Skills are present in the given AML string");
            }

            ConstructorAmlSkills(docSee.SkillExecutionEngine[0], docSkills.ExecutableSkills);
        }

        public SkillBasedResourceController(AMLSkillExecutionEngine AmlSee)
        {
            ConstructorAmlSkills(AmlSee, null);
        }

        #endregion //Constructor / Destructor


        public void AddExecutableSkills(ExecutableSkill aNewExecutableSkill)
        {

            //Check the skill belongs to your SEE
            if (this._SeeAmlDescription.ID != aNewExecutableSkill.AmlSkillDescription.ResponsibleSEE)
                return;

            int Index = _AllExecutableSkills.FindIndex(elem => elem.AmlSkillDescription.ID == aNewExecutableSkill.AmlSkillDescription.ID);

            if (Index != -1)
                return; //The skill already exists

            //If code passes here, this mean that the skill is new

            _AllExecutableSkills.Add(aNewExecutableSkill);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////   CONFIGURATION    //////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


            //Extract all Pre/Post/Alt conditions configurations
            List<string> ValuesConcat, DescriptionsConcat;
            ParsePrePostAltConditions(aNewExecutableSkill, out ValuesConcat, out DescriptionsConcat);

            for (int i = 0; i < ValuesConcat.Count; i++)
            {
                //First check this is not the "SkillProDefinitions.ANY_CONFIGURATIONS" const or the SkillProDefinitions.SAME_CONFIGURATION
                if (!SkillProDefinitions.CheckTwoStrings(ValuesConcat[i], SkillProDefinitions.ANY_CONFIGURATIONS) && !SkillProDefinitions.CheckTwoStrings(ValuesConcat[i], SkillProDefinitions.SAME_CONFIGURATION))
                {
                    //Add the configuration to known configurations, if unknown
                    if (!_AllKnownConfigurations.ContainsKey(ValuesConcat[i]))
                    {
                        _AllKnownConfigurations.Add(ValuesConcat[i], DescriptionsConcat[i]);
                    }
                }
            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////   PRODUCT    //////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Get all products from skill

            //Get products from Precondtions
            var DicoFromPreconditions = SkillProDefinitions.ParseProductStringPreCondition(aNewExecutableSkill.AmlSkillDescription.PreCondition.Product.Value);
            List<string> AllProductsReferencedInPreconditions = DicoFromPreconditions.Keys.ToList();

            //Get products from Postcondtions
            var DicoFromPostconditions = SkillProDefinitions.ParseProductStringPostCondition(aNewExecutableSkill.AmlSkillDescription.PostCondition.Product.Value);
            List<string> AllProductsReferencedInPostconditions = DicoFromPostconditions.Keys.ToList();

            //Get products from AltPostcondtions
            List<string> AllProductsReferencedInAltPostconditions;
            if (aNewExecutableSkill.AmlSkillDescription.AltPostCondition != null && aNewExecutableSkill.AmlSkillDescription.AltPostCondition.Product != null && aNewExecutableSkill.AmlSkillDescription.AltPostCondition.Product.Value != null)
            {
                var DicoFromAltPostconditions = SkillProDefinitions.ParseProductStringPostCondition(aNewExecutableSkill.AmlSkillDescription.AltPostCondition.Product.Value);
                AllProductsReferencedInAltPostconditions = DicoFromAltPostconditions.Keys.ToList();
            }
            else
            {
                AllProductsReferencedInAltPostconditions = new List<string>();
            }

            List<string> AllConcatProducts = AllProductsReferencedInPreconditions.Concat(AllProductsReferencedInPostconditions).ToList();
            AllConcatProducts = AllConcatProducts.Concat(AllProductsReferencedInAltPostconditions).ToList();

            for (int i = 0; i < AllConcatProducts.Count; i++)
            {
                //Check if product is already known
                if (!_AllKnownProductsAndQuantities.ContainsKey(AllConcatProducts[i]))
                {
                    //you start with quantity == 0 if the product is new
                    _AllKnownProductsAndQuantities.Add(AllConcatProducts[i], "0");
                }

            }

        }

        public void AddExecutableSkills(List<ExecutableSkill> someNewExecutableSkill)
        {
            for (int i = 0; i < someNewExecutableSkill.Count; i++)
            {
                this.AddExecutableSkills(someNewExecutableSkill[i]);
            }
        }

        public int CheckSkillIsKnown(string SkillId, out string ID)
        {
            if (_AllExecutableSkills == null || _AllExecutableSkills.Count == 0)
            {
                ID = null;
                return -1;
            }

            int Index = _AllExecutableSkills.FindIndex(elem => elem.AmlSkillDescription.ID == SkillId);

            if (Index == -1)
            {
                ID = null;
                return -2;
            }

            ID = SkillId;

            return 0;

        }

        /// <summary>
        /// For format of configuration string, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"
        /// </summary>
        /// <param name="ConfigurationString">For format, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"</param>
        /// <returns>0 : current configuration match required configuration.  -1 : current configuration is null . -2 :  current configuration DOES NOT match required configuration</returns>
        public int CheckCurrentConfiguration(string ConfigurationString)
        {
            //For format, check delivrable D2.1.0, section 2.3, "Addendum: Complex Resource Configurations in Conditions of Executable Skills"

            var Empty = new KeyValuePair<string, string>();

            if (CurrentConfiguration.Equals(Empty))
            {
                return -1;
            }

            string[] ConfigStrings = SkillProDefinitions.ParseConfigurationString_Precondition(ConfigurationString);

            if (ConfigStrings.Length == 0)
                return -2;

            //Check if ConfigStrings contains "SkillProDefinitions.ANY_CONFIGURATIONS" const or ConfigStrings contains the current config

            for (int i = 0; i < ConfigStrings.Length; i++)
            {
                if (SkillProDefinitions.CheckStringIsAnyConfiguration(ConfigStrings[i]))
                    return 0; //The array contained "SkillProDefinitions.ANY_CONFIGURATIONS", so current configuration is inevitably OK

                if (CurrentConfiguration.Key == ConfigStrings[i])
                    return 0; //The array contained the current configuration of the SBRC
            }

            //If code passes here, this mean that the given configurations are NOK

            return -3;
        }

        /// <summary>
        /// For format of product string, check delivrable D2.1.0, section 2.3, "Addendum: Numeric Products in Conditions of Executable Skills"
        /// </summary>
        /// <param name="ProductString">For format of product string, check delivrable D2.1.0, section 2.3, "Addendum: Numeric Products in Conditions of Executable Skills"</param>
        /// <returns>0 : current product match required product.  -1 : current configuration is null . -2 :  current product DOES NOT match required product</returns>
        public int CheckCurrentProduct(string ProductString)
        {
            var Empty = new KeyValuePair<string, string>();

            lock (_LockProducts)
            {
                if (CurrentProductsAndQuantities.Equals(Empty))
                {
                    return -1;
                }

                Dictionary<string, List<List<string>>> AllProductsAndConditions = SkillProDefinitions.ParseProductStringPreCondition(ProductString);

                if (AllProductsAndConditions.Keys.Count == 0)
                    return 0; //There is no contraints on the product => current product is inevitably OK

                //If code passes here, this mean that you have to check quantities
                var DicoCurrentProductsAndQuantities = CurrentProductsToDictionnary();

                bool TestsAreAllOk = SkillProUtils.CheckNumericalConstraintsOnProducts(DicoCurrentProductsAndQuantities, AllProductsAndConditions);

                if (TestsAreAllOk)
                    return 0;
                else
                    return -2;

            }

        }

        /// <summary>
        /// Goes through all executable skills, and return the callable skills. E.g. ExecSkill:XXX
        /// </summary>
        /// <returns></returns>
        public List<string> GetCallableStrings()
        {
            return AllExecutableSkills.Select(elem => elem.ToCallableString()).ToList();
        }

        /// <summary>
        /// See deliverable D2.1.0, section 2.3, Addendum: Complex Resource Configurations in Conditions of Executable Skills 
        /// </summary>
        /// <param name="PostconditionConfigurationElement"></param>
        /// <param name="WhatToSetOnTheOpcUaServer"></param>
        public void ExecutePostConditionConfigurationString(ConditionElement PostconditionConfigurationElement, out string WhatToSetOnTheOpcUaServer)
        {

            //string[] PostconditionConfigurations = SkillProDefinitions.ParseConfigurationString_Precondition(PostconditionConfigurationElement.Value);
            string PostConditionConfiguration = SkillProDefinitions.ParseConfigurationString_Postcondition(PostconditionConfigurationElement.Value);

            lock (_LockConfigs)
            {

                if (string.IsNullOrEmpty(PostConditionConfiguration))
                    throw new SkillProException("The given post condition configuration does not follow the standard given in deliverable D2.1.0, section 2.3", new NotImplementedException());

                if (SkillProDefinitions.CheckTwoStrings(PostConditionConfiguration, SkillProDefinitions.SAME_CONFIGURATION))
                {
                    //This is "SAME" configuration
                    //You don't have to change the current configuration (this is the same)
                    WhatToSetOnTheOpcUaServer = CurrentConfiguration.Key;
                }
                else
                {
                    //This is the next configuration
                    string Description;
                    if (PostconditionConfigurationElement.Description == null)
                        Description = _AllKnownConfigurations[PostConditionConfiguration];
                    else
                        Description = PostconditionConfigurationElement.Description;

                    CurrentConfiguration = new KeyValuePair<string, string>(PostConditionConfiguration, Description);
                    WhatToSetOnTheOpcUaServer = PostConditionConfiguration;
                }
            }

        }

        public void ExecutePostConditionProductString(ConditionElement PostconditionProduitElement, out string WhatToSetOnTheOpcUaServer)
        {
            Dictionary<string, string> ProductsAndOperations = SkillProDefinitions.ParseProductStringPostCondition(PostconditionProduitElement.Value);

            //Turn the current products and quantities into a dictionnary
            Dictionary<string, string> DicoCurProductsAndQuantities = new Dictionary<string, string>();

            lock (_LockProducts)
            {
                var CopyCurrentProductsAndQuantities = CurrentProductsAndQuantities;

                for (int i = 0; i < CopyCurrentProductsAndQuantities.Count; i++)
                {
                    DicoCurProductsAndQuantities.Add(CopyCurrentProductsAndQuantities[i].Key, CopyCurrentProductsAndQuantities[i].Value);
                }

                //Apply the operations on the current products and quantity (the dictionnary)
                try
                {
                    for (int i = 0; i < ProductsAndOperations.Keys.Count; i++)
                    {
                        string CurProductKey = ProductsAndOperations.Keys.ElementAt(i);
                        string CurProductQuantity = DicoCurProductsAndQuantities[CurProductKey];

                        double CurValueToAddOrRemove = double.Parse(ProductsAndOperations[CurProductKey]);
                        string OperationAndValue;

                        if (CurValueToAddOrRemove >= 0)
                            OperationAndValue = "+" + CurValueToAddOrRemove.ToString();
                        else
                            OperationAndValue = CurValueToAddOrRemove.ToString();

                        string ExpressionString = CurProductQuantity + OperationAndValue;
                        double NewQuantity = SkillProUtils.EvaluateDoubleExpression(ExpressionString);

                        DicoCurProductsAndQuantities[CurProductKey] = NewQuantity.ToString();
                    }
                }
                catch (Exception ex)
                {
                    throw new SkillProException("EXCEPTION while trying to apply the operations on the products quantity in ExecutePostConditionProductString", ex);
                }

                //If code passes here, this mean that things worked properly
                //And you can set the current products and quantity
                this.CurrentProductsAndQuantities = DicoCurProductsAndQuantities.ToList();

                WhatToSetOnTheOpcUaServer = CurrentProductsToOpcUaString();

            }

        }

        public string CurrentProductsToOpcUaString()
        {
            Dictionary<string, int> Dico = CurrentProductsToDictionnary_int();

            return SkillProDefinitions.JSS.Serialize(Dico);
        }

        public Dictionary<string, int> CurrentProductsToDictionnary_int()
        {
            Dictionary<string, int> DicoReturned = new Dictionary<string, int>();
            var CopyCurrentProductsAndQuantities = CurrentProductsAndQuantities;

            for (int i = 0; i < CopyCurrentProductsAndQuantities.Count; i++)
            {
                DicoReturned.Add(CopyCurrentProductsAndQuantities[i].Key, int.Parse(CopyCurrentProductsAndQuantities[i].Value));
            }

            return DicoReturned;
        }

        public Dictionary<string, string> CurrentProductsToDictionnary()
        {
            Dictionary<string, string> DicoReturned = new Dictionary<string, string>();
            var CopyCurrentProductsAndQuantities = CurrentProductsAndQuantities;

            for (int i = 0; i < CopyCurrentProductsAndQuantities.Count; i++)
            {
                DicoReturned.Add(CopyCurrentProductsAndQuantities[i].Key, CopyCurrentProductsAndQuantities[i].Value);
            }

            return DicoReturned;
        }

        //####################################### PRIVATE METHODS ###############################################################
        //#######################################################################################################################

        private void ConstructorMain(KeyValuePair<string, string> initialResourceConfigurationIdAndDescription, List<KeyValuePair<string, string>> initialProductsAndQuantities, List<ExecutableSkill> ExecutableSkills)
        {
            if (ExecutableSkills != null && ExecutableSkills.Count != 0)
                AddExecutableSkills(ExecutableSkills);

            CurrentConfiguration = initialResourceConfigurationIdAndDescription;
            CurrentProductsAndQuantities = initialProductsAndQuantities;
        }

        private void ConstructorAmlSkills(AMLSkillExecutionEngine AmlSee, List<AMLExecutableSkill> ExecutableSkills)
        {
            List<ExecutableSkill> ConvertedExeSkills = null;

            if (ExecutableSkills == null || ExecutableSkills.Count == 0)
                ConvertedExeSkills = null;
            else
                ConvertedExeSkills = ExecutableSkills.ConvertAll<ExecutableSkill>(AMLExecutableSkill.ToExecutableSkill);

            //Get default configuration condition
            string[] ConditionValues, ConditionDescriptions;
            AmlSee.DefaultCondition.Configuration.SplitPreConditionConfigurations(out ConditionValues, out ConditionDescriptions);

            if (ConditionValues == null || ConditionDescriptions == null || ConditionValues.Length != 1 || ConditionDescriptions.Length != 1)
                throw new SkillProException("Invalid given default configuration condition : require one and only one configuration condition");

            KeyValuePair<string, string> initialResourceConfigurationIdAndDescription = new KeyValuePair<string, string>(ConditionValues[0], ConditionDescriptions[0]);

            _SeeAmlDescription = AmlSee;

            //Get default product
            var initialProductAndQuantity = SkillProDefinitions.ParseProductStringInitialCondition(AmlSee.DefaultCondition.Product.Value);

            ConstructorMain(initialResourceConfigurationIdAndDescription, initialProductAndQuantity, ConvertedExeSkills);
        }

        //private int CheckIsKnown(WhichBiDictionnary theBiDictionnary, string DisplayNameOrId, out string ID)
        //{
        //    int returnCode = 0;

        //    switch (theBiDictionnary)
        //    {
        //        case WhichBiDictionnary.MappingConfigurationIds:
        //            returnCode=CheckBiDictionnary(ref _AllKnownConfigurations.MappingDisplayNameAndIds, DisplayNameOrId, out ID);
        //            break;
        //        case WhichBiDictionnary.MappingProductIds:
        //            returnCode=CheckBiDictionnary(ref _AllKnownProductsAndQuantities.MappingDisplayNameAndIds, DisplayNameOrId, out ID);
        //            break;
        //        case WhichBiDictionnary.MappingSkillNames:
        //            returnCode = CheckBiDictionnary(ref _NamesAndIDsOfSkills.MappingDisplayNameAndIds, DisplayNameOrId, out ID);
        //            break;
        //        default :
        //            throw new NotImplementedException();
        //            break;
        //    }

        //    return returnCode;
        //}

        //private int CheckBiDictionnary(ref BiDictionary<string, string> BiDictionnary, string DisplayNameOrId, out string ID)
        //{
        //    if (BiDictionnary.TryGetByFirst(DisplayNameOrId, out ID))
        //    {
        //        //The user gave the description of the Product => the ID is know in identifierOfProductConfiguration
        //    }
        //    else
        //    {
        //        string dummy;
        //        //Check if the user gave the "ID" of the product: the Value of the BiDictionnary

        //        if (!BiDictionnary.TryGetBySecond(DisplayNameOrId, out dummy))
        //        {
        //            //This product condition is unknown
        //            return -1;
        //        }

        //        //Yes the user gave the "ID" of the product => write it directly on the OPC-UA server
        //        ID = DisplayNameOrId;
        //    }

        //    return 0;
        //}

        //private void CheckDisplayNameAndIdConsistency(ref BiDictionary<string, string> BiDictionnary, string ExpectedUniqueDisplayName, string ExpectedUniqueID, string nameForException)
        //{
        //    string second;
        //    string first;
        //    try
        //    {
        //        BiDictionnary.TryGetByFirst(ExpectedUniqueDisplayName, out second);
        //        BiDictionnary.TryGetBySecond(ExpectedUniqueID, out first);
        //    }
        //    catch (Exception ex)
        //    {
        //        throw new SkillProException("Impossible to Set the current " + nameForException + " because it is probably unknown (from the executable skills the SBRC manages)", ex);
        //    }

        //    if ((ExpectedUniqueDisplayName != first) || (ExpectedUniqueID != second))
        //    {
        //        throw new SkillProException("Impossible to Set the current " + nameForException + " because the UniqueDisplayName does not match the UniqueIdOnOpcuaServer");
        //    }
        //}

        private static void ParsePrePostAltConditions(ExecutableSkill aNewExecutableSkill, out List<string> ValuesConcat, out List<string> DescriptionsConcat)
        {
            string[] PreConditionValues;
            string[] PreConditionDescriptions;
            aNewExecutableSkill.AmlSkillDescription.PreCondition.Configuration.SplitPreConditionConfigurations(out PreConditionValues, out PreConditionDescriptions);

            string[] PostConditionValues;
            string[] PostConditionDescriptions;
            aNewExecutableSkill.AmlSkillDescription.PostCondition.Configuration.SplitPreConditionConfigurations(out PostConditionValues, out PostConditionDescriptions);

            string[] AltPostConditionValues;
            string[] AltPostConditionDescriptions;
            aNewExecutableSkill.AmlSkillDescription.PreCondition.Configuration.SplitPreConditionConfigurations(out AltPostConditionValues, out AltPostConditionDescriptions);

            ValuesConcat = new List<string>(PreConditionValues);
            ValuesConcat.AddRange(PostConditionValues);
            ValuesConcat.AddRange(AltPostConditionValues);

            DescriptionsConcat = new List<string>(PreConditionDescriptions);
            DescriptionsConcat.AddRange(PostConditionDescriptions);
            DescriptionsConcat.AddRange(AltPostConditionDescriptions);
        }

    }

    //public class MappingDisplaynameId
    //{
    //    public BiDictionary<string, string> MappingDisplayNameAndIds = null;

    //    public MappingDisplaynameId(List<string> DescriptionOfProductCondition, List<string> AssociatedIds)
    //    {
    //        MappingDisplayNameAndIds = new BiDictionary<string, string>();

    //        for (int i = 0; i < DescriptionOfProductCondition.Count; i++)
    //        {
    //            MappingDisplayNameAndIds.Add(DescriptionOfProductCondition[i], AssociatedIds[i]);
    //        }
    //    }

    //    public MappingDisplaynameId(string DescriptionOfProductCondition, string AssociatedId)
    //    {
    //        MappingDisplayNameAndIds = new BiDictionary<string, string>();
    //        MappingDisplayNameAndIds.Add(DescriptionOfProductCondition, AssociatedId);
    //    }

    //    public MappingDisplaynameId()
    //    {
    //        this.MappingDisplayNameAndIds = new BiDictionary<string, string>();
    //    }

    //}

    //public class MappingProductIds : MappingDisplaynameId
    //{
    //    public MappingProductIds(List<string> DescriptionOfProductCondition, List<string> AssociatedIds) : base(DescriptionOfProductCondition, AssociatedIds) { }

    //    public MappingProductIds(string DescriptionOfProductCondition, string AssociatedId) : base(DescriptionOfProductCondition, AssociatedId) { }

    //    public MappingProductIds() : base() { }
    //}

    //public class MappingConfigurationIds : MappingDisplaynameId
    //{
    //    public MappingConfigurationIds(List<string> DescriptionOfConfigurationCondition, List<string> AssociatedIds) : base(DescriptionOfConfigurationCondition, AssociatedIds) { }

    //    public MappingConfigurationIds(string DescriptionOfConfigurationCondition, string AssociatedId) : base(DescriptionOfConfigurationCondition, AssociatedId) { }

    //    public MappingConfigurationIds() : base() { }
    //}


    //public class MappingSkillNames : MappingDisplaynameId
    //{
    //    public MappingSkillNames(List<string> SkillNames, List<string> AssociatedIds) : base(SkillNames, AssociatedIds) { }

    //    public MappingSkillNames(string SkillName, string AssociatedId) : base(SkillName, AssociatedId) { }

    //    public MappingSkillNames() : base() { }
    //}

}
