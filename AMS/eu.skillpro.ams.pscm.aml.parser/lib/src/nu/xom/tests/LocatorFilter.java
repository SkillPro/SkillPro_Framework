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

package nu.xom.tests;

import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * <p>
 *  Makes sure SAX does not have a locator. 
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.2b2
 *
 */
class LocatorFilter extends XMLFilterImpl {
    
    public LocatorFilter(XMLReader reader) {
        super(reader);
    }

    public void setDocumentLocator(Locator loc) {
        super.setDocumentLocator(new NullLocator(loc));
    }
    
}


class NullLocator implements Locator {

    private Locator loc;
    
    public NullLocator(Locator loc) {
        this.loc = loc;
    }

    public String getSystemId() {
        return null;
    }

    public String getPublicId() {
        return loc.getPublicId();
    }

    public int getLineNumber() {
        return loc.getLineNumber();
    }

    public int getColumnNumber() {
        return loc.getColumnNumber();
    }
    
}
