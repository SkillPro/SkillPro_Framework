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
using System.Xml;
using System.Net;
using System.IO;
using System.Runtime.Serialization.Json;
using System.Runtime.Serialization;

namespace eu.skillpro.see.CS.AML
{
    public enum StringType { FilePath, AmlString }

    /// <summary>
    /// Serialize/Deserialize AML file/stream into AMLDocument
    /// </summary>
    public static class AMLSerializer
    {
        #region Public Methods

        public static AMLDocument DeserializeFromAmlString(string AmlString, string optionnalSEEId = "")
        {
            // Create XML file from Webservice answer, for instance
            XmlDocument document = GetDocumentFromString(AmlString);

            return Deserialize(document, optionnalSEEId);
        }

        public static AMLDocument DeserializeFromFile(string filePath, string optionnalSEEId = "")
        {
            // Load AML in a XMLDocument, from file
            XmlDocument document = GetDocumentFromFile(filePath);

            return Deserialize(document, optionnalSEEId);
        }

        public static List<List<AMLExecutableSkill>> GetResourceSkillsPerExecutableSkill(string filePathOrXmlString, StringType TypeOfGivenString, out List<List<string>> AllAmlDescriptions)
        {
            XmlDocument document;
            List<List<AMLExecutableSkill>> returnedList = new List<List<AMLExecutableSkill>>();
            AllAmlDescriptions = new List<List<string>>();

            switch (TypeOfGivenString)
            {
                case StringType.FilePath:
                    document = GetDocumentFromFile(filePathOrXmlString);
                    break;
                case StringType.AmlString:
                    document = GetDocumentFromString(filePathOrXmlString);
                    break;
                default:
                    throw new NotImplementedException();
            }

            XmlNodeList internalElements = RemoveInstanceHierarchyIfNeeded(document);


            for (int i = 0; i < internalElements.Count; i++)
            {
                XmlNode node = internalElements[i];

                List<AMLExecutableSkill> currentExecutableSkill = new List<AMLExecutableSkill>();
                List<string> CurrentAmlSkills = new List<string>();

                if (node.SelectSingleNode("RoleRequirements") != null && node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"] != null && node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"].Value == "SkillProRoleClassLib/ExecutableSkill")
                {

                    XmlNodeList NodesResourceExecutableSKills = node.SelectNodes("InternalElement");

                    for (int j = 0; j < NodesResourceExecutableSKills.Count; j++)
                    {
                        XmlNode nodeResourceExeSkill = NodesResourceExecutableSKills[j];

                        if (nodeResourceExeSkill.SelectSingleNode("RoleRequirements") != null && nodeResourceExeSkill.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"] != null && nodeResourceExeSkill.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"].Value == "SkillProRoleClassLib/ResourceExecutableSkill")
                        {
                            AMLExecutableSkill deserializedExeSkill = GetExecutableSkill(nodeResourceExeSkill);
                            currentExecutableSkill.Add(deserializedExeSkill);
                            CurrentAmlSkills.Add(nodeResourceExeSkill.OuterXml);
                        }
                    }
                }

                if (currentExecutableSkill.Count != 0)
                {
                    returnedList.Add(currentExecutableSkill);
                    AllAmlDescriptions.Add(CurrentAmlSkills);
                }
            }

