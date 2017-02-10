/* Copyright 2005 Elliotte Rusty Harold
   
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

/**
 * <p>
 * Indicates problems with XPath syntax or evaluation.
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.1b3
 * 
 */
public class XPathException extends RuntimeException {
    

    private static final long serialVersionUID = 6362087755031657439L;
    
    private String expression;
    private Throwable cause;

    
    /**
     * <p>
     * Creates a new <code>XPathException</code> 
     * with a detail message.
     * </p>
     * 
     * @param message a string indicating the specific problem
     */
    public XPathException(String message) {
        super(message);
    }


    /**
     * <p>
     * Creates a new <code>IllegalNameException</code> 
     * with a detail message and an underlying root cause.
     * </p>
     * 
     * @param message a string indicating the specific problem
     * @param cause the original cause of this exception
     */
    public XPathException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }


    /**
     * <p>
     *  Return the original cause that led to this exception,
     *  or null if there was no original exception.
     * </p>
     *
     * @return the root cause of this exception
     */
    public Throwable getCause() {
        return this.cause;  
    }

    
    // null is insufficient for detecting an uninitialized cause.
    // The cause may be set to null which may not then be reset.
    private boolean causeSet = false;

    
    /**
     * <p>
     * Sets the root cause of this exception. This may 
     * only be called once. Subsequent calls throw an 
     * <code>IllegalStateException</code>.
     * </p>
     * 
     * <p>
     * This method is unnecessary in Java 1.4 where it could easily be
     * inherited from the superclass. However, including it here
     * allows this  method to be used in Java 1.3 and earlier.
     * </p>
     *
     * @param cause the root cause of this exception
     * 
     * @return this <code>XMLException</code>
     * 
     * @throws IllegalArgumentException if the cause is this exception
     *   (An exception cannot be its own cause.)
     * @throws IllegalStateException if this method is called twice
     */
    public Throwable initCause(Throwable cause) {
        
        if (causeSet) {
            throw new IllegalStateException("Can't overwrite cause");
        } 
        else if (cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted"); 
        }
        else this.cause = cause;
        causeSet = true;
        return this;
        
    }
    
    
    /**
     * <p>
     * Sets the specific XPath expression that caused this exception.
     * </p>
     * 
     * @param expression the XPath expression that caused the exception
     */
    void setXPath(String expression) {
        this.expression = expression;
    }
    
    
    /**
     * <p>
     * Returns the specific XPath expression being evaluated when this
     * excepiton was thrown.
     * </p>
     * 
     * @return the XPath expression that caused the exception
     */
    public String getXPath() {
        return this.expression;
    }
    
    
}
