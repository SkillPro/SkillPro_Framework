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
    public class OpcUaConfiguration
    {
        /*! 
        *  \brief     Class to contain all needed parameters concerning OPC UA communication.
        *  \details   Class to contain all needed parameters concerning OPC UA communication.
        *  \author    Boris Bocquet, AKÉO PLUS SAS, France
        *  \version   1.0.0
        *  \date      2016/10/05
        *  \copyright LGPL , SkillPro 7FP EU
        *  \attention Unauthorized use or copy of such ipr may violate copyright, trademark, and other laws
        *  \pre       OPC-UA server respecting the specifications of deliverable 2.3  must be running
        *  \bug       .
        *  \warning   .
        *  \todo	MORE CODE!
        */

        //####################################### ATTRIBUTES ###################################################################"
        //####################################################################################################################"

        #region related to connection

        public string ServerUrl { get; set; }

        public ushort SpaceNameIndex { get; set; }

        #endregion //related to connection

        #region related to SEE

        public uint SeeNodeId { get; set; }

        public string SeeId { get; set; }

        public string SeeName { get; set; }

        public string AmlDescription { get; set; }

        #endregion //related to SEE

        #region related to "createSee"

        public string CreateSeeNodeId { get; set; }

        public uint CreateSeeMethodId { get; set; }

        #endregion


        #region parameters

        public int TimeOut { get; set; }

        public double SamplingIntervals { get; set; }

        #endregion //parameters


        //####################################### PROPERTIES ###################################################################"
        //####################################################################################################################"


        //####################################### PUBLIC METHODES ###################################################################"
        //####################################################################################################################"


        /// <summary>
        /// Default constructor, with common default paramters.
        /// </summary>
        public OpcUaConfiguration()
        {
            #region mandatory to set these values

            #region related to connection

            ServerUrl = "";

            SpaceNameIndex = 0;

            #endregion //related to connection

            #region related to SEE

            SeeNodeId = 0;

            SeeId = "";

            AmlDescription = "";

            SeeName = "";

            #endregion //related to SEE


            #region related to "createSee"

            CreateSeeNodeId = "SEE";

            CreateSeeMethodId = 1506;

            #endregion

            #endregion //mandatory to set these values

            #region optionnal

            #region parameters

            TimeOut = 5000;

            SamplingIntervals = 100;

            #endregion //parameters

            #endregion //optionnal
        }


        //####################################### PRIVATE METHODES ###################################################################"
        //####################################################################################################################"

    }

}