            return returnedList;
        }

        public static AMLDocument Deserialize(XmlDocument document, string optionnalSEEId = "")
        {
            AMLDocument amlDocument = new AMLDocument();

            XmlNodeList internalElements = RemoveInstanceHierarchyIfNeeded(document);

            List<XmlNode> matchingNodes = new List<XmlNode>();

            GetInternalElementRecursivly(internalElements, ref matchingNodes);

            foreach (XmlNode node in matchingNodes)
            {
                if (node.SelectSingleNode("RoleRequirements") != null && node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"] != null)
                {
                    string role = node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"].Value;

                    switch (role)
                    {
                        // Check if Role match ResourceExecutableSkill
                        case "SkillProRoleClassLib/ResourceExecutableSkill":

                            AMLExecutableSkill deserializedExeSkill = GetExecutableSkill(node);

                            if (string.IsNullOrEmpty(optionnalSEEId))
                            {
                                //No need to check the ResponsibleSEEID => add all Exeskills
                                amlDocument.ExecutableSkills.Add(deserializedExeSkill);
                            }
                            else
                            {
                                //Check the ResponsibleSEEID
                                if (deserializedExeSkill.ResponsibleSEE == optionnalSEEId)
                                    amlDocument.ExecutableSkills.Add(deserializedExeSkill);
                            }
                            break;

                        // Check if Role match SkillExecutionEngine
                        case "SkillProRoleClassLib/SkillExecutionEngine":

                            AMLSkillExecutionEngine deserializedSEE = GetSkillExecutionEngine(node);

                            if (string.IsNullOrEmpty(optionnalSEEId))
                            {
                                //No need to check the ResponsibleSEEID => add all Exeskills
                                amlDocument.SkillExecutionEngine.Add(deserializedSEE);
                            }
                            else
                            {
                                //Check the ResponsibleSEEID
                                if (deserializedSEE.ID == optionnalSEEId)
                                    amlDocument.SkillExecutionEngine.Add(deserializedSEE);
                            }

                            break;
                    }

                }
            }

            return amlDocument;

        }

        public static void GetInternalElementRecursivly(XmlNodeList nodes, ref List<XmlNode> matchingNodes)
        {
            foreach (XmlNode node in nodes)
            {
                if (node.SelectSingleNode("RoleRequirements") != null && node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"] != null)
                {
                    string role = node.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"].Value;

                    if (role == "SkillProRoleClassLib/ResourceExecutableSkill" || role == "SkillProRoleClassLib/SkillExecutionEngine")
                    {
                        matchingNodes.Add(node);
                    }
                    else
                    {
                        GetInternalElementRecursivly(node.SelectNodes("InternalElement"), ref matchingNodes);
                    }

                }
                else
                {
                    GetInternalElementRecursivly(node.SelectNodes("InternalElement"), ref matchingNodes);
                }
            }

        }

        #endregion

        #region Private Methods

        private static XmlNodeList RemoveInstanceHierarchyIfNeeded(XmlDocument document)
        {
            XmlNode instanceHierarchy;
            XmlNodeList internalElements;

            if (document.DocumentElement.Name == "InstanceHierarchy")
            {
                internalElements = document.DocumentElement.SelectNodes("InternalElement");
            }
            else
            {
                instanceHierarchy = document.DocumentElement.SelectSingleNode("InstanceHierarchy");

                if (instanceHierarchy != null)
                {
                    internalElements = instanceHierarchy.SelectNodes("InternalElement");
                }
                else
                {
                    internalElements = document.SelectNodes("InternalElement");
                }
            }
            return internalElements;
        }

        private static XmlDocument GetDocumentFromFile(string FilePath)
        {
            // Load AML in a XMLDocument from file
            XmlDocument document = new XmlDocument();
            document.Load(FilePath);
            return document;
        }

        private static XmlDocument GetDocumentFromString(string XmlString)
        {
            // Load AML in a XMLDocument from file
            XmlDocument document = new XmlDocument();
            document.LoadXml(XmlString);
            return document;
        }

        /// <summary>
        /// Extract informations from ExecutableSkill tag
        /// </summary>
        /// <param name="node"></param>
        /// <returns></returns>
        private static AMLExecutableSkill GetExecutableSkill(XmlNode node)
        {
            AMLExecutableSkill executableSkill = new AMLExecutableSkill();

            if (node.Attributes["ID"] != null)
            {
                executableSkill.ID = node.Attributes["ID"].Value;
            }

            if (node.Attributes["Name"] != null)
            {
                executableSkill.Name = node.Attributes["Name"].Value;
            }

            // List attributes nodes of ExecutableSkill
            XmlNodeList attributesNodes = node.SelectNodes("Attribute");

            foreach (XmlNode attributeNode in attributesNodes)
            {
                GetExecutableSkillInformations(executableSkill, attributeNode);
            }

            return executableSkill;
        }

        /// <summary>
        /// Extract the SkillExecutionEngine from AML
        /// </summary>
        /// <param name="node"></param>
        /// <returns></returns>
        private static AMLSkillExecutionEngine GetSkillExecutionEngine(XmlNode node)
        {
            AMLSkillExecutionEngine skillExecutionEngine = new AMLSkillExecutionEngine();

            if (node.Attributes["ID"] != null)
            {
                skillExecutionEngine.ID = node.Attributes["ID"].Value;
            }

            if (node.Attributes["Name"] != null)
            {
                skillExecutionEngine.Name = node.Attributes["Name"].Value;
            }

            // List attributes nodes of SkillExecutionEngine
            XmlNodeList attributesNodes = node.SelectNodes("Attribute");

            foreach (XmlNode attributeNode in attributesNodes)
            {
                GetSkillExecutionEngineInformations(skillExecutionEngine, attributeNode);
            }

            // List InternalElement nodes of SkillExecutionEngine
            XmlNodeList internalElementNodes = node.SelectNodes("InternalElement");

            foreach (XmlNode internalElementNode in internalElementNodes)
            {
                if (internalElementNode.SelectSingleNode("RoleRequirements") != null && internalElementNode.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"] != null &&
                    internalElementNode.SelectSingleNode("RoleRequirements").Attributes["RefBaseRoleClassPath"].Value == "SkillProRoleClassLib/SkillProResource")
                {
                    skillExecutionEngine.Resources.Add(GetResourceInformation(internalElementNode));
                }
            }

            return skillExecutionEngine;
        }

        /// <summary>
        /// Extract executable skill information from AML
        /// </summary>
        /// <param name="executableSkill"></param>
        /// <param name="attributeNode"></param>
        private static void GetExecutableSkillInformations(AMLExecutableSkill executableSkill, XmlNode attributeNode)
        {
            if (attributeNode.Attributes["Name"] != null)
            {
                string attributeName = attributeNode.Attributes["Name"].Value;

                switch (attributeName)
                {
                    case "PreCondition":
                        executableSkill.PreCondition = new Condition();
                        GetConditionInformations(executableSkill.PreCondition, attributeNode);

                        break;
                    case "PostCondition":
                        executableSkill.PostCondition = new Condition();
                        GetConditionInformations(executableSkill.PostCondition, attributeNode);
                        break;
                    case "AltPostCondition":
                        executableSkill.AltPostCondition = new Condition();
                        GetConditionInformations(executableSkill.AltPostCondition, attributeNode);
                        break;

                    case "Execution":
                        executableSkill.Execution = new Execution();
                        foreach (XmlNode executionAttribute in attributeNode.SelectNodes("Attribute"))
                        {
                            GetExecutionInformation(executableSkill.Execution, executionAttribute);
                        }
                        break;
                    case "ResourceId":
                        XmlNode valueNode = attributeNode.SelectSingleNode("Value");

                        if (valueNode != null)
                        {
                            executableSkill.ResourceId = valueNode.InnerText;
                        }

                        break;
                    case "ResponsibleSEE":
                        valueNode = attributeNode.SelectSingleNode("Value");

                        if (valueNode != null)
                        {
                            executableSkill.ResponsibleSEE = valueNode.InnerText;
                        }

                        break;
                    case "Duration":
                        valueNode = attributeNode.SelectSingleNode("Value");

                        if (valueNode != null)
                        {
                            executableSkill.Duration = valueNode.InnerText;
                        }

                        break;
                    case "Slack":
                        valueNode = attributeNode.SelectSingleNode("Value");

                        if (valueNode != null)
                        {
                            executableSkill.Slack = valueNode.InnerText;
                        }

                        break;
                }
            }
        }

        /// <summary>
        /// Extract executable skill information from AML
        /// </summary>
        /// <param name="skillExecuteEngine"></param>
        /// <param name="attributeNode"></param>
        private static void GetSkillExecutionEngineInformations(AMLSkillExecutionEngine skillExecuteEngine, XmlNode attributeNode)
        {
            if (attributeNode.Attributes["Name"] != null)
            {
                string attributeName = attributeNode.Attributes["Name"].Value;

                switch (attributeName)
                {
                    case "Default_Condition":
                        skillExecuteEngine.DefaultCondition = new Condition();
                        GetConditionInformations(skillExecuteEngine.DefaultCondition, attributeNode);
                        break;
                    case "MESCommType":
                        skillExecuteEngine.MESCommType = new CommType();
                        GetCommInformations(skillExecuteEngine.MESCommType, attributeNode);
                        break;
                    case "AMSCommType":
                        skillExecuteEngine.AMSCommType = new CommType();
                        GetCommInformations(skillExecuteEngine.AMSCommType, attributeNode);
                        break;
                }
            }
        }

        /// <summary>
        /// Extract condition information from AML
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="conditionNode"></param>
        private static void GetConditionInformations(Condition condition, XmlNode conditionNode)
        {
            foreach (XmlNode conditionElementNode in conditionNode.SelectNodes("Attribute"))
            {
                if (conditionElementNode.Attributes["Name"] != null && conditionElementNode.Attributes["Name"].Value != null)
                {
                    string attributeName = conditionElementNode.Attributes["Name"].Value;

                    switch (attributeName)
                    {
                        case "Configuration":
                            condition.Configuration = new ConditionElement();

                            XmlNode valueNode = conditionElementNode.SelectSingleNode("Value");

                            if (valueNode != null)
                            {
                                condition.Configuration.Value = valueNode.InnerText;
                            }

                            XmlNode descriptionNode = conditionElementNode.SelectSingleNode("Description");

                            if (descriptionNode != null)
                            {
                                condition.Configuration.Description = descriptionNode.InnerText;
                            }
                            else
                            {
                                if (valueNode != null)
                                {
                                    //There are no description => Take the value (UUID) instead
                                    condition.Configuration.Description = valueNode.InnerText;
                                }
                            }

                            break;
                        case "Product":
                            condition.Product = new ConditionElement();
                            descriptionNode = conditionElementNode.SelectSingleNode("Description");


                            valueNode = conditionElementNode.SelectSingleNode("Value");

                            if (valueNode != null)
                            {
                                condition.Product.Value = valueNode.InnerText;
                            }

                            if (descriptionNode != null)
                            {
                                condition.Product.Description = descriptionNode.InnerText;
                            }
                            else
                            {
                                if (valueNode != null)
                                {
                                    //There are no description => Take the value (UUID) instead
                                    condition.Product.Description = valueNode.InnerText;
                                }
                            }

                            break;
                    }
                }
            }

        }

        private static AMLResource GetResourceInformation(XmlNode internalElementNode)
        {
            AMLResource resource = new AMLResource();

            if (internalElementNode.Attributes["Name"] != null)
            {
                resource.Name = internalElementNode.Attributes["Name"].Value;
            }

            if (internalElementNode.Attributes["ID"] != null)
            {
                resource.ID = internalElementNode.Attributes["ID"].Value;
            }

            return resource;
        }

        /// <summary>
        /// Extract CommType informations from AML
        /// </summary>
        /// <param name="commType"></param>
        /// <param name="CommNode"></param>
        private static void GetCommInformations(CommType commType, XmlNode CommNode)
        {

            XmlNode valueNode = CommNode.SelectSingleNode("Value");

            if (valueNode != null)
            {
                commType.Type = valueNode.InnerText;
            }

            foreach (XmlNode conditionNode in CommNode.SelectNodes("Attribute"))
            {
                if (conditionNode.Attributes["Name"] != null && conditionNode.Attributes["Name"].Value != null)
                {
                    string attributeName = conditionNode.Attributes["Name"].Value;

                    switch (attributeName)
                    {
                        case "uri":

                            valueNode = conditionNode.SelectSingleNode("Value");

                            if (valueNode != null)
                            {
                                commType.URI = valueNode.InnerText;
                            }
                            break;
                        case "nodeId":

                            valueNode = conditionNode.SelectSingleNode("Value");

                            if (valueNode != null)
                            {
                                commType.NodeId = valueNode.InnerText;
                            }
                            break;
                    }
                }
            }
        }

        /// <summary>
        /// Extract Execution informations
        /// </summary>
        /// <param name="execution"></param>
        /// <param name="attributeNode"></param>
        private static void GetExecutionInformation(Execution execution, XmlNode attributeNode)
        {
            if (attributeNode.Attributes["Name"] != null)
            {
                string attributeName = attributeNode.Attributes["Name"].Value;

                switch (attributeName)
                {
                    case "Type":
                        XmlNode valueNode = attributeNode.SelectSingleNode("Value");

                        if (valueNode != null)
                        {
                            execution.Type = valueNode.InnerText;
                        }

                        break;
                    case "Data":
                        foreach (XmlNode dataAttribute in attributeNode.SelectNodes("Attribute"))
                        {
                            Data data = new Data();
                            if (dataAttribute.Attributes["Name"] != null && dataAttribute.Attributes["Name"].Value != null)
                            {
                                data.Name = dataAttribute.Attributes["Name"].Value;
                            }

                            XmlNode descriptionNode = dataAttribute.SelectSingleNode("Description");

                            if (descriptionNode != null)
                            {
                                data.Description = descriptionNode.InnerText;
                            }

                            valueNode = dataAttribute.SelectSingleNode("Value");

                            if (valueNode != null)
                            {
                                data.Value = valueNode.InnerText;
                            }

                            execution.Data.Add(data);
                        }

                        break;
                }
            }
        }

        #endregion
    }
}
