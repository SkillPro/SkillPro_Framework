/* Copyright 2002-2006 Elliotte Rusty Harold
   
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

import java.io.IOException;
import java.io.Writer;

final class UnsynchronizedBufferedWriter extends Writer {
    
    private final static int CAPACITY = 8192;
    private char[] buffer = new char[CAPACITY];
    private int    position = 0;
    private Writer out;
    
    
    public UnsynchronizedBufferedWriter(Writer out) {
        this.out = out;
    }

    
    public void write(char[] buffer, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("XOM bug: this statement shouldn't be reachable.");
    }
    
    
    public void write(String s) throws IOException {
         write(s, 0, s.length());
    }

    
    public void write(String s, int offset, int length) throws IOException {
    
        while (length > 0) {
            int n = CAPACITY - position;
            if (length < n) n = length;
            s.getChars(offset, offset + n, buffer, position);
            position += n;
            offset += n;
            length -= n;
            if (position >= CAPACITY) flushInternal();
        }
        
    }
        
    
    public void write(int c) throws IOException {
        if (position >= CAPACITY) flushInternal();
        buffer[position] = (char) c;
        position++;
    }

    
    public void flush() throws IOException {
        flushInternal();
        out.flush();
    }


    private void flushInternal() throws IOException {
        if (position != 0) {
            out.write(buffer, 0, position);
            position = 0;
        }
    }

    
    public void close() throws IOException {
        throw new UnsupportedOperationException("How'd we get here?");
    }

}
