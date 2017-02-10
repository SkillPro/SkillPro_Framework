/* Copyright 2004, 2009 Elliotte Rusty Harold
   
   This library is free software; you can redistribute it and/or modify
   it under the terms of version 2.1 of the GNU Lesser General Public 
   License as published by the Free Software Foundation.
   
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
   GNU Lesser General Public License for more details.
 *    
   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the 
   Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
   Boston, MA 02111-1307  USA
   
   You can contact Elliotte Rusty Harold by sending e-mail to
   elharo@metalab.unc.edu. Please include the word "XOM" in the
   subject line. The XOM home page is located at http://www.xom.nu/
*/
/*****************************************************************************
 *
 * Copyright 2012-2016 SkillPro Consortium
 *
 * Author: PDE, FZI, pde@fzi.de
 *
 * Date of creation: 2012-2016
 *
 * Module: Production System Configuration Manager (PSCM)
 *
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 *
 * This file is part of the AMS (Asset Management System), which has been developed
 * at the PDE department of the FZI, Karlsruhe. It is part of the SkillPro Framework,
 * which is is developed in the SkillPro project, funded by the European FP7
 * programme (Grant Agreement 287733).
 *
 * The SkillPro Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The SkillPro Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the SkillPro Framework. If not, see <http://www.gnu.org/licenses/>.
*****************************************************************************/

package nu.xom;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.DTDConfiguration;

/**
 * <p>
 * This class is used by the <code>Builder</code> to prevent Xerces
 * from accepting XML 1.1 documents. When using regular Xerces
 * (<code>org.apache.xerces.parsers.SAXParser</code>) XOM verifies
 * everything. When using this subclass, XOM will rely on Xerces
 * to verify the rules, and skip its own verification checks.
 * </p>
 * 
 * <p>
 * This class does not support schema validation. If you want to use
 * the W3C XML Schema Language, you'll need to download and install 
 * the full version of Xerces from <a target="_top"
 * href="http://xml.apache.org/xerces2-j">http://xml.apache.org</a>.
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.2.2b2
 * 
 */
class XML1_0Parser extends SAXParser {

    XML1_0Parser() {
        super(new DTDConfiguration());
    }

}
